package com.example.save.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.save.data.local.dao.LoanDao;
import com.example.save.data.local.dao.MemberDao;
import com.example.save.data.local.entities.LoanEntity;
import com.example.save.data.local.entities.MemberEntity;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.local.dao.TransactionDao;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Room Database for the Save app
 */
@Database(entities = { MemberEntity.class, LoanEntity.class,
                TransactionEntity.class,
                com.example.save.data.local.entities.TaskEntity.class,
                com.example.save.data.local.entities.NotificationEntity.class,
                com.example.save.data.local.entities.ApprovalEntity.class }, version = 13, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

        private static volatile AppDatabase INSTANCE;
        private static final String DATABASE_NAME = "save_database";

        public abstract MemberDao memberDao();

        public abstract LoanDao loanDao();

        public abstract TransactionDao transactionDao();

        public abstract com.example.save.data.local.dao.TaskDao taskDao();

        public abstract com.example.save.data.local.dao.NotificationDao notificationDao();

        public abstract com.example.save.data.local.dao.ApprovalDao approvalDao();

        public static AppDatabase getInstance(Context context) {
                if (INSTANCE == null) {
                        synchronized (AppDatabase.class) {
                                if (INSTANCE == null) {
                                        INSTANCE = Room.databaseBuilder(
                                                        context.getApplicationContext(),
                                                        AppDatabase.class,
                                                        DATABASE_NAME)
                                                        .fallbackToDestructiveMigration() // Wipe data on version change
                                                        .build();
                                }
                        }
                }
                return INSTANCE;
        }
}
