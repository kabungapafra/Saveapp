package com.example.save.data.models;

public class TransactionWithApproval {
    public TransactionEntity transaction;
    public int approvalCount;
    public boolean isApprovedByAdmin;

    public TransactionWithApproval(TransactionEntity transaction, boolean isApprovedByAdmin) {
        this.transaction = transaction;
        this.isApprovedByAdmin = isApprovedByAdmin;
        this.approvalCount = isApprovedByAdmin ? 1 : 0;
    }
}
