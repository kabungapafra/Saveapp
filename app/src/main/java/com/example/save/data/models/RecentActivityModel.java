package com.example.save.data.models;

/**
 * Data model for recent activities displayed on the dashboard
 */
public class RecentActivityModel {
    public String title;
    public String description;
    public String amount;
    public boolean isPositive;

    public RecentActivityModel(String title, String description, String amount, boolean isPositive) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.isPositive = isPositive;
    }
}
