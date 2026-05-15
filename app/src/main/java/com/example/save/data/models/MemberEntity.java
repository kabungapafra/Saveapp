package com.example.save.data.models;

public class MemberEntity {
    private String id;
    private String name;
    private String role;

    private String phone;
    private String password;
    private String payoutDate;
    private double payoutAmount;
    private boolean hasReceivedPayout;
    private double shortfallAmount;
    private double contributionTarget;
    private double contributionPaid;
    private boolean isFirstLogin;
    private int paymentStreak;
    private String nextPayoutDate;
    private String nextPaymentDueDate;
    private boolean isAutoPayEnabled;
    private double autoPayAmount;
    private int autoPayDay;
    private int creditScore;
    private String joinedDate;
    private double loanBalance;
    private boolean isActive;
    private String status;
    
    @com.google.gson.annotations.SerializedName("reliability_label")
    private String reliabilityLabel;
    
    @com.google.gson.annotations.SerializedName("reliability_color")
    private String reliabilityColor;
    
    @com.google.gson.annotations.SerializedName("is_eligible")
    private boolean isEligible;

    public MemberEntity() {}

    public MemberEntity(String name, String role, String phone) {
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.isActive = true;
        this.status = "PENDING";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPayoutDate() { return payoutDate; }
    public void setPayoutDate(String payoutDate) { this.payoutDate = payoutDate; }
    public double getPayoutAmount() { return payoutAmount; }
    public void setPayoutAmount(double payoutAmount) { this.payoutAmount = payoutAmount; }
    public boolean isHasReceivedPayout() { return hasReceivedPayout; }
    public void setHasReceivedPayout(boolean hasReceivedPayout) { this.hasReceivedPayout = hasReceivedPayout; }
    public double getShortfallAmount() { return shortfallAmount; }
    public void setShortfallAmount(double shortfallAmount) { this.shortfallAmount = shortfallAmount; }
    public double getContributionTarget() { return contributionTarget; }
    public void setContributionTarget(double contributionTarget) { this.contributionTarget = contributionTarget; }
    public double getContributionPaid() { return contributionPaid; }
    public void setContributionPaid(double contributionPaid) { this.contributionPaid = contributionPaid; }
    public boolean isFirstLogin() { return isFirstLogin; }
    public void setFirstLogin(boolean firstLogin) { this.isFirstLogin = firstLogin; }
    public int getPaymentStreak() { return paymentStreak; }
    public void setPaymentStreak(int paymentStreak) { this.paymentStreak = paymentStreak; }
    public String getNextPayoutDate() { return nextPayoutDate; }
    public void setNextPayoutDate(String nextPayoutDate) { this.nextPayoutDate = nextPayoutDate; }
    public String getNextPaymentDueDate() { return nextPaymentDueDate; }
    public void setNextPaymentDueDate(String nextPaymentDueDate) { this.nextPaymentDueDate = nextPaymentDueDate; }
    public boolean isAutoPayEnabled() { return isAutoPayEnabled; }
    public void setAutoPayEnabled(boolean autoPayEnabled) { isAutoPayEnabled = autoPayEnabled; }
    public double getAutoPayAmount() { return autoPayAmount; }
    public void setAutoPayAmount(double autoPayAmount) { this.autoPayAmount = autoPayAmount; }
    public int getAutoPayDay() { return autoPayDay; }
    public void setAutoPayDay(int autoPayDay) { this.autoPayDay = autoPayDay; }
    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }
    public String getJoinedDate() { return joinedDate; }
    public void setJoinedDate(String joinedDate) { this.joinedDate = joinedDate; }
    public double getLoanBalance() { return loanBalance; }
    public void setLoanBalance(double loanBalance) { this.loanBalance = loanBalance; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getReliabilityLabel() { return reliabilityLabel != null ? reliabilityLabel : "ELIGIBLE"; }
    public void setReliabilityLabel(String reliabilityLabel) { this.reliabilityLabel = reliabilityLabel; }
    
    public String getReliabilityColor() { return reliabilityColor != null ? reliabilityColor : "#10B981"; }
    public void setReliabilityColor(String reliabilityColor) { this.reliabilityColor = reliabilityColor; }
    
    public boolean isEligible() { return isEligible; }
    public void setEligible(boolean eligible) { isEligible = eligible; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
