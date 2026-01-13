package com.example.save.data.models;

import java.util.Calendar;

/**
 * Data model for date items in the horizontal date picker
 */
public class DateItem {
    public String day;
    public String date;
    public boolean isSelected;
    public Calendar calendar;

    public DateItem(String day, String date, boolean isSelected, Calendar calendar) {
        this.day = day;
        this.date = date;
        this.isSelected = isSelected;
        this.calendar = (Calendar) calendar.clone();
    }
}
