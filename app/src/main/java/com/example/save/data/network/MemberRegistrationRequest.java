package com.example.save.data.network;

public class MemberRegistrationRequest {
    private String name;
    private String email;
    private String phone;
    private String otp;
    private String role;

    public MemberRegistrationRequest(String name, String email, String phone, String otp, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.otp = otp;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
