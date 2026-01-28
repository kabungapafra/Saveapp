package com.example.save.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.save.data.local.entities.LoanEntity;

import java.util.List;

/**
 * Data Access Object for Loan operations
 */
@Dao
public interface LoanDao {

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(LoanEntity loan);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insertAll(List<LoanEntity> loans);

        @Update
        void update(LoanEntity loan);

        @Delete
        void delete(LoanEntity loan);

        @Query("SELECT * FROM loans ORDER BY id DESC")
        LiveData<List<LoanEntity>> getAllLoans();

        @Query("SELECT * FROM loans ORDER BY id DESC")
        List<LoanEntity> getAllLoansSync();

        @Query("SELECT * FROM loans WHERE id = :loanId")
        LoanEntity getLoanByIdSync(String loanId);

        @Query("UPDATE loans SET status = :status WHERE id = :id")
        void updateStatus(String id, String status);

        @Query("SELECT * FROM loans WHERE status = 'PENDING' ORDER BY id DESC")
        List<LoanEntity> getPendingLoans();

        @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY id DESC")
        List<LoanEntity> getActiveLoans();

        @Query("SELECT * FROM loans WHERE memberName = :memberName AND status = 'ACTIVE' LIMIT 1")
        LoanEntity getActiveLoanByMemberName(String memberName);

        @Query("SELECT SUM(amount + interest - repaidAmount) FROM loans WHERE status = 'ACTIVE'")
        double getTotalOutstanding();

        @Query("SELECT SUM(interest) FROM loans WHERE status IN ('ACTIVE', 'PAID')")
        double getTotalInterestEarned();

        @Query("DELETE FROM loans")
        void deleteAll();

        @Query("SELECT l.*, " +
                        "(SELECT COUNT(*) FROM approvals a WHERE a.targetId = l.id AND a.type = 'LOAN') as approvalCount, "
                        +
                        "0 as isApprovedByAdmin " +
                        "FROM loans l " +
                        "WHERE l.memberName = :memberName " +
                        "ORDER BY l.id DESC")
        LiveData<List<com.example.save.data.models.LoanWithApproval>> getMemberLoansWithApproval(String memberName);

        @Query("SELECT l.*, " +
                        "(SELECT COUNT(*) FROM approvals a WHERE a.targetId = l.id AND a.type = 'LOAN') as approvalCount, "
                        +
                        "(SELECT COUNT(*) > 0 FROM approvals a WHERE a.targetId = l.id AND a.type = 'LOAN' AND a.adminEmail = :adminEmail) as isApprovedByAdmin "
                        +
                        "FROM loans l " +
                        "WHERE l.status = 'PENDING' " +
                        "ORDER BY l.id DESC")
        LiveData<List<com.example.save.data.models.LoanWithApproval>> getPendingLoansWithApproval(String adminEmail);
}
