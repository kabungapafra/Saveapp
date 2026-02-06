package com.example.save.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.save.data.local.Converters;
import com.example.save.data.local.entities.ApprovalEntity;
import java.lang.Class;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ApprovalDao_Impl implements ApprovalDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ApprovalEntity> __insertionAdapterOfApprovalEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteApprovalsForTarget;

  public ApprovalDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfApprovalEntity = new EntityInsertionAdapter<ApprovalEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `approvals` (`id`,`type`,`targetId`,`adminEmail`,`approvalDate`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ApprovalEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getType() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getType());
        }
        if (entity.getTargetId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTargetId());
        }
        if (entity.getAdminEmail() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAdminEmail());
        }
        final Long _tmp = Converters.dateToTimestamp(entity.getApprovalDate());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, _tmp);
        }
      }
    };
    this.__preparedStmtOfDeleteApprovalsForTarget = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM approvals WHERE type = ? AND targetId = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final ApprovalEntity approval) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfApprovalEntity.insert(approval);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteApprovalsForTarget(final String type, final String targetId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteApprovalsForTarget.acquire();
    int _argIndex = 1;
    if (type == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, type);
    }
    _argIndex = 2;
    if (targetId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, targetId);
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
      __preparedStmtOfDeleteApprovalsForTarget.release(_stmt);
    }
  }

  @Override
  public List<ApprovalEntity> getApprovalsForTarget(final String type, final String targetId) {
    final String _sql = "SELECT * FROM approvals WHERE type = ? AND targetId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    _argIndex = 2;
    if (targetId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, targetId);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
      final int _cursorIndexOfAdminEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "adminEmail");
      final int _cursorIndexOfApprovalDate = CursorUtil.getColumnIndexOrThrow(_cursor, "approvalDate");
      final List<ApprovalEntity> _result = new ArrayList<ApprovalEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ApprovalEntity _item;
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final String _tmpTargetId;
        if (_cursor.isNull(_cursorIndexOfTargetId)) {
          _tmpTargetId = null;
        } else {
          _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
        }
        final String _tmpAdminEmail;
        if (_cursor.isNull(_cursorIndexOfAdminEmail)) {
          _tmpAdminEmail = null;
        } else {
          _tmpAdminEmail = _cursor.getString(_cursorIndexOfAdminEmail);
        }
        final Date _tmpApprovalDate;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfApprovalDate)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfApprovalDate);
        }
        _tmpApprovalDate = Converters.fromTimestamp(_tmp);
        _item = new ApprovalEntity(_tmpType,_tmpTargetId,_tmpAdminEmail,_tmpApprovalDate);
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
  public int getApprovalCount(final String type, final String targetId) {
    final String _sql = "SELECT COUNT(*) FROM approvals WHERE type = ? AND targetId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    _argIndex = 2;
    if (targetId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, targetId);
    }
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

  @Override
  public ApprovalEntity getAdminApproval(final String type, final String targetId,
      final String adminEmail) {
    final String _sql = "SELECT * FROM approvals WHERE type = ? AND targetId = ? AND adminEmail = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    _argIndex = 2;
    if (targetId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, targetId);
    }
    _argIndex = 3;
    if (adminEmail == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, adminEmail);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
      final int _cursorIndexOfTargetId = CursorUtil.getColumnIndexOrThrow(_cursor, "targetId");
      final int _cursorIndexOfAdminEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "adminEmail");
      final int _cursorIndexOfApprovalDate = CursorUtil.getColumnIndexOrThrow(_cursor, "approvalDate");
      final ApprovalEntity _result;
      if (_cursor.moveToFirst()) {
        final String _tmpType;
        if (_cursor.isNull(_cursorIndexOfType)) {
          _tmpType = null;
        } else {
          _tmpType = _cursor.getString(_cursorIndexOfType);
        }
        final String _tmpTargetId;
        if (_cursor.isNull(_cursorIndexOfTargetId)) {
          _tmpTargetId = null;
        } else {
          _tmpTargetId = _cursor.getString(_cursorIndexOfTargetId);
        }
        final String _tmpAdminEmail;
        if (_cursor.isNull(_cursorIndexOfAdminEmail)) {
          _tmpAdminEmail = null;
        } else {
          _tmpAdminEmail = _cursor.getString(_cursorIndexOfAdminEmail);
        }
        final Date _tmpApprovalDate;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfApprovalDate)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfApprovalDate);
        }
        _tmpApprovalDate = Converters.fromTimestamp(_tmp);
        _result = new ApprovalEntity(_tmpType,_tmpTargetId,_tmpAdminEmail,_tmpApprovalDate);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
