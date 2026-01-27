package com.example.save.data.network;

public class LoanRepaymentRequest {
    private double amount;
    private String paymentMethod;
    private String phoneNumber;

    public LoanRepaymentRequest(double amount, String paymentMethod, String phoneNumber) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
