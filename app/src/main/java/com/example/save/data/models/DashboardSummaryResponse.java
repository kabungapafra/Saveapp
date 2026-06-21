package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class DashboardSummaryResponse {
    @SerializedName("total_members")
    private int totalMembers;

    @SerializedName("active_members")
    private int activeMembers;

    @SerializedName("total_balance")
    private double totalBalance;

    @SerializedName("personal_savings")
    private double personalSavings;

    @SerializedName("pending_approvals_count")
    private int pendingApprovalsCount;

    @SerializedName("group_name")
    private String groupName;

    @SerializedName("admin_name")
    private String adminName;

    @SerializedName("monthly_contributions")
    private double monthlyContributions;

    @SerializedName("interest_earned")
    private double interestEarned;

    @SerializedName("contribution_amount")
    private double contributionAmount;

    // Getters
    public int getTotalMembers() { return totalMembers; }
    public int getActiveMembers() { return activeMembers; }
    public double getTotalBalance() { return totalBalance; }
    public double getPersonalSavings() { return personalSavings; }
    public int getPendingApprovalsCount() { return pendingApprovalsCount; }
    public String getGroupName() { return groupName; }
    public String getAdminName() { return adminName; }
    public double getMonthlyContributions() { return monthlyContributions; }
    public double getInterestEarned() { return interestEarned; }
    public double getContributionAmount() { return contributionAmount; }
}
