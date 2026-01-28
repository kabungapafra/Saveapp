package com.example.save.data.models;

public class LoanRequest {
    private String id;
    private String memberName;
    private double amount;
    private int durationMonths;
    private String guarantor;
    private String guarantorPhone;
    private String reason;
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private String requestDate;
    private double interestRate;
    private double totalRepayment;

    public LoanRequest(String memberName, double amount, int durationMonths, String guarantor, String guarantorPhone,
            String reason) {
        this.memberName = memberName;
        this.amount = amount;
        this.durationMonths = durationMonths;
        this.guarantor = guarantor;
        this.guarantorPhone = guarantorPhone;
        this.reason = reason;
        this.status = "PENDING";
        this.requestDate = java.text.DateFormat.getDateInstance().format(new java.util.Date());
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public String getGuarantor() {
        return guarantor;
    }

    public void setGuarantor(String guarantor) {
        this.guarantor = guarantor;
    }

    public String getGuarantorPhone() {
        return guarantorPhone;
    }

    public void setGuarantorPhone(String guarantorPhone) {
        this.guarantorPhone = guarantorPhone;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getTotalRepayment() {
        return totalRepayment;
    }

    public void setTotalRepayment(double totalRepayment) {
        this.totalRepayment = totalRepayment;
    }
}
