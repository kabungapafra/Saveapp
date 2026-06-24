package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class SavingsPoolRequest {

    @SerializedName("total_amount")
    private double totalAmount;

    @SerializedName("save_date")
    private String saveDate;

    @SerializedName("receive_date")
    private String receiveDate;

    public SavingsPoolRequest(double totalAmount, String saveDate, String receiveDate) {
        this.totalAmount = totalAmount;
        this.saveDate = saveDate;
        this.receiveDate = receiveDate;
    }
}
