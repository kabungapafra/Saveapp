package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class SavingsPoolRequest {

    @SerializedName("contrib_per_period")
    private double contribPerPeriod;

    @SerializedName("frequency")
    private String frequency;

    @SerializedName("save_date")
    private String saveDate;

    @SerializedName("receive_date")
    private String receiveDate;

    public SavingsPoolRequest(double contribPerPeriod, String frequency, String saveDate, String receiveDate) {
        this.contribPerPeriod = contribPerPeriod;
        this.frequency = frequency;
        this.saveDate = saveDate;
        this.receiveDate = receiveDate;
    }
}
