package com.example.save.data.models;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

public class Member {
    private String id;
    private String name;
    private String role;
    private boolean isActive;
    private String phone;
    private String email;
    private String payoutDate;
    private String payoutAmount;
    private boolean hasReceivedPayout;
    private double shortfallAmount;
    private boolean isFirstLogin; // Track if member needs to change password
    private String nextPayoutDate;
    private String nextPaymentDueDate;
    private int creditScore;

    private String password;

    public Member(String name, String role, boolean isActive) {
        this(name, role, isActive, "0700000000", "email@example.com");
    }

    public Member(String name, String role, boolean isActive, String phone, String email) {
        this.name = name;
        this.role = role;
        this.isActive = isActive;
        this.phone = phone;
        this.email = email;
        this.password = "123456"; // Default password
        this.payoutDate = "Not Scheduled";
        this.payoutAmount = "0";
        this.hasReceivedPayout = false;
        this.shortfallAmount = 0;
        this.isFirstLogin = true; // New members must change password
        this.nextPayoutDate = "Not Scheduled";
        this.nextPaymentDueDate = "TBD";
        this.contributionTarget = 1000000; // Default target 1M
        this.contributionPaid = 0;
        this.paymentStreak = 0;
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

    public boolean hasReceivedPayout() {
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

    // Payment / Contribution Fields for Installments
    private double contributionTarget;
    private double contributionPaid;

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

    public int getPaymentProgress() {
        if (contributionTarget == 0)
            return 0;
        return (int) ((contributionPaid / contributionTarget) * 100);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        isFirstLogin = firstLogin;
    }

    private int paymentStreak;

    public int getPaymentStreak() {
        return paymentStreak;
    }

    public void setPaymentStreak(int paymentStreak) {
        this.paymentStreak = paymentStreak;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFormattedId() {
        if (id == null)
            return "N/A";
        if (id.length() > 8)
            return "MEM-" + id.substring(0, 8).toUpperCase();
        return "MEM-" + id.toUpperCase();
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

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    // Auto-Pay Fields
    private boolean autoPayEnabled;
    private double autoPayAmount;
    private int autoPayDay;

    public boolean isAutoPayEnabled() {
        return autoPayEnabled;
    }

    public void setAutoPayEnabled(boolean autoPayEnabled) {
        this.autoPayEnabled = autoPayEnabled;
    }

    public double getAutoPayAmount() {
        return autoPayAmount;
    }

    public void setAutoPayAmount(double autoPayAmount) {
        this.autoPayAmount = autoPayAmount;
    }

    public int getAutoPayDay() {
        return autoPayDay;
    }

    public void setAutoPayDay(int autoPayDay) {
        this.autoPayDay = autoPayDay;
    }
}
