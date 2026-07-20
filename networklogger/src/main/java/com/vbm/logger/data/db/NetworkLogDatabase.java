package com.vbm.logger.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.vbm.logger.data.dao.NetworkLogDao;
import com.vbm.logger.data.entity.NetworkLogEntity;

@Database(entities = {NetworkLogEntity.class}, version = 1, exportSchema = false)
public abstract class NetworkLogDatabase extends RoomDatabase {

    // Namespaced so it never collides with the host app's own database.
    private static final String DB_NAME = "network_logger_internal.db";

    private static volatile NetworkLogDatabase instance;

    public abstract NetworkLogDao networkLogDao();

    public static NetworkLogDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (NetworkLogDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    NetworkLogDatabase.class,
                                    DB_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
