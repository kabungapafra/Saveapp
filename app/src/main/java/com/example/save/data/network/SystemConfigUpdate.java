package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for PUT /api/config.
 * All fields are nullable — only non-null fields will be sent and updated on
 * the backend.
 */
public class SystemConfigUpdate {

    @SerializedName("loan_interest_rate")
    private Double loanInterestRate;

    @SerializedName("max_loan_multiplier")
    private Double maxLoanMultiplier;

    @SerializedName("max_loan_limit")
    private Double maxLoanLimit;

    @SerializedName("payout_amount")
    private Double payoutAmount;

    @SerializedName("retention_percentage")
    private Double retentionPercentage;

    @SerializedName("late_penalty_rate")
    private Double latePenaltyRate;

    @SerializedName("contribution_amount")
    private Double contributionAmount;

    @SerializedName("max_loan_duration")
    private Integer maxLoanDuration;

    // --- Builder-style setters (return this for chaining) ---

    public SystemConfigUpdate setMaxLoanDuration(Integer v) {
        this.maxLoanDuration = v;
        return this;
    }

    public SystemConfigUpdate setLoanInterestRate(Double v) {
        this.loanInterestRate = v;
        return this;
    }

    public SystemConfigUpdate setMaxLoanMultiplier(Double v) {
        this.maxLoanMultiplier = v;
        return this;
    }

    public SystemConfigUpdate setMaxLoanLimit(Double v) {
        this.maxLoanLimit = v;
        return this;
    }

    public SystemConfigUpdate setPayoutAmount(Double v) {
        this.payoutAmount = v;
        return this;
    }

    public SystemConfigUpdate setRetentionPercentage(Double v) {
        this.retentionPercentage = v;
        return this;
    }

    public SystemConfigUpdate setLatePenaltyRate(Double v) {
        this.latePenaltyRate = v;
        return this;
    }

    public SystemConfigUpdate setContributionAmount(Double v) {
        this.contributionAmount = v;
        return this;
    }

    // --- Getters ---
    public Double getLoanInterestRate() {
        return loanInterestRate;
    }

    public Double getMaxLoanMultiplier() {
        return maxLoanMultiplier;
    }

    public Double getMaxLoanLimit() {
        return maxLoanLimit;
    }

    public Double getPayoutAmount() {
        return payoutAmount;
    }

    public Double getRetentionPercentage() {
        return retentionPercentage;
    }

    public Double getLatePenaltyRate() {
        return latePenaltyRate;
    }

    public Double getContributionAmount() {
        return contributionAmount;
    }

    public Integer getMaxLoanDuration() {
        return maxLoanDuration;
    }
}
