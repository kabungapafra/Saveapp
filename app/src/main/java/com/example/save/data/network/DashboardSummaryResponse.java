package com.example.save.data.network;

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

    // Getters
    public int getTotalMembers() {
        return totalMembers;
    }

    public int getActiveMembers() {
        return activeMembers;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public double getPersonalSavings() {
        return personalSavings;
    }

    public int getPendingApprovalsCount() {
        return pendingApprovalsCount;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getAdminName() {
        return adminName;
    }

    // Setters
    public void setTotalMembers(int totalMembers) {
        this.totalMembers = totalMembers;
    }

    public void setActiveMembers(int activeMembers) {
        this.activeMembers = activeMembers;
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    public void setPersonalSavings(double personalSavings) {
        this.personalSavings = personalSavings;
    }

    public void setPendingApprovalsCount(int pendingApprovalsCount) {
        this.pendingApprovalsCount = pendingApprovalsCount;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }
}
