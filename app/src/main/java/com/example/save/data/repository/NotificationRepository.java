package com.example.save.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.data.models.Notification;
import com.example.save.data.network.ApiResponse;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationRepository {

    private static final String TAG = "NotificationRepo";

    private final Application application;
    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> unreadCountLiveData = new MutableLiveData<>(0);

    private static NotificationRepository instance;

    private NotificationRepository(Application application) {
        this.application = application;
    }

    public static synchronized NotificationRepository getInstance(Application application) {
        if (instance == null) {
            instance = new NotificationRepository(application);
        }
        return instance;
    }

    public LiveData<List<Notification>> getAllNotifications() {
        return notificationsLiveData;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCountLiveData;
    }

    /** Fetch notifications from the server and update LiveData. */
    public void fetchFromServer() {
        ApiService api = RetrofitClient.getClient(application).create(ApiService.class);
        api.getNotifications().enqueue(new Callback<List<Notification>>() {
            @Override
            public void onResponse(Call<List<Notification>> call, Response<List<Notification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notification> list = response.body();
                    // Assign local IDs so adapter keying works
                    for (int i = 0; i < list.size(); i++) {
                        list.get(i).setId(i);
                    }
                    updateLiveData(list);
                } else {
                    Log.w(TAG, "fetchFromServer failed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Notification>> call, Throwable t) {
                Log.e(TAG, "fetchFromServer network error: " + t.getMessage());
            }
        });
    }

    public void markAsRead(long localId) {
        List<Notification> current = notificationsLiveData.getValue();
        if (current == null) return;
        for (Notification n : current) {
            if (n.getId() == localId) {
                n.setRead(true);
                if (n.getServerId() != null) {
                    ApiService api = RetrofitClient.getClient(application).create(ApiService.class);
                    api.markNotificationRead(n.getServerId()).enqueue(new Callback<ApiResponse>() {
                        @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
                        @Override public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Log.e(TAG, "markAsRead failed: " + t.getMessage());
                        }
                    });
                }
                break;
            }
        }
        updateLiveData(new ArrayList<>(current));
    }

    public void markAllAsRead() {
        List<Notification> current = notificationsLiveData.getValue();
        if (current == null) return;
        for (Notification n : current) n.setRead(true);
        updateLiveData(new ArrayList<>(current));

        ApiService api = RetrofitClient.getClient(application).create(ApiService.class);
        api.markAllNotificationsRead().enqueue(new Callback<ApiResponse>() {
            @Override public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {}
            @Override public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "markAllAsRead failed: " + t.getMessage());
            }
        });
    }

    /** Used by FCM service to inject an incoming push into the live list instantly. */
    public void addNotification(String title, String message, String type) {
        List<Notification> current = new ArrayList<>();
        if (notificationsLiveData.getValue() != null) current.addAll(notificationsLiveData.getValue());
        Notification n = new Notification(title, message, type);
        n.setId(System.currentTimeMillis());
        current.add(0, n);
        updateLiveData(current);
    }

    public void addUniqueNotification(String title, String message, String type) {
        List<Notification> current = new ArrayList<>();
        if (notificationsLiveData.getValue() != null) current.addAll(notificationsLiveData.getValue());
        for (Notification n : current) {
            if (!n.isRead() && type.equals(n.getType()) && title.equals(n.getTitle())) return;
        }
        addNotification(title, message, type);
    }

    public void clearAll() {
        updateLiveData(new ArrayList<>());
    }

    private void updateLiveData(List<Notification> notifications) {
        new Handler(Looper.getMainLooper()).post(() -> {
            notificationsLiveData.setValue(notifications);
            int unread = 0;
            for (Notification n : notifications) if (!n.isRead()) unread++;
            unreadCountLiveData.setValue(unread);
        });
    }
}
