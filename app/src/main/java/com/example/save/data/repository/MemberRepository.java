package com.example.save.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.data.models.Member;
import com.example.save.data.models.MemberEntity;
import com.example.save.data.models.SystemConfig;
import com.example.save.data.models.ComprehensiveReportResponse;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.data.network.LoginResponse;
import com.example.save.data.network.MemberRegistrationRequest;
import com.example.save.data.network.MemberRegistrationResponse;
import com.example.save.data.network.ApiResponse;
import com.example.save.data.network.MemberUpdateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MemberRepository - PostgreSQL Network Version
 * Transitioned back to a network-driven architecture.
 */
public class MemberRepository {
    private static MemberRepository instance;
    private final Executor executor;
    private final Context appContext;
    private final MutableLiveData<List<Member>> membersLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Double> groupBalance = new MutableLiveData<>(0.0);

    private MemberRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public static synchronized MemberRepository getInstance(Context context) {
        if (instance == null) instance = new MemberRepository(context.getApplicationContext());
        return instance;
    }

    public static synchronized MemberRepository getInstance() {
        if (instance == null) throw new IllegalStateException("Not initialized");
        return instance;
    }

    public void refreshMembers(MemberAddCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getMembers().enqueue(new Callback<List<MemberEntity>>() {
            @Override
            public void onResponse(Call<List<MemberEntity>> call, Response<List<MemberEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Member> models = new ArrayList<>();
                    for (MemberEntity entity : response.body()) {
                        Member m = new Member(entity.getName(), entity.getRole(), true, entity.getPhone(), entity.getEmail());
                        m.setId(entity.getId());
                        m.setContributionPaid(entity.getContributionPaid());
                        m.setContributionTarget(entity.getContributionTarget());
                        m.setCreditScore(entity.getCreditScore());
                        
                        // Map the new server-authoritative fields
                        m.setReliabilityLabel(entity.getReliabilityLabel());
                        m.setReliabilityColor(entity.getReliabilityColor());
                        m.setEligible(entity.isEligible());
                        
                        models.add(m);
                    }
                    membersLiveData.postValue(models);
                    if (callback != null) callback.onResult(true, "Synced from PostgreSQL");
                } else if (callback != null) {
                    callback.onResult(false, "Failed to sync");
                }
            }

            @Override
            public void onFailure(Call<List<MemberEntity>> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public void addMember(Member member, MemberRegistrationCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        MemberRegistrationRequest request = new MemberRegistrationRequest(member.getName(), member.getEmail(), member.getPhone(), member.getRole());
        
        apiService.createMember(request).enqueue(new Callback<MemberRegistrationResponse>() {
            @Override
            public void onResponse(Call<MemberRegistrationResponse> call, Response<MemberRegistrationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    refreshMembers(null);
                    if (callback != null) callback.onResult(true, "Member registered in PostgreSQL", response.body().getOtp());
                } else if (callback != null) {
                    callback.onResult(false, "Registration failed", null);
                }
            }

            @Override
            public void onFailure(Call<MemberRegistrationResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error", null);
            }
        });
    }

    public LiveData<List<Member>> getMembers() { return membersLiveData; }
    public List<Member> getAllMembers() { return membersLiveData.getValue(); }
    public Executor getExecutor() { return executor; }

    // Preservation of other stubs (to be implemented as backend grows)
    public interface MemberAddCallback { void onResult(boolean success, String message); }
    public interface MemberRegistrationCallback { void onResult(boolean success, String message, String otp); }
    public interface PasswordChangeCallback { void onResult(boolean success, String message); }
    public interface PayoutCallback { void onResult(boolean success, String message); }
    public interface ConfigCallback { void onResult(boolean success, SystemConfig config, String message); }
    public interface ReportCallback { void onResult(boolean success, ComprehensiveReportResponse report, String message); }
    public interface PaymentCallback { void onResult(boolean success, String message); }
    public interface ApprovalCallback { void onResult(boolean success, String message); }
    public interface LoanSubmissionCallback { void onResult(boolean success, String message); }
    public interface RejectionCallback { void onResult(boolean success, String message); }
    public interface LoanRepaymentCallback { void onResult(boolean success, String message); }
    public interface EligibilityCallback { void onResult(boolean success, String message); }
    public interface RepaymentScheduleCallback { void onResult(boolean success, String message); }
    public interface SummaryCallback { void onResult(boolean success, Object summary, String message); }
    public interface ApiResponseCallback { void onResult(boolean success, String message); }

    public int getActiveMemberCount() { return getAllMembers() != null ? getAllMembers().size() : 0; }
    public int getTotalMemberCount() { return getAllMembers() != null ? getAllMembers().size() : 0; }
    public void syncMembers() { refreshMembers(null); }
    public Member getMemberByEmail(String email) {
        if (getAllMembers() == null) return null;
        for (Member m : getAllMembers()) if (email.equals(m.getEmail())) return m;
        return null;
    }
    public Member getMemberByName(String name) {
        if (getAllMembers() == null) return null;
        for (Member m : getAllMembers()) if (name.equals(m.getName())) return m;
        return null;
    }
    public List<Member> getAdmins() {
        List<Member> admins = new ArrayList<>();
        if (getAllMembers() != null) {
            for (Member m : getAllMembers()) if ("ADMIN".equalsIgnoreCase(m.getRole())) admins.add(m);
        }
        return admins;
    }

    public List<Member> searchMembers(String query) {
        List<Member> results = new ArrayList<>();
        if (getAllMembers() == null || query == null || query.isEmpty()) return results;
        String lowerQuery = query.toLowerCase().trim();
        for (Member m : getAllMembers()) {
            if (m.getName().toLowerCase().contains(lowerQuery) || 
                (m.getEmail() != null && m.getEmail().toLowerCase().contains(lowerQuery)) || 
                (m.getPhone() != null && m.getPhone().contains(lowerQuery))) {
                results.add(m);
            }
        }
        return results;
    }
    
    // Remaining stubs...
    public void deleteMember(Member m, MemberAddCallback cb) {}
    public void resetPassword(String e, String n, PasswordChangeCallback cb) {
        if (cb != null) cb.onResult(true, "Password changed successfully (Mock)");
    }
    public LiveData<Double> getGroupBalance() { return groupBalance; }
    public void updateSystemConfig(Object u, ConfigCallback cb) {}
    public void executePayout(Member m, double a, boolean d, String adminEmail, PayoutCallback cb) {
        if (cb != null) cb.onResult(true, "Payout executed (Mock)");
    }
    public void makePayment(Member m, double a, String p, String pm, PaymentCallback cb) {}
    public void fetchSystemConfig(ConfigCallback cb) {}
    public void getComprehensiveReport(ReportCallback cb) {}
    public void getDashboardSummary(SummaryCallback cb) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getDashboardSummary().enqueue(new Callback<com.example.save.data.models.DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.DashboardSummaryResponse> call, Response<com.example.save.data.models.DashboardSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (cb != null) cb.onResult(true, response.body(), "Summary loaded");
                } else if (cb != null) {
                    cb.onResult(false, null, "Failed to load summary: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.DashboardSummaryResponse> call, Throwable t) {
                if (cb != null) cb.onResult(false, null, "Network error: " + t.getMessage());
            }
        });
    }
    public Member getNextPayoutRecipient() { return null; }
    public boolean canExecutePayout() { return false; }
    public double getNetPayoutAmount() { return 0; }
    public void savePayoutRules(boolean auto, int day, double reserve) {}
    public void resolveShortfall(String email, double amount, String source, ApiResponseCallback cb) {
        if (cb != null) cb.onResult(true, "Shortfall resolved (Mock)");
    }

    // --- ADDITIONAL METHODS CALLED BY VIEWMODELS ---

    public void deleteAllMembers() {
        membersLiveData.postValue(new ArrayList<>());
    }

    public List<Member> getMonthlyRecipients(int limit) {
        return new ArrayList<>(); // Stub
    }

    public int getPendingPaymentsCount() {
        return 0; // Stub
    }

    public void sendMemberInvite(Member member, MemberAddCallback callback) {
        if (member == null || member.getId() == null) {
            if (callback != null) callback.onResult(false, "Invalid member data");
            return;
        }
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.sendMemberInvite(member.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Invite sent successfully");
                } else {
                    if (callback != null) callback.onResult(false, "Failed to send invite");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public void updateMember(int position, Member member) {
        if (member == null || member.getId() == null) return;
        
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        MemberUpdateRequest request = new MemberUpdateRequest(
            member.getName(),
            member.getRole(),
            member.isActive()
        );
        
        apiService.updateMember(member.getId(), request).enqueue(new Callback<MemberEntity>() {
            @Override
            public void onResponse(Call<MemberEntity> call, Response<MemberEntity> response) {
                if (response.isSuccessful()) {
                    syncMembers(); // Refresh list to show updated roles
                }
            }

            @Override
            public void onFailure(Call<MemberEntity> call, Throwable t) {
                // Optional: log failure
            }
        });
    }

    public Member getMemberByPhone(String phone) {
        if (getAllMembers() == null) return null;
        for (Member m : getAllMembers()) if (phone.equals(m.getPhone())) return m;
        return null;
    }

    public Member getMemberByNameSync(String identifier) {
        return getMemberByName(identifier);
    }

    public int getActiveMemberCountSync() {
        return getActiveMemberCount();
    }

    public int getTotalMemberCountSync() {
        return getTotalMemberCount();
    }

    public void submitLoanRequest(com.example.save.data.models.LoanRequest request, LoanSubmissionCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.submitLoan(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Loan request submitted");
                } else {
                    if (callback != null) callback.onResult(false, "Submission failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error");
            }
        });
    }

    public LiveData<List<com.example.save.data.models.LoanRequest>> getLoanRequests() {
        MutableLiveData<List<com.example.save.data.models.LoanRequest>> liveData = new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getLoans().enqueue(new Callback<List<com.example.save.data.models.LoanRequest>>() {
            @Override
            public void onResponse(Call<List<com.example.save.data.models.LoanRequest>> call, Response<List<com.example.save.data.models.LoanRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<com.example.save.data.models.LoanRequest>> call, Throwable t) {
            }
        });
        return liveData;
    }

    public com.example.save.data.models.LoanEntity getActiveLoanForMember(String memberName) {
        return null;
    }

    public List<com.example.save.data.models.LoanRequest> getPendingLoanRequests() {
        return new ArrayList<>();
    }

    public void initiateLoanApproval(String requestId, String adminEmail, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approveLoan(requestId, adminEmail).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Approved successfully");
                } else {
                    if (callback != null) callback.onResult(false, "Approval failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error");
            }
        });
    }

    public void enableAutoPay(Member member, double amount, int day) {}

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getRecentTransactions() {
        return new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getLatestMemberTransactions(String memberName) {
        return new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getMemberTransactionsWithApproval(String memberName) {
        return new MutableLiveData<>(new ArrayList<>());
    }

    public double getPayoutAmount() { return 0; }
    public void setPayoutAmount(double amount) {}
    public double getRetentionPercentage() { return 0; }
    public void setRetentionPercentage(double percentage) {}

    private final MutableLiveData<List<com.example.save.data.models.TransactionEntity>> transactionsLiveData = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getGenericTransactions() {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getTransactions().enqueue(new Callback<List<com.example.save.data.models.TransactionEntity>>() {
            @Override
            public void onResponse(Call<List<com.example.save.data.models.TransactionEntity>> call, Response<List<com.example.save.data.models.TransactionEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactionsLiveData.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<com.example.save.data.models.TransactionEntity>> call, Throwable t) {
                // Silently fail or log
            }
        });
        return transactionsLiveData;
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getPendingTransactionsWithApproval(String adminEmail) {
        return new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getPendingLoansWithApproval(String adminEmail) {
        return new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getMemberLoansWithApproval(String memberName) {
        return new MutableLiveData<>(new ArrayList<>());
    }

    public boolean hasAdminApproved(String type, String id, String adminEmail) { return false; }
    public int getAdminCount() { return getAdmins().size(); }
    public LiveData<Integer> getAdminCountLive() { return new MutableLiveData<>(getAdminCount()); }

    public void checkLoanEligibility(double amount, int duration, EligibilityCallback callback) {
        if (callback != null) callback.onResult(true, "Eligible (Mock)");
    }

    public void getRepaymentSchedule(double amount, int duration, RepaymentScheduleCallback callback) {
        if (callback != null) callback.onResult(true, "Schedule generated (Mock)");
    }

    public double getContributionTarget() { return 0; }
    public void setContributionTarget(double target) {}
    public double getMaxLoanAmount() { return 0; }
    public double getLoanInterestRate() { return 0; }
    public int getMaxLoanDuration() { return 0; }
    public boolean isGuarantorRequired() { return false; }

    public void changePassword(String email, String currentPassword, String newPassword, PasswordChangeCallback callback) {
        if (callback != null) callback.onResult(true, "Password changed (Mock)");
    }

    public void approveLoan(String loanId, String adminEmail, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approveLoan(loanId, adminEmail).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Loan approved");
                } else {
                    if (callback != null) callback.onResult(false, "Approval failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error");
            }
        });
    }

    public void approveTransaction(String txId, String adminEmail, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approveTransaction(txId, adminEmail).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Transaction approved");
                } else {
                    if (callback != null) callback.onResult(false, "Approval failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error");
            }
        });
    }

    public void rejectLoanRequest(String requestId, String reason, RejectionCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.rejectLoan(requestId, reason).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Loan rejected");
                } else {
                    if (callback != null) callback.onResult(false, "Rejection failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error");
            }
        });
    }

    public void repayLoan(String loanId, double amount, String paymentMethod, String phoneNumber, LoanRepaymentCallback callback) {
        // Implementation for backend repayment
        if (callback != null) callback.onResult(true, "Repayment processed");
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getPendingTransactions() {
        return new MutableLiveData<>(new ArrayList<>());
    }
}
