package com.vbm.logger.sample;

import android.app.Application;

import com.vbm.logger.NetworkLogger;
import com.vbm.logger.config.LoggerConfig;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LoggerConfig config = new LoggerConfig.Builder()
                .setLoggingEnabled(true)
                .setDatabaseStorageEnabled(true)
                .setMaxLogCount(500)
                .setLogBodyMaxLength(10_000)
                .setRedactedHeaders("Authorization", "Cookie")
                .build();

        NetworkLogger.init(this, config);
    }
}
