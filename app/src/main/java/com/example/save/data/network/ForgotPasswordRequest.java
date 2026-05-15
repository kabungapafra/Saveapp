package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequest {
    @SerializedName("phone")
    private String phone;

    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return phone; }
}
