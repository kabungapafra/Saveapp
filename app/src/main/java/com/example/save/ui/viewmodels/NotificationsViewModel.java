package com.example.save.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.save.data.models.Notification;
import com.example.save.data.repository.NotificationRepository;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {
    private final NotificationRepository repository;
    private final LiveData<List<Notification>> allNotifications;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        repository = NotificationRepository.getInstance(application);
        allNotifications = repository.getAllNotifications();
    }

    public LiveData<List<Notification>> getNotifications() {
        return allNotifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return repository.getUnreadCount();
    }

    public void markAsRead(Notification notification) {
        // UI assumes read immediately, but background needs to update DB.
        // For simple impl, we might not update list immediately unless we implement
        // proper diffutil or refresh
        repository.markAsRead(notification.getId());
    }

    public void markAllAsRead() {
        repository.markAllAsRead();
    }

    // Testing helper - Removed to prevent spam
    public void createTestNotification() {
        // Disabled
    }

    public void createAnnouncement(String title, String message) {
        repository.addUniqueNotification(title, message, "ANNOUNCEMENT");
    }

    public void ensureSystemNotification(String title, String message, String type) {
        // Use unique notification to avoid spamming the database on every dashboard
        // load
        repository.addUniqueNotification(title, message, type);
    }
}
