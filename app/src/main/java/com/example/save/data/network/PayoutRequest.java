package com.example.save.data.network;

public class PayoutRequest {
    private String memberEmail;
    private double amount;
    private boolean deferRemaining;

    public PayoutRequest(String memberEmail, double amount, boolean deferRemaining) {
        this.memberEmail = memberEmail;
        this.amount = amount;
        this.deferRemaining = deferRemaining;
    }

    public String getMemberEmail() {
        return memberEmail;
    }

    public void setMemberEmail(String memberEmail) {
        this.memberEmail = memberEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isDeferRemaining() {
        return deferRemaining;
    }

    public void setDeferRemaining(boolean deferRemaining) {
        this.deferRemaining = deferRemaining;
    }
}
