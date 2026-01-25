package com.example.save.data.models;

import androidx.room.Embedded;
import com.example.save.data.local.entities.TransactionEntity;

public class TransactionWithApproval {
    @Embedded
    public TransactionEntity transaction;

    public int approvalCount;
    public boolean isApprovedByAdmin;
}
