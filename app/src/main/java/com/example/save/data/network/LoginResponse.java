package com.example.save.data.network;

public class LoginResponse {
    private String token;
    private String name;
    private String email;
    private String role;
    private boolean success;

    public String getToken() { return token; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public boolean isSuccess() { return success; }
}
