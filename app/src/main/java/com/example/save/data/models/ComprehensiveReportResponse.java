package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.example.save.data.models.TransactionEntity;

public class ComprehensiveReportResponse {
    @SerializedName("generated_at")
    private String generatedAt;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("total_balance")
    private double totalBalance;

    @SerializedName("total_members")
    private int totalMembers;

    @SerializedName("active_loans_count")
    private int activeLoansCount;

    @SerializedName("active_loans_amount")
    private double activeLoansAmount;

    @SerializedName("total_contributions")
    private double totalContributions;

    @SerializedName("total_payouts_amount")
    private double totalPayoutsAmount;

    @SerializedName("members")
    private List<Member> members;

    @SerializedName("recent_transactions")
    private List<TransactionEntity> recentTransactions;

    @SerializedName("active_loans")
    private List<Loan> activeLoans;

    @SerializedName("payouts")
    private List<TransactionEntity> payouts;

    // Getters
    public String getGeneratedAt() {
        return generatedAt;
    }

    public String getGroupName() {
        return groupName;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public int getTotalMembers() {
        return totalMembers;
    }

    public int getActiveLoansCount() {
        return activeLoansCount;
    }

    public double getActiveLoansAmount() {
        return activeLoansAmount;
    }

    public double getTotalContributions() {
        return totalContributions;
    }

    public double getTotalPayoutsAmount() {
        return totalPayoutsAmount;
    }

    public List<Member> getMembers() {
        return members;
    }

    public List<TransactionEntity> getRecentTransactions() {
        return recentTransactions;
    }

    public List<Loan> getActiveLoans() {
        return activeLoans;
    }

    public List<TransactionEntity> getPayouts() {
        return payouts;
    }

    // Setters — used when the report is assembled client-side (no single backend endpoint)
    public void setGeneratedAt(String v) { this.generatedAt = v; }
    public void setGroupName(String v) { this.groupName = v; }
    public void setTotalBalance(double v) { this.totalBalance = v; }
    public void setTotalMembers(int v) { this.totalMembers = v; }
    public void setActiveLoansCount(int v) { this.activeLoansCount = v; }
    public void setActiveLoansAmount(double v) { this.activeLoansAmount = v; }
    public void setTotalContributions(double v) { this.totalContributions = v; }
    public void setTotalPayoutsAmount(double v) { this.totalPayoutsAmount = v; }
    public void setMembers(List<Member> v) { this.members = v; }
    public void setRecentTransactions(List<TransactionEntity> v) { this.recentTransactions = v; }
    public void setActiveLoans(List<Loan> v) { this.activeLoans = v; }
    public void setPayouts(List<TransactionEntity> v) { this.payouts = v; }
}
