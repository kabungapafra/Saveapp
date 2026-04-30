package com.example.save.data.models;

import java.util.Date;

public class TransactionEntity {
    private String id;
    private String memberName;
    private String type; // CONTRIBUTION, LOAN_REPAYMENT, etc.
    private double amount;
    private String description;
    private Date date;
    private String status; // PENDING_APPROVAL, APPROVED, REJECTED
    private boolean isPositive; // true for income, false for payout

    public TransactionEntity() {}

    public TransactionEntity(String memberName, String type, double amount, String description, boolean isPositive) {
        this.memberName = memberName;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.isPositive = isPositive;
        this.date = new Date();
        this.status = "APPROVED";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPositive() { return isPositive; }
    public void setPositive(boolean positive) { isPositive = positive; }
}
