package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    
    @SerializedName("phone")
    private String phone;
    
    @SerializedName("newPassword")
    private String newPassword;


    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return phone; }
    

    public String getNewPassword() { return newPassword; }
}
