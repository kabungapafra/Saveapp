package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class LoanEligibilityResponse {
    @SerializedName("is_eligible")
    private boolean isEligible;

    @SerializedName("max_eligible_amount")
    private double maxEligibleAmount;

    @SerializedName("reason")
    private String reason;

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public double getMaxEligibleAmount() {
        return maxEligibleAmount;
    }

    public void setMaxEligibleAmount(double maxEligibleAmount) {
        this.maxEligibleAmount = maxEligibleAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
