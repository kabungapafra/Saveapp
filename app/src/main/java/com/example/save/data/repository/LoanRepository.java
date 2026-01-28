package com.example.save.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.save.data.local.AppDatabase;
import com.example.save.data.local.dao.LoanDao;
import com.example.save.data.local.entities.LoanEntity;
import com.example.save.data.models.Loan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoanRepository {
    private static LoanRepository instance;
    private final LoanDao loanDao;
    private final Executor executor;
    private LiveData<List<Loan>> loansLiveData;

    private LoanRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        loanDao = database.loanDao();
        executor = Executors.newSingleThreadExecutor();

        // Transform entity LiveData to model LiveData
        LiveData<List<LoanEntity>> entityLiveData = loanDao.getAllLoans();
        loansLiveData = Transformations.map(entityLiveData, this::convertEntitiesToModels);
    }

    public static synchronized LoanRepository getInstance(Context context) {
        if (instance == null) {
            instance = new LoanRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Keep singleton method for backward compatibility
    public static synchronized LoanRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LoanRepository not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }

    public LiveData<List<Loan>> getLoans() {
        return loansLiveData;
    }

    public List<Loan> getAllLoans() {
        List<LoanEntity> entities = loanDao.getAllLoansSync();
        return convertEntitiesToModels(entities);
    }

    public List<Loan> getLoansSync() {
        return getAllLoans();
    }

    public List<Loan> getPendingLoans() {
        List<LoanEntity> entities = loanDao.getPendingLoans();
        return convertEntitiesToModels(entities);
    }

    public List<Loan> getActiveLoans() {
        List<LoanEntity> entities = loanDao.getActiveLoans();
        return convertEntitiesToModels(entities);
    }

    public void approveLoan(String id) {
        executor.execute(() -> {
            LoanEntity entity = loanDao.getLoanByIdSync(id);
            if (entity != null) {
                entity.setStatus(Loan.STATUS_ACTIVE);
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, 30); // Default 30 day term
                entity.setDueDate(cal.getTime());
                loanDao.update(entity);
            }
        });
    }

    public void rejectLoan(String id) {
        executor.execute(() -> {
            LoanEntity entity = loanDao.getLoanByIdSync(id);
            if (entity != null) {
                entity.setStatus(Loan.STATUS_REJECTED);
                loanDao.update(entity);
            }
        });
    }

    public double getTotalOutstanding() {
        Double total = loanDao.getTotalOutstanding();
        return total != null ? total : 0.0;
    }

    public double getTotalInterestEarned() {
        Double total = loanDao.getTotalInterestEarned();
        return total != null ? total : 0.0;
    }

    // Conversion Methods
    private List<Loan> convertEntitiesToModels(List<LoanEntity> entities) {
        List<Loan> loans = new ArrayList<>();
        for (LoanEntity entity : entities) {
            loans.add(convertEntityToModel(entity));
        }
        return loans;
    }

    private Loan convertEntityToModel(LoanEntity entity) {
        Loan loan = new Loan(
                entity.getId(),
                entity.getMemberId(),
                entity.getMemberName(),
                entity.getAmount(),
                entity.getInterest(),
                entity.getReason(),
                entity.getDateRequested(),
                entity.getStatus());
        loan.setDueDate(entity.getDueDate());
        loan.setRepaidAmount(entity.getRepaidAmount());
        return loan;
    }
}
