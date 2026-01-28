package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey
    @androidx.annotation.NonNull
    private String id;

    private String memberName; // New: Associate with user
    private String type; // "CONTRIBUTION", "LOAN_PAYOUT", "LOAN_REPAYMENT"
    private double amount;
    private String description;
    private Date date;
    private boolean isPositive; // For UI display (Green/Red)
    private String paymentMethod; // New: "Phone", "Bank", "Cash"
    private String status; // "COMPLETED", "PENDING_APPROVAL", "REJECTED"

    public TransactionEntity(String memberName, String type, double amount, String description, Date date,
            boolean isPositive, String paymentMethod, String status) {
        this.memberName = memberName;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.isPositive = isPositive;
        this.paymentMethod = paymentMethod;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
