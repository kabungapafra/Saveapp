package com.example.save.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.save.data.local.entities.NotificationEntity;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(NotificationEntity notification);

    @Update
    void update(NotificationEntity notification);

    @Delete
    void delete(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<NotificationEntity>> getAllNotifications();

    @Query("SELECT * FROM notifications WHERE isRead = 0")
    List<NotificationEntity> getUnreadNotificationsSync();

    @Query("SELECT * FROM notifications WHERE type = :type AND isRead = 0")
    List<NotificationEntity> getUnreadByTypeSync(String type);

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    LiveData<Integer> getUnreadCount();

    @Query("DELETE FROM notifications")
    void deleteAll();
}
