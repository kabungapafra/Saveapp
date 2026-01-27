package com.example.save.data.network;

public class ApprovalRequest {
    public long transactionId;
    public String adminEmail;

    public ApprovalRequest(long transactionId, String adminEmail) {
        this.transactionId = transactionId;
        this.adminEmail = adminEmail;
    }
}
