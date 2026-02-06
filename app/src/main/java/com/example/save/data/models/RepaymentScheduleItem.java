package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class RepaymentScheduleItem {
    @SerializedName("month")
    private int month;

    @SerializedName("due_date")
    private String dueDate; // ISO string

    @SerializedName("amount")
    private double amount;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
