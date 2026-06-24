package com.example.save.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.save.receivers.PaymentApprovalReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentScheduler {

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private static final long HOURS_24_MS = 24L * 60 * 60 * 1000;

    // Request codes (unique per alarm type)
    private static final int RC_POOL_APPROVAL = 1001;
    private static final int RC_LOAN_BASE     = 2000; // loan alarms use RC_LOAN_BASE + hashCode

    /**
     * Schedule a notification 24h before the pool payout receive date.
     * Safe to call again — cancels any previous pool alarm first.
     */
    public static void schedulePoolPayout(Context ctx, String receiveDate, String amountFormatted) {
        Date date = parseDate(receiveDate);
        if (date == null) return;

        long triggerAt = date.getTime() - HOURS_24_MS;
        if (triggerAt <= System.currentTimeMillis()) return; // already past

        Intent intent = new Intent(ctx, PaymentApprovalReceiver.class)
                .setAction(PaymentApprovalReceiver.ACTION_POOL_APPROVAL)
                .putExtra(PaymentApprovalReceiver.EXTRA_AMOUNT, amountFormatted)
                .putExtra(PaymentApprovalReceiver.EXTRA_DATE, receiveDate);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, RC_POOL_APPROVAL, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        schedule(ctx, pi, triggerAt);
    }

    /**
     * Schedule a notification 24h before a loan disbursement date.
     */
    public static void scheduleLoanApproval(Context ctx, String loanId,
                                             String dueDate, String amountFormatted) {
        Date date = parseDate(dueDate);
        if (date == null) return;

        long triggerAt = date.getTime() - HOURS_24_MS;
        if (triggerAt <= System.currentTimeMillis()) return;

        int requestCode = RC_LOAN_BASE + Math.abs(loanId.hashCode() % 1000);

        Intent intent = new Intent(ctx, PaymentApprovalReceiver.class)
                .setAction(PaymentApprovalReceiver.ACTION_LOAN_APPROVAL)
                .putExtra(PaymentApprovalReceiver.EXTRA_LOAN_ID, loanId)
                .putExtra(PaymentApprovalReceiver.EXTRA_AMOUNT, amountFormatted)
                .putExtra(PaymentApprovalReceiver.EXTRA_DATE, dueDate);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        schedule(ctx, pi, triggerAt);
    }

    private static void schedule(Context ctx, PendingIntent pi, long triggerAtMs) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pi);
        }
    }

    /**
     * Schedule a 24h disbursement-confirmation notification starting from now.
     * Call this immediately after a loan is approved so admin gets reminded
     * one day later to confirm the funds are released.
     */
    public static void scheduleLoanDisbursement(Context ctx, String loanId, String amountFormatted) {
        long triggerAt = System.currentTimeMillis() + HOURS_24_MS;
        int  requestCode = RC_LOAN_BASE + Math.abs(loanId.hashCode() % 1000);

        String tomorrow = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(triggerAt));

        Intent intent = new Intent(ctx, PaymentApprovalReceiver.class)
                .setAction(PaymentApprovalReceiver.ACTION_LOAN_APPROVAL)
                .putExtra(PaymentApprovalReceiver.EXTRA_LOAN_ID, loanId)
                .putExtra(PaymentApprovalReceiver.EXTRA_AMOUNT, amountFormatted)
                .putExtra(PaymentApprovalReceiver.EXTRA_DATE, tomorrow);

        PendingIntent pi = PendingIntent.getBroadcast(ctx, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        schedule(ctx, pi, triggerAt);
    }

    private static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty() || dateStr.equals("--")) return null;
        try {
            return DATE_FMT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
