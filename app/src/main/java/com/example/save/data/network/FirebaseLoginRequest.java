package com.example.save.data.network;

public class FirebaseLoginRequest {
    private String idToken;
    private String groupName;

    public FirebaseLoginRequest(String idToken, String groupName) {
        this.idToken = idToken;
        this.groupName = groupName;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
