package com.example.save.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.save.R;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.utils.SessionManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaveFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static final String CHANNEL_ID = "save_loans";
    private static final String CHANNEL_NAME = "Loans & Payments";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM token refreshed");
        getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE)
                .edit().putString("fcm_token", token).apply();
        if (SessionManager.getInstance(this).isLoggedIn()) {
            sendTokenToServer(this, token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        // Data-only messages: backend omits the notification key so this fires in all app states
        if ((title == null || body == null) && !remoteMessage.getData().isEmpty()) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        String type = remoteMessage.getData().get("type");
        String convId = remoteMessage.getData().get("conv_id");

        Log.d(TAG, "Message received: " + title + " type=" + type);
        if (title != null && body != null) {
            String navigateTo = resolveNavigateTo(type);
            showNotification(title, body, navigateTo, convId);
        }
    }

    private void showNotification(String title, String body) {
        showNotification(title, body, null, null);
    }

    private void showNotification(String title, String body, String navigateTo, String convId) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Loan requests, repayments, and reminders");
            manager.createNotificationChannel(channel);
        }

        Intent intent;
        try {
            Class<?> splash = Class.forName("com.example.save.ui.activities.SplashActivity");
            intent = new Intent(this, splash);
        } catch (ClassNotFoundException e) {
            return;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("NAVIGATE_TO", navigateTo != null ? navigateTo : "NOTIFICATIONS");
        if (convId != null) intent.putExtra("conv_id", convId);
        intent.putExtra("from_notification", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private static String resolveNavigateTo(String type) {
        if (type == null) return "NOTIFICATIONS";
        switch (type) {
            case "CHAT_REQUEST":         return "CHAT_REQUESTS";   // admin: see pending requests
            case "CHAT_ACCEPTED":        return "LIVE_CHAT";        // member: go to chat
            case "CHAT_DECLINED":        return "NOTIFICATIONS";    // member: see declined notice
            case "CHAT_ENDED":           return "CHAT_ENDED";       // member: go to feedback
            case "CHAT_ENDED_BY_MEMBER": return "NOTIFICATIONS";    // admin: just a notice
            default:                     return "NOTIFICATIONS";
        }
    }

    /**
     * Call this after every login. Fetches the current FCM token directly from Firebase
     * (does NOT rely on onNewToken having fired, which only happens for new tokens).
     */
    public static void registerTokenWithServer(Context context) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (token == null || token.isEmpty()) {
                        Log.w(TAG, "FCM getToken returned null/empty");
                        return;
                    }
                    Log.d(TAG, "FCM token fetched, registering with server");
                    context.getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE)
                            .edit().putString("fcm_token", token).apply();
                    sendTokenToServer(context, token);
                })
                .addOnFailureListener(e -> Log.e(TAG, "FCM getToken failed: " + e.getMessage()));
    }

    private static void sendTokenToServer(Context context, String token) {
        ApiService api = RetrofitClient.getClient(context).create(ApiService.class);
        api.updateFcmToken(new FcmTokenRequest(token)).enqueue(new Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.network.ApiResponse> call,
                    Response<com.example.save.data.network.ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM token registered with server");
                } else {
                    Log.w(TAG, "FCM token registration failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                Log.e(TAG, "FCM token registration network error: " + t.getMessage());
            }
        });
    }

    public static class FcmTokenRequest {
        private final String token;
        public FcmTokenRequest(String token) { this.token = token; }
        public String getToken() { return token; }
    }
}
