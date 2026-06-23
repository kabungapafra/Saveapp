package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class PayoutEntity {
    @SerializedName("id")
    private String id;

    @SerializedName("member_name")
    private String memberName;

    @SerializedName("amount")
    private double amount;

    @SerializedName("net_amount")
    private double netAmount;

    @SerializedName("retention_amount")
    private double retentionAmount;

    @SerializedName("status")
    private String status;

    @SerializedName("executed_at")
    private String executedAt;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getMemberName() { return memberName; }
    public double getAmount() { return amount; }
    public double getNetAmount() { return netAmount; }
    public double getRetentionAmount() { return retentionAmount; }
    public String getStatus() { return status; }
    public String getExecutedAt() { return executedAt; }
    public String getCreatedAt() { return createdAt; }
}
