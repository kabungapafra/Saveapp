package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class SystemConfig {
    @SerializedName("loan_interest_rate")
    private double loanInterestRate;

    @SerializedName("max_loan_multiplier")
    private double maxLoanMultiplier;

    @SerializedName("max_loan_limit")
    private double maxLoanLimit;

    @SerializedName("payout_amount")
    private double payoutAmount;

    @SerializedName("retention_percentage")
    private double retentionPercentage;

    @SerializedName("late_penalty_rate")
    private double latePenaltyRate;

    public double getLoanInterestRate() {
        return loanInterestRate;
    }

    public void setLoanInterestRate(double loanInterestRate) {
        this.loanInterestRate = loanInterestRate;
    }

    public double getMaxLoanMultiplier() {
        return maxLoanMultiplier;
    }

    public void setMaxLoanMultiplier(double maxLoanMultiplier) {
        this.maxLoanMultiplier = maxLoanMultiplier;
    }

    public double getMaxLoanLimit() {
        return maxLoanLimit;
    }

    public void setMaxLoanLimit(double maxLoanLimit) {
        this.maxLoanLimit = maxLoanLimit;
    }

    public double getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(double payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public double getRetentionPercentage() {
        return retentionPercentage;
    }

    public void setRetentionPercentage(double retentionPercentage) {
        this.retentionPercentage = retentionPercentage;
    }

    public double getLatePenaltyRate() {
        return latePenaltyRate;
    }

    public void setLatePenaltyRate(double latePenaltyRate) {
        this.latePenaltyRate = latePenaltyRate;
    }
}
