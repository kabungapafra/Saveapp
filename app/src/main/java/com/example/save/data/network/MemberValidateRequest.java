package com.example.save.data.network;

public class MemberValidateRequest {
    private String phone;
    private String group_name;

    public MemberValidateRequest(String phone, String group_name) {
        this.phone = phone;
        this.group_name = group_name;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGroupName() { return group_name; }
    public void setGroupName(String group_name) { this.group_name = group_name; }
}
