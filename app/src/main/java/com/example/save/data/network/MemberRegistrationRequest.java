package com.example.save.data.network;

public class MemberRegistrationRequest {
    private String name;
    private String email;
    private String phone;
    private String role;
    private String password;
    private String otp;

    public MemberRegistrationRequest(String name, String email, String phone, String role, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.password = password;
        this.otp = ""; // Scrapped
    }
}
