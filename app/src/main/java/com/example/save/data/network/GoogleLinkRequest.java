package com.example.save.data.network;

public class GoogleLinkRequest {
    private String idToken;

    public GoogleLinkRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() { return idToken; }
}
