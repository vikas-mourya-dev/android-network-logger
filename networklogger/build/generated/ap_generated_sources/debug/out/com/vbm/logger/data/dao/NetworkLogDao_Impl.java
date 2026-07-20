package com.vbm.logger.data.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.vbm.logger.data.entity.NetworkLogEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class NetworkLogDao_Impl implements NetworkLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NetworkLogEntity> __insertionAdapterOfNetworkLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOldest;

  public NetworkLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNetworkLogEntity = new EntityInsertionAdapter<NetworkLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `network_logs` (`id`,`url`,`method`,`requestHeaders`,`requestBody`,`responseHeaders`,`responseBody`,`statusCode`,`latencyMs`,`requestTimestamp`,`responseTimestamp`,`errorMessage`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final NetworkLogEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getUrl() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getUrl());
        }
        if (entity.getMethod() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMethod());
        }
        if (entity.getRequestHeaders() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRequestHeaders());
        }
        if (entity.getRequestBody() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getRequestBody());
        }
        if (entity.getResponseHeaders() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getResponseHeaders());
        }
        if (entity.getResponseBody() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getResponseBody());
        }
        statement.bindLong(8, entity.getStatusCode());
        statement.bindLong(9, entity.getLatencyMs());
        statement.bindLong(10, entity.getRequestTimestamp());
        statement.bindLong(11, entity.getResponseTimestamp());
        if (entity.getErrorMessage() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getErrorMessage());
        }
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM network_logs WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM network_logs";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteOldest = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM network_logs WHERE id IN (SELECT id FROM network_logs ORDER BY requestTimestamp ASC LIMIT ?)";
        return _query;
      }
    };
  }

  @Override
  public long insert(final NetworkLogEntity entity) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfNetworkLogEntity.insertAndReturnId(entity);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteById(final long id) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, id);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteById.release(_stmt);
    }
  }

  @Override
  public void clearAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfClearAll.release(_stmt);
    }
  }

  @Override
  public void deleteOldest(final int excess) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOldest.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, excess);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteOldest.release(_stmt);
    }
  }

  @Override
  public LiveData<List<NetworkLogEntity>> getAllLogs() {
    final String _sql = "SELECT * FROM network_logs ORDER BY requestTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"network_logs"}, false, new Callable<List<NetworkLogEntity>>() {
      @Override
      @Nullable
      public List<NetworkLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
          final int _cursorIndexOfRequestHeaders = CursorUtil.getColumnIndexOrThrow(_cursor, "requestHeaders");
          final int _cursorIndexOfRequestBody = CursorUtil.getColumnIndexOrThrow(_cursor, "requestBody");
          final int _cursorIndexOfResponseHeaders = CursorUtil.getColumnIndexOrThrow(_cursor, "responseHeaders");
          final int _cursorIndexOfResponseBody = CursorUtil.getColumnIndexOrThrow(_cursor, "responseBody");
          final int _cursorIndexOfStatusCode = CursorUtil.getColumnIndexOrThrow(_cursor, "statusCode");
          final int _cursorIndexOfLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "latencyMs");
          final int _cursorIndexOfRequestTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "requestTimestamp");
          final int _cursorIndexOfResponseTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "responseTimestamp");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final List<NetworkLogEntity> _result = new ArrayList<NetworkLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final NetworkLogEntity _item;
            _item = new NetworkLogEntity();
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            _item.setId(_tmpId);
            final String _tmpUrl;
            if (_cursor.isNull(_cursorIndexOfUrl)) {
              _tmpUrl = null;
            } else {
              _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            }
            _item.setUrl(_tmpUrl);
            final String _tmpMethod;
            if (_cursor.isNull(_cursorIndexOfMethod)) {
              _tmpMethod = null;
            } else {
              _tmpMethod = _cursor.getString(_cursorIndexOfMethod);
            }
            _item.setMethod(_tmpMethod);
            final String _tmpRequestHeaders;
            if (_cursor.isNull(_cursorIndexOfRequestHeaders)) {
              _tmpRequestHeaders = null;
            } else {
              _tmpRequestHeaders = _cursor.getString(_cursorIndexOfRequestHeaders);
            }
            _item.setRequestHeaders(_tmpRequestHeaders);
            final String _tmpRequestBody;
            if (_cursor.isNull(_cursorIndexOfRequestBody)) {
              _tmpRequestBody = null;
            } else {
              _tmpRequestBody = _cursor.getString(_cursorIndexOfRequestBody);
            }
            _item.setRequestBody(_tmpRequestBody);
            final String _tmpResponseHeaders;
            if (_cursor.isNull(_cursorIndexOfResponseHeaders)) {
              _tmpResponseHeaders = null;
            } else {
              _tmpResponseHeaders = _cursor.getString(_cursorIndexOfResponseHeaders);
            }
            _item.setResponseHeaders(_tmpResponseHeaders);
            final String _tmpResponseBody;
            if (_cursor.isNull(_cursorIndexOfResponseBody)) {
              _tmpResponseBody = null;
            } else {
              _tmpResponseBody = _cursor.getString(_cursorIndexOfResponseBody);
            }
            _item.setResponseBody(_tmpResponseBody);
            final int _tmpStatusCode;
            _tmpStatusCode = _cursor.getInt(_cursorIndexOfStatusCode);
            _item.setStatusCode(_tmpStatusCode);
            final long _tmpLatencyMs;
            _tmpLatencyMs = _cursor.getLong(_cursorIndexOfLatencyMs);
            _item.setLatencyMs(_tmpLatencyMs);
            final long _tmpRequestTimestamp;
            _tmpRequestTimestamp = _cursor.getLong(_cursorIndexOfRequestTimestamp);
            _item.setRequestTimestamp(_tmpRequestTimestamp);
            final long _tmpResponseTimestamp;
            _tmpResponseTimestamp = _cursor.getLong(_cursorIndexOfResponseTimestamp);
            _item.setResponseTimestamp(_tmpResponseTimestamp);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            _item.setErrorMessage(_tmpErrorMessage);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public NetworkLogEntity getLogById(final long id) {
    final String _sql = "SELECT * FROM network_logs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "method");
      final int _cursorIndexOfRequestHeaders = CursorUtil.getColumnIndexOrThrow(_cursor, "requestHeaders");
      final int _cursorIndexOfRequestBody = CursorUtil.getColumnIndexOrThrow(_cursor, "requestBody");
      final int _cursorIndexOfResponseHeaders = CursorUtil.getColumnIndexOrThrow(_cursor, "responseHeaders");
      final int _cursorIndexOfResponseBody = CursorUtil.getColumnIndexOrThrow(_cursor, "responseBody");
      final int _cursorIndexOfStatusCode = CursorUtil.getColumnIndexOrThrow(_cursor, "statusCode");
      final int _cursorIndexOfLatencyMs = CursorUtil.getColumnIndexOrThrow(_cursor, "latencyMs");
      final int _cursorIndexOfRequestTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "requestTimestamp");
      final int _cursorIndexOfResponseTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "responseTimestamp");
      final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
      final NetworkLogEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new NetworkLogEntity();
        final long _tmpId;
        _tmpId = _cursor.getLong(_cursorIndexOfId);
        _result.setId(_tmpId);
        final String _tmpUrl;
        if (_cursor.isNull(_cursorIndexOfUrl)) {
          _tmpUrl = null;
        } else {
          _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
        }
        _result.setUrl(_tmpUrl);
        final String _tmpMethod;
        if (_cursor.isNull(_cursorIndexOfMethod)) {
          _tmpMethod = null;
        } else {
          _tmpMethod = _cursor.getString(_cursorIndexOfMethod);
        }
        _result.setMethod(_tmpMethod);
        final String _tmpRequestHeaders;
        if (_cursor.isNull(_cursorIndexOfRequestHeaders)) {
          _tmpRequestHeaders = null;
        } else {
          _tmpRequestHeaders = _cursor.getString(_cursorIndexOfRequestHeaders);
        }
        _result.setRequestHeaders(_tmpRequestHeaders);
        final String _tmpRequestBody;
        if (_cursor.isNull(_cursorIndexOfRequestBody)) {
          _tmpRequestBody = null;
        } else {
          _tmpRequestBody = _cursor.getString(_cursorIndexOfRequestBody);
        }
        _result.setRequestBody(_tmpRequestBody);
        final String _tmpResponseHeaders;
        if (_cursor.isNull(_cursorIndexOfResponseHeaders)) {
          _tmpResponseHeaders = null;
        } else {
          _tmpResponseHeaders = _cursor.getString(_cursorIndexOfResponseHeaders);
        }
        _result.setResponseHeaders(_tmpResponseHeaders);
        final String _tmpResponseBody;
        if (_cursor.isNull(_cursorIndexOfResponseBody)) {
          _tmpResponseBody = null;
        } else {
          _tmpResponseBody = _cursor.getString(_cursorIndexOfResponseBody);
        }
        _result.setResponseBody(_tmpResponseBody);
        final int _tmpStatusCode;
        _tmpStatusCode = _cursor.getInt(_cursorIndexOfStatusCode);
        _result.setStatusCode(_tmpStatusCode);
        final long _tmpLatencyMs;
        _tmpLatencyMs = _cursor.getLong(_cursorIndexOfLatencyMs);
        _result.setLatencyMs(_tmpLatencyMs);
        final long _tmpRequestTimestamp;
        _tmpRequestTimestamp = _cursor.getLong(_cursorIndexOfRequestTimestamp);
        _result.setRequestTimestamp(_tmpRequestTimestamp);
        final long _tmpResponseTimestamp;
        _tmpResponseTimestamp = _cursor.getLong(_cursorIndexOfResponseTimestamp);
        _result.setResponseTimestamp(_tmpResponseTimestamp);
        final String _tmpErrorMessage;
        if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
          _tmpErrorMessage = null;
        } else {
          _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
        }
        _result.setErrorMessage(_tmpErrorMessage);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getLogCount() {
    final String _sql = "SELECT COUNT(*) FROM network_logs";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
