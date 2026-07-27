package com.vbm.logger;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vbm.logger.callback.NetworkLogCallback;
import com.vbm.logger.config.LoggerConfig;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.data.db.NetworkLogDatabase;
import com.vbm.logger.data.entity.NetworkLogEntity;
import com.vbm.logger.data.repository.NetworkLogRepository;
import com.vbm.logger.data.repository.NetworkLogRepositoryImpl;
import com.vbm.logger.floating.FloatingBubbleManager;
import com.vbm.logger.interceptor.NetworkLogInterceptor;
import com.vbm.logger.util.AppExecutors;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Interceptor;

/**
 * Public entry point for the library. Initialize once with {@link #init}, then
 * add {@link #getInterceptor()} to your OkHttpClient.
 */
public final class NetworkLogger {

    private static final String TAG = "NetworkLogger";

    private static volatile NetworkLogger instance;

    private final NetworkLogRepository repository;
    private final NetworkLogInterceptor interceptor;
    private final AtomicReference<LoggerConfig> configRef;
    private final Application application;

    private NetworkLogger(Context context, LoggerConfig config) {
        this.configRef = new AtomicReference<>(config);
        this.application = context instanceof Application ? (Application) context : null;
        NetworkLogDatabase database = NetworkLogDatabase.getInstance(context);
        this.repository = new NetworkLogRepositoryImpl(
                database.networkLogDao(), AppExecutors.getInstance(), config.getMaxLogCount());
        this.interceptor = new NetworkLogInterceptor(repository, configRef);

        // Registered unconditionally (not just when the bubble is shown): a lifecycle callback
        // added later would miss the onStart() of an activity that's already running.
        if (application != null) {
            FloatingBubbleManager.registerTracking(application);
        }
    }

    /**
     * Must be called once, typically from {@code Application.onCreate()}.
     * Uses applicationContext internally to avoid leaking the caller's context.
     */
    public static void init(@NonNull Context context, @NonNull LoggerConfig config) {
        if (instance != null) {
            Log.w(TAG, "init() called more than once; ignoring this call and keeping the existing "
                    + "configuration. Use setEnabled()/other runtime setters to change behavior after init().");
            return;
        }
        synchronized (NetworkLogger.class) {
            if (instance != null) {
                Log.w(TAG, "init() called more than once; ignoring this call and keeping the existing "
                        + "configuration. Use setEnabled()/other runtime setters to change behavior after init().");
                return;
            }
            instance = new NetworkLogger(context.getApplicationContext(), config);
            if (config.isFloatingBubbleEnabled()) {
                instance.showFloatingBubble();
            }
        }
    }

    public static NetworkLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("NetworkLogger.init() must be called before getInstance()");
        }
        return instance;
    }

    /** Add this to your OkHttpClient.Builder via addInterceptor(). */
    public static Interceptor getInterceptor() {
        return getInstance().interceptor;
    }

    public void setEnabled(boolean enabled) {
        configRef.updateAndGet(current -> LoggerConfig.builder()
                .setLoggingEnabled(enabled)
                .setDatabaseStorageEnabled(current.isDatabaseStorageEnabled())
                .setMaxLogCount(current.getMaxLogCount())
                .setLogBodyMaxLength(current.getLogBodyMaxLength())
                .setRedactedHeaders(current.getRedactedHeaders().toArray(new String[0]))
                .setFloatingBubbleEnabled(current.isFloatingBubbleEnabled())
                .setExcludedActivityClasses(current.getExcludedActivityClasses().toArray(new Class<?>[0]))
                .build());
    }

    public boolean isLoggingEnabled() {
        return configRef.get().isLoggingEnabled();
    }

    /**
     * Shows a draggable in-app debug bubble (Start/Stop Logging, View Logs, Clear Logs) on every
     * activity of this app. Requires {@link #init} to have been called with an {@code Application}
     * context (the usual case). Safe to call repeatedly.
     */
    public void showFloatingBubble() {
        if (application == null) {
            Log.w(TAG, "showFloatingBubble() requires NetworkLogger.init() to receive an Application context");
            return;
        }
        FloatingBubbleManager.start(application);
    }

    /** Hides the debug bubble from every activity. */
    public void hideFloatingBubble() {
        if (application != null) {
            FloatingBubbleManager.stop(application);
        }
    }

    public boolean isFloatingBubbleVisible() {
        return FloatingBubbleManager.isEnabled();
    }

    /** Whether the debug bubble is configured to never attach to this activity class. */
    public boolean isActivityExcludedFromBubble(Class<?> activityClass) {
        return configRef.get().getExcludedActivityClasses().contains(activityClass);
    }

    /** All logs, most recent first. */
    public LiveData<List<NetworkLogEntity>> getLogs() {
        return repository.getAllLogs();
    }

    /** All logs in the given order — see {@link NetworkLogSortOrder}. */
    public LiveData<List<NetworkLogEntity>> getLogs(NetworkLogSortOrder sortOrder) {
        return repository.getLogs(sortOrder);
    }

    /** Looks up a single log by id; {@code callback} is invoked on the main thread with {@code null} if not found. */
    public void getLogById(long id, NetworkLogCallback callback) {
        repository.getLogById(id, callback);
    }

    /** Deletes a single log entry. */
    public void deleteLogById(long id) {
        repository.deleteById(id);
    }

    /** Deletes every stored log. */
    public void clearLogs() {
        repository.clearAll();
    }
}
