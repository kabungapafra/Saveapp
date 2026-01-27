package com.example.save.data.network;

public class LoginRequest {
    private String phone;
    private String email;
    private String password;
    private String groupName;
    private String loginType; // "admin" or "member"

    public LoginRequest(String phone, String password, String groupName, String loginType) {
        this.phone = phone;
        this.password = password;
        this.groupName = groupName;
        this.loginType = loginType;
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
