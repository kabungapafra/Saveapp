package com.example.save.data.models;

public class PaymentItem {
    public String title;
    public String amount;
    public String status;
    public String type; // "loan", "contribution", etc.

    public PaymentItem(String title, String amount, String status, String type) {
        this.title = title;
        this.amount = amount;
        this.status = status;
        this.type = type;
    }
}
