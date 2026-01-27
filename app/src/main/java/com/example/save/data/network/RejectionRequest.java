package com.example.save.data.network;

public class RejectionRequest {
    private String reason;

    public RejectionRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
