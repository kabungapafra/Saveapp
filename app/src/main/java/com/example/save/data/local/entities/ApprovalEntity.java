package com.example.save.data.local.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import java.util.Date;
import androidx.room.Index;

/**
 * Entity to track admin approvals for sensitive actions (Loans, Payouts)
 */
@Entity(tableName = "approvals", indices = { @Index(value = { "type", "targetId", "adminEmail" }, unique = true) })
public class ApprovalEntity {
    @PrimaryKey
    @androidx.annotation.NonNull
    private String id;

    private String type; // "LOAN" or "PAYOUT"
    private String targetId; // ID of the LoanEntity or TransactionEntity
    private String adminEmail; // Email of the admin who approved
    private Date approvalDate;

    public ApprovalEntity(String type, String targetId, String adminEmail, Date approvalDate) {
        this.type = type;
        this.targetId = targetId;
        this.adminEmail = adminEmail;
        this.approvalDate = approvalDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }
}
