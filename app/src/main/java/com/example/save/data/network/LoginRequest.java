package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String phone;
    private String password;
    private String loginType;
    private String groupName;

    public LoginRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }

    public void setPhone(String phone) { this.phone = phone; }

    public void setLoginType(String type) { this.loginType = type; }
    public void setGroupName(String name) { this.groupName = name; }
}
