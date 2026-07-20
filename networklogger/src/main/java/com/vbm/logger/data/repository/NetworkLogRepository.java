package com.vbm.logger.data.repository;

import androidx.lifecycle.LiveData;

import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.List;

public interface NetworkLogRepository {

    interface LogCallback {
        void onLoaded(NetworkLogEntity entity);
    }

    void insert(NetworkLogEntity entity);

    LiveData<List<NetworkLogEntity>> getAllLogs();

    void getLogById(long id, LogCallback callback);

    void deleteById(long id);

    void clearAll();
}
