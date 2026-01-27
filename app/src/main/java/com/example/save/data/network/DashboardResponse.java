package com.example.save.data.network;

public class DashboardResponse {
    private double groupBalance;
    private int activeMembers;
    private int pendingLoans;
    private int activeLoans;
    private double totalContributions;
    private double totalPayouts;
    private double totalLoans;

    // Getters and setters
    public double getGroupBalance() {
        return groupBalance;
    }

    public void setGroupBalance(double groupBalance) {
        this.groupBalance = groupBalance;
    }

    public int getActiveMembers() {
        return activeMembers;
    }

    public void setActiveMembers(int activeMembers) {
        this.activeMembers = activeMembers;
    }

    public int getPendingLoans() {
        return pendingLoans;
    }

    public void setPendingLoans(int pendingLoans) {
        this.pendingLoans = pendingLoans;
    }

    public int getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(int activeLoans) {
        this.activeLoans = activeLoans;
    }

    public double getTotalContributions() {
        return totalContributions;
    }

    public void setTotalContributions(double totalContributions) {
        this.totalContributions = totalContributions;
    }

    public double getTotalPayouts() {
        return totalPayouts;
    }

    public void setTotalPayouts(double totalPayouts) {
        this.totalPayouts = totalPayouts;
    }

    public double getTotalLoans() {
        return totalLoans;
    }

    public void setTotalLoans(double totalLoans) {
        this.totalLoans = totalLoans;
    }
}
