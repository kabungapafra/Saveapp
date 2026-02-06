package com.example.save.data.local.dao;

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
import com.example.save.data.local.Converters;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.models.TransactionWithApproval;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TransactionDao_Impl implements TransactionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TransactionEntity> __insertionAdapterOfTransactionEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  public TransactionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTransactionEntity = new EntityInsertionAdapter<TransactionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `transactions` (`id`,`memberName`,`type`,`amount`,`description`,`date`,`isPositive`,`paymentMethod`,`status`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TransactionEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getMemberName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getMemberName());
        }
        if (entity.getType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getType());
        }
        statement.bindDouble(4, entity.getAmount());
        if (entity.getDescription() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDescription());
        }
        final Long _tmp = Converters.dateToTimestamp(entity.getDate());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp);
        }
        final int _tmp_1 = entity.isPositive() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        if (entity.getPaymentMethod() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPaymentMethod());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getStatus());
        }
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE transactions SET status = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final TransactionEntity transaction) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTransactionEntity.insert(transaction);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void updateStatus(final String id, final String status) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
    int _argIndex = 1;
    if (status == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, status);
    }
    _argIndex = 2;
    if (id == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, id);
    }
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfUpdateStatus.release(_stmt);
    }
  }

  @Override
  public LiveData<List<TransactionEntity>> getRecentTransactions() {
    final String _sql = "SELECT * FROM transactions ORDER BY date DESC LIMIT 20";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<List<TransactionEntity>>() {
      @Override
      @Nullable
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpMemberName;
            if (_cursor.isNull(_cursorIndexOfMemberName)) {
              _tmpMemberName = null;
            } else {
              _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Date _tmpDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDate);
            }
            _tmpDate = Converters.fromTimestamp(_tmp);
            final boolean _tmpIsPositive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
            _tmpIsPositive = _tmp_1 != 0;
            final String _tmpPaymentMethod;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmpPaymentMethod = null;
            } else {
              _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _item = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
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
  public LiveData<List<TransactionEntity>> getLatestMemberTransactions(final String memberName) {
    final String _sql = "SELECT * FROM transactions WHERE memberName = ? ORDER BY date DESC LIMIT 5";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (memberName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<List<TransactionEntity>>() {
      @Override
      @Nullable
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpMemberName;
            if (_cursor.isNull(_cursorIndexOfMemberName)) {
              _tmpMemberName = null;
            } else {
              _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Date _tmpDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDate);
            }
            _tmpDate = Converters.fromTimestamp(_tmp);
            final boolean _tmpIsPositive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
            _tmpIsPositive = _tmp_1 != 0;
            final String _tmpPaymentMethod;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmpPaymentMethod = null;
            } else {
              _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _item = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
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
  public List<TransactionEntity> getAllTransactionsSync() {
    final String _sql = "SELECT * FROM transactions ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
      final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final TransactionEntity _item;
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final Date _tmpDate;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDate);
        }
        _tmpDate = Converters.fromTimestamp(_tmp);
        final boolean _tmpIsPositive;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
        _tmpIsPositive = _tmp_1 != 0;
        final String _tmpPaymentMethod;
        if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
          _tmpPaymentMethod = null;
        } else {
          _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public double getTotalIncoming() {
    final String _sql = "SELECT TOTAL(amount) FROM transactions WHERE isPositive = 1 AND status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final double _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getDouble(0);
      } else {
        _result = 0.0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public double getTotalOutgoing() {
    final String _sql = "SELECT TOTAL(amount) FROM transactions WHERE isPositive = 0 AND status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final double _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getDouble(0);
      } else {
        _result = 0.0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<Double> getGroupBalance() {
    final String _sql = "SELECT TOTAL(CASE WHEN isPositive = 1 THEN amount ELSE -amount END) FROM transactions WHERE status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
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
  public LiveData<List<TransactionEntity>> getPendingTransactions() {
    final String _sql = "SELECT * FROM transactions WHERE status = 'PENDING_APPROVAL' ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"transactions"}, false, new Callable<List<TransactionEntity>>() {
      @Override
      @Nullable
      public List<TransactionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionEntity _item;
            final String _tmpMemberName;
            if (_cursor.isNull(_cursorIndexOfMemberName)) {
              _tmpMemberName = null;
            } else {
              _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final Date _tmpDate;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDate);
            }
            _tmpDate = Converters.fromTimestamp(_tmp);
            final boolean _tmpIsPositive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
            _tmpIsPositive = _tmp_1 != 0;
            final String _tmpPaymentMethod;
            if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
              _tmpPaymentMethod = null;
            } else {
              _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _item = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
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
  public TransactionEntity getTransactionById(final String id) {
    final String _sql = "SELECT * FROM transactions WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
      final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final TransactionEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final Date _tmpDate;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDate);
        }
        _tmpDate = Converters.fromTimestamp(_tmp);
        final boolean _tmpIsPositive;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
        _tmpIsPositive = _tmp_1 != 0;
        final String _tmpPaymentMethod;
        if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
          _tmpPaymentMethod = null;
        } else {
          _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _result = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _result.setId(_tmpId);
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
  public List<TransactionEntity> getTransactionsPaged(final int limit, final int offset) {
    final String _sql = "SELECT * FROM transactions ORDER BY date DESC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 2;
    _statement.bindLong(_argIndex, offset);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
      final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
      final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final List<TransactionEntity> _result = new ArrayList<TransactionEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final TransactionEntity _item;
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        final String _tmpDescription;
        if (_cursor.isNull(_cursorIndexOfDescription)) {
          _tmpDescription = null;
        } else {
          _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
        }
        final Date _tmpDate;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDate)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDate);
        }
        _tmpDate = Converters.fromTimestamp(_tmp);
        final boolean _tmpIsPositive;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
        _tmpIsPositive = _tmp_1 != 0;
        final String _tmpPaymentMethod;
        if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
          _tmpPaymentMethod = null;
        } else {
          _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
        }
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<TransactionWithApproval>> getPendingTransactionsWithApproval(
      final String adminEmail) {
    final String _sql = "SELECT t.*, (SELECT COUNT(*) FROM approvals a WHERE a.targetId = t.id AND a.type = 'PAYOUT') as approvalCount, (SELECT COUNT(*) > 0 FROM approvals a WHERE a.targetId = t.id AND a.type = 'PAYOUT' AND a.adminEmail = ?) as isApprovedByAdmin FROM transactions t WHERE t.status = 'PENDING_APPROVAL' OR t.status = 'PENDING' ORDER BY t.date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (adminEmail == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, adminEmail);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"approvals",
        "transactions"}, false, new Callable<List<TransactionWithApproval>>() {
      @Override
      @Nullable
      public List<TransactionWithApproval> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfApprovalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "approvalCount");
          final int _cursorIndexOfIsApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "isApprovedByAdmin");
          final List<TransactionWithApproval> _result = new ArrayList<TransactionWithApproval>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionWithApproval _item;
            final TransactionEntity _tmpTransaction;
            if (!(_cursor.isNull(_cursorIndexOfId) && _cursor.isNull(_cursorIndexOfMemberName) && _cursor.isNull(_cursorIndexOfType) && _cursor.isNull(_cursorIndexOfAmount) && _cursor.isNull(_cursorIndexOfDescription) && _cursor.isNull(_cursorIndexOfDate) && _cursor.isNull(_cursorIndexOfIsPositive) && _cursor.isNull(_cursorIndexOfPaymentMethod) && _cursor.isNull(_cursorIndexOfStatus))) {
              final String _tmpMemberName;
              if (_cursor.isNull(_cursorIndexOfMemberName)) {
                _tmpMemberName = null;
              } else {
                _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
              }
              final String _tmpType;
              if (_cursor.isNull(_cursorIndexOfType)) {
                _tmpType = null;
              } else {
                _tmpType = _cursor.getString(_cursorIndexOfType);
              }
              final double _tmpAmount;
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
              final String _tmpDescription;
              if (_cursor.isNull(_cursorIndexOfDescription)) {
                _tmpDescription = null;
              } else {
                _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              }
              final Date _tmpDate;
              final Long _tmp;
              if (_cursor.isNull(_cursorIndexOfDate)) {
                _tmp = null;
              } else {
                _tmp = _cursor.getLong(_cursorIndexOfDate);
              }
              _tmpDate = Converters.fromTimestamp(_tmp);
              final boolean _tmpIsPositive;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
              _tmpIsPositive = _tmp_1 != 0;
              final String _tmpPaymentMethod;
              if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
                _tmpPaymentMethod = null;
              } else {
                _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
              }
              final String _tmpStatus;
              if (_cursor.isNull(_cursorIndexOfStatus)) {
                _tmpStatus = null;
              } else {
                _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              }
              _tmpTransaction = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
              final String _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getString(_cursorIndexOfId);
              }
              _tmpTransaction.setId(_tmpId);
            } else {
              _tmpTransaction = null;
            }
            _item = new TransactionWithApproval();
            _item.approvalCount = _cursor.getInt(_cursorIndexOfApprovalCount);
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsApprovedByAdmin);
            _item.isApprovedByAdmin = _tmp_2 != 0;
            _item.transaction = _tmpTransaction;
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
  public LiveData<List<TransactionWithApproval>> getMemberTransactionsWithApproval(
      final String memberName) {
    final String _sql = "SELECT t.*, (SELECT COUNT(*) FROM approvals a WHERE a.targetId = t.id AND a.type = 'PAYOUT') as approvalCount, 0 as isApprovedByAdmin FROM transactions t WHERE t.memberName = ? ORDER BY t.date DESC LIMIT 20";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (memberName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"approvals",
        "transactions"}, false, new Callable<List<TransactionWithApproval>>() {
      @Override
      @Nullable
      public List<TransactionWithApproval> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfIsPositive = CursorUtil.getColumnIndexOrThrow(_cursor, "isPositive");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfApprovalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "approvalCount");
          final int _cursorIndexOfIsApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "isApprovedByAdmin");
          final List<TransactionWithApproval> _result = new ArrayList<TransactionWithApproval>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TransactionWithApproval _item;
            final TransactionEntity _tmpTransaction;
            if (!(_cursor.isNull(_cursorIndexOfId) && _cursor.isNull(_cursorIndexOfMemberName) && _cursor.isNull(_cursorIndexOfType) && _cursor.isNull(_cursorIndexOfAmount) && _cursor.isNull(_cursorIndexOfDescription) && _cursor.isNull(_cursorIndexOfDate) && _cursor.isNull(_cursorIndexOfIsPositive) && _cursor.isNull(_cursorIndexOfPaymentMethod) && _cursor.isNull(_cursorIndexOfStatus))) {
              final String _tmpMemberName;
              if (_cursor.isNull(_cursorIndexOfMemberName)) {
                _tmpMemberName = null;
              } else {
                _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
              }
              final String _tmpType;
              if (_cursor.isNull(_cursorIndexOfType)) {
                _tmpType = null;
              } else {
                _tmpType = _cursor.getString(_cursorIndexOfType);
              }
              final double _tmpAmount;
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
              final String _tmpDescription;
              if (_cursor.isNull(_cursorIndexOfDescription)) {
                _tmpDescription = null;
              } else {
                _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
              }
              final Date _tmpDate;
              final Long _tmp;
              if (_cursor.isNull(_cursorIndexOfDate)) {
                _tmp = null;
              } else {
                _tmp = _cursor.getLong(_cursorIndexOfDate);
              }
              _tmpDate = Converters.fromTimestamp(_tmp);
              final boolean _tmpIsPositive;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsPositive);
              _tmpIsPositive = _tmp_1 != 0;
              final String _tmpPaymentMethod;
              if (_cursor.isNull(_cursorIndexOfPaymentMethod)) {
                _tmpPaymentMethod = null;
              } else {
                _tmpPaymentMethod = _cursor.getString(_cursorIndexOfPaymentMethod);
              }
              final String _tmpStatus;
              if (_cursor.isNull(_cursorIndexOfStatus)) {
                _tmpStatus = null;
              } else {
                _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              }
              _tmpTransaction = new TransactionEntity(_tmpMemberName,_tmpType,_tmpAmount,_tmpDescription,_tmpDate,_tmpIsPositive,_tmpPaymentMethod,_tmpStatus);
              final String _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getString(_cursorIndexOfId);
              }
              _tmpTransaction.setId(_tmpId);
            } else {
              _tmpTransaction = null;
            }
            _item = new TransactionWithApproval();
            _item.approvalCount = _cursor.getInt(_cursorIndexOfApprovalCount);
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsApprovedByAdmin);
            _item.isApprovedByAdmin = _tmp_2 != 0;
            _item.transaction = _tmpTransaction;
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
  public int getPendingPayoutCount() {
    final String _sql = "SELECT COUNT(*) FROM transactions WHERE status = 'PENDING_APPROVAL' AND type = 'MEMBER_PAYOUT'";
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
