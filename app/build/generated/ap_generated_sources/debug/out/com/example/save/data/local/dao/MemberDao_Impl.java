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
import com.example.save.data.local.entities.MemberEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MemberDao_Impl implements MemberDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MemberEntity> __insertionAdapterOfMemberEntity;

  private final EntityDeletionOrUpdateAdapter<MemberEntity> __deletionAdapterOfMemberEntity;

  private final EntityDeletionOrUpdateAdapter<MemberEntity> __updateAdapterOfMemberEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  private final SharedSQLiteStatement __preparedStmtOfResetAllContributions;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAllDueDates;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMemberById;

  public MemberDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMemberEntity = new EntityInsertionAdapter<MemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `members` (`id`,`name`,`role`,`isActive`,`phone`,`email`,`password`,`payoutDate`,`payoutAmount`,`hasReceivedPayout`,`shortfallAmount`,`isFirstLogin`,`contributionTarget`,`contributionPaid`,`paymentStreak`,`nextPayoutDate`,`nextPaymentDueDate`,`isAutoPayEnabled`,`autoPayDay`,`autoPayAmount`,`creditScore`,`joinedDate`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MemberEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getRole() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRole());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhone());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getEmail());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPassword());
        }
        if (entity.getPayoutDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPayoutDate());
        }
        if (entity.getPayoutAmount() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getPayoutAmount());
        }
        final int _tmp_1 = entity.isHasReceivedPayout() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindDouble(11, entity.getShortfallAmount());
        final int _tmp_2 = entity.isFirstLogin() ? 1 : 0;
        statement.bindLong(12, _tmp_2);
        statement.bindDouble(13, entity.getContributionTarget());
        statement.bindDouble(14, entity.getContributionPaid());
        statement.bindLong(15, entity.getPaymentStreak());
        if (entity.getNextPayoutDate() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getNextPayoutDate());
        }
        if (entity.getNextPaymentDueDate() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getNextPaymentDueDate());
        }
        final int _tmp_3 = entity.isAutoPayEnabled() ? 1 : 0;
        statement.bindLong(18, _tmp_3);
        statement.bindLong(19, entity.getAutoPayDay());
        statement.bindDouble(20, entity.getAutoPayAmount());
        statement.bindLong(21, entity.getCreditScore());
        if (entity.getJoinedDate() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getJoinedDate());
        }
      }
    };
    this.__deletionAdapterOfMemberEntity = new EntityDeletionOrUpdateAdapter<MemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `members` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MemberEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
      }
    };
    this.__updateAdapterOfMemberEntity = new EntityDeletionOrUpdateAdapter<MemberEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `members` SET `id` = ?,`name` = ?,`role` = ?,`isActive` = ?,`phone` = ?,`email` = ?,`password` = ?,`payoutDate` = ?,`payoutAmount` = ?,`hasReceivedPayout` = ?,`shortfallAmount` = ?,`isFirstLogin` = ?,`contributionTarget` = ?,`contributionPaid` = ?,`paymentStreak` = ?,`nextPayoutDate` = ?,`nextPaymentDueDate` = ?,`isAutoPayEnabled` = ?,`autoPayDay` = ?,`autoPayAmount` = ?,`creditScore` = ?,`joinedDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final MemberEntity entity) {
        if (entity.getId() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getId());
        }
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getRole() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRole());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(4, _tmp);
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getPhone());
        }
        if (entity.getEmail() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getEmail());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPassword());
        }
        if (entity.getPayoutDate() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getPayoutDate());
        }
        if (entity.getPayoutAmount() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getPayoutAmount());
        }
        final int _tmp_1 = entity.isHasReceivedPayout() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindDouble(11, entity.getShortfallAmount());
        final int _tmp_2 = entity.isFirstLogin() ? 1 : 0;
        statement.bindLong(12, _tmp_2);
        statement.bindDouble(13, entity.getContributionTarget());
        statement.bindDouble(14, entity.getContributionPaid());
        statement.bindLong(15, entity.getPaymentStreak());
        if (entity.getNextPayoutDate() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getNextPayoutDate());
        }
        if (entity.getNextPaymentDueDate() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getNextPaymentDueDate());
        }
        final int _tmp_3 = entity.isAutoPayEnabled() ? 1 : 0;
        statement.bindLong(18, _tmp_3);
        statement.bindLong(19, entity.getAutoPayDay());
        statement.bindDouble(20, entity.getAutoPayAmount());
        statement.bindLong(21, entity.getCreditScore());
        if (entity.getJoinedDate() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getJoinedDate());
        }
        if (entity.getId() == null) {
          statement.bindNull(23);
        } else {
          statement.bindString(23, entity.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM members";
        return _query;
      }
    };
    this.__preparedStmtOfResetAllContributions = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE members SET contributionPaid = 0";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateAllDueDates = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE members SET nextPaymentDueDate = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMemberById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM members WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public void insert(final MemberEntity member) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfMemberEntity.insert(member);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void insertAll(final List<MemberEntity> members) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfMemberEntity.insert(members);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final MemberEntity member) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfMemberEntity.handle(member);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final MemberEntity member) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfMemberEntity.handle(member);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
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
  public void resetAllContributions() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfResetAllContributions.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfResetAllContributions.release(_stmt);
    }
  }

  @Override
  public void updateAllDueDates(final String nextDate) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAllDueDates.acquire();
    int _argIndex = 1;
    if (nextDate == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, nextDate);
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
      __preparedStmtOfUpdateAllDueDates.release(_stmt);
    }
  }

  @Override
  public void deleteMemberById(final String memberId) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMemberById.acquire();
    int _argIndex = 1;
    if (memberId == null) {
      _stmt.bindNull(_argIndex);
    } else {
      _stmt.bindString(_argIndex, memberId);
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
      __preparedStmtOfDeleteMemberById.release(_stmt);
    }
  }

  @Override
  public LiveData<List<MemberEntity>> getAllMembers() {
    final String _sql = "SELECT * FROM members ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"members"}, false, new Callable<List<MemberEntity>>() {
      @Override
      @Nullable
      public List<MemberEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
          final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
          final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
          final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
          final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
          final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
          final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
          final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
          final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
          final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
          final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
          final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
          final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
          final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
          final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
          final List<MemberEntity> _result = new ArrayList<MemberEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MemberEntity _item;
            _item = new MemberEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _item.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item.setActive(_tmpIsActive);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            _item.setPhone(_tmpPhone);
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            _item.setEmail(_tmpEmail);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _item.setPassword(_tmpPassword);
            final String _tmpPayoutDate;
            if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
              _tmpPayoutDate = null;
            } else {
              _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
            }
            _item.setPayoutDate(_tmpPayoutDate);
            final String _tmpPayoutAmount;
            if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
              _tmpPayoutAmount = null;
            } else {
              _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
            }
            _item.setPayoutAmount(_tmpPayoutAmount);
            final boolean _tmpHasReceivedPayout;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
            _tmpHasReceivedPayout = _tmp_1 != 0;
            _item.setHasReceivedPayout(_tmpHasReceivedPayout);
            final double _tmpShortfallAmount;
            _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
            _item.setShortfallAmount(_tmpShortfallAmount);
            final boolean _tmpIsFirstLogin;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
            _tmpIsFirstLogin = _tmp_2 != 0;
            _item.setFirstLogin(_tmpIsFirstLogin);
            final double _tmpContributionTarget;
            _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
            _item.setContributionTarget(_tmpContributionTarget);
            final double _tmpContributionPaid;
            _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
            _item.setContributionPaid(_tmpContributionPaid);
            final int _tmpPaymentStreak;
            _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
            _item.setPaymentStreak(_tmpPaymentStreak);
            final String _tmpNextPayoutDate;
            if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
              _tmpNextPayoutDate = null;
            } else {
              _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
            }
            _item.setNextPayoutDate(_tmpNextPayoutDate);
            final String _tmpNextPaymentDueDate;
            if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
              _tmpNextPaymentDueDate = null;
            } else {
              _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
            }
            _item.setNextPaymentDueDate(_tmpNextPaymentDueDate);
            final boolean _tmpIsAutoPayEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
            _tmpIsAutoPayEnabled = _tmp_3 != 0;
            _item.setAutoPayEnabled(_tmpIsAutoPayEnabled);
            final int _tmpAutoPayDay;
            _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
            _item.setAutoPayDay(_tmpAutoPayDay);
            final double _tmpAutoPayAmount;
            _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
            _item.setAutoPayAmount(_tmpAutoPayAmount);
            final int _tmpCreditScore;
            _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
            _item.setCreditScore(_tmpCreditScore);
            final String _tmpJoinedDate;
            if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
              _tmpJoinedDate = null;
            } else {
              _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
            }
            _item.setJoinedDate(_tmpJoinedDate);
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
  public List<MemberEntity> getAllMembersSync() {
    final String _sql = "SELECT * FROM members ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final List<MemberEntity> _result = new ArrayList<MemberEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MemberEntity _item;
        _item = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _item.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _item.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _item.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _item.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _item.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _item.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _item.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _item.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _item.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _item.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _item.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _item.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _item.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _item.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _item.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _item.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _item.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _item.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _item.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _item.setJoinedDate(_tmpJoinedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<MemberEntity> getMemberById(final String memberId) {
    final String _sql = "SELECT * FROM members WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (memberId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberId);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"members"}, false, new Callable<MemberEntity>() {
      @Override
      @Nullable
      public MemberEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
          final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
          final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
          final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
          final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
          final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
          final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
          final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
          final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
          final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
          final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
          final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
          final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
          final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
          final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
          final MemberEntity _result;
          if (_cursor.moveToFirst()) {
            _result = new MemberEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _result.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _result.setName(_tmpName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _result.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _result.setActive(_tmpIsActive);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            _result.setPhone(_tmpPhone);
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            _result.setEmail(_tmpEmail);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _result.setPassword(_tmpPassword);
            final String _tmpPayoutDate;
            if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
              _tmpPayoutDate = null;
            } else {
              _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
            }
            _result.setPayoutDate(_tmpPayoutDate);
            final String _tmpPayoutAmount;
            if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
              _tmpPayoutAmount = null;
            } else {
              _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
            }
            _result.setPayoutAmount(_tmpPayoutAmount);
            final boolean _tmpHasReceivedPayout;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
            _tmpHasReceivedPayout = _tmp_1 != 0;
            _result.setHasReceivedPayout(_tmpHasReceivedPayout);
            final double _tmpShortfallAmount;
            _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
            _result.setShortfallAmount(_tmpShortfallAmount);
            final boolean _tmpIsFirstLogin;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
            _tmpIsFirstLogin = _tmp_2 != 0;
            _result.setFirstLogin(_tmpIsFirstLogin);
            final double _tmpContributionTarget;
            _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
            _result.setContributionTarget(_tmpContributionTarget);
            final double _tmpContributionPaid;
            _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
            _result.setContributionPaid(_tmpContributionPaid);
            final int _tmpPaymentStreak;
            _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
            _result.setPaymentStreak(_tmpPaymentStreak);
            final String _tmpNextPayoutDate;
            if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
              _tmpNextPayoutDate = null;
            } else {
              _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
            }
            _result.setNextPayoutDate(_tmpNextPayoutDate);
            final String _tmpNextPaymentDueDate;
            if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
              _tmpNextPaymentDueDate = null;
            } else {
              _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
            }
            _result.setNextPaymentDueDate(_tmpNextPaymentDueDate);
            final boolean _tmpIsAutoPayEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
            _tmpIsAutoPayEnabled = _tmp_3 != 0;
            _result.setAutoPayEnabled(_tmpIsAutoPayEnabled);
            final int _tmpAutoPayDay;
            _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
            _result.setAutoPayDay(_tmpAutoPayDay);
            final double _tmpAutoPayAmount;
            _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
            _result.setAutoPayAmount(_tmpAutoPayAmount);
            final int _tmpCreditScore;
            _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
            _result.setCreditScore(_tmpCreditScore);
            final String _tmpJoinedDate;
            if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
              _tmpJoinedDate = null;
            } else {
              _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
            }
            _result.setJoinedDate(_tmpJoinedDate);
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
  public MemberEntity getMemberByName(final String name) {
    final String _sql = "SELECT * FROM members WHERE name = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (name == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, name);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final MemberEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _result.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _result.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _result.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _result.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _result.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _result.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _result.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _result.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _result.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _result.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _result.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _result.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _result.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _result.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _result.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _result.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _result.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _result.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _result.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _result.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _result.setJoinedDate(_tmpJoinedDate);
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
  public MemberEntity getMemberByEmail(final String email) {
    final String _sql = "SELECT * FROM members WHERE email = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (email == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, email);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final MemberEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _result.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _result.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _result.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _result.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _result.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _result.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _result.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _result.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _result.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _result.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _result.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _result.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _result.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _result.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _result.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _result.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _result.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _result.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _result.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _result.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _result.setJoinedDate(_tmpJoinedDate);
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
  public MemberEntity getMemberByPhone(final String phone) {
    final String _sql = "SELECT * FROM members WHERE phone = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (phone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, phone);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final MemberEntity _result;
      if (_cursor.moveToFirst()) {
        _result = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _result.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _result.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _result.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _result.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _result.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _result.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _result.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _result.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _result.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _result.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _result.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _result.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _result.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _result.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _result.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _result.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _result.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _result.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _result.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _result.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _result.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _result.setJoinedDate(_tmpJoinedDate);
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
  public LiveData<List<MemberEntity>> searchMembers(final String query) {
    final String _sql = "SELECT * FROM members WHERE name LIKE '%' || ? || '%' OR role LIKE '%' || ? || '%' ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    return __db.getInvalidationTracker().createLiveData(new String[] {"members"}, false, new Callable<List<MemberEntity>>() {
      @Override
      @Nullable
      public List<MemberEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
          final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
          final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
          final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
          final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
          final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
          final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
          final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
          final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
          final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
          final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
          final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
          final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
          final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
          final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
          final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
          final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
          final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
          final List<MemberEntity> _result = new ArrayList<MemberEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MemberEntity _item;
            _item = new MemberEntity();
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            _item.setId(_tmpId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            _item.setName(_tmpName);
            final String _tmpRole;
            if (_cursor.isNull(_cursorIndexOfRole)) {
              _tmpRole = null;
            } else {
              _tmpRole = _cursor.getString(_cursorIndexOfRole);
            }
            _item.setRole(_tmpRole);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item.setActive(_tmpIsActive);
            final String _tmpPhone;
            if (_cursor.isNull(_cursorIndexOfPhone)) {
              _tmpPhone = null;
            } else {
              _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
            }
            _item.setPhone(_tmpPhone);
            final String _tmpEmail;
            if (_cursor.isNull(_cursorIndexOfEmail)) {
              _tmpEmail = null;
            } else {
              _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
            }
            _item.setEmail(_tmpEmail);
            final String _tmpPassword;
            if (_cursor.isNull(_cursorIndexOfPassword)) {
              _tmpPassword = null;
            } else {
              _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
            }
            _item.setPassword(_tmpPassword);
            final String _tmpPayoutDate;
            if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
              _tmpPayoutDate = null;
            } else {
              _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
            }
            _item.setPayoutDate(_tmpPayoutDate);
            final String _tmpPayoutAmount;
            if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
              _tmpPayoutAmount = null;
            } else {
              _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
            }
            _item.setPayoutAmount(_tmpPayoutAmount);
            final boolean _tmpHasReceivedPayout;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
            _tmpHasReceivedPayout = _tmp_1 != 0;
            _item.setHasReceivedPayout(_tmpHasReceivedPayout);
            final double _tmpShortfallAmount;
            _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
            _item.setShortfallAmount(_tmpShortfallAmount);
            final boolean _tmpIsFirstLogin;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
            _tmpIsFirstLogin = _tmp_2 != 0;
            _item.setFirstLogin(_tmpIsFirstLogin);
            final double _tmpContributionTarget;
            _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
            _item.setContributionTarget(_tmpContributionTarget);
            final double _tmpContributionPaid;
            _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
            _item.setContributionPaid(_tmpContributionPaid);
            final int _tmpPaymentStreak;
            _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
            _item.setPaymentStreak(_tmpPaymentStreak);
            final String _tmpNextPayoutDate;
            if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
              _tmpNextPayoutDate = null;
            } else {
              _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
            }
            _item.setNextPayoutDate(_tmpNextPayoutDate);
            final String _tmpNextPaymentDueDate;
            if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
              _tmpNextPaymentDueDate = null;
            } else {
              _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
            }
            _item.setNextPaymentDueDate(_tmpNextPaymentDueDate);
            final boolean _tmpIsAutoPayEnabled;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
            _tmpIsAutoPayEnabled = _tmp_3 != 0;
            _item.setAutoPayEnabled(_tmpIsAutoPayEnabled);
            final int _tmpAutoPayDay;
            _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
            _item.setAutoPayDay(_tmpAutoPayDay);
            final double _tmpAutoPayAmount;
            _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
            _item.setAutoPayAmount(_tmpAutoPayAmount);
            final int _tmpCreditScore;
            _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
            _item.setCreditScore(_tmpCreditScore);
            final String _tmpJoinedDate;
            if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
              _tmpJoinedDate = null;
            } else {
              _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
            }
            _item.setJoinedDate(_tmpJoinedDate);
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
  public List<MemberEntity> getAdmins() {
    final String _sql = "SELECT * FROM members WHERE LOWER(role) IN ('administrator', 'admin') ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final List<MemberEntity> _result = new ArrayList<MemberEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MemberEntity _item;
        _item = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _item.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _item.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _item.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _item.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _item.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _item.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _item.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _item.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _item.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _item.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _item.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _item.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _item.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _item.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _item.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _item.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _item.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _item.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _item.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _item.setJoinedDate(_tmpJoinedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getAdminCount() {
    final String _sql = "SELECT COUNT(*) FROM members WHERE LOWER(role) IN ('administrator', 'admin')";
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

  @Override
  public LiveData<Integer> getAdminCountLive() {
    final String _sql = "SELECT COUNT(*) FROM members WHERE LOWER(role) IN ('administrator', 'admin')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"members"}, false, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
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
  public List<MemberEntity> getActiveMembers() {
    final String _sql = "SELECT * FROM members WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final List<MemberEntity> _result = new ArrayList<MemberEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MemberEntity _item;
        _item = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _item.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _item.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _item.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _item.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _item.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _item.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _item.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _item.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _item.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _item.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _item.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _item.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _item.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _item.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _item.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _item.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _item.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _item.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _item.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _item.setJoinedDate(_tmpJoinedDate);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getMemberCount() {
    final String _sql = "SELECT COUNT(*) FROM members";
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

  @Override
  public int getActiveMemberCount() {
    final String _sql = "SELECT COUNT(*) FROM members WHERE isActive = 1";
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

  @Override
  public List<MemberEntity> getMembersWithShortfalls() {
    final String _sql = "SELECT * FROM members WHERE shortfallAmount > 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
      final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
      final int _cursorIndexOfPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "phone");
      final int _cursorIndexOfEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "email");
      final int _cursorIndexOfPassword = CursorUtil.getColumnIndexOrThrow(_cursor, "password");
      final int _cursorIndexOfPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutDate");
      final int _cursorIndexOfPayoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "payoutAmount");
      final int _cursorIndexOfHasReceivedPayout = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReceivedPayout");
      final int _cursorIndexOfShortfallAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "shortfallAmount");
      final int _cursorIndexOfIsFirstLogin = CursorUtil.getColumnIndexOrThrow(_cursor, "isFirstLogin");
      final int _cursorIndexOfContributionTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionTarget");
      final int _cursorIndexOfContributionPaid = CursorUtil.getColumnIndexOrThrow(_cursor, "contributionPaid");
      final int _cursorIndexOfPaymentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentStreak");
      final int _cursorIndexOfNextPayoutDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPayoutDate");
      final int _cursorIndexOfNextPaymentDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "nextPaymentDueDate");
      final int _cursorIndexOfIsAutoPayEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isAutoPayEnabled");
      final int _cursorIndexOfAutoPayDay = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayDay");
      final int _cursorIndexOfAutoPayAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "autoPayAmount");
      final int _cursorIndexOfCreditScore = CursorUtil.getColumnIndexOrThrow(_cursor, "creditScore");
      final int _cursorIndexOfJoinedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "joinedDate");
      final List<MemberEntity> _result = new ArrayList<MemberEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final MemberEntity _item;
        _item = new MemberEntity();
        final String _tmpId;
        if (_cursor.isNull(_cursorIndexOfId)) {
          _tmpId = null;
        } else {
          _tmpId = _cursor.getString(_cursorIndexOfId);
        }
        _item.setId(_tmpId);
        final String _tmpName;
        if (_cursor.isNull(_cursorIndexOfName)) {
          _tmpName = null;
        } else {
          _tmpName = _cursor.getString(_cursorIndexOfName);
        }
        _item.setName(_tmpName);
        final String _tmpRole;
        if (_cursor.isNull(_cursorIndexOfRole)) {
          _tmpRole = null;
        } else {
          _tmpRole = _cursor.getString(_cursorIndexOfRole);
        }
        _item.setRole(_tmpRole);
        final boolean _tmpIsActive;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsActive);
        _tmpIsActive = _tmp != 0;
        _item.setActive(_tmpIsActive);
        final String _tmpPhone;
        if (_cursor.isNull(_cursorIndexOfPhone)) {
          _tmpPhone = null;
        } else {
          _tmpPhone = _cursor.getString(_cursorIndexOfPhone);
        }
        _item.setPhone(_tmpPhone);
        final String _tmpEmail;
        if (_cursor.isNull(_cursorIndexOfEmail)) {
          _tmpEmail = null;
        } else {
          _tmpEmail = _cursor.getString(_cursorIndexOfEmail);
        }
        _item.setEmail(_tmpEmail);
        final String _tmpPassword;
        if (_cursor.isNull(_cursorIndexOfPassword)) {
          _tmpPassword = null;
        } else {
          _tmpPassword = _cursor.getString(_cursorIndexOfPassword);
        }
        _item.setPassword(_tmpPassword);
        final String _tmpPayoutDate;
        if (_cursor.isNull(_cursorIndexOfPayoutDate)) {
          _tmpPayoutDate = null;
        } else {
          _tmpPayoutDate = _cursor.getString(_cursorIndexOfPayoutDate);
        }
        _item.setPayoutDate(_tmpPayoutDate);
        final String _tmpPayoutAmount;
        if (_cursor.isNull(_cursorIndexOfPayoutAmount)) {
          _tmpPayoutAmount = null;
        } else {
          _tmpPayoutAmount = _cursor.getString(_cursorIndexOfPayoutAmount);
        }
        _item.setPayoutAmount(_tmpPayoutAmount);
        final boolean _tmpHasReceivedPayout;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfHasReceivedPayout);
        _tmpHasReceivedPayout = _tmp_1 != 0;
        _item.setHasReceivedPayout(_tmpHasReceivedPayout);
        final double _tmpShortfallAmount;
        _tmpShortfallAmount = _cursor.getDouble(_cursorIndexOfShortfallAmount);
        _item.setShortfallAmount(_tmpShortfallAmount);
        final boolean _tmpIsFirstLogin;
        final int _tmp_2;
        _tmp_2 = _cursor.getInt(_cursorIndexOfIsFirstLogin);
        _tmpIsFirstLogin = _tmp_2 != 0;
        _item.setFirstLogin(_tmpIsFirstLogin);
        final double _tmpContributionTarget;
        _tmpContributionTarget = _cursor.getDouble(_cursorIndexOfContributionTarget);
        _item.setContributionTarget(_tmpContributionTarget);
        final double _tmpContributionPaid;
        _tmpContributionPaid = _cursor.getDouble(_cursorIndexOfContributionPaid);
        _item.setContributionPaid(_tmpContributionPaid);
        final int _tmpPaymentStreak;
        _tmpPaymentStreak = _cursor.getInt(_cursorIndexOfPaymentStreak);
        _item.setPaymentStreak(_tmpPaymentStreak);
        final String _tmpNextPayoutDate;
        if (_cursor.isNull(_cursorIndexOfNextPayoutDate)) {
          _tmpNextPayoutDate = null;
        } else {
          _tmpNextPayoutDate = _cursor.getString(_cursorIndexOfNextPayoutDate);
        }
        _item.setNextPayoutDate(_tmpNextPayoutDate);
        final String _tmpNextPaymentDueDate;
        if (_cursor.isNull(_cursorIndexOfNextPaymentDueDate)) {
          _tmpNextPaymentDueDate = null;
        } else {
          _tmpNextPaymentDueDate = _cursor.getString(_cursorIndexOfNextPaymentDueDate);
        }
        _item.setNextPaymentDueDate(_tmpNextPaymentDueDate);
        final boolean _tmpIsAutoPayEnabled;
        final int _tmp_3;
        _tmp_3 = _cursor.getInt(_cursorIndexOfIsAutoPayEnabled);
        _tmpIsAutoPayEnabled = _tmp_3 != 0;
        _item.setAutoPayEnabled(_tmpIsAutoPayEnabled);
        final int _tmpAutoPayDay;
        _tmpAutoPayDay = _cursor.getInt(_cursorIndexOfAutoPayDay);
        _item.setAutoPayDay(_tmpAutoPayDay);
        final double _tmpAutoPayAmount;
        _tmpAutoPayAmount = _cursor.getDouble(_cursorIndexOfAutoPayAmount);
        _item.setAutoPayAmount(_tmpAutoPayAmount);
        final int _tmpCreditScore;
        _tmpCreditScore = _cursor.getInt(_cursorIndexOfCreditScore);
        _item.setCreditScore(_tmpCreditScore);
        final String _tmpJoinedDate;
        if (_cursor.isNull(_cursorIndexOfJoinedDate)) {
          _tmpJoinedDate = null;
        } else {
          _tmpJoinedDate = _cursor.getString(_cursorIndexOfJoinedDate);
        }
        _item.setJoinedDate(_tmpJoinedDate);
        _result.add(_item);
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
