package com.example.save.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.data.models.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotificationRepository {
    private final Executor executor;
    private final MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> unreadCountLiveData = new MutableLiveData<>(0);

    private static NotificationRepository instance;

    private NotificationRepository(Application application) {
        executor = Executors.newSingleThreadExecutor();
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

    public void addNotification(String title, String message, String type) {
        executor.execute(() -> {
            List<Notification> current = new ArrayList<>(notificationsLiveData.getValue());
            Notification n = new Notification(title, message, type);
            n.setId(System.currentTimeMillis());
            n.setTimestamp(System.currentTimeMillis());
            n.setRead(false);
            current.add(0, n);
            updateLiveData(current);
        });
    }

    public void addUniqueNotification(String title, String message, String type) {
        executor.execute(() -> {
            List<Notification> current = new ArrayList<>(notificationsLiveData.getValue());
            boolean alreadyExists = false;
            for (Notification n : current) {
                if (!n.isRead() && n.getType().equals(type) && n.getTitle().equals(title)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                Notification n = new Notification(title, message, type);
                n.setId(System.currentTimeMillis());
                n.setTimestamp(System.currentTimeMillis());
                n.setRead(false);
                current.add(0, n);
                updateLiveData(current);
            }
        });
    }

    public void markAsRead(long id) {
        executor.execute(() -> {
            List<Notification> current = new ArrayList<>(notificationsLiveData.getValue());
            for (Notification n : current) {
                if (n.getId() == id) {
                    n.setRead(true);
                    break;
                }
            }
            updateLiveData(current);
        });
    }

    public void markAllAsRead() {
        executor.execute(() -> {
            List<Notification> current = new ArrayList<>(notificationsLiveData.getValue());
            for (Notification n : current) {
                n.setRead(true);
            }
            updateLiveData(current);
        });
    }

    public void clearAll() {
        executor.execute(() -> {
            updateLiveData(new ArrayList<>());
        });
    }

    private void updateLiveData(List<Notification> notifications) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            notificationsLiveData.setValue(notifications);
            int unread = 0;
            for (Notification n : notifications) {
                if (!n.isRead()) unread++;
            }
            unreadCountLiveData.setValue(unread);
        });
    }
}
