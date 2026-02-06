package com.example.save.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.example.save.data.local.dao.ApprovalDao;
import com.example.save.data.local.dao.ApprovalDao_Impl;
import com.example.save.data.local.dao.LoanDao;
import com.example.save.data.local.dao.LoanDao_Impl;
import com.example.save.data.local.dao.MemberDao;
import com.example.save.data.local.dao.MemberDao_Impl;
import com.example.save.data.local.dao.NotificationDao;
import com.example.save.data.local.dao.NotificationDao_Impl;
import com.example.save.data.local.dao.TransactionDao;
import com.example.save.data.local.dao.TransactionDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MemberDao _memberDao;

  private volatile LoanDao _loanDao;

  private volatile TransactionDao _transactionDao;

  private volatile NotificationDao _notificationDao;

  private volatile ApprovalDao _approvalDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(15) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `members` (`id` TEXT NOT NULL, `name` TEXT, `role` TEXT, `isActive` INTEGER NOT NULL, `phone` TEXT, `email` TEXT, `password` TEXT, `payoutDate` TEXT, `payoutAmount` TEXT, `hasReceivedPayout` INTEGER NOT NULL, `shortfallAmount` REAL NOT NULL, `isFirstLogin` INTEGER NOT NULL, `contributionTarget` REAL NOT NULL, `contributionPaid` REAL NOT NULL, `paymentStreak` INTEGER NOT NULL, `nextPayoutDate` TEXT, `nextPaymentDueDate` TEXT, `isAutoPayEnabled` INTEGER NOT NULL, `autoPayDay` INTEGER NOT NULL, `autoPayAmount` REAL NOT NULL, `creditScore` INTEGER NOT NULL, `joinedDate` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_members_name` ON `members` (`name`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_members_email` ON `members` (`email`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `loans` (`id` TEXT NOT NULL, `memberId` TEXT, `memberName` TEXT, `amount` REAL NOT NULL, `interest` REAL NOT NULL, `reason` TEXT, `dateRequested` INTEGER, `dueDate` INTEGER, `status` TEXT, `repaidAmount` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` TEXT NOT NULL, `memberName` TEXT, `type` TEXT, `amount` REAL NOT NULL, `description` TEXT, `date` INTEGER, `isPositive` INTEGER NOT NULL, `paymentMethod` TEXT, `status` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT, `message` TEXT, `timestamp` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `type` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `approvals` (`id` TEXT NOT NULL, `type` TEXT, `targetId` TEXT, `adminEmail` TEXT, `approvalDate` INTEGER, PRIMARY KEY(`id`))");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_approvals_type_targetId_adminEmail` ON `approvals` (`type`, `targetId`, `adminEmail`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'b2d05250e757b3d433cc31197cc15dda')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `members`");
        db.execSQL("DROP TABLE IF EXISTS `loans`");
        db.execSQL("DROP TABLE IF EXISTS `transactions`");
        db.execSQL("DROP TABLE IF EXISTS `notifications`");
        db.execSQL("DROP TABLE IF EXISTS `approvals`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMembers = new HashMap<String, TableInfo.Column>(22);
        _columnsMembers.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("role", new TableInfo.Column("role", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("phone", new TableInfo.Column("phone", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("email", new TableInfo.Column("email", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("password", new TableInfo.Column("password", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("payoutDate", new TableInfo.Column("payoutDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("payoutAmount", new TableInfo.Column("payoutAmount", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("hasReceivedPayout", new TableInfo.Column("hasReceivedPayout", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("shortfallAmount", new TableInfo.Column("shortfallAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("isFirstLogin", new TableInfo.Column("isFirstLogin", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("contributionTarget", new TableInfo.Column("contributionTarget", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("contributionPaid", new TableInfo.Column("contributionPaid", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("paymentStreak", new TableInfo.Column("paymentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("nextPayoutDate", new TableInfo.Column("nextPayoutDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("nextPaymentDueDate", new TableInfo.Column("nextPaymentDueDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("isAutoPayEnabled", new TableInfo.Column("isAutoPayEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("autoPayDay", new TableInfo.Column("autoPayDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("autoPayAmount", new TableInfo.Column("autoPayAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("creditScore", new TableInfo.Column("creditScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMembers.put("joinedDate", new TableInfo.Column("joinedDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMembers = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMembers = new HashSet<TableInfo.Index>(2);
        _indicesMembers.add(new TableInfo.Index("index_members_name", false, Arrays.asList("name"), Arrays.asList("ASC")));
        _indicesMembers.add(new TableInfo.Index("index_members_email", true, Arrays.asList("email"), Arrays.asList("ASC")));
        final TableInfo _infoMembers = new TableInfo("members", _columnsMembers, _foreignKeysMembers, _indicesMembers);
        final TableInfo _existingMembers = TableInfo.read(db, "members");
        if (!_infoMembers.equals(_existingMembers)) {
          return new RoomOpenHelper.ValidationResult(false, "members(com.example.save.data.local.entities.MemberEntity).\n"
                  + " Expected:\n" + _infoMembers + "\n"
                  + " Found:\n" + _existingMembers);
        }
        final HashMap<String, TableInfo.Column> _columnsLoans = new HashMap<String, TableInfo.Column>(10);
        _columnsLoans.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("memberId", new TableInfo.Column("memberId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("memberName", new TableInfo.Column("memberName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("interest", new TableInfo.Column("interest", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("reason", new TableInfo.Column("reason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("dateRequested", new TableInfo.Column("dateRequested", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("dueDate", new TableInfo.Column("dueDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLoans.put("repaidAmount", new TableInfo.Column("repaidAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLoans = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLoans = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLoans = new TableInfo("loans", _columnsLoans, _foreignKeysLoans, _indicesLoans);
        final TableInfo _existingLoans = TableInfo.read(db, "loans");
        if (!_infoLoans.equals(_existingLoans)) {
          return new RoomOpenHelper.ValidationResult(false, "loans(com.example.save.data.local.entities.LoanEntity).\n"
                  + " Expected:\n" + _infoLoans + "\n"
                  + " Found:\n" + _existingLoans);
        }
        final HashMap<String, TableInfo.Column> _columnsTransactions = new HashMap<String, TableInfo.Column>(9);
        _columnsTransactions.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("memberName", new TableInfo.Column("memberName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("amount", new TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("date", new TableInfo.Column("date", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("isPositive", new TableInfo.Column("isPositive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("paymentMethod", new TableInfo.Column("paymentMethod", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTransactions.put("status", new TableInfo.Column("status", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTransactions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTransactions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTransactions = new TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions);
        final TableInfo _existingTransactions = TableInfo.read(db, "transactions");
        if (!_infoTransactions.equals(_existingTransactions)) {
          return new RoomOpenHelper.ValidationResult(false, "transactions(com.example.save.data.local.entities.TransactionEntity).\n"
                  + " Expected:\n" + _infoTransactions + "\n"
                  + " Found:\n" + _existingTransactions);
        }
        final HashMap<String, TableInfo.Column> _columnsNotifications = new HashMap<String, TableInfo.Column>(6);
        _columnsNotifications.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("message", new TableInfo.Column("message", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("isRead", new TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNotifications.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNotifications = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNotifications = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNotifications = new TableInfo("notifications", _columnsNotifications, _foreignKeysNotifications, _indicesNotifications);
        final TableInfo _existingNotifications = TableInfo.read(db, "notifications");
        if (!_infoNotifications.equals(_existingNotifications)) {
          return new RoomOpenHelper.ValidationResult(false, "notifications(com.example.save.data.local.entities.NotificationEntity).\n"
                  + " Expected:\n" + _infoNotifications + "\n"
                  + " Found:\n" + _existingNotifications);
        }
        final HashMap<String, TableInfo.Column> _columnsApprovals = new HashMap<String, TableInfo.Column>(5);
        _columnsApprovals.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsApprovals.put("type", new TableInfo.Column("type", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsApprovals.put("targetId", new TableInfo.Column("targetId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsApprovals.put("adminEmail", new TableInfo.Column("adminEmail", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsApprovals.put("approvalDate", new TableInfo.Column("approvalDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysApprovals = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesApprovals = new HashSet<TableInfo.Index>(1);
        _indicesApprovals.add(new TableInfo.Index("index_approvals_type_targetId_adminEmail", true, Arrays.asList("type", "targetId", "adminEmail"), Arrays.asList("ASC", "ASC", "ASC")));
        final TableInfo _infoApprovals = new TableInfo("approvals", _columnsApprovals, _foreignKeysApprovals, _indicesApprovals);
        final TableInfo _existingApprovals = TableInfo.read(db, "approvals");
        if (!_infoApprovals.equals(_existingApprovals)) {
          return new RoomOpenHelper.ValidationResult(false, "approvals(com.example.save.data.local.entities.ApprovalEntity).\n"
                  + " Expected:\n" + _infoApprovals + "\n"
                  + " Found:\n" + _existingApprovals);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "b2d05250e757b3d433cc31197cc15dda", "46ae0c0fcfc0869542f8df4497a74408");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "members","loans","transactions","notifications","approvals");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `members`");
      _db.execSQL("DELETE FROM `loans`");
      _db.execSQL("DELETE FROM `transactions`");
      _db.execSQL("DELETE FROM `notifications`");
      _db.execSQL("DELETE FROM `approvals`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MemberDao.class, MemberDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LoanDao.class, LoanDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(NotificationDao.class, NotificationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ApprovalDao.class, ApprovalDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MemberDao memberDao() {
    if (_memberDao != null) {
      return _memberDao;
    } else {
      synchronized(this) {
        if(_memberDao == null) {
          _memberDao = new MemberDao_Impl(this);
        }
        return _memberDao;
      }
    }
  }

  @Override
  public LoanDao loanDao() {
    if (_loanDao != null) {
      return _loanDao;
    } else {
      synchronized(this) {
        if(_loanDao == null) {
          _loanDao = new LoanDao_Impl(this);
        }
        return _loanDao;
      }
    }
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public NotificationDao notificationDao() {
    if (_notificationDao != null) {
      return _notificationDao;
    } else {
      synchronized(this) {
        if(_notificationDao == null) {
          _notificationDao = new NotificationDao_Impl(this);
        }
        return _notificationDao;
      }
    }
  }

  @Override
  public ApprovalDao approvalDao() {
    if (_approvalDao != null) {
      return _approvalDao;
    } else {
      synchronized(this) {
        if(_approvalDao == null) {
          _approvalDao = new ApprovalDao_Impl(this);
        }
        return _approvalDao;
      }
    }
  }
}
