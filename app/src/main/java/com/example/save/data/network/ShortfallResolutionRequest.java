package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class ShortfallResolutionRequest {
    @SerializedName("amount")
    private double amount;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    public ShortfallResolutionRequest(double amount, String paymentMethod) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
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
