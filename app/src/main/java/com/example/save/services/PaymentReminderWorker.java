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

        List<MemberEntity> members = memberDao.getAllMembersSync();

        for (MemberEntity member : members) {
            // Auto-Pay Logic
            if (member.isAutoPayEnabled() && member.getAutoPayDay() == currentDay) {
                // Simulate Auto-Pay (In real app, integrate with payment gateway)
                // For now, we notify the user that auto-pay "processed" or is due
                notificationHelper.sendNotification(
                        "Auto-Pay Processed",
                        "Your automated contribution of UGX " + (int) member.getAutoPayAmount() + " for "
                                + member.getName() + " has been initiated.");
            }

            // Payment Reminder Logic (2 days before due date)
            // Assuming nextPaymentDueDate is stored as "dd MMM" or similar string, better
            // to parse or store as timestamp
            // For simplicity in this demo, we check if today is matching a generic logic or
            // simply always remind if pending
            if (member.getContributionPaid() < member.getContributionTarget()) {
                // Check if late or close to end of month?
                // Let's just remind if it's past the 25th and not paid
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
