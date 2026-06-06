package com.example.save.data.models;

/**
 * Request payload for a deposit (contribution) transaction.
 */
public class DepositRequest {
    private String memberId;
    private double amount;
    private String paymentMethod;
    private String description;

    public DepositRequest() {}

    public DepositRequest(String memberId, double amount, String paymentMethod, String description) {
        this.memberId = memberId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }

    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
