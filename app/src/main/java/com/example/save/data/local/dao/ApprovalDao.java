package com.example.save.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.save.data.local.entities.ApprovalEntity;
import java.util.List;

@Dao
public interface ApprovalDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.ABORT)
    void insert(ApprovalEntity approval);

    @Query("SELECT * FROM approvals WHERE type = :type AND targetId = :targetId")
    List<ApprovalEntity> getApprovalsForTarget(String type, String targetId);

    @Query("SELECT COUNT(*) FROM approvals WHERE type = :type AND targetId = :targetId")
    int getApprovalCount(String type, String targetId);

    @Query("SELECT * FROM approvals WHERE type = :type AND targetId = :targetId AND adminEmail = :adminEmail")
    ApprovalEntity getAdminApproval(String type, String targetId, String adminEmail);

    @Query("DELETE FROM approvals WHERE type = :type AND targetId = :targetId")
    void deleteApprovalsForTarget(String type, String targetId);
}
