package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class OtpVerificationRequest {

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

    @SerializedName("idToken")
    private String idToken;

    public OtpVerificationRequest(String phone, String otp, String name, String password, String groupName) {
        this.phone = phone;
        this.otp = otp;
        this.name = name;
        this.password = password;
        this.groupName = groupName;
    }

    public OtpVerificationRequest(String phone, String otp, String name, String password, String groupName, String idToken) {
        this.phone = phone;
        this.otp = otp;
        this.name = name;
        this.password = password;
        this.groupName = groupName;
        this.idToken = idToken;
    }

    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return phone; }

    public void setIdToken(String idToken) { this.idToken = idToken; }
    public String getIdToken() { return idToken; }
}
