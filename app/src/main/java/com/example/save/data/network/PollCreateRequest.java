package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PollCreateRequest {
    private String role;
    @SerializedName("nominee_member_ids") private List<String> nomineeMemberIds;
    @SerializedName("expires_hours") private int expiresHours;

    public PollCreateRequest(String role, List<String> nomineeMemberIds) {
        this.role = role;
        this.nomineeMemberIds = nomineeMemberIds;
        this.expiresHours = 24;
    }
}
