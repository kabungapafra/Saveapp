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

    @SerializedName("contribution_amount")
    private double contributionAmount;

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

    public double getContributionAmount() {
        return contributionAmount;
    }

    public void setContributionAmount(double contributionAmount) {
        this.contributionAmount = contributionAmount;
    }

    @SerializedName("max_loan_duration")
    private int maxLoanDuration;

    @SerializedName("currency")
    private String currency;

    public int getMaxLoanDuration() {
        return maxLoanDuration;
    }

    public void setMaxLoanDuration(int maxLoanDuration) {
        this.maxLoanDuration = maxLoanDuration;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("recipients")
    private int recipients;

    @SerializedName("loan_late_fee")
    private double loanLateFee;

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("automatic_payouts")
    private boolean automaticPayouts;

    @SerializedName("scheduled_contributions")
    private boolean scheduledContributions;

    @SerializedName("smart_roundups")
    private boolean smartRoundups;

    @SerializedName("automated_cycle")
    private boolean automatedCycle;

    @SerializedName("loan_requests")
    private boolean loanRequests;

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public int getRecipients() { return recipients; }
    public void setRecipients(int recipients) { this.recipients = recipients; }

    public double getLoanLateFee() { return loanLateFee; }
    public void setLoanLateFee(double loanLateFee) { this.loanLateFee = loanLateFee; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public boolean isAutomaticPayouts() { return automaticPayouts; }
    public void setAutomaticPayouts(boolean automaticPayouts) { this.automaticPayouts = automaticPayouts; }

    public boolean isScheduledContributions() { return scheduledContributions; }
    public void setScheduledContributions(boolean scheduledContributions) { this.scheduledContributions = scheduledContributions; }

    public boolean isSmartRoundups() { return smartRoundups; }
    public void setSmartRoundups(boolean smartRoundups) { this.smartRoundups = smartRoundups; }

    public boolean isAutomatedCycle() { return automatedCycle; }
    public void setAutomatedCycle(boolean automatedCycle) { this.automatedCycle = automatedCycle; }

    public boolean isLoanRequests() { return loanRequests; }
    public void setLoanRequests(boolean loanRequests) { this.loanRequests = loanRequests; }
}
