package com.example.save.data.network;

public class GoogleLoginRequest {
    private String idToken;
    private String loginType;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
    public void setLoginType(String loginType) { this.loginType = loginType; }
}
