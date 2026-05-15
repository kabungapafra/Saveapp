package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class OtpRequest {
    @SerializedName("phone")
    private String phone;

    public OtpRequest(String phone) {
        this.phone = phone;
    }

    public String getPhone() { return phone; }
}
