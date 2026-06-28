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

    @SerializedName("yearly_contributions")
    private double yearlyContributions;

    @SerializedName("interest_earned")
    private double interestEarned;

    @SerializedName("contribution_amount")
    private double contributionAmount;

    @SerializedName("total_contributors")
    private int totalContributors;

    @SerializedName("available_savings")
    private double availableSavings;

    @SerializedName("loan_balance")
    private double loanBalance;

    @SerializedName("my_interest_share")
    private double myInterestShare;

    @SerializedName("monthly_target")
    private double monthlyTarget;

    @SerializedName("yearly_target")
    private double yearlyTarget;

    @SerializedName("monthly_target_progress")
    private int monthlyTargetProgress;

    @SerializedName("yearly_target_progress")
    private int yearlyTargetProgress;

    @SerializedName("upcoming_payout_amount")
    private double upcomingPayoutAmount;

    @SerializedName("next_contribution_date")
    private String nextContributionDate;

    // Getters
    public int getTotalMembers() { return totalMembers; }
    public int getActiveMembers() { return activeMembers; }
    public double getTotalBalance() { return totalBalance; }
    public double getPersonalSavings() { return personalSavings; }
    public int getPendingApprovalsCount() { return pendingApprovalsCount; }
    public String getGroupName() { return groupName; }
    public String getAdminName() { return adminName; }
    public double getMonthlyContributions() { return monthlyContributions; }
    public double getYearlyContributions() { return yearlyContributions; }
    public double getInterestEarned() { return interestEarned; }
    public double getContributionAmount() { return contributionAmount; }
    public int getTotalContributors() { return totalContributors > 0 ? totalContributors : getTotalMembers(); }
    public double getAvailableSavings() { return availableSavings; }
    public double getLoanBalance() { return loanBalance; }
    public double getMyInterestShare() { return myInterestShare; }
    public double getMonthlyTarget() { return monthlyTarget; }
    public double getYearlyTarget() { return yearlyTarget; }
    public int getMonthlyTargetProgress() { return monthlyTargetProgress; }
    public int getYearlyTargetProgress() { return yearlyTargetProgress; }
    public double getUpcomingPayoutAmount() { return upcomingPayoutAmount; }
    public String getNextContributionDate() { return nextContributionDate != null ? nextContributionDate : "--"; }
}
