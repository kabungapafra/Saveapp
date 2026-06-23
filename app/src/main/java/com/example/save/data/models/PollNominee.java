package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;

public class PollNominee {
    private String id;
    @SerializedName("member_id") private String memberId;
    @SerializedName("member_name") private String memberName;
    @SerializedName("vote_count") private int voteCount;

    public String getId() { return id; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public int getVoteCount() { return voteCount; }
}
