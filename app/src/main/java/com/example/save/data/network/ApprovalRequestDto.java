package com.example.save.data.network;

public class ApprovalRequestDto {
    private String id;
    private String adminPhone;

    public ApprovalRequestDto(String id, String adminPhone) {
        this.id = id;
        this.adminPhone = adminPhone;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdminPhone() { return adminPhone; }
    public void setAdminPhone(String adminPhone) { this.adminPhone = adminPhone; }
}
