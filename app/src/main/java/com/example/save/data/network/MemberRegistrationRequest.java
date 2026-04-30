package com.example.save.data.network;

public class MemberRegistrationRequest {
    private String name;
    private String email;
    private String phone;
    private String role;

    public MemberRegistrationRequest(String name, String email, String phone, String role) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }
}
