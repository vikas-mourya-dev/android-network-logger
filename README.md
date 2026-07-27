# NetworkLogger

A drop-in OkHttp interceptor for Android that captures every request/response, persists it to a
local Room database, and exposes it as `LiveData` — plus a prebuilt log-viewer screen and an
optional draggable in-app debug bubble.

## Opening the project

Requires Gradle 9.4.1 (via the checked-in wrapper: `./gradlew`), AGP 9.2.1, and JDK 17.
Verified with `./gradlew assembleDebug`.

## Implementation guide

### 1. Add the JitPack repository

In your root `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add the dependency

In your app module's `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.vikas-mourya-dev:android-network-logger:v1.2.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'  // networklogger declares this compileOnly
}
```

### 3. Initialize once, in `Application.onCreate()`

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LoggerConfig config = new LoggerConfig.Builder()
                .setLoggingEnabled(true)
                .setDatabaseStorageEnabled(true)
                .setMaxLogCount(500)
                .setLogBodyMaxLength(10_000)
                .setRedactedHeaders("Authorization", "Cookie")
                .setFloatingBubbleEnabled(false)   // see "Enabling the debug bubble" below
                .build();

        NetworkLogger.init(this, config);
    }
}
```

`init()` must receive an `Application` (or any `Context` whose `getApplicationContext()`
resolves to one) — that's what lets the debug bubble and internal database avoid leaking an
Activity. Don't forget to register `MyApplication` in your manifest's `<application android:name=...>`.

### 4. Attach the interceptor to your OkHttpClient

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(NetworkLogger.getInterceptor())
        .build();
```

Wherever you build the client Retrofit/your own network layer uses — that's the only wiring
required. Every request/response through that client is now captured.

### 5. (Optional) Enable the floating debug bubble

See [Enabling the debug bubble](#enabling-the-debug-bubble) below.

### 6. Read, sort, and manage logs from your own UI

See [Using the repository methods](#using-the-repository-methods) below — or skip building your
own screen entirely and launch the built-in one:

```java
startActivity(NetworkLogActivity.newIntent(context));
```

That's the whole integration. The sample app in `sample-app/` demonstrates all of the above end
to end (GET/POST/failing-call buttons, a log list, and the debug-bubble switch).

## Using the repository methods

Everything a consuming app needs goes through the `NetworkLogger` facade — you never touch
Room/DAO types directly, and every call here is safe to make from any thread on the main looper
(reads deliver via `LiveData`/callback on the main thread; writes happen on a background
executor internally).

**Read all logs** (as `LiveData`, so your UI updates automatically as new requests come in):

```java
LiveData<List<NetworkLogEntity>> logs = NetworkLogger.getInstance().getLogs();
logs.observe(this, list -> adapter.submitList(list));
```

**Fetch and sort** — pass a `NetworkLogSortOrder` instead:

```java
LiveData<List<NetworkLogEntity>> byLatency =
        NetworkLogger.getInstance().getLogs(NetworkLogSortOrder.LATENCY_DESC);
```

Available orders: `TIME_DESC` (default, newest first), `TIME_ASC`, `LATENCY_DESC`,
`LATENCY_ASC`, `STATUS_CODE`.

**Look up a single log by id:**

```java
NetworkLogger.getInstance().getLogById(id, entity -> {
    // invoked on the main thread; entity is null if no log with that id exists
});
```

**Delete:**

```java
NetworkLogger.getInstance().deleteLogById(id);   // a single record
NetworkLogger.getInstance().clearLogs();         // every stored log
```

**Toggle logging at runtime** (without tearing down the interceptor):

```java
NetworkLogger.getInstance().setEnabled(false);
boolean currentlyOn = NetworkLogger.getInstance().isLoggingEnabled();
```

**Displaying timestamps:** each `NetworkLogEntity` exposes `getRequestTimestamp()` /
`getResponseTimestamp()` as raw epoch millis, plus `getFormattedRequestTime()` /
`getFormattedResponseTime()` which render them in the **host device's local timezone** (not
GMT) — safe to show directly in UI without any conversion on your part.

## Enabling the debug bubble

A draggable in-app bubble (Start/Stop Logging, View Logs, Clear Logs) you can turn on/off at
runtime — so you only capture traffic while actively debugging, rather than logging everything
for the whole life of the app. It's rendered inside your own activities' content view, **not** a
system overlay: no `SYSTEM_ALERT_WINDOW` permission, no foreground service, and it can never
show over other apps — only within this one.

**Turn it on/off from anywhere** (e.g. a debug settings screen, a shake gesture, a build-variant
check):

```java
NetworkLogger.getInstance().showFloatingBubble();   // attaches to every activity of this app
NetworkLogger.getInstance().hideFloatingBubble();
boolean visible = NetworkLogger.getInstance().isFloatingBubbleVisible();
```

**Or show it automatically** as soon as `init()` runs, via config:

```java
LoggerConfig config = new LoggerConfig.Builder()
        .setFloatingBubbleEnabled(true)
        .build();
NetworkLogger.init(this, config);
```

**Using it:** drag the bubble anywhere on screen; tap it (without dragging) to open its action
menu — Start Logging / Stop Logging (whichever matches the current state is disabled), View
Logs, Clear Logs. Its color reflects whether logging is currently on (teal) or stopped (grey),
updating live as you toggle it.

**Excluding specific activities:** if your app hosts a third-party SDK screen whose theme doesn't
extend AppCompat/MaterialComponents, the bubble already inflates its own menu/dialog against a
bundled MaterialComponents theme so it won't crash there — but you can still opt any activity out
entirely:

```java
LoggerConfig config = new LoggerConfig.Builder()
        .setExcludedActivityClasses(PaymentSdkActivity.class, OtherSdkActivity.class)
        .build();
```

## Config options

| Option | Default | Description |
|---|---|---|
| `setLoggingEnabled` | `true` | Master on/off switch, checked on every call |
| `setDatabaseStorageEnabled` | `true` | Whether captured logs are persisted |
| `setMaxLogCount` | `500` | Oldest logs beyond this count are pruned after each insert |
| `setLogBodyMaxLength` | `10000` | Request/response bodies are truncated past this many characters |
| `setRedactedHeaders` | none | Header names replaced with `[redacted]` in stored logs |
| `setFloatingBubbleEnabled` | `false` | Shows the draggable debug bubble as soon as `init()` runs |
| `setExcludedActivityClasses` | none | Activities the debug bubble never attaches to |

## Design notes

- The interceptor uses `response.peekBody()` rather than consuming the response, so the host
  app's own Retrofit/OkHttp call always receives an untouched body.
- Request bodies marked `isOneShot()` (streaming uploads, single-use sources) are never buffered
  for logging — doing so would read them twice and could corrupt or break the real upload. These
  show up as `[one-shot/streaming body skipped]` instead of their content.
- All database writes go through a dedicated single-thread executor — the interceptor never
  blocks the network thread on disk I/O.
- Every internal operation (body parsing, DB writes) is wrapped in try/catch: a bug in this
  library cannot crash the host app's network call.
- The Room database is namespaced (`network_logger_internal.db`) so it never collides with the
  host app's own database.
- The debug bubble attaches to `android.R.id.content` via `Application.ActivityLifecycleCallbacks`
  rather than a `TYPE_APPLICATION_OVERLAY` window, so it needs no special permission and can only
  ever render inside this app.

## Modules

- `networklogger/` — the library
- `sample-app/` — a demo app hitting JSONPlaceholder with GET/POST/failing-call buttons, a live
  view of captured logs, and a switch for the debug bubble
