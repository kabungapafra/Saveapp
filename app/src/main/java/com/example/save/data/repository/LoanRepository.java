package com.example.save.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.save.data.models.Loan;
import java.util.ArrayList;
import java.util.List;

/**
 * LoanRepository - Fully Static Version
 * Transitioned to satisfy ViewModel requirements.
 */
public class LoanRepository {
    private final MutableLiveData<List<Loan>> loansLiveData = new MutableLiveData<>(new ArrayList<>());
    private static LoanRepository instance;

    private LoanRepository(Application application) {
        // Mock data
        List<Loan> mockLoans = new ArrayList<>();
        loansLiveData.setValue(mockLoans);
    }

    public static synchronized LoanRepository getInstance(Application application) {
        if (instance == null) {
            instance = new LoanRepository(application);
        }
        return instance;
    }

    public LiveData<List<Loan>> getLoans() {
        return loansLiveData;
    }

    public void approveLoan(String id) {
        // Stub
    }

    public void rejectLoan(String id) {
        // Stub
    }

    public double getTotalOutstanding() {
        return 0;
    }

    public double getTotalInterestEarned() {
        return 0;
    }

    public List<Loan> getLoansSync() {
        return loansLiveData.getValue();
    }

    public void addLoan(Loan loan) {
        List<Loan> current = new ArrayList<>(loansLiveData.getValue());
        current.add(loan);
        loansLiveData.setValue(current);
    }
}
