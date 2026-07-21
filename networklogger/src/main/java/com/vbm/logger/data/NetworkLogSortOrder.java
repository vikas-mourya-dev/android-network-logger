package com.vbm.logger.data;

/** Sort orders available when fetching logs via {@link com.vbm.logger.NetworkLogger#getLogs(NetworkLogSortOrder)}. */
public enum NetworkLogSortOrder {
    TIME_DESC,
    TIME_ASC,
    LATENCY_DESC,
    LATENCY_ASC,
    STATUS_CODE
}
