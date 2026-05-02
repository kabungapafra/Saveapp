package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequest {
    @SerializedName("email")
    private String email;

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return email; }
}
