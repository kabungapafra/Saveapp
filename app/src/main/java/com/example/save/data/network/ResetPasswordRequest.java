package com.example.save.data.network;

public class ResetPasswordRequest {
    private String email;
    private String newPassword;
    private String otp;

    public ResetPasswordRequest(String email, String newPassword, String otp) {
        this.email = email;
        this.newPassword = newPassword;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
