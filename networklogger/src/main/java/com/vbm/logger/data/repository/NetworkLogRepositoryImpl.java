package com.vbm.logger.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.vbm.logger.callback.NetworkLogCallback;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.data.dao.NetworkLogDao;
import com.vbm.logger.data.entity.NetworkLogEntity;
import com.vbm.logger.util.AppExecutors;

import java.util.List;

public class NetworkLogRepositoryImpl implements NetworkLogRepository {

    private static final String TAG = "NetworkLogger";

    private final NetworkLogDao dao;
    private final AppExecutors executors;
    private final int maxLogCount;

    public NetworkLogRepositoryImpl(NetworkLogDao dao, AppExecutors executors, int maxLogCount) {
        this.dao = dao;
        this.executors = executors;
        this.maxLogCount = maxLogCount;
    }

    @Override
    public void insert(NetworkLogEntity entity) {
        executors.diskIO().execute(() -> {
            try {
                dao.insert(entity);
                pruneIfNeeded();
            } catch (Exception e) {
                // A storage failure must never crash the host app's network call.
                Log.w(TAG, "Failed to persist network log", e);
            }
        });
    }

    private void pruneIfNeeded() {
        if (maxLogCount <= 0) {
            return;
        }
        int count = dao.getLogCount();
        int excess = count - maxLogCount;
        if (excess > 0) {
            dao.deleteOldest(excess);
        }
    }

    @Override
    public LiveData<List<NetworkLogEntity>> getAllLogs() {
        return dao.getAllLogs();
    }

    @Override
    public LiveData<List<NetworkLogEntity>> getLogs(NetworkLogSortOrder sortOrder) {
        switch (sortOrder) {
            case TIME_ASC:
                return dao.getAllLogsByTimeAsc();
            case LATENCY_DESC:
                return dao.getAllLogsByLatencyDesc();
            case LATENCY_ASC:
                return dao.getAllLogsByLatencyAsc();
            case STATUS_CODE:
                return dao.getAllLogsByStatusCode();
            case TIME_DESC:
            default:
                return dao.getAllLogsByTimeDesc();
        }
    }

    @Override
    public void getLogById(long id, NetworkLogCallback callback) {
        executors.diskIO().execute(() -> {
            NetworkLogEntity entity = null;
            try {
                entity = dao.getLogById(id);
            } catch (Exception e) {
                Log.w(TAG, "Failed to load network log", e);
            }
            NetworkLogEntity result = entity;
            executors.mainThread().execute(() -> callback.onResult(result));
        });
    }

    @Override
    public void deleteById(long id) {
        executors.diskIO().execute(() -> {
            try {
                dao.deleteById(id);
            } catch (Exception e) {
                Log.w(TAG, "Failed to delete network log", e);
            }
        });
    }

    @Override
    public void clearAll() {
        executors.diskIO().execute(() -> {
            try {
                dao.clearAll();
            } catch (Exception e) {
                Log.w(TAG, "Failed to clear network logs", e);
            }
        });
    }
}
