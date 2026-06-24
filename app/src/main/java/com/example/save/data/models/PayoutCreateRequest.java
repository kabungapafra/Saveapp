package com.example.save.data.models;

public class PayoutCreateRequest {
    private String memberPhone;
    private double amount;
    private boolean deferRemaining;

    public PayoutCreateRequest(String memberPhone, double amount, boolean deferRemaining) {
        this.memberPhone = memberPhone;
        this.amount = amount;
        this.deferRemaining = deferRemaining;
    }

    public String getMemberPhone() { return memberPhone; }
    public double getAmount() { return amount; }
    public boolean isDeferRemaining() { return deferRemaining; }
}
