package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class SavingsPool {

    @SerializedName("id")
    private String id;

    @SerializedName("contrib_per_period")
    private double contribPerPeriod;

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("per_member_amount")
    private double perMemberAmount;

    @SerializedName("amount_collected")
    private double amountCollected;

    @SerializedName("member_count")
    private int memberCount;

    @SerializedName("frequency")
    private String frequency;

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

    @SerializedName("is_member")
    private boolean member;

    @SerializedName("joined_members")
    private java.util.List<PoolMember> joinedMembers;

    public String getId() { return id; }
    public double getContribPerPeriod() { return contribPerPeriod; }
    public double getTotalAmount() { return totalAmount; }
    public double getPerMemberAmount() { return perMemberAmount; }
    public double getAmountCollected() { return amountCollected; }
    public int getMemberCount() { return memberCount; }
    public String getFrequency() { return frequency; }
    public String getSaveDate() { return saveDate; }
    public String getReceiveDate() { return receiveDate; }
    public String getStatus() { return status; }
    public boolean isPayoutTriggered() { return payoutTriggered; }
    public String getCreatedAt() { return createdAt; }
    public boolean isMember() { return member; }
    public java.util.List<PoolMember> getJoinedMembers() {
        return joinedMembers != null ? joinedMembers : new java.util.ArrayList<>();
    }

    public static class PoolMember {
        @SerializedName("id")   private String id;
        @SerializedName("name") private String name;
        @SerializedName("phone") private String phone;
        public String getId()   { return id; }
        public String getName() { return name; }
        public String getPhone(){ return phone; }
    }
}
