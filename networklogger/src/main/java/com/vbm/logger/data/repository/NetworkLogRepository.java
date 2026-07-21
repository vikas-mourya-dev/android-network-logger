package com.vbm.logger.data.repository;

import androidx.lifecycle.LiveData;

import com.vbm.logger.callback.NetworkLogCallback;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.List;

/**
 * Read/write access to captured logs. Obtain an instance via
 * {@link com.vbm.logger.NetworkLogger#getInstance()} rather than constructing this directly.
 */
public interface NetworkLogRepository {

    void insert(NetworkLogEntity entity);

    /** All logs, most recent first. Equivalent to {@code getLogs(NetworkLogSortOrder.TIME_DESC)}. */
    LiveData<List<NetworkLogEntity>> getAllLogs();

    /** All logs in the given order. */
    LiveData<List<NetworkLogEntity>> getLogs(NetworkLogSortOrder sortOrder);

    void getLogById(long id, NetworkLogCallback callback);

    void deleteById(long id);

    void clearAll();
}
