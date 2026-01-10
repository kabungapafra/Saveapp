package com.example.save.data.repository;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.data.models.Loan;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LoanRepository {
    private static LoanRepository instance;
    private List<Loan> loans;
    private MutableLiveData<List<Loan>> loansLiveData;

    private LoanRepository() {
        loans = new ArrayList<>();
        loansLiveData = new MutableLiveData<>();
        generateMockData();
        updateLiveData();
    }

    public static synchronized LoanRepository getInstance() {
        if (instance == null) {
            instance = new LoanRepository();
        }
        return instance;
    }

    public LiveData<List<Loan>> getLoans() {
        return loansLiveData;
    }

    private void generateMockData() {
        Calendar cal = Calendar.getInstance();

        // Active Loan
        cal.add(Calendar.DAY_OF_YEAR, -10);
        Loan l1 = new Loan(UUID.randomUUID().toString(), "m1", "John Doe", 200000, 20000, "Medical Emergency",
                cal.getTime(), Loan.STATUS_ACTIVE);
        cal.add(Calendar.DAY_OF_YEAR, 30); // Due in 20 days
        l1.setDueDate(cal.getTime());
        l1.setRepaidAmount(50000);
        loans.add(l1);

        // Pending Request
        cal = Calendar.getInstance();
        Loan l2 = new Loan(UUID.randomUUID().toString(), "m2", "Jane Smith", 500000, 50000, "School Fees",
                cal.getTime(), Loan.STATUS_PENDING);
        loans.add(l2);

        // Another Active Loan
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);
        Loan l3 = new Loan(UUID.randomUUID().toString(), "m3", "Peter Parker", 100000, 10000, "Business Stock",
                cal.getTime(), Loan.STATUS_ACTIVE);
        cal.add(Calendar.DAY_OF_YEAR, 25);
        l3.setDueDate(cal.getTime());
        l3.setRepaidAmount(80000); // 72% paid
        loans.add(l3);

        // Another Pending
        cal = Calendar.getInstance();
        Loan l4 = new Loan(UUID.randomUUID().toString(), "m4", "Clark Kent", 1000000, 100000, "Farm Equipment",
                cal.getTime(), Loan.STATUS_PENDING);
        loans.add(l4);
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }

    public List<Loan> getPendingLoans() {
        List<Loan> pending = new ArrayList<>();
        for (Loan l : loans) {
            if (Loan.STATUS_PENDING.equals(l.getStatus())) {
                pending.add(l);
            }
        }
        return pending;
    }

    public List<Loan> getActiveLoans() {
        List<Loan> active = new ArrayList<>();
        for (Loan l : loans) {
            if (Loan.STATUS_ACTIVE.equals(l.getStatus())) {
                active.add(l);
            }
        }
        return active;
    }

    public void approveLoan(String id) {
        for (Loan l : loans) {
            if (l.getId().equals(id)) {
                l.setStatus(Loan.STATUS_ACTIVE);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_YEAR, 30); // Default 30 day term
                l.setDueDate(cal.getTime());
                updateLiveData();
                break;
            }
        }
    }

    public void rejectLoan(String id) {
        for (Loan l : loans) {
            if (l.getId().equals(id)) {
                l.setStatus(Loan.STATUS_REJECTED); // Or remove
                // loans.remove(l); // Alternatively remove
                updateLiveData();
                break;
            }
        }
    }

    public double getTotalOutstanding() {
        double total = 0;
        for (Loan l : loans) {
            if (Loan.STATUS_ACTIVE.equals(l.getStatus())) {
                total += (l.getTotalDue() - l.getRepaidAmount());
            }
        }
        return total;
    }

    public double getTotalInterestEarned() {
        // Simplified: Interest on active + paid loans
        double total = 0;
        for (Loan l : loans) {
            if (Loan.STATUS_ACTIVE.equals(l.getStatus()) || Loan.STATUS_PAID.equals(l.getStatus())) {
                total += l.getInterest();
            }
        }
        return total;
    }

    private void updateLiveData() {
        loansLiveData.setValue(new ArrayList<>(loans));
    }
}
