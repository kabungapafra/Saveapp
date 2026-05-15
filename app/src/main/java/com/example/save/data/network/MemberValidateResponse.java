package com.example.save.data.network;

public class MemberValidateResponse {
    private boolean success;
    private String member_name;
    private boolean needs_onboarding;

    public boolean isSuccess() { return success; }
    public String getMemberName() { return member_name; }
    public boolean isNeedsOnboarding() { return needs_onboarding; }
}
