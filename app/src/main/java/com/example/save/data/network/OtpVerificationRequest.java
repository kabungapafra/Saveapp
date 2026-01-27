package com.example.save.data.network;

public class OtpVerificationRequest {
    private String email;
    private String otp;
    private String name;
    private String phone;
    private String password;
    private String groupName;

    // For admin signup
    public OtpVerificationRequest(String name, String phone, String email, String password, String otp, String groupName) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.otp = otp;
        this.groupName = groupName;
    }

    // For password reset
    public OtpVerificationRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
}
