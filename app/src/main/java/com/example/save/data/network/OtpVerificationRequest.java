package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class OtpVerificationRequest {
    @SerializedName("email")
    private String email;
    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("otp")
    private String otp;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("groupName")
    private String groupName;

    public OtpVerificationRequest(String email, String otp, String name, String password, String groupName) {
        this.email = email;
        this.otp = otp;
        this.name = name;
        this.password = password;
        this.groupName = groupName;
    }

    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return phone; }
}
