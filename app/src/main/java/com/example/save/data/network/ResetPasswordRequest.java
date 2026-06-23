package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    @SerializedName("phone")
    private String phone;

    @SerializedName("newPassword")
    private String newPassword;

    @SerializedName("idToken")
    private String idToken;

    public void setPhone(String phone) { this.phone = phone; }
    public String getPhone() { return phone; }

    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getNewPassword() { return newPassword; }

    public void setIdToken(String idToken) { this.idToken = idToken; }
    public String getIdToken() { return idToken; }
}
