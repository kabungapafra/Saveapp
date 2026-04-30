package com.example.save.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.save.data.models.LoanRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * LoanRepository - Fully Static Version
 * No Database, No Network.
 */
public class LoanRepository {
    private final MutableLiveData<List<LoanRequest>> loansLiveData = new MutableLiveData__(new ArrayList<>());
    private static LoanRepository instance;

    private LoanRepository(Application application) {
        // Mock data
        List<LoanRequest> mockLoans = new ArrayList<>();
        loansLiveData.setValue(mockLoans);
    }

    public static synchronized LoanRepository getInstance(Application application) {
        if (instance == null) {
            instance = new LoanRepository(application);
        }
        return instance;
    }

    public LiveData<List<LoanRequest>> getAllLoans() {
        return loansLiveData;
    }

    public void addLoan(LoanRequest loan) {
        List<LoanRequest> current = new ArrayList<>(loansLiveData.getValue());
        current.add(loan);
        loansLiveData.setValue(current);
    }

    public void updateLoan(LoanRequest loan) {
        List<LoanRequest> current = new ArrayList<>(loansLiveData.getValue());
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getId() != null && current.get(i).getId().equals(loan.getId())) {
                current.set(i, loan);
                break;
            }
        }
        loansLiveData.setValue(current);
    }
}
