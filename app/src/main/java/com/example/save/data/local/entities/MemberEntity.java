package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity for Member data
 */
@Entity(tableName = "members", indices = { @Index(value = "name"), @Index(value = "email", unique = true) })
public class MemberEntity {

    @PrimaryKey
    @androidx.annotation.NonNull
    private String id;

    private String name;
    private String role;
    private boolean isActive;
    private String phone;
    private String email;
    private String password; // Stores OTP or hashed password
    private String payoutDate;
    private String payoutAmount;
    private boolean hasReceivedPayout;
    private double shortfallAmount;
    private boolean isFirstLogin; // Track if member must change password after first OTP login
    private double contributionTarget;
    private double contributionPaid;
    private int paymentStreak;
    private String nextPayoutDate;
    private String nextPaymentDueDate;
    private boolean isAutoPayEnabled; // New
    private int autoPayDay; // New: Day of month (1-31)
    private double autoPayAmount; // New
    private int creditScore; // New
    private String joinedDate;

    public MemberEntity() {
    }

    @androidx.room.Ignore
    public MemberEntity(String name, String role, String email, String phone) {
        this.name = name;
        this.role = role;
        this.isActive = true; // Default to true
        this.phone = phone;
        this.email = email;
        this.password = "123456"; // Default password
        this.isFirstLogin = true; // New members must change password
        this.nextPayoutDate = "Not Scheduled";
        this.nextPaymentDueDate = "TBD";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPayoutDate() {
        return payoutDate;
    }

    public void setPayoutDate(String payoutDate) {
        this.payoutDate = payoutDate;
    }

    public String getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(String payoutAmount) {
        this.payoutAmount = payoutAmount;
    }

    public boolean isHasReceivedPayout() {
        return hasReceivedPayout;
    }

    public void setHasReceivedPayout(boolean hasReceivedPayout) {
        this.hasReceivedPayout = hasReceivedPayout;
    }

    public double getShortfallAmount() {
        return shortfallAmount;
    }

    public void setShortfallAmount(double shortfallAmount) {
        this.shortfallAmount = shortfallAmount;
    }

    public double getContributionTarget() {
        return contributionTarget;
    }

    public void setContributionTarget(double contributionTarget) {
        this.contributionTarget = contributionTarget;
    }

    public double getContributionPaid() {
        return contributionPaid;
    }

    public void setContributionPaid(double contributionPaid) {
        this.contributionPaid = contributionPaid;
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    public int getPaymentStreak() {
        return paymentStreak;
    }

    public void setPaymentStreak(int paymentStreak) {
        this.paymentStreak = paymentStreak;
    }

    public String getNextPayoutDate() {
        return nextPayoutDate;
    }

    public void setNextPayoutDate(String nextPayoutDate) {
        this.nextPayoutDate = nextPayoutDate;
    }

    public String getNextPaymentDueDate() {
        return nextPaymentDueDate;
    }

    public void setNextPaymentDueDate(String nextPaymentDueDate) {
        this.nextPaymentDueDate = nextPaymentDueDate;
    }

    public boolean isAutoPayEnabled() {
        return isAutoPayEnabled;
    }

    public void setAutoPayEnabled(boolean autoPayEnabled) {
        isAutoPayEnabled = autoPayEnabled;
    }

    public int getAutoPayDay() {
        return autoPayDay;
    }

    public void setAutoPayDay(int autoPayDay) {
        this.autoPayDay = autoPayDay;
    }

    public double getAutoPayAmount() {
        return autoPayAmount;
    }

    public void setAutoPayAmount(double autoPayAmount) {
        this.autoPayAmount = autoPayAmount;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public String getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(String joinedDate) {
        this.joinedDate = joinedDate;
    }
}
