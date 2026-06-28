package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

/** Server-computed aggregates for the admin loan-management screen. */
public class LoanSummaryResponse {

    @SerializedName("total_active_amount")
    private double totalActiveAmount;

    @SerializedName("overdue_amount")
    private double overdueAmount;

    @SerializedName("active_count")
    private int activeCount;

    @SerializedName("pending_count")
    private int pendingCount;

    public double getTotalActiveAmount() { return totalActiveAmount; }
    public double getOverdueAmount() { return overdueAmount; }
    public int getActiveCount() { return activeCount; }
    public int getPendingCount() { return pendingCount; }
}
