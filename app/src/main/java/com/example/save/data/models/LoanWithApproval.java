package com.example.save.data.models;

public class LoanWithApproval {
    public LoanEntity loan;
    public int approvalCount;
    public boolean isApprovedByAdmin;

    public LoanWithApproval(LoanEntity loan, boolean isApprovedByAdmin) {
        this.loan = loan;
        this.isApprovedByAdmin = isApprovedByAdmin;
        this.approvalCount = isApprovedByAdmin ? 1 : 0;
    }
}
