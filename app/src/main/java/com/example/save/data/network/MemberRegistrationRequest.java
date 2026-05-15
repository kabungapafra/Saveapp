package com.example.save.data.network;

public class MemberRegistrationRequest {
    private String name;
    private String phone;
    private String role;
    private String password;
    private String otp;

    public MemberRegistrationRequest(String name, String phone, String role, String password) {
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.password = password;
        this.otp = ""; // Scrapped
    }
}
