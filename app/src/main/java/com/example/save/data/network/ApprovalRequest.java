package com.example.save.data.network;

public class ApprovalRequest {
    public String txId;
    public String adminEmail;

    public ApprovalRequest(String txId, String adminEmail) {
        this.txId = txId;
        this.adminEmail = adminEmail;
    }
}
