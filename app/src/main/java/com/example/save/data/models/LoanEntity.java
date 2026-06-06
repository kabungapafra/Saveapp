package com.example.save.data.models;

import java.util.Date;

public class LoanEntity {
    private String id;
    private String memberId;
    private String memberName;
    private double amount;
    private double interest;
    private String reason;
    private Date dateRequested;
    private String status;
    private Date dueDate;
    private double repaidAmount;
    private int approvalsReceived = 0;
    private int totalAdminsRequired = 0;

    public LoanEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public double getInterest() { return interest; }
    public void setInterest(double interest) { this.interest = interest; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Date getDateRequested() { return dateRequested; }
    public void setDateRequested(Date dateRequested) { this.dateRequested = dateRequested; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    public double getRepaidAmount() { return repaidAmount; }
    public void setRepaidAmount(double repaidAmount) { this.repaidAmount = repaidAmount; }
    public int getApprovalsReceived() { return approvalsReceived; }
    public void setApprovalsReceived(int approvalsReceived) { this.approvalsReceived = approvalsReceived; }
    public int getTotalAdminsRequired() { return totalAdminsRequired; }
    public void setTotalAdminsRequired(int totalAdminsRequired) { this.totalAdminsRequired = totalAdminsRequired; }
}
