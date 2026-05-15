package com.example.save.data.network;

public class OnboardingCheckRequest {
    private String phone;
    private String groupName;

    public OnboardingCheckRequest(String phone, String groupName) {
        this.phone = phone;
        this.groupName = groupName;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
}
