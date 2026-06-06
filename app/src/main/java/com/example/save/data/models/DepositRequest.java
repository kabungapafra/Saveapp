package com.example.save.data.models;

/**
 * Request payload for a deposit (contribution) transaction.
 */
public class DepositRequest {
    private String memberId;
    private double amount;
    private String paymentMethod;

    public DepositRequest() {}

    public DepositRequest(String memberId, double amount, String paymentMethod) {
        this.memberId = memberId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
