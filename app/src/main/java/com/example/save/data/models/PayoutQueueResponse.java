package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PayoutQueueResponse {
    @SerializedName("cycle_id")
    private String cycleId;
    @SerializedName("cycle_number")
    private int cycleNumber;
    private List<PayoutQueueEntry> queue;
    @SerializedName("next_batch")
    private List<PayoutQueueEntry> nextBatch;
    private int total;
    private int pending;
    private int paid;
    private String message;

    public String getCycleId() { return cycleId; }
    public int getCycleNumber() { return cycleNumber; }
    public List<PayoutQueueEntry> getQueue() { return queue; }
    public List<PayoutQueueEntry> getNextBatch() { return nextBatch; }
    public int getTotal() { return total; }
    public int getPending() { return pending; }
    public int getPaid() { return paid; }
    public String getMessage() { return message; }
}
