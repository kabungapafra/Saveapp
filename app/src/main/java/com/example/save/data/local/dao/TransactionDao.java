package com.example.save.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.save.data.local.entities.TransactionEntity;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 20")
    LiveData<List<TransactionEntity>> getRecentTransactions();

    @Query("SELECT * FROM transactions WHERE memberName = :memberName ORDER BY date DESC LIMIT 5")
    LiveData<List<TransactionEntity>> getLatestMemberTransactions(String memberName);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactionsSync();

    @Query("SELECT TOTAL(amount) FROM transactions WHERE isPositive = 1 AND status = 'COMPLETED'")
    double getTotalIncoming();

    @Query("SELECT TOTAL(amount) FROM transactions WHERE isPositive = 0 AND status = 'COMPLETED'")
    double getTotalOutgoing();

    @Query("SELECT TOTAL(CASE WHEN isPositive = 1 THEN amount ELSE -amount END) FROM transactions WHERE status = 'COMPLETED'")
    LiveData<Double> getGroupBalance();

    @Query("SELECT * FROM transactions WHERE status = 'PENDING_APPROVAL' ORDER BY date DESC")
    LiveData<List<TransactionEntity>> getPendingTransactions();

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getTransactionById(long id);

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    void updateStatus(long id, String status);

    // Pagination
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    List<TransactionEntity> getTransactionsPaged(int limit, int offset);
}
