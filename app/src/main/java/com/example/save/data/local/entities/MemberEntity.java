package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room entity for Member data
 */
@Entity(tableName = "members", indices = { @Index(value = "name"), @Index(value = "email", unique = true) })
public class MemberEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

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
    private double contributionTarget;
    private double contributionPaid;

    public MemberEntity() {
    }

    public MemberEntity(String name, String role, String email, String phone) {
        this.name = name;
        this.role = role;
        this.isActive = true; // Default to true
        this.phone = phone;
        this.email = email;
        this.password = "123456"; // Default password
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
