package com.example.save.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.save.data.models.Loan;
import com.example.save.data.repository.LoanRepository;

import java.util.ArrayList;
import java.util.List;

public class LoansViewModel extends AndroidViewModel {
    private final LoanRepository repository;

    public LoansViewModel(@NonNull Application application) {
        super(application);
        this.repository = LoanRepository.getInstance(application);
    }

    public LiveData<List<Loan>> getLoans() {
        return repository.getLoans();
    }

    public void approveLoan(String id) {
        repository.approveLoan(id);
    }

    public void rejectLoan(String id) {
        repository.rejectLoan(id);
    }

    public double getTotalOutstanding() {
        return repository.getTotalOutstanding();
    }

    public double getTotalInterestEarned() {
        return repository.getTotalInterestEarned();
    }
}
