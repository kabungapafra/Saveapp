package com.example.save.data.models;

import androidx.room.Embedded;
import com.example.save.data.local.entities.LoanEntity;

public class LoanWithApproval {
    @Embedded
    public LoanEntity loan;

    public int approvalCount;
    public boolean isApprovedByAdmin;
}
