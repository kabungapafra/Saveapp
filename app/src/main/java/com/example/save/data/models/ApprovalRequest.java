package com.example.save.data.models;

import com.example.save.ui.adapters.ApprovalsAdapter;
import java.util.Date;

public class ApprovalRequest implements ApprovalsAdapter.ApprovalItem {
    private String id;
    private String type; // "LOAN" or "PAYOUT"
    private String title;
    private double amount;
    private String description;
    private Date date;
    private String status;
    private boolean approvedByMe;

    public ApprovalRequest(String id, String type, String title, double amount, String description, Date date, String status, boolean approvedByMe) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.status = status;
        this.approvedByMe = approvedByMe;
    }

    @Override public String getId() { return id; }
    @Override public String getType() { return type; }
    @Override public String getTitle() { return title; }
    @Override public double getAmount() { return amount; }
    @Override public String getDescription() { return description; }
    @Override public Date getDate() { return date; }
    @Override public String getStatus() { return status; }
    @Override public boolean hasApproved() { return approvedByMe; }

    public void setApprovedByMe(boolean approvedByMe) { this.approvedByMe = approvedByMe; }
}
