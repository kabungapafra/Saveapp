package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.TypeConverters;
import com.example.save.data.local.Converters;

import java.util.Date;

/**
 * Room entity for Loan data
 */
@Entity(tableName = "loans")
@TypeConverters(Converters.class)
public class LoanEntity {

    @PrimaryKey
    @androidx.annotation.NonNull
    private String id;

    private String memberId;
    private String memberName;
    private double amount;
    private double interest;
    private String reason;
    private Date dateRequested;
    private Date dueDate;
    private String status;
    private double repaidAmount;

    public LoanEntity() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getInterest() {
        return interest;
    }

    public void setInterest(double interest) {
        this.interest = interest;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDateRequested() {
        return dateRequested;
    }

    public void setDateRequested(Date dateRequested) {
        this.dateRequested = dateRequested;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(double repaidAmount) {
        this.repaidAmount = repaidAmount;
    }
}
