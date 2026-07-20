package com.vbm.logger.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.vbm.logger.NetworkLogger;
import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.List;

public class NetworkLogViewModel extends AndroidViewModel {

    private final LiveData<List<NetworkLogEntity>> logs;

    public NetworkLogViewModel(@NonNull Application application) {
        super(application);
        logs = NetworkLogger.getInstance().getLogs();
    }

    public LiveData<List<NetworkLogEntity>> getLogs() {
        return logs;
    }

    public void clearLogs() {
        NetworkLogger.getInstance().clearLogs();
    }
}
