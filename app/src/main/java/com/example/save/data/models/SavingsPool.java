package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class SavingsPool {

    @SerializedName("id")
    private String id;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("per_member_amount")
    private double perMemberAmount;

    @SerializedName("member_count")
    private int memberCount;

    @SerializedName("save_date")
    private String saveDate;

    @SerializedName("receive_date")
    private String receiveDate;

    @SerializedName("status")
    private String status;

    @SerializedName("notified_24h")
    private boolean notified24h;

    @SerializedName("payout_triggered")
    private boolean payoutTriggered;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public double getTotalAmount() { return totalAmount; }
    public double getPerMemberAmount() { return perMemberAmount; }
    public int getMemberCount() { return memberCount; }
    public String getSaveDate() { return saveDate; }
    public String getReceiveDate() { return receiveDate; }
    public String getStatus() { return status; }
    public boolean isPayoutTriggered() { return payoutTriggered; }
    public String getCreatedAt() { return createdAt; }
}
