package com.example.save.data.network;

public class LoginResponse {
    private String token;
    private String name;
    private String role;
    private boolean success;
    private boolean is_creator;
    private String group_name;
    private String phone;

    public String getToken() { return token; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public boolean isSuccess() { return success; }
    public boolean isCreator() { return is_creator; }
    public String getGroupName() { return group_name; }
    public String getPhone() { return phone; }
}
