package com.vbm.logger.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.vbm.logger.NetworkLogger;
import com.vbm.logger.data.NetworkLogSortOrder;
import com.vbm.logger.data.entity.NetworkLogEntity;

import java.util.List;

public class NetworkLogViewModel extends AndroidViewModel {

    private final MutableLiveData<NetworkLogSortOrder> sortOrder =
            new MutableLiveData<>(NetworkLogSortOrder.TIME_DESC);
    private final LiveData<List<NetworkLogEntity>> logs =
            Transformations.switchMap(sortOrder, NetworkLogger.getInstance()::getLogs);

    public NetworkLogViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<NetworkLogEntity>> getLogs() {
        return logs;
    }

    public void setSortOrder(NetworkLogSortOrder order) {
        sortOrder.setValue(order);
    }

    public void deleteLogById(long id) {
        NetworkLogger.getInstance().deleteLogById(id);
    }

    public void clearLogs() {
        NetworkLogger.getInstance().clearLogs();
    }
}
