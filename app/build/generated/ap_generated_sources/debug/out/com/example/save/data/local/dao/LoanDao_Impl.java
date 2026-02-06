package com.example.save.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.save.data.local.Converters;
import com.example.save.data.local.entities.LoanEntity;
import com.example.save.data.models.LoanWithApproval;
import java.lang.Class;
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
public final class LoanDao_Impl implements LoanDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LoanEntity> __insertionAdapterOfLoanEntity;

  private final EntityDeletionOrUpdateAdapter<LoanEntity> __deletionAdapterOfLoanEntity;

  private final EntityDeletionOrUpdateAdapter<LoanEntity> __updateAdapterOfLoanEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public LoanDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLoanEntity = new EntityInsertionAdapter<LoanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `loans` (`id`,`memberId`,`memberName`,`amount`,`interest`,`reason`,`dateRequested`,`dueDate`,`status`,`repaidAmount`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final LoanEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getMemberId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getMemberId());
        }
        if (entity.getMemberName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMemberName());
        }
        statement.bindDouble(4, entity.getAmount());
        statement.bindDouble(5, entity.getInterest());
        if (entity.getReason() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getReason());
        }
        final Long _tmp = Converters.dateToTimestamp(entity.getDateRequested());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, _tmp);
        }
        final Long _tmp_1 = Converters.dateToTimestamp(entity.getDueDate());
        if (_tmp_1 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_1);
        }
        if (entity.getStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getStatus());
        }
        statement.bindDouble(10, entity.getRepaidAmount());
      }
    };
    this.__deletionAdapterOfLoanEntity = new EntityDeletionOrUpdateAdapter<LoanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `loans` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final LoanEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfLoanEntity = new EntityDeletionOrUpdateAdapter<LoanEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `loans` SET `id` = ?,`memberId` = ?,`memberName` = ?,`amount` = ?,`interest` = ?,`reason` = ?,`dateRequested` = ?,`dueDate` = ?,`status` = ?,`repaidAmount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final LoanEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getMemberId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getMemberId());
        }
        if (entity.getMemberName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getMemberName());
        }
        statement.bindDouble(4, entity.getAmount());
        statement.bindDouble(5, entity.getInterest());
        if (entity.getReason() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getReason());
        }
        final Long _tmp = Converters.dateToTimestamp(entity.getDateRequested());
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, _tmp);
        }
        final Long _tmp_1 = Converters.dateToTimestamp(entity.getDueDate());
        if (_tmp_1 == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, _tmp_1);
        }
        if (entity.getStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getStatus());
        }
        statement.bindDouble(10, entity.getRepaidAmount());
        if (entity.getId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getId());
        }
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE loans SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM loans";
        return _query;
      }
    };
  }

  @Override
  public void insert(final LoanEntity loan) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfLoanEntity.insert(loan);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<LoanEntity> loans) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfLoanEntity.insert(loans);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final LoanEntity loan) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfLoanEntity.handle(loan);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final LoanEntity loan) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfLoanEntity.handle(loan);
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
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<LoanEntity>> getAllLoans() {
    final String _sql = "SELECT * FROM loans ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"loans"}, false, new Callable<List<LoanEntity>>() {
      @Override
      @Nullable
      public List<LoanEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
          final List<LoanEntity> _result = new ArrayList<LoanEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanEntity _item;
            _item = new LoanEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpMemberId;
            if (_cursor.isNull(_cursorIndexOfMemberId)) {
              _tmpMemberId = null;
            } else {
              _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
            }
            _item.setMemberId(_tmpMemberId);
            final String _tmpMemberName;
            if (_cursor.isNull(_cursorIndexOfMemberName)) {
              _tmpMemberName = null;
            } else {
              _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
            }
            _item.setMemberName(_tmpMemberName);
            final double _tmpAmount;
            _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
            _item.setAmount(_tmpAmount);
            final double _tmpInterest;
            _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
            _item.setInterest(_tmpInterest);
            final String _tmpReason;
            if (_cursor.isNull(_cursorIndexOfReason)) {
              _tmpReason = null;
            } else {
              _tmpReason = _cursor.getString(_cursorIndexOfReason);
            }
            _item.setReason(_tmpReason);
            final Date _tmpDateRequested;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfDateRequested)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
            }
            _tmpDateRequested = Converters.fromTimestamp(_tmp);
            _item.setDateRequested(_tmpDateRequested);
            final Date _tmpDueDate;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
            }
            _tmpDueDate = Converters.fromTimestamp(_tmp_1);
            _item.setDueDate(_tmpDueDate);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _item.setStatus(_tmpStatus);
            final double _tmpRepaidAmount;
            _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
            _item.setRepaidAmount(_tmpRepaidAmount);
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
  public List<LoanEntity> getAllLoansSync() {
    final String _sql = "SELECT * FROM loans ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
      final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
      final List<LoanEntity> _result = new ArrayList<LoanEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LoanEntity _item;
        _item = new LoanEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpMemberId;
        if (_cursor.isNull(_cursorIndexOfMemberId)) {
          _tmpMemberId = null;
        } else {
          _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
        }
        _item.setMemberId(_tmpMemberId);
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        _item.setMemberName(_tmpMemberName);
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        _item.setAmount(_tmpAmount);
        final double _tmpInterest;
        _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
        _item.setInterest(_tmpInterest);
        final String _tmpReason;
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _tmpReason = null;
        } else {
          _tmpReason = _cursor.getString(_cursorIndexOfReason);
        }
        _item.setReason(_tmpReason);
        final Date _tmpDateRequested;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDateRequested)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
        }
        _tmpDateRequested = Converters.fromTimestamp(_tmp);
        _item.setDateRequested(_tmpDateRequested);
        final Date _tmpDueDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfDueDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
        }
        _tmpDueDate = Converters.fromTimestamp(_tmp_1);
        _item.setDueDate(_tmpDueDate);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final double _tmpRepaidAmount;
        _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
        _item.setRepaidAmount(_tmpRepaidAmount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LoanEntity getLoanByIdSync(final String loanId) {
    final String _sql = "SELECT * FROM loans WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (loanId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, loanId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
      final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
      final LoanEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new LoanEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _result.setId(_tmpId);
        final String _tmpMemberId;
        if (_cursor.isNull(_cursorIndexOfMemberId)) {
          _tmpMemberId = null;
        } else {
          _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
        }
        _result.setMemberId(_tmpMemberId);
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        _result.setMemberName(_tmpMemberName);
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        _result.setAmount(_tmpAmount);
        final double _tmpInterest;
        _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
        _result.setInterest(_tmpInterest);
        final String _tmpReason;
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _tmpReason = null;
        } else {
          _tmpReason = _cursor.getString(_cursorIndexOfReason);
        }
        _result.setReason(_tmpReason);
        final Date _tmpDateRequested;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDateRequested)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
        }
        _tmpDateRequested = Converters.fromTimestamp(_tmp);
        _result.setDateRequested(_tmpDateRequested);
        final Date _tmpDueDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfDueDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
        }
        _tmpDueDate = Converters.fromTimestamp(_tmp_1);
        _result.setDueDate(_tmpDueDate);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.setStatus(_tmpStatus);
        final double _tmpRepaidAmount;
        _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
        _result.setRepaidAmount(_tmpRepaidAmount);
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
  public List<LoanEntity> getPendingLoans() {
    final String _sql = "SELECT * FROM loans WHERE status = 'PENDING' ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
      final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
      final List<LoanEntity> _result = new ArrayList<LoanEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LoanEntity _item;
        _item = new LoanEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpMemberId;
        if (_cursor.isNull(_cursorIndexOfMemberId)) {
          _tmpMemberId = null;
        } else {
          _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
        }
        _item.setMemberId(_tmpMemberId);
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        _item.setMemberName(_tmpMemberName);
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        _item.setAmount(_tmpAmount);
        final double _tmpInterest;
        _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
        _item.setInterest(_tmpInterest);
        final String _tmpReason;
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _tmpReason = null;
        } else {
          _tmpReason = _cursor.getString(_cursorIndexOfReason);
        }
        _item.setReason(_tmpReason);
        final Date _tmpDateRequested;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDateRequested)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
        }
        _tmpDateRequested = Converters.fromTimestamp(_tmp);
        _item.setDateRequested(_tmpDateRequested);
        final Date _tmpDueDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfDueDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
        }
        _tmpDueDate = Converters.fromTimestamp(_tmp_1);
        _item.setDueDate(_tmpDueDate);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final double _tmpRepaidAmount;
        _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
        _item.setRepaidAmount(_tmpRepaidAmount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<LoanEntity> getActiveLoans() {
    final String _sql = "SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
      final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
      final List<LoanEntity> _result = new ArrayList<LoanEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LoanEntity _item;
        _item = new LoanEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpMemberId;
        if (_cursor.isNull(_cursorIndexOfMemberId)) {
          _tmpMemberId = null;
        } else {
          _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
        }
        _item.setMemberId(_tmpMemberId);
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        _item.setMemberName(_tmpMemberName);
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        _item.setAmount(_tmpAmount);
        final double _tmpInterest;
        _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
        _item.setInterest(_tmpInterest);
        final String _tmpReason;
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _tmpReason = null;
        } else {
          _tmpReason = _cursor.getString(_cursorIndexOfReason);
        }
        _item.setReason(_tmpReason);
        final Date _tmpDateRequested;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDateRequested)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
        }
        _tmpDateRequested = Converters.fromTimestamp(_tmp);
        _item.setDateRequested(_tmpDateRequested);
        final Date _tmpDueDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfDueDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
        }
        _tmpDueDate = Converters.fromTimestamp(_tmp_1);
        _item.setDueDate(_tmpDueDate);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _item.setStatus(_tmpStatus);
        final double _tmpRepaidAmount;
        _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
        _item.setRepaidAmount(_tmpRepaidAmount);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LoanEntity getActiveLoanByMemberName(final String memberName) {
    final String _sql = "SELECT * FROM loans WHERE memberName = ? AND status = 'ACTIVE' LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (memberName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberName);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
      final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
      final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
      final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
      final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
      final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
      final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
      final LoanEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new LoanEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _result.setId(_tmpId);
        final String _tmpMemberId;
        if (_cursor.isNull(_cursorIndexOfMemberId)) {
          _tmpMemberId = null;
        } else {
          _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
        }
        _result.setMemberId(_tmpMemberId);
        final String _tmpMemberName;
        if (_cursor.isNull(_cursorIndexOfMemberName)) {
          _tmpMemberName = null;
        } else {
          _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
        }
        _result.setMemberName(_tmpMemberName);
        final double _tmpAmount;
        _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
        _result.setAmount(_tmpAmount);
        final double _tmpInterest;
        _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
        _result.setInterest(_tmpInterest);
        final String _tmpReason;
        if (_cursor.isNull(_cursorIndexOfReason)) {
          _tmpReason = null;
        } else {
          _tmpReason = _cursor.getString(_cursorIndexOfReason);
        }
        _result.setReason(_tmpReason);
        final Date _tmpDateRequested;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfDateRequested)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
        }
        _tmpDateRequested = Converters.fromTimestamp(_tmp);
        _result.setDateRequested(_tmpDateRequested);
        final Date _tmpDueDate;
        final Long _tmp_1;
        if (_cursor.isNull(_cursorIndexOfDueDate)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
        }
        _tmpDueDate = Converters.fromTimestamp(_tmp_1);
        _result.setDueDate(_tmpDueDate);
        final String _tmpStatus;
        if (_cursor.isNull(_cursorIndexOfStatus)) {
          _tmpStatus = null;
        } else {
          _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        }
        _result.setStatus(_tmpStatus);
        final double _tmpRepaidAmount;
        _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
        _result.setRepaidAmount(_tmpRepaidAmount);
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
  public double getTotalOutstanding() {
    final String _sql = "SELECT SUM(amount + interest - repaidAmount) FROM loans WHERE status = 'ACTIVE'";
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
  public double getTotalInterestEarned() {
    final String _sql = "SELECT SUM(interest) FROM loans WHERE status IN ('ACTIVE', 'PAID')";
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
  public LiveData<List<LoanWithApproval>> getMemberLoansWithApproval(final String memberName) {
    final String _sql = "SELECT l.*, (SELECT COUNT(*) FROM approvals a WHERE a.targetId = l.id AND a.type = 'LOAN') as approvalCount, 0 as isApprovedByAdmin FROM loans l WHERE l.memberName = ? ORDER BY l.id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (memberName == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberName);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"approvals",
        "loans"}, false, new Callable<List<LoanWithApproval>>() {
      @Override
      @Nullable
      public List<LoanWithApproval> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
          final int _cursorIndexOfApprovalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "approvalCount");
          final int _cursorIndexOfIsApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "isApprovedByAdmin");
          final List<LoanWithApproval> _result = new ArrayList<LoanWithApproval>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanWithApproval _item;
            final LoanEntity _tmpLoan;
            if (!(_cursor.isNull(_cursorIndexOfId) && _cursor.isNull(_cursorIndexOfMemberId) && _cursor.isNull(_cursorIndexOfMemberName) && _cursor.isNull(_cursorIndexOfAmount) && _cursor.isNull(_cursorIndexOfInterest) && _cursor.isNull(_cursorIndexOfReason) && _cursor.isNull(_cursorIndexOfDateRequested) && _cursor.isNull(_cursorIndexOfDueDate) && _cursor.isNull(_cursorIndexOfStatus) && _cursor.isNull(_cursorIndexOfRepaidAmount))) {
              _tmpLoan = new LoanEntity();
              final String _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getString(_cursorIndexOfId);
              }
              _tmpLoan.setId(_tmpId);
              final String _tmpMemberId;
              if (_cursor.isNull(_cursorIndexOfMemberId)) {
                _tmpMemberId = null;
              } else {
                _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
              }
              _tmpLoan.setMemberId(_tmpMemberId);
              final String _tmpMemberName;
              if (_cursor.isNull(_cursorIndexOfMemberName)) {
                _tmpMemberName = null;
              } else {
                _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
              }
              _tmpLoan.setMemberName(_tmpMemberName);
              final double _tmpAmount;
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
              _tmpLoan.setAmount(_tmpAmount);
              final double _tmpInterest;
              _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
              _tmpLoan.setInterest(_tmpInterest);
              final String _tmpReason;
              if (_cursor.isNull(_cursorIndexOfReason)) {
                _tmpReason = null;
              } else {
                _tmpReason = _cursor.getString(_cursorIndexOfReason);
              }
              _tmpLoan.setReason(_tmpReason);
              final Date _tmpDateRequested;
              final Long _tmp;
              if (_cursor.isNull(_cursorIndexOfDateRequested)) {
                _tmp = null;
              } else {
                _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
              }
              _tmpDateRequested = Converters.fromTimestamp(_tmp);
              _tmpLoan.setDateRequested(_tmpDateRequested);
              final Date _tmpDueDate;
              final Long _tmp_1;
              if (_cursor.isNull(_cursorIndexOfDueDate)) {
                _tmp_1 = null;
              } else {
                _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
              }
              _tmpDueDate = Converters.fromTimestamp(_tmp_1);
              _tmpLoan.setDueDate(_tmpDueDate);
              final String _tmpStatus;
              if (_cursor.isNull(_cursorIndexOfStatus)) {
                _tmpStatus = null;
              } else {
                _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              }
              _tmpLoan.setStatus(_tmpStatus);
              final double _tmpRepaidAmount;
              _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
              _tmpLoan.setRepaidAmount(_tmpRepaidAmount);
            } else {
              _tmpLoan = null;
            }
            _item = new LoanWithApproval();
            _item.approvalCount = _cursor.getInt(_cursorIndexOfApprovalCount);
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsApprovedByAdmin);
            _item.isApprovedByAdmin = _tmp_2 != 0;
            _item.loan = _tmpLoan;
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
  public LiveData<List<LoanWithApproval>> getPendingLoansWithApproval(final String adminEmail) {
    final String _sql = "SELECT l.*, (SELECT COUNT(*) FROM approvals a WHERE a.targetId = l.id AND a.type = 'LOAN') as approvalCount, (SELECT COUNT(*) > 0 FROM approvals a WHERE a.targetId = l.id AND a.type = 'LOAN' AND a.adminEmail = ?) as isApprovedByAdmin FROM loans l WHERE l.status = 'PENDING' ORDER BY l.id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (adminEmail == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, adminEmail);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"approvals",
        "loans"}, false, new Callable<List<LoanWithApproval>>() {
      @Override
      @Nullable
      public List<LoanWithApproval> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMemberId = CursorUtil.getColumnIndexOrThrow(_cursor, "memberId");
          final int _cursorIndexOfMemberName = CursorUtil.getColumnIndexOrThrow(_cursor, "memberName");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfInterest = CursorUtil.getColumnIndexOrThrow(_cursor, "interest");
          final int _cursorIndexOfReason = CursorUtil.getColumnIndexOrThrow(_cursor, "reason");
          final int _cursorIndexOfDateRequested = CursorUtil.getColumnIndexOrThrow(_cursor, "dateRequested");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRepaidAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "repaidAmount");
          final int _cursorIndexOfApprovalCount = CursorUtil.getColumnIndexOrThrow(_cursor, "approvalCount");
          final int _cursorIndexOfIsApprovedByAdmin = CursorUtil.getColumnIndexOrThrow(_cursor, "isApprovedByAdmin");
          final List<LoanWithApproval> _result = new ArrayList<LoanWithApproval>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LoanWithApproval _item;
            final LoanEntity _tmpLoan;
            if (!(_cursor.isNull(_cursorIndexOfId) && _cursor.isNull(_cursorIndexOfMemberId) && _cursor.isNull(_cursorIndexOfMemberName) && _cursor.isNull(_cursorIndexOfAmount) && _cursor.isNull(_cursorIndexOfInterest) && _cursor.isNull(_cursorIndexOfReason) && _cursor.isNull(_cursorIndexOfDateRequested) && _cursor.isNull(_cursorIndexOfDueDate) && _cursor.isNull(_cursorIndexOfStatus) && _cursor.isNull(_cursorIndexOfRepaidAmount))) {
              _tmpLoan = new LoanEntity();
              final String _tmpId;
              if (_cursor.isNull(_cursorIndexOfId)) {
                _tmpId = null;
              } else {
                _tmpId = _cursor.getString(_cursorIndexOfId);
              }
              _tmpLoan.setId(_tmpId);
              final String _tmpMemberId;
              if (_cursor.isNull(_cursorIndexOfMemberId)) {
                _tmpMemberId = null;
              } else {
                _tmpMemberId = _cursor.getString(_cursorIndexOfMemberId);
              }
              _tmpLoan.setMemberId(_tmpMemberId);
              final String _tmpMemberName;
              if (_cursor.isNull(_cursorIndexOfMemberName)) {
                _tmpMemberName = null;
              } else {
                _tmpMemberName = _cursor.getString(_cursorIndexOfMemberName);
              }
              _tmpLoan.setMemberName(_tmpMemberName);
              final double _tmpAmount;
              _tmpAmount = _cursor.getDouble(_cursorIndexOfAmount);
              _tmpLoan.setAmount(_tmpAmount);
              final double _tmpInterest;
              _tmpInterest = _cursor.getDouble(_cursorIndexOfInterest);
              _tmpLoan.setInterest(_tmpInterest);
              final String _tmpReason;
              if (_cursor.isNull(_cursorIndexOfReason)) {
                _tmpReason = null;
              } else {
                _tmpReason = _cursor.getString(_cursorIndexOfReason);
              }
              _tmpLoan.setReason(_tmpReason);
              final Date _tmpDateRequested;
              final Long _tmp;
              if (_cursor.isNull(_cursorIndexOfDateRequested)) {
                _tmp = null;
              } else {
                _tmp = _cursor.getLong(_cursorIndexOfDateRequested);
              }
              _tmpDateRequested = Converters.fromTimestamp(_tmp);
              _tmpLoan.setDateRequested(_tmpDateRequested);
              final Date _tmpDueDate;
              final Long _tmp_1;
              if (_cursor.isNull(_cursorIndexOfDueDate)) {
                _tmp_1 = null;
              } else {
                _tmp_1 = _cursor.getLong(_cursorIndexOfDueDate);
              }
              _tmpDueDate = Converters.fromTimestamp(_tmp_1);
              _tmpLoan.setDueDate(_tmpDueDate);
              final String _tmpStatus;
              if (_cursor.isNull(_cursorIndexOfStatus)) {
                _tmpStatus = null;
              } else {
                _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
              }
              _tmpLoan.setStatus(_tmpStatus);
              final double _tmpRepaidAmount;
              _tmpRepaidAmount = _cursor.getDouble(_cursorIndexOfRepaidAmount);
              _tmpLoan.setRepaidAmount(_tmpRepaidAmount);
            } else {
              _tmpLoan = null;
            }
            _item = new LoanWithApproval();
            _item.approvalCount = _cursor.getInt(_cursorIndexOfApprovalCount);
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsApprovedByAdmin);
            _item.isApprovedByAdmin = _tmp_2 != 0;
            _item.loan = _tmpLoan;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
