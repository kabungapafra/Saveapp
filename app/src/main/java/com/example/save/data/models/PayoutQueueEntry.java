package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class PayoutQueueEntry {
    @SerializedName("queue_entry_id")
    private String queueEntryId;
    @SerializedName("member_id")
    private String memberId;
    @SerializedName("member_name")
    private String memberName;
    @SerializedName("member_phone")
    private String memberPhone;
    private int position;
    @SerializedName("credit_score")
    private double creditScore;
    @SerializedName("has_received_payout")
    private boolean hasReceivedPayout;
    @SerializedName("payout_date")
    private String payoutDate;
    @SerializedName("actual_payout_date")
    private String actualPayoutDate;

    public String getQueueEntryId() { return queueEntryId; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getMemberPhone() { return memberPhone; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public double getCreditScore() { return creditScore; }
    public boolean hasReceivedPayout() { return hasReceivedPayout; }
    public String getPayoutDate() { return payoutDate; }
    public String getActualPayoutDate() { return actualPayoutDate; }
}
