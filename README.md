# NetworkLogger

## Opening the project

Requires Gradle 9.4.1 (via the checked-in wrapper: `./gradlew`), AGP 9.2.1, and JDK 17.
Verified with `./gradlew assembleDebug`.


A drop-in OkHttp interceptor that captures every request/response, persists it to a local
Room database, and exposes it as `LiveData` — plus an optional prebuilt log-viewer screen.

## Install

Add JitPack to your root `settings.gradle`:

```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Then add the dependency:

```groovy
implementation 'com.github.vikas-mourya-dev:android-network-logger:v1.1.0'
implementation 'com.squareup.okhttp3:okhttp:4.12.0'  // networklogger declares this compileOnly
```

## Setup

```java
// Application.onCreate()
LoggerConfig config = new LoggerConfig.Builder()
        .setLoggingEnabled(true)
        .setDatabaseStorageEnabled(true)
        .setMaxLogCount(500)
        .setLogBodyMaxLength(10_000)
        .setRedactedHeaders("Authorization", "Cookie")
        .build();

NetworkLogger.init(this, config);
```

```java
OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(NetworkLogger.getInterceptor())
        .build();
```

## Repository API

Everything a consuming app needs goes through the `NetworkLogger` facade — you never touch
Room/DAO types directly.

```java
// All logs, most recent first
LiveData<List<NetworkLogEntity>> logs = NetworkLogger.getInstance().getLogs();

// Fetch and sort
LiveData<List<NetworkLogEntity>> byLatency =
        NetworkLogger.getInstance().getLogs(NetworkLogSortOrder.LATENCY_DESC);
```

Available `NetworkLogSortOrder` values: `TIME_DESC` (default), `TIME_ASC`, `LATENCY_DESC`,
`LATENCY_ASC`, `STATUS_CODE`.

```java
// Look up a single log
NetworkLogger.getInstance().getLogById(id, entity -> {
    // called on the main thread; entity is null if not found
});

// Delete
NetworkLogger.getInstance().deleteLogById(id);   // one record
NetworkLogger.getInstance().clearLogs();         // everything

// Toggle at runtime
NetworkLogger.getInstance().setEnabled(false);
```

Each `NetworkLogEntity` exposes `getRequestTimestamp()`/`getResponseTimestamp()` as raw epoch
millis, plus `getFormattedRequestTime()`/`getFormattedResponseTime()` which render them in the
**host device's local timezone** (not GMT) — safe to show directly in UI.

Or launch the built-in viewer instead of building your own screen:

```java
startActivity(NetworkLogActivity.newIntent(context));
```

## Config options

| Option | Default | Description |
|---|---|---|
| `setLoggingEnabled` | `true` | Master on/off switch, checked on every call |
| `setDatabaseStorageEnabled` | `true` | Whether captured logs are persisted |
| `setMaxLogCount` | `500` | Oldest logs beyond this count are pruned after each insert |
| `setLogBodyMaxLength` | `10000` | Request/response bodies are truncated past this many characters |
| `setRedactedHeaders` | none | Header names replaced with `[redacted]` in stored logs |

## Design notes

- The interceptor uses `response.peekBody()` rather than consuming the response, so the host
  app's own Retrofit/OkHttp call always receives an untouched body.
- All database writes go through a dedicated single-thread executor — the interceptor never
  blocks the network thread on disk I/O.
- Every internal operation (body parsing, DB writes) is wrapped in try/catch: a bug in this
  library cannot crash the host app's network call.
- The Room database is namespaced (`network_logger_internal.db`) so it never collides with the
  host app's own database.

## Modules

- `networklogger/` — the library
- `sample-app/` — a demo app hitting JSONPlaceholder with GET/POST/failing-call buttons and a
  live view of captured logs
