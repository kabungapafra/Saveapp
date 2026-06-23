package com.example.save.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Poll {
    private String id;
    @SerializedName("group_name") private String groupName;
    private String role;
    private String title;
    private String status;
    @SerializedName("created_by") private String createdBy;
    @SerializedName("created_by_name") private String createdByName;
    @SerializedName("expires_at") private String expiresAt;
    @SerializedName("created_at") private String createdAt;
    @SerializedName("closed_at") private String closedAt;
    private List<PollNominee> nominees;
    @SerializedName("total_votes") private int totalVotes;
    @SerializedName("has_voted") private boolean hasVoted;

    public String getId() { return id; }
    public String getGroupName() { return groupName; }
    public String getRole() { return role; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getCreatedBy() { return createdBy; }
    public String getCreatedByName() { return createdByName; }
    public String getExpiresAt() { return expiresAt; }
    public String getCreatedAt() { return createdAt; }
    public String getClosedAt() { return closedAt; }
    public List<PollNominee> getNominees() { return nominees; }
    public int getTotalVotes() { return totalVotes; }
    public boolean isHasVoted() { return hasVoted; }
}
