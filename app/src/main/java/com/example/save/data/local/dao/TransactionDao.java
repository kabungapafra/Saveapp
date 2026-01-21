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

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<TransactionEntity> getAllTransactionsSync();
}
