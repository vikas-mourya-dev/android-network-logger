package com.vbm.logger.callback;

import com.vbm.logger.data.entity.NetworkLogEntity;

/** Result callback for single-record repository lookups, delivered on the main thread. */
public interface NetworkLogCallback {
    void onResult(NetworkLogEntity entity);
}
