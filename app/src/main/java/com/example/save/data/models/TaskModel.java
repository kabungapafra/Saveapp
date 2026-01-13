package com.example.save.data.models;

import java.util.Calendar;

/**
 * Data model for tasks in the timeline/daily tasks view
 */
public class TaskModel {
    public String time;
    public String title;
    public String subtitle;
    public String status;
    public String progress;
    public int color;
    public int iconRes;
    public Calendar dateAssigned;
    public String amount;

    public TaskModel(String time, String title, String subtitle, String status, String progress, int color, int iconRes,
            Calendar dateAssigned, String amount) {
        this.time = time;
        this.title = title;
        this.subtitle = subtitle;
        this.status = status;
        this.progress = progress;
        this.color = color;
        this.iconRes = iconRes;
        this.dateAssigned = dateAssigned != null ? (Calendar) dateAssigned.clone() : null;
        this.amount = amount;
    }
}
