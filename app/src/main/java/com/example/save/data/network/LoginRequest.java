package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String email;
    private String password;
    private String loginType;
    private String groupName;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void setLoginType(String type) { this.loginType = type; }
    public void setGroupName(String name) { this.groupName = name; }
}
