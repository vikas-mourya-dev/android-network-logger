package com.vbm.logger.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.List;

@Dao
public interface NetworkLogDao {

    @Insert
    long insert(NetworkLogEntity entity);

    @Query("SELECT * FROM network_logs ORDER BY requestTimestamp DESC")
    LiveData<List<NetworkLogEntity>> getAllLogs();

    @Query("SELECT * FROM network_logs WHERE id = :id")
    NetworkLogEntity getLogById(long id);

    @Query("DELETE FROM network_logs WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM network_logs")
    void clearAll();

    @Query("SELECT COUNT(*) FROM network_logs")
    int getLogCount();

    @Query("DELETE FROM network_logs WHERE id IN (" +
            "SELECT id FROM network_logs ORDER BY requestTimestamp ASC LIMIT :excess)")
    void deleteOldest(int excess);
}
