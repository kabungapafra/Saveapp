package com.example.save.data.network;

public class MemberCompleteOnboardingRequest {
    private String phone;
    private String group_name;
    private String new_pin;

    public MemberCompleteOnboardingRequest(String phone, String group_name, String new_pin) {
        this.phone = phone;
        this.group_name = group_name;
        this.new_pin = new_pin;
    }
}
