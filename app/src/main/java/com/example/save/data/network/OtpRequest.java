package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class OtpRequest {
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;

    public OtpRequest(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public String getEmail() { return email; }
    public String getPhone() { return phone; }
}
