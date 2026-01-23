package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;
    public String description; // Will store "Sending to: X"
    public String amount;
    public String time;
    public Date dateAssigned;
    public boolean isCompleted;
    public String status; // Pending, Completed, etc.
    public int color;
    public int iconRes;

    public TaskEntity(String title, String description, String amount, String time, Date dateAssigned, String status,
            int color, int iconRes) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.time = time;
        this.dateAssigned = dateAssigned;
        this.status = status;
        this.color = color;
        this.iconRes = iconRes;
        this.isCompleted = "Completed".equalsIgnoreCase(status);
    }
}
