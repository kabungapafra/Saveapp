package com.example.save.data.network;

public class TransactionRequest {
    private String memberName;
    private String type;
    private double amount;
    private String description;
    private String paymentMethod;

    public TransactionRequest(String memberName, String type, double amount, String description, String paymentMethod) {
        this.memberName = memberName;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.paymentMethod = paymentMethod;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
