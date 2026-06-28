package com.example.save.data.models;

public class MemberEntity {
    private String id;
    private String name;
    private String role;

    private String phone;
    private String password;
    @com.google.gson.annotations.SerializedName("contribution_target")
    private double contributionTarget;
    @com.google.gson.annotations.SerializedName("contribution_paid")
    private double contributionPaid;
    @com.google.gson.annotations.SerializedName("is_first_login")
    private boolean isFirstLogin;
    @com.google.gson.annotations.SerializedName("payment_streak")
    private int paymentStreak;
    @com.google.gson.annotations.SerializedName("next_payout_date")
    private String nextPayoutDate;
    @com.google.gson.annotations.SerializedName("next_payment_due_date")
    private String nextPaymentDueDate;
    @com.google.gson.annotations.SerializedName("is_auto_pay_enabled")
    private boolean isAutoPayEnabled;
    @com.google.gson.annotations.SerializedName("auto_pay_amount")
    private double autoPayAmount;
    @com.google.gson.annotations.SerializedName("auto_pay_day")
    private int autoPayDay;
    @com.google.gson.annotations.SerializedName("credit_score")
    private int creditScore;
    @com.google.gson.annotations.SerializedName("created_at")
    private String joinedDate;
    @com.google.gson.annotations.SerializedName("loan_balance")
    private double loanBalance;
    @com.google.gson.annotations.SerializedName("is_active")
    private boolean isActive;
    private String status;
    @com.google.gson.annotations.SerializedName("has_received_payout")
    private boolean hasReceivedPayout;
    @com.google.gson.annotations.SerializedName("shortfall_amount")
    private double shortfallAmount;
    @com.google.gson.annotations.SerializedName("payout_date")
    private String payoutDate;
    @com.google.gson.annotations.SerializedName("payout_amount")
    private double payoutAmount;
    
    @com.google.gson.annotations.SerializedName("reliability_label")
    private String reliabilityLabel;
    
    @com.google.gson.annotations.SerializedName("reliability_color")
    private String reliabilityColor;
    
    @com.google.gson.annotations.SerializedName("is_eligible")
    private boolean isEligible;

    @com.google.gson.annotations.SerializedName("profile_image")
    private String profileImage;

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
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
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
