package com.example.save.data.network;

public class MemberUpdateRequest {
    private String name;
    private String role;
    private Boolean is_active;

    public MemberUpdateRequest(String name, String role, Boolean is_active) {
        this.name = name;
        this.role = role;
        this.is_active = is_active;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public Boolean getIsActive() { return is_active; }
}
