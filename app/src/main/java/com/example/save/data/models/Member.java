package com.example.save.data.models;
import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

public class Member {
    private String name;
    private String role;
    private boolean isActive;
    private String phone;
    private String email;
    private String payoutDate;
    private String payoutAmount;
    private boolean hasReceivedPayout;
    private double shortfallAmount;

    public Member(String name, String role, boolean isActive) {
        this(name, role, isActive, "0700000000", "email@example.com");
    }

    public Member(String name, String role, boolean isActive, String phone, String email) {
        this.name = name;
        this.role = role;
        this.isActive = isActive;
        this.phone = phone;
        this.email = email;
        this.payoutDate = "Not Scheduled";
        this.payoutAmount = "0";
        this.hasReceivedPayout = false;
        this.shortfallAmount = 0;
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
}
