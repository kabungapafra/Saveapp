package com.example.save.data.network;

public class OnboardingSetPasswordRequest {
    private String phone;
    private String password;
    private String idToken;

    public OnboardingSetPasswordRequest(String phone, String password, String idToken) {
        this.phone = phone;
        this.password = password;
        this.idToken = idToken;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }
}
