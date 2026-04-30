package com.example.save.data.network;

public class MemberRegistrationResponse {
    private boolean success;
    private String message;
    private String otp;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getOtp() { return otp; }
}
