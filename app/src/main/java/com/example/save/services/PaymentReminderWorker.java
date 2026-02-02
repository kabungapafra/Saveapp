package com.example.save.services;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.save.data.local.AppDatabase;
import com.example.save.data.local.dao.MemberDao;
import com.example.save.data.local.entities.MemberEntity;
import com.example.save.utils.NotificationHelper;

import java.util.Calendar;
import java.util.List;

public class PaymentReminderWorker extends Worker {

    public PaymentReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(context);
        MemberDao memberDao = database.memberDao();
        NotificationHelper notificationHelper = new NotificationHelper(context);

        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        android.content.SharedPreferences prefs = context.getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
        boolean remindersEnabled = prefs.getBoolean("payment_reminders", true);

        if (!remindersEnabled) {
            return Result.success();
        }

        List<MemberEntity> members = memberDao.getAllMembersSync();

        for (MemberEntity member : members) {
            // Auto-Pay Logic
            if (member.isAutoPayEnabled() && member.getAutoPayDay() == currentDay) {
                // ... (Auto-pay logic)
            }

            // Targeted Payment Notifications
            String dueDateStr = member.getNextPaymentDueDate();
            if (dueDateStr != null && !"TBD".equals(dueDateStr)) {
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy",
                            java.util.Locale.getDefault());
                    java.util.Date dueDate = sdf.parse(dueDateStr);
                    long diff = dueDate.getTime() - System.currentTimeMillis();
                    long daysDiff = diff / (24 * 60 * 60 * 1000);

                    if (member.getContributionPaid() < member.getContributionTarget()) {
                        if (daysDiff == 2) {
                            notificationHelper.sendNotification(
                                    "Friendly Reminder",
                                    "Hi " + member.getName() + ", your contribution of UGX " +
                                            (int) (member.getContributionTarget() - member.getContributionPaid()) +
                                            " is due in 2 days (" + dueDateStr + ").");
                        } else if (diff < 0) {
                            notificationHelper.sendNotification(
                                    "Urgent: Payment Overdue",
                                    "Hi " + member.getName()
                                            + ", your contribution for the current cycle is OVERDUE. Please pay immediately to avoid penalties.");
                        }
                    }
                } catch (Exception e) {
                    // Log or handle parse error
                }
            } else if (member.getContributionPaid() < member.getContributionTarget()) {
                // Legacy logic fallback if no date
                if (currentDay >= 25) {
                    notificationHelper.sendNotification(
                            "Payment Reminder",
                            "Hi " + member.getName()
                                    + ", your monthly contribution is due soon. Please pay to avoid penalties.");
                }
            }
        }

        return Result.success();
    }
}
