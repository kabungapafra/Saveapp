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
    void insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT 20")
    LiveData<List<TransactionEntity>> getRecentTransactions();

    @Query("SELECT * FROM transactions WHERE memberName = :memberName ORDER BY date DESC LIMIT 5")
    LiveData<List<TransactionEntity>> getLatestMemberTransactions(String memberName);

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactionsSync();

    @Query("SELECT TOTAL(amount) FROM transactions WHERE isPositive = 1")
    double getTotalIncoming();

    @Query("SELECT TOTAL(amount) FROM transactions WHERE isPositive = 0")
    double getTotalOutgoing();

    @Query("SELECT TOTAL(CASE WHEN isPositive = 1 THEN amount ELSE -amount END) FROM transactions")
    LiveData<Double> getGroupBalance();

    // Pagination
    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit OFFSET :offset")
    List<TransactionEntity> getTransactionsPaged(int limit, int offset);
}
