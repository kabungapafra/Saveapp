package com.example.save.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.Nullable;

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
import com.example.save.data.network.ApprovalRequestDto;
import com.example.save.data.network.RejectionRequest;

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
        if (instance == null)
            instance = new MemberRepository(context.getApplicationContext());
        return instance;
    }

    public static synchronized MemberRepository getInstance() {
        if (instance == null)
            throw new IllegalStateException("Not initialized");
        return instance;
    }

    private boolean isSyncing = false;

    public void clearData() {
        membersLiveData.postValue(new ArrayList<>());
        groupBalance.postValue(0.0);
        isSyncing = false;
    }

    public void refreshMembers(MemberAddCallback callback) {
        if (isSyncing) {
            if (callback != null)
                callback.onResult(true, "Sync already in progress");
            return;
        }
        isSyncing = true;
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getMembers(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<MemberEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<MemberEntity>> call, Response<com.example.save.data.models.PaginatedResponse<MemberEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Member> models = new ArrayList<>();
                    List<String[]> avatarsToRehydrate = new ArrayList<>();
                    com.example.save.utils.SessionManager session =
                            com.example.save.utils.SessionManager.getInstance(appContext);
                    for (MemberEntity entity : response.body().getData()) {
                        Member m = new Member(entity.getName(), entity.getRole(), true, entity.getPhone());
                        m.setId(entity.getId());

                        // Rehydrate the avatar from the server when it isn't cached locally
                        // (e.g. after the app was uninstalled and reinstalled).
                        String b64 = entity.getProfileImage();
                        if (b64 != null && !b64.isEmpty()) {
                            m.setProfileImage(b64);
                            if (entity.getPhone() != null) {
                                String normalized = entity.getPhone().replaceAll("[^0-9+]", "");
                                if (session.getProfileImage(normalized) == null) {
                                    avatarsToRehydrate.add(new String[]{normalized, b64});
                                }
                            }
                        }
                        m.setContributionPaid(entity.getContributionPaid());
                        m.setContributionTarget(entity.getContributionTarget());
                        m.setCreditScore(entity.getCreditScore());

                        // Map the new server-authoritative fields
                        m.setReliabilityLabel(entity.getReliabilityLabel());
                        m.setReliabilityColor(entity.getReliabilityColor());
                        m.setEligible(entity.isEligible());
                        m.setStatus(entity.getStatus());
                        m.setJoinedDate(entity.getJoinedDate());
                        m.setHasReceivedPayout(entity.isHasReceivedPayout());
                        m.setShortfallAmount(entity.getShortfallAmount());

                        models.add(m);
                    }
                    membersLiveData.postValue(models);
                    rehydrateAvatars(avatarsToRehydrate);
                    isSyncing = false;
                    if (callback != null)
                        callback.onResult(true, "Synced from PostgreSQL");
                } else {
                    isSyncing = false;
                    if (callback != null)
                        callback.onResult(false, "Failed to sync");
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<MemberEntity>> call, Throwable t) {
                isSyncing = false;
                if (callback != null)
                    callback.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public void addMember(Member member, MemberRegistrationCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        MemberRegistrationRequest request = new MemberRegistrationRequest(member.getName(),
                member.getPhone(), member.getRole(), member.getPassword());

        apiService.createMember(request).enqueue(new Callback<MemberRegistrationResponse>() {
            @Override
            public void onResponse(Call<MemberRegistrationResponse> call,
                    Response<MemberRegistrationResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    refreshMembers(null);
                    if (callback != null)
                        callback.onResult(true, "Member registered in PostgreSQL", response.body().getOtp());
                } else if (callback != null) {
                    String errorMsg = "Registration failed";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    callback.onResult(false, errorMsg, null);
                }
            }

            @Override
            public void onFailure(Call<MemberRegistrationResponse> call, Throwable t) {
                if (callback != null)
                    callback.onResult(false, "Network error", null);
            }
        });
    }

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public List<Member> getAllMembers() {
        return membersLiveData.getValue();
    }

    public Executor getExecutor() {
        return executor;
    }

    // Preservation of other stubs (to be implemented as backend grows)
    public interface MemberAddCallback {
        void onResult(boolean success, String message);
    }

    public interface MemberRegistrationCallback {
        void onResult(boolean success, String message, @Nullable String otp);
    }

    public interface PasswordChangeCallback {
        void onResult(boolean success, String message);
    }

    public interface PayoutCallback {
        void onResult(boolean success, String message);
    }

    public interface ConfigCallback {
        void onResult(boolean success, SystemConfig config, String message);
    }

    public interface ReportCallback {
        void onResult(boolean success, ComprehensiveReportResponse report, String message);
    }

    public interface PaymentCallback {
        void onResult(boolean success, String message);
    }

    public interface ApprovalCallback {
        void onResult(boolean success, String message);
    }

    public interface LoanSubmissionCallback {
        void onResult(boolean success, String message);
    }

    public interface RejectionCallback {
        void onResult(boolean success, String message);
    }

    public interface LoanRepaymentCallback {
        void onResult(boolean success, String message);
    }

    public interface EligibilityCallback {
        void onResult(boolean success, String message);
    }

    public interface RepaymentScheduleCallback {
        void onResult(boolean success, String message);
    }

    public interface SummaryCallback {
        void onResult(boolean success, Object summary, String message);
    }

    public interface ApiResponseCallback {
        void onResult(boolean success, String message);
    }

    public interface LoanSummaryCallback {
        void onResult(boolean success, com.example.save.data.models.LoanSummaryResponse summary, String message);
    }

    public int getActiveMemberCount() {
        return getAllMembers() != null ? getAllMembers().size() : 0;
    }

    public int getTotalMemberCount() {
        return getAllMembers() != null ? getAllMembers().size() : 0;
    }

    public void syncMembers() {
        refreshMembers(null);
    }

    public Member getMemberByPhone(String phone) {
        if (getAllMembers() == null)
            return null;
        for (Member m : getAllMembers())
            if (phone.equals(m.getPhone()))
                return m;
        return null;
    }

    public Member getMemberByName(String name) {
        if (getAllMembers() == null)
            return null;
        for (Member m : getAllMembers())
            if (name.equals(m.getName()))
                return m;
        return null;
    }

    public List<Member> getAdmins() {
        List<Member> admins = new ArrayList<>();
        if (getAllMembers() != null) {
            for (Member m : getAllMembers())
                if ("ADMIN".equalsIgnoreCase(m.getRole()))
                    admins.add(m);
        }
        return admins;
    }

    public List<Member> searchMembers(String query) {
        List<Member> results = new ArrayList<>();
        if (getAllMembers() == null || query == null || query.isEmpty())
            return results;
        String lowerQuery = query.toLowerCase().trim();
        for (Member m : getAllMembers()) {
            if (m.getName().toLowerCase().contains(lowerQuery) ||
                    (m.getPhone() != null && m.getPhone().contains(lowerQuery))) {
                results.add(m);
            }
        }
        return results;
    }

    // Remaining stubs...
    public void deleteMember(Member m, MemberAddCallback cb) {
        if (m == null || m.getId() == null) {
            if (cb != null) cb.onResult(false, "Invalid member");
            return;
        }

        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.deleteMember(m.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    syncMembers(); // Refresh list after deletion
                    if (cb != null) cb.onResult(true, "Member deleted");
                } else {
                    if (cb != null) cb.onResult(false, "Deletion failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (cb != null) cb.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public void resetPassword(String e, String n, PasswordChangeCallback cb) {
        if (cb != null)
            cb.onResult(true, "Password changed successfully (Mock)");
    }

    public LiveData<Double> getGroupBalance() {
        return groupBalance;
    }

    public void updateSystemConfig(Object u, ConfigCallback cb) {
    }

    public void executePayout(Member m, double a, boolean d, String adminEmail, PayoutCallback cb) {
        if (m == null || m.getPhone() == null) {
            if (cb != null) cb.onResult(false, "Member phone is required");
            return;
        }
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        com.example.save.data.models.PayoutCreateRequest req =
                new com.example.save.data.models.PayoutCreateRequest(m.getPhone(), a, d);
        apiService.createPayout(req).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (cb != null) cb.onResult(true, "Payout submitted. Awaiting admin approval.");
                } else {
                    String msg = "Payout failed";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    if (cb != null) cb.onResult(false, msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (cb != null) cb.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public void makePayment(Member m, double a, String p, String pm, PaymentCallback cb) {
    // Build deposit request payload
    com.example.save.data.models.DepositRequest request = new com.example.save.data.models.DepositRequest();
    request.setMemberId(m.getId());
    request.setAmount(a);
    request.setPaymentMethod(pm);
    request.setDescription("Monthly Contribution via " + pm);

    ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
    apiService.makeDeposit(request).enqueue(new retrofit2.Callback<com.example.save.data.models.Transaction>() {
        @Override
        public void onResponse(retrofit2.Call<com.example.save.data.models.Transaction> call, retrofit2.Response<com.example.save.data.models.Transaction> response) {
            if (response.isSuccessful() && response.body() != null) {
                if (cb != null) cb.onResult(true, "Deposit successful");
            } else {
                String errorMsg = "Deposit failed: " + response.code();
                try {
                    if (response.errorBody() != null) {
                        errorMsg = response.errorBody().string();
                    }
                } catch (Exception e) {
                    // ignore parsing errors
                }
                if (cb != null) cb.onResult(false, errorMsg);
            }
        }

        @Override
        public void onFailure(retrofit2.Call<com.example.save.data.models.Transaction> call, Throwable t) {
            if (cb != null) cb.onResult(false, "Network error: " + t.getMessage());
        }
    });
}



    public void fetchSystemConfig(MemberRepository.ConfigCallback cb) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getSystemConfig().enqueue(new Callback<SystemConfig>() {
            @Override
            public void onResponse(Call<SystemConfig> call, Response<SystemConfig> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SystemConfig config = response.body();
                    android.content.SharedPreferences.Editor editor = appContext
                            .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE).edit();

                    // Numeric rules
                    editor.putString("rule_edit_contribution_amount", "UGX " + (int) config.getContributionAmount());
                    editor.putString("rule_edit_retention", (int) config.getRetentionPercentage() + "%");
                    editor.putString("rule_edit_late_fee", "UGX " + (int) config.getLatePenaltyRate());
                    editor.putString("rule_edit_loan_interest", (int) config.getLoanInterestRate() + "%");
                    editor.putString("rule_edit_loan_late_fee", "UGX " + (int) config.getLoanLateFee());

                    int recipients = config.getRecipients();
                    if (recipients > 0) {
                        editor.putString("rule_edit_recipients", recipients + " Member" + (recipients == 1 ? "" : "s"));
                    }

                    // Text rules
                    if (config.getFrequency() != null && !config.getFrequency().isEmpty()) {
                        editor.putString("rule_frequency", config.getFrequency());
                    }
                    if (config.getStartDate() != null && !config.getStartDate().isEmpty()) {
                        editor.putString("rule_start_date", config.getStartDate());
                    }

                    // Toggle rules
                    editor.putBoolean("switch_automatic_payouts", config.isAutomaticPayouts());
                    editor.putBoolean("switch_scheduled_contributions", config.isScheduledContributions());
                    editor.putBoolean("switch_smart_roundups", config.isSmartRoundups());
                    editor.putBoolean("switch_automated_cycle", config.isAutomatedCycle());
                    editor.putBoolean("switch_loan_requests", config.isLoanRequests());

                    // Calculate and store the next payout date
                    String startDate = config.getStartDate();
                    String frequency = config.getFrequency();
                    if (startDate != null && !startDate.isEmpty() && frequency != null && !frequency.isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
                            java.util.Date date = sdf.parse(startDate);
                            if (date != null) {
                                java.util.Calendar cal = java.util.Calendar.getInstance();
                                cal.setTime(date);
                                switch (frequency) {
                                    case "Daily": cal.add(java.util.Calendar.DAY_OF_YEAR, 1); break;
                                    case "Weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); break;
                                    case "Bi-weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 2); break;
                                    case "Monthly": cal.add(java.util.Calendar.MONTH, 1); break;
                                    case "Every 2 Months": cal.add(java.util.Calendar.MONTH, 2); break;
                                    case "Every 3 Months": cal.add(java.util.Calendar.MONTH, 3); break;
                                    case "Every 4 Months": cal.add(java.util.Calendar.MONTH, 4); break;
                                    case "Every 5 Months": cal.add(java.util.Calendar.MONTH, 5); break;
                                    case "Every 6 Months": cal.add(java.util.Calendar.MONTH, 6); break;
                                }
                                editor.putString("rule_next_payout_date", sdf.format(cal.getTime()));
                            }
                        } catch (Exception ignored) {}
                    }

                    editor.apply();

                    if (cb != null) cb.onResult(true, config, "Config loaded from server");
                } else {
                    if (cb != null) cb.onResult(false, null, "Failed to fetch config: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SystemConfig> call, Throwable t) {
                if (cb != null) cb.onResult(false, null, "Network error: " + t.getMessage());
            }
        });
    }

    public void getComprehensiveReport(ReportCallback cb) {
        if (cb == null) return;
        ApiService api = RetrofitClient.getClient(appContext).create(ApiService.class);

        // 1) Dashboard summary (balances, totals, group name)
        api.getDashboardSummary().enqueue(new Callback<com.example.save.data.models.DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.DashboardSummaryResponse> call,
                    Response<com.example.save.data.models.DashboardSummaryResponse> response) {
                final com.example.save.data.models.DashboardSummaryResponse summary =
                        response.isSuccessful() ? response.body() : null;
                if (summary == null) {
                    cb.onResult(false, null, "Could not load report data");
                    return;
                }
                // 2) Loans
                api.getLoans(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>>() {
                    @Override
                    public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> c,
                            Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> r) {
                        final List<com.example.save.data.models.LoanEntity> loans =
                                (r.isSuccessful() && r.body() != null) ? r.body().getData() : new ArrayList<>();
                        // 3) Payouts
                        api.getPayouts(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>>() {
                            @Override
                            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> c2,
                                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> r2) {
                                List<com.example.save.data.models.PayoutEntity> payouts =
                                        (r2.isSuccessful() && r2.body() != null) ? r2.body().getData() : new ArrayList<>();
                                cb.onResult(true, buildReport(summary, loans, payouts), "Report ready");
                            }
                            @Override
                            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> c2, Throwable t) {
                                cb.onResult(true, buildReport(summary, loans, new ArrayList<>()), "Report ready");
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> c, Throwable t) {
                        cb.onResult(true, buildReport(summary, new ArrayList<>(), new ArrayList<>()), "Report ready");
                    }
                });
            }
            @Override
            public void onFailure(Call<com.example.save.data.models.DashboardSummaryResponse> call, Throwable t) {
                cb.onResult(false, null, "Network error: " + t.getMessage());
            }
        });
    }

    /** Assembles a ComprehensiveReportResponse from the available endpoints (no single backend endpoint exists). */
    private ComprehensiveReportResponse buildReport(
            com.example.save.data.models.DashboardSummaryResponse summary,
            List<com.example.save.data.models.LoanEntity> loanEntities,
            List<com.example.save.data.models.PayoutEntity> payoutEntities) {

        ComprehensiveReportResponse report = new ComprehensiveReportResponse();
        report.setGeneratedAt(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date()));
        report.setGroupName(summary.getGroupName());
        report.setTotalBalance(summary.getTotalBalance());
        report.setTotalMembers(summary.getTotalMembers());
        report.setTotalContributions(summary.getYearlyContributions());

        // Members (from local cache populated by the members list screen)
        List<Member> members = getAllMembers();
        report.setMembers(members != null ? members : new ArrayList<>());

        // Active loans → detail list + aggregates
        List<com.example.save.data.models.Loan> activeLoans = new ArrayList<>();
        double activeLoansAmount = 0;
        if (loanEntities != null) {
            for (com.example.save.data.models.LoanEntity e : loanEntities) {
                String st = e.getStatus() != null ? e.getStatus() : "";
                if (!"ACTIVE".equalsIgnoreCase(st) && !"APPROVED".equalsIgnoreCase(st)) continue;
                com.example.save.data.models.Loan loan = new com.example.save.data.models.Loan(
                        e.getId(), e.getMemberId(), e.getMemberName(), e.getAmount(), e.getInterest(),
                        e.getReason(), e.getDateRequested(), st);
                loan.setDueDate(e.getDueDate());
                loan.setRepaidAmount(e.getRepaidAmount());
                activeLoans.add(loan);
                activeLoansAmount += e.getAmount();
            }
        }
        report.setActiveLoans(activeLoans);
        report.setActiveLoansCount(activeLoans.size());
        report.setActiveLoansAmount(activeLoansAmount);

        // Payouts → detail list (as TransactionEntity) + total
        List<com.example.save.data.models.TransactionEntity> payoutTx = new ArrayList<>();
        double totalPayouts = 0;
        java.text.SimpleDateFormat iso = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        if (payoutEntities != null) {
            for (com.example.save.data.models.PayoutEntity p : payoutEntities) {
                java.util.Date d = null;
                String raw = (p.getExecutedAt() != null && !p.getExecutedAt().isEmpty()) ? p.getExecutedAt() : p.getCreatedAt();
                if (raw != null && raw.length() >= 10) {
                    try { d = iso.parse(raw.substring(0, 10)); } catch (java.text.ParseException ignored) { }
                }
                com.example.save.data.models.TransactionEntity tx = new com.example.save.data.models.TransactionEntity(
                        p.getId(), p.getMemberName(), p.getNetAmount(), "Payout", d, false,
                        p.getStatus(), "PAYOUT");
                payoutTx.add(tx);
                totalPayouts += p.getNetAmount();
            }
        }
        report.setPayouts(payoutTx);
        report.setTotalPayoutsAmount(totalPayouts);

        return report;
    }

    public void getDashboardSummary(SummaryCallback cb) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getDashboardSummary().enqueue(new Callback<com.example.save.data.models.DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.DashboardSummaryResponse> call,
                    Response<com.example.save.data.models.DashboardSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupBalance.postValue(response.body().getTotalBalance());
                    if (cb != null)
                        cb.onResult(true, response.body(), "Summary loaded");
                } else if (cb != null) {
                    cb.onResult(false, null, "Failed to load summary: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.DashboardSummaryResponse> call, Throwable t) {
                if (cb != null)
                    cb.onResult(false, null, "Network error: " + t.getMessage());
            }
        });
    }

    /** Fetches server-computed loan aggregates for the loan-management screen. */
    public void getLoanSummary(LoanSummaryCallback cb) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getLoanSummary().enqueue(new Callback<com.example.save.data.models.LoanSummaryResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.LoanSummaryResponse> call,
                    Response<com.example.save.data.models.LoanSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (cb != null) cb.onResult(true, response.body(), "Loan summary loaded");
                } else if (cb != null) {
                    cb.onResult(false, null, "Failed to load loan summary: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.LoanSummaryResponse> call, Throwable t) {
                if (cb != null) cb.onResult(false, null, "Network error: " + t.getMessage());
            }
        });
    }

    public Member getNextPayoutRecipient() {
        return null;
    }

    public boolean canExecutePayout() {
        return false;
    }

    public double getNetPayoutAmount() {
        return 0;
    }

    public void savePayoutRules(boolean auto, int day, double reserve) {
    }

    public void resolveShortfall(String phone, double amount, String source, ApiResponseCallback cb) {
        if (cb != null)
            cb.onResult(true, "Shortfall resolved (Mock)");
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
            if (callback != null)
                callback.onResult(false, "Invalid member data");
            return;
        }
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.sendMemberInvite(member.getId()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null)
                        callback.onResult(true, "Invite sent successfully");
                } else {
                    if (callback != null)
                        callback.onResult(false, "Failed to send invite");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null)
                    callback.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    /** Decode base64 avatars off the main thread, cache them as files, and store the paths. */
    private void rehydrateAvatars(List<String[]> pending) {
        if (pending == null || pending.isEmpty()) return;
        executor.execute(() -> {
            com.example.save.utils.SessionManager session =
                    com.example.save.utils.SessionManager.getInstance(appContext);
            for (String[] pair : pending) {
                String normalized = pair[0];
                String b64 = pair[1];
                try {
                    byte[] bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
                    java.io.File file = new java.io.File(appContext.getFilesDir(),
                            "profile_remote_" + normalized.replaceAll("[^0-9]", "") + ".jpg");
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                    fos.write(bytes);
                    fos.flush();
                    fos.close();
                    session.saveProfileImage(normalized, file.getAbsolutePath());
                } catch (Exception ignored) {
                    // Skip a malformed avatar; the rest still rehydrate.
                }
            }
        });
    }

    /** Upload a member's avatar (base64) to the server so it survives reinstall. */
    public void updateProfileImage(String memberId, String base64, MemberAddCallback callback) {
        if (memberId == null || memberId.isEmpty()) {
            if (callback != null) callback.onResult(false, "No member id");
            return;
        }
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.updateMember(memberId, MemberUpdateRequest.forProfileImage(base64))
                .enqueue(new Callback<MemberEntity>() {
                    @Override
                    public void onResponse(Call<MemberEntity> call, Response<MemberEntity> response) {
                        if (callback != null)
                            callback.onResult(response.isSuccessful(),
                                    response.isSuccessful() ? "Photo saved" : "Failed to save photo");
                    }

                    @Override
                    public void onFailure(Call<MemberEntity> call, Throwable t) {
                        if (callback != null)
                            callback.onResult(false, "Network error: " + t.getMessage());
                    }
                });
    }

    public void updateMember(int position, Member member) {
        if (member == null || member.getId() == null)
            return;

        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        MemberUpdateRequest request = new MemberUpdateRequest(
                member.getName(),
                member.getRole(),
                member.isActive());

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
        apiService.submitLoan(request).enqueue(new Callback<com.example.save.data.models.LoanEntity>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.LoanEntity> call, Response<com.example.save.data.models.LoanEntity> response) {
                if (response.isSuccessful()) {
                    if (callback != null)
                        callback.onResult(true, "Loan request submitted");
                } else {
                    String msg = "Submission failed";
                    try {
                        if (response.errorBody() != null) msg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    if (callback != null) callback.onResult(false, msg);
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.LoanEntity> call, Throwable t) {
                if (callback != null)
                    callback.onResult(false, "Network error: " + t.getMessage());
            }
        });
    }

    public LiveData<List<com.example.save.data.models.LoanEntity>> getLoanRequests() {
        MutableLiveData<List<com.example.save.data.models.LoanEntity>> liveData = new MutableLiveData<>(
                new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getLoans(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call, Throwable t) {
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

    public void initiateLoanApproval(String requestId, String adminPhone, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approveLoan(requestId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null)
                        callback.onResult(true, "Approved successfully");
                } else {
                    if (callback != null)
                        callback.onResult(false, "Approval failed");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null)
                    callback.onResult(false, "Network error");
            }
        });
    }

    public void enableAutoPay(Member member, double amount, int day) {
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getRecentTransactions() {
        MutableLiveData<List<com.example.save.data.models.TransactionEntity>> liveData = new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getTransactions(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body().getData());
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call, Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getLatestMemberTransactions(
            String memberName) {
        MutableLiveData<List<com.example.save.data.models.TransactionEntity>> liveData = new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getTransactions(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.save.data.models.TransactionEntity> filtered = new ArrayList<>();
                    for (com.example.save.data.models.TransactionEntity tx : response.body().getData()) {
                        if (memberName == null || memberName.equalsIgnoreCase(tx.getMemberName())) {
                            filtered.add(tx);
                        }
                    }
                    liveData.postValue(filtered);
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call, Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getMemberTransactionsWithApproval(
            String memberName) {
        MutableLiveData<List<com.example.save.data.models.TransactionWithApproval>> liveData = new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getTransactions(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.save.data.models.TransactionWithApproval> list = new ArrayList<>();
                    for (com.example.save.data.models.TransactionEntity tx : response.body().getData()) {
                        if (memberName == null || memberName.equalsIgnoreCase(tx.getMemberName())) {
                            list.add(new com.example.save.data.models.TransactionWithApproval(tx, false));
                        }
                    }
                    liveData.postValue(list);
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call, Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    public double getPayoutAmount() {
        return 0;
    }

    public void setPayoutAmount(double amount) {
    }

    public double getRetentionPercentage() {
        return 0;
    }

    public void setRetentionPercentage(double percentage) {
    }

    private final MutableLiveData<List<com.example.save.data.models.TransactionEntity>> transactionsLiveData = new MutableLiveData<>(
            new ArrayList<>());

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getPendingTransactionsWithApproval(
            String adminPhone) {
        MutableLiveData<List<com.example.save.data.models.TransactionWithApproval>> liveData = new MutableLiveData<>(
                new ArrayList<>());

        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getTransactions(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.save.data.models.TransactionWithApproval> pendingTxs = new ArrayList<>();
                    for (com.example.save.data.models.TransactionEntity tx : response.body().getData()) {
                        if ("PENDING".equalsIgnoreCase(tx.getStatus())
                                || (tx.getStatus() != null && tx.getStatus().startsWith("PENDING"))) {
                            pendingTxs.add(new com.example.save.data.models.TransactionWithApproval(tx, false));
                        }
                    }
                    liveData.postValue(pendingTxs);
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call, Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getGenericTransactions() {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getTransactions(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    transactionsLiveData.postValue(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.TransactionEntity>> call, Throwable t) {
                // Silently fail or log
            }
        });
        return transactionsLiveData;
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getPendingLoansWithApproval(String adminPhone) {
        MutableLiveData<List<com.example.save.data.models.LoanWithApproval>> liveData = new MutableLiveData<>(new ArrayList<>());
        
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getLoans(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call, Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<com.example.save.data.models.LoanWithApproval> pendingLoans = new ArrayList<>();
                    for (com.example.save.data.models.LoanEntity loan : response.body().getData()) {
                        if ("PENDING".equalsIgnoreCase(loan.getStatus()) || (loan.getStatus() != null && loan.getStatus().startsWith("PENDING"))) {
                            pendingLoans.add(new com.example.save.data.models.LoanWithApproval(loan, false));
                        }
                    }
                    liveData.postValue(pendingLoans);
                }
            }

            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call, Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });
        
        return liveData;
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getMemberLoansWithApproval(String memberName) {
        MutableLiveData<List<com.example.save.data.models.LoanWithApproval>> liveData = new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getLoans(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<com.example.save.data.models.LoanWithApproval> result = new ArrayList<>();
                    for (com.example.save.data.models.LoanEntity e : response.body().getData()) {
                        if (memberName != null && memberName.equalsIgnoreCase(e.getMemberName())) {
                            boolean approved = "ACTIVE".equalsIgnoreCase(e.getStatus()) || "APPROVED".equalsIgnoreCase(e.getStatus());
                            result.add(new com.example.save.data.models.LoanWithApproval(e, approved));
                        }
                    }
                    liveData.postValue(result);
                }
            }
            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call, Throwable t) {}
        });
        return liveData;
    }

    public boolean hasAdminApproved(String type, String id, String adminPhone) {
        return false;
    }

    public int getAdminCount() {
        return getAdmins().size();
    }

    public LiveData<Integer> getAdminCountLive() {
        return new MutableLiveData<>(getAdminCount());
    }

    public void checkLoanEligibility(double amount, int duration, EligibilityCallback callback) {
        if (callback != null)
            callback.onResult(true, "Eligible (Mock)");
    }

    public void getRepaymentSchedule(double amount, int duration, RepaymentScheduleCallback callback) {
        if (callback != null)
            callback.onResult(true, "Schedule generated (Mock)");
    }

    public double getContributionTarget() {
        String str = appContext.getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                .getString("rule_edit_contribution_amount", "0")
                .replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(str) * 12;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setContributionTarget(double target) {
    }

    public double getMaxLoanAmount() {
        return 0;
    }

    public double getLoanInterestRate() {
        return 0;
    }

    public int getMaxLoanDuration() {
        return 0;
    }

    public boolean isGuarantorRequired() {
        return false;
    }

    public void changePassword(String email, String currentPassword, String newPassword,
            PasswordChangeCallback callback) {
        if (callback != null)
            callback.onResult(true, "Password changed (Mock)");
    }

    public void approveLoan(String loanId, String adminPhone, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approveLoan(loanId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null)
                        callback.onResult(true, "Loan approved");
                } else {
                    if (callback != null)
                        callback.onResult(false, "Approval failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null)
                    callback.onResult(false, "Network error");
            }
        });
    }

    public void approvePayout(String payoutId, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approvePayout(payoutId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null) callback.onResult(true, "Payout approved");
                } else {
                    if (callback != null) callback.onResult(false, "Payout approval failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null) callback.onResult(false, "Network error");
            }
        });
    }

    public LiveData<List<com.example.save.data.models.PayoutEntity>> getMemberPayoutHistory(String memberName) {
        MutableLiveData<List<com.example.save.data.models.PayoutEntity>> liveData = new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getPayouts(100, 0).enqueue(new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> call,
                    Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<com.example.save.data.models.PayoutEntity> list = new ArrayList<>();
                    for (com.example.save.data.models.PayoutEntity p : response.body().getData()) {
                        if (memberName == null || memberName.equalsIgnoreCase(p.getMemberName())) {
                            list.add(p);
                        }
                    }
                    liveData.postValue(list);
                } else {
                    liveData.postValue(new ArrayList<>());
                }
            }
            @Override
            public void onFailure(Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> call, Throwable t) {
                liveData.postValue(new ArrayList<>());
            }
        });
        return liveData;
    }

    public LiveData<List<com.example.save.data.models.ApprovalRequest>> getPendingPayoutApprovals() {
        MutableLiveData<List<com.example.save.data.models.ApprovalRequest>> liveData =
                new MutableLiveData<>(new ArrayList<>());
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.getPayouts(100, 0).enqueue(
                new Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>>() {
                    @Override
                    public void onResponse(
                            Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> call,
                            Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<com.example.save.data.models.ApprovalRequest> pending = new ArrayList<>();
                            for (com.example.save.data.models.PayoutEntity p : response.body().getData()) {
                                if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                                    pending.add(new com.example.save.data.models.ApprovalRequest(
                                            p.getId(),
                                            "DISBURSEMENT",
                                            p.getMemberName(),
                                            p.getAmount(),
                                            "Payout Disbursement",
                                            null,
                                            p.getStatus(),
                                            false));
                                }
                            }
                            liveData.postValue(pending);
                        }
                    }
                    @Override
                    public void onFailure(
                            Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.PayoutEntity>> call,
                            Throwable t) {
                        liveData.postValue(new ArrayList<>());
                    }
                });
        return liveData;
    }

    public void approveTransaction(String txId, String adminPhone, ApprovalCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.approveTransaction(txId, new ApprovalRequestDto(txId, adminPhone))
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful()) {
                            if (callback != null)
                                callback.onResult(true, "Transaction approved");
                        } else {
                            if (callback != null)
                                callback.onResult(false, "Approval failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public void rejectLoanRequest(String requestId, String reason, RejectionCallback callback) {
        ApiService apiService = RetrofitClient.getClient(appContext).create(ApiService.class);
        apiService.rejectLoan(requestId, new RejectionRequest(reason)).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    if (callback != null)
                        callback.onResult(true, "Loan rejected");
                } else {
                    if (callback != null)
                        callback.onResult(false, "Rejection failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (callback != null)
                    callback.onResult(false, "Network error");
            }
        });
    }

    public void repayLoan(String loanId, double amount, String paymentMethod, String phoneNumber,
            LoanRepaymentCallback callback) {
        // Implementation for backend repayment
        if (callback != null)
            callback.onResult(true, "Repayment processed");
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getPendingTransactions() {
        return new MutableLiveData<>(new ArrayList<>());
    }
}
