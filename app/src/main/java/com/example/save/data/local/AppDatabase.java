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

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Room Database for the Save app
 */
@Database(entities = { MemberEntity.class, LoanEntity.class }, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

        private static volatile AppDatabase INSTANCE;
        private static final String DATABASE_NAME = "save_database";

        public abstract MemberDao memberDao();

        public abstract LoanDao loanDao();

        public static AppDatabase getInstance(Context context) {
                if (INSTANCE == null) {
                        synchronized (AppDatabase.class) {
                                if (INSTANCE == null) {
                                        INSTANCE = Room.databaseBuilder(
                                                        context.getApplicationContext(),
                                                        AppDatabase.class,
                                                        DATABASE_NAME)
                                                        .fallbackToDestructiveMigration() // Wipe data on version change
                                                        .addCallback(new Callback() {
                                                                @Override
                                                                public void onCreate(
                                                                                @NonNull SupportSQLiteDatabase db) {
                                                                        super.onCreate(db);
                                                                        // Seed initial data on first database creation
                                                                        Executors.newSingleThreadExecutor()
                                                                                        .execute(() -> {
                                                                                                seedDemoData(INSTANCE);
                                                                                        });
                                                                }
                                                        })
                                                        .build();
                                }
                        }
                }
                return INSTANCE;
        }

        /**
         * Seeds demo data into the database
         */
        private static void seedDemoData(AppDatabase database) {
                MemberDao memberDao = database.memberDao();
                LoanDao loanDao = database.loanDao();

                // Demo data seeding removed for production/operational mode
                // The app will start empty, requiring the user to Sign Up as an Admin first.

                /*
                 * // Seed Members
                 * MemberEntity alice = createMember("Alice Johnson", "Member", true,
                 * "0701234567",
                 * "alice@example.com", "123456");
                 * // ... (rest of commented out code)
                 * loanDao.insert(loan4);
                 */
        }

        private static MemberEntity createMember(String name, String role, boolean isActive,
                        String phone, String email, String password) {
                MemberEntity member = new MemberEntity();
                member.setName(name);
                member.setRole(role);
                member.setActive(isActive);
                member.setPhone(phone);
                member.setEmail(email);
                member.setPassword(password);
                member.setPayoutDate("Not Scheduled");
                member.setPayoutAmount("0");
                member.setHasReceivedPayout(false);
                member.setShortfallAmount(0);
                member.setContributionTarget(1000000);
                member.setContributionPaid(0);
                return member;
        }

        private static LoanEntity createLoan(String memberId, String memberName, double amount,
                        double interest, String reason, Date dateRequested, String status) {
                LoanEntity loan = new LoanEntity();
                loan.setExternalId(UUID.randomUUID().toString());
                loan.setMemberId(memberId);
                loan.setMemberName(memberName);
                loan.setAmount(amount);
                loan.setInterest(interest);
                loan.setReason(reason);
                loan.setDateRequested(dateRequested);
                loan.setStatus(status);
                loan.setRepaidAmount(0);
                return loan;
        }
}
