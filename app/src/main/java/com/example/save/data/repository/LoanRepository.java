package com.example.save.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.save.data.models.Loan;
import com.example.save.data.models.LoanEntity;
import com.example.save.data.models.PaginatedResponse;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoanRepository {
    private final MutableLiveData<List<Loan>> loansLiveData = new MutableLiveData<>(new ArrayList<>());
    private static LoanRepository instance;
    private final Application application;

    private LoanRepository(Application application) {
        this.application = application;
        fetchLoans();
    }

    public static synchronized LoanRepository getInstance(Application application) {
        if (instance == null) {
            instance = new LoanRepository(application);
        }
        return instance;
    }

    private void fetchLoans() {
        ApiService apiService = RetrofitClient.getClient(application).create(ApiService.class);
        apiService.getLoans(100, 0).enqueue(new Callback<PaginatedResponse<LoanEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<LoanEntity>> call, Response<PaginatedResponse<LoanEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LoanEntity> entities = response.body().getData();
                    List<Loan> loans = new ArrayList<>();
                    if (entities != null) {
                        for (LoanEntity e : entities) {
                            Loan loan = new Loan(e.getId(), e.getMemberId(), e.getMemberName(),
                                    e.getAmount(), e.getInterest(), e.getReason(),
                                    e.getDateRequested(), e.getStatus());
                            loan.setDueDate(e.getDueDate());
                            loan.setRepaidAmount(e.getRepaidAmount());
                            loans.add(loan);
                        }
                    }
                    loansLiveData.postValue(loans);
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<LoanEntity>> call, Throwable t) {
            }
        });
    }

    public LiveData<List<Loan>> getLoans() {
        return loansLiveData;
    }

    public void refresh() {
        fetchLoans();
    }

    public void approveLoan(String id) {
        ApiService apiService = RetrofitClient.getClient(application).create(ApiService.class);
        apiService.approveLoan(id).enqueue(new Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.network.ApiResponse> call, Response<com.example.save.data.network.ApiResponse> response) {
                if (response.isSuccessful()) fetchLoans();
            }
            @Override
            public void onFailure(Call<com.example.save.data.network.ApiResponse> call, Throwable t) {}
        });
    }

    public void rejectLoan(String id) {
        ApiService apiService = RetrofitClient.getClient(application).create(ApiService.class);
        com.example.save.data.network.RejectionRequest req = new com.example.save.data.network.RejectionRequest("Rejected by admin");
        apiService.rejectLoan(id, req).enqueue(new Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.network.ApiResponse> call, Response<com.example.save.data.network.ApiResponse> response) {
                if (response.isSuccessful()) fetchLoans();
            }
            @Override
            public void onFailure(Call<com.example.save.data.network.ApiResponse> call, Throwable t) {}
        });
    }

    public double getTotalOutstanding() {
        List<Loan> loans = loansLiveData.getValue();
        if (loans == null) return 0;
        double total = 0;
        for (Loan l : loans) {
            if (Loan.STATUS_ACTIVE.equals(l.getStatus())) {
                total += l.getTotalDue() - l.getRepaidAmount();
            }
        }
        return total;
    }

    public double getTotalInterestEarned() {
        List<Loan> loans = loansLiveData.getValue();
        if (loans == null) return 0;
        double total = 0;
        for (Loan l : loans) {
            if (Loan.STATUS_PAID.equals(l.getStatus())) {
                total += l.getInterest();
            }
        }
        return total;
    }

    public List<Loan> getLoansSync() {
        return loansLiveData.getValue();
    }

    public void addLoan(Loan loan) {
        List<Loan> current = new ArrayList<>(loansLiveData.getValue() != null ? loansLiveData.getValue() : new ArrayList<>());
        current.add(loan);
        loansLiveData.postValue(current);
    }
}
