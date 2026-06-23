package com.example.save.data.network;

import com.google.gson.annotations.SerializedName;

public class CastVoteRequestBody {
    @SerializedName("nominee_id") private String nomineeId;

    public CastVoteRequestBody(String nomineeId) {
        this.nomineeId = nomineeId;
    }
}
