package com.example.save.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.save.R;
import com.example.save.ui.activities.OnboardingActivity;

public class NotificationHelper {

    public static final String CHANNEL_ID_GENERAL = "general_channel";
    public static final String CHANNEL_ID_PAYMENTS = "payments_channel";
    public static final String CHANNEL_ID_LOANS = "loans_channel";
    public static final String CHANNEL_ID_SECURITY = "security_channel";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null)
                return;

            createChannel(manager, CHANNEL_ID_GENERAL, "General Updates", "General app notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            createChannel(manager, CHANNEL_ID_PAYMENTS, "Payments", "Payment reminders and confirmations",
                    NotificationManager.IMPORTANCE_HIGH);
            createChannel(manager, CHANNEL_ID_LOANS, "Loans", "Loan updates and alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            createChannel(manager, CHANNEL_ID_SECURITY, "Security", "Security alerts",
                    NotificationManager.IMPORTANCE_HIGH);
        }
    }

    private void createChannel(NotificationManager manager, String id, String name, String description,
            int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            manager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String body, String channelId) {
        // Fallback to general channel if invalid
        if (channelId == null || channelId.isEmpty()) {
            channelId = CHANNEL_ID_GENERAL;
        }

        // Create an Intent for the activity to open when user taps the notification
        // For now, we open OnboardingActivity (which routes to Login/Main)
        // In a real app, you might want to open specific activities based on payload
        Intent intent = new Intent(context, OnboardingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.save_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Permission check for Android 13+ is handled by the caller or assumed granted
        // if this is called
        // However, SecurityException can be thrown if permission is missing
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void sendNotification(String title, String body) {
        showNotification(title, body, CHANNEL_ID_PAYMENTS);
    }
}
