package com.vbm.logger;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.vbm.logger.callback.NetworkLogCallback;
import com.vbm.logger.config.LoggerConfig;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.data.db.NetworkLogDatabase;
import com.vbm.logger.data.entity.NetworkLogEntity;
import com.vbm.logger.data.repository.NetworkLogRepository;
import com.vbm.logger.data.repository.NetworkLogRepositoryImpl;
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

    private static volatile NetworkLogger instance;

    private final NetworkLogRepository repository;
    private final NetworkLogInterceptor interceptor;
    private final AtomicReference<LoggerConfig> configRef;

    private NetworkLogger(Context context, LoggerConfig config) {
        this.configRef = new AtomicReference<>(config);
        NetworkLogDatabase database = NetworkLogDatabase.getInstance(context);
        this.repository = new NetworkLogRepositoryImpl(
                database.networkLogDao(), AppExecutors.getInstance(), config.getMaxLogCount());
        this.interceptor = new NetworkLogInterceptor(repository, configRef);
    }

    /**
     * Must be called once, typically from {@code Application.onCreate()}.
     * Uses applicationContext internally to avoid leaking the caller's context.
     */
    public static void init(@NonNull Context context, @NonNull LoggerConfig config) {
        if (instance == null) {
            synchronized (NetworkLogger.class) {
                if (instance == null) {
                    instance = new NetworkLogger(context.getApplicationContext(), config);
                }
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
        LoggerConfig current = configRef.get();
        configRef.set(LoggerConfig.builder()
                .setLoggingEnabled(enabled)
                .setDatabaseStorageEnabled(current.isDatabaseStorageEnabled())
                .setMaxLogCount(current.getMaxLogCount())
                .setLogBodyMaxLength(current.getLogBodyMaxLength())
                .setRedactedHeaders(current.getRedactedHeaders().toArray(new String[0]))
                .build());
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
