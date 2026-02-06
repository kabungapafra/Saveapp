package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class RepaymentScheduleRequest {
    @SerializedName("amount")
    private double amount;

    @SerializedName("duration_months")
    private int durationMonths;

    public RepaymentScheduleRequest(double amount, int durationMonths) {
        this.amount = amount;
        this.durationMonths = durationMonths;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }
}
