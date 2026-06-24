package com.example.save.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.save.utils.NotificationHelper;
import com.example.save.data.repository.NotificationRepository;

public class PaymentApprovalReceiver extends BroadcastReceiver {

    public static final String ACTION_POOL_APPROVAL  = "com.example.save.POOL_APPROVAL";
    public static final String ACTION_LOAN_APPROVAL  = "com.example.save.LOAN_APPROVAL";
    public static final String EXTRA_AMOUNT          = "amount";
    public static final String EXTRA_DATE            = "date";
    public static final String EXTRA_LOAN_ID         = "loan_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        NotificationHelper helper = new NotificationHelper(context);

        if (ACTION_POOL_APPROVAL.equals(intent.getAction())) {
            String amount = intent.getStringExtra(EXTRA_AMOUNT);
            String date   = intent.getStringExtra(EXTRA_DATE);
            String title  = "Savings Pool Payout — Approval Required";
            String body   = "Pool payout of " + amount + " is due tomorrow ("
                    + date + "). Open the app to approve before payment is released.";

            helper.showNotification(title, body, NotificationHelper.CHANNEL_ID_PAYMENTS);
            NotificationRepository.getInstance(
                    (android.app.Application) context.getApplicationContext())
                    .addNotification(title, body, "SAVINGS");

        } else if (ACTION_LOAN_APPROVAL.equals(intent.getAction())) {
            String loanId = intent.getStringExtra(EXTRA_LOAN_ID);
            String amount = intent.getStringExtra(EXTRA_AMOUNT);
            String date   = intent.getStringExtra(EXTRA_DATE);
            String title  = "Loan Disbursement — Approval Required";
            String body   = "Loan of " + amount + " is due for release on " + date
                    + ". Approve within 24 hours to trigger payment.";

            helper.showNotification(title, body, NotificationHelper.CHANNEL_ID_LOANS);
            NotificationRepository.getInstance(
                    (android.app.Application) context.getApplicationContext())
                    .addNotification(title, body, "LOANS");
        }
    }
}
