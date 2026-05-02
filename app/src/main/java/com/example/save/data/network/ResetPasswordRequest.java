package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("email")
    private String email;
    
    @SerializedName("newPassword")
    private String newPassword;

    public void setEmail(String email) { this.email = email; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public String getEmail() { return email; }
    public String getNewPassword() { return newPassword; }
}
