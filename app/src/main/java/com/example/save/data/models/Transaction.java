package com.example.save.data.models;

public class Transaction {
    private String type;
    private String date;
    private double amount;
    private boolean isCredit; // true for deposits/income, false for debits/expenses
    private int iconRes;
    private int iconBackgroundColor;

    public Transaction(String type, String date, double amount, boolean isCredit, int iconRes,
            int iconBackgroundColor) {
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.isCredit = isCredit;
        this.iconRes = iconRes;
        this.iconBackgroundColor = iconBackgroundColor;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isCredit() {
        return isCredit;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getIconBackgroundColor() {
        return iconBackgroundColor;
    }
}
