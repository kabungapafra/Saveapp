package com.example.save.data.network;

public class ApprovalRequestDto {
    private String txId;
    private String adminEmail;

    public ApprovalRequestDto(String txId, String adminEmail) {
        this.txId = txId;
        this.adminEmail = adminEmail;
    }

    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }
    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
}
