package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String type; // "CONTRIBUTION", "LOAN_PAYOUT", "LOAN_REPAYMENT"
    private double amount;
    private String description;
    private Date date;
    private boolean isPositive; // For UI display (Green/Red)

    public TransactionEntity(String type, double amount, String description, Date date, boolean isPositive) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.isPositive = isPositive;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
}
