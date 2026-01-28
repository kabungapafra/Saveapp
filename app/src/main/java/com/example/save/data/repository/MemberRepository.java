package com.example.save.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.save.data.local.AppDatabase;
import com.example.save.data.local.dao.LoanDao;
import com.example.save.data.local.dao.MemberDao;
import com.example.save.data.local.dao.TransactionDao;
import com.example.save.data.local.entities.LoanEntity;
import com.example.save.data.local.entities.MemberEntity;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.models.Member;
import com.example.save.utils.ValidationUtils;
import com.example.save.data.network.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MemberRepository {
    private static MemberRepository instance;
    private final MemberDao memberDao;
    private final TransactionDao transactionDao;
    private final LoanDao loanDao;
    private final com.example.save.data.local.dao.ApprovalDao approvalDao;
    private final Executor executor;
    private final android.content.SharedPreferences prefs;
    private final Context appContext; // Store context for API calls
    private LiveData<List<Member>> membersLiveData;
    private LiveData<Double> groupBalance;
    private double contributionTarget;
    private double payoutAmount;
    private double retentionPercentage;
    private final com.example.save.utils.NotificationHelper notificationHelper;

    // Loan Configuration - These are now read-only defaults, actual values come
    // from backend
    private double maxLoanAmount = 500000;
    private double loanInterestRate = 5.0;
    private int maxLoanDuration = 12;
    private boolean requireGuarantor = true;

    private MemberRepository(Context context) {
        this.appContext = context.getApplicationContext(); // Store application context
        AppDatabase database = AppDatabase.getInstance(context);
        memberDao = database.memberDao();
        transactionDao = database.transactionDao();
        loanDao = database.loanDao();
        approvalDao = database.approvalDao();
        executor = Executors.newSingleThreadExecutor();
        prefs = context.getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
        notificationHelper = new com.example.save.utils.NotificationHelper(context);

        contributionTarget = 1000000; // Default: 1M UGX
        payoutAmount = Double.longBitsToDouble(prefs.getLong("payout_amount", Double.doubleToLongBits(500000.0)));
        retentionPercentage = Double
                .longBitsToDouble(prefs.getLong("retention_percentage", Double.doubleToLongBits(0.0)));

        groupBalance = transactionDao.getGroupBalance(); // Central source of truth from DB

        // Transform entity LiveData to model LiveData
        LiveData<List<MemberEntity>> entityLiveData = memberDao.getAllMembers();
        membersLiveData = Transformations.map(entityLiveData, this::convertEntitiesToModels);

    }

    public static synchronized MemberRepository getInstance(Context context) {
        if (instance == null) {
            instance = new MemberRepository(context.getApplicationContext());
        }
        return instance;
    }

    // Keep singleton method for backward compatibility (will fail if context not
    // set)
    public static synchronized MemberRepository getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MemberRepository not initialized. Call getInstance(Context) first.");
        }
        return instance;
    }

    public Executor getExecutor() {
        return executor;
    }

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public List<Member> getAllMembers() {
        // This needs to run on background thread
        List<MemberEntity> entities = memberDao.getAllMembersSync();
        return convertEntitiesToModels(entities);
    }

    public void addMember(Member member, MemberAddCallback callback) {
        if (member != null) {
            // Normalize phone before saving
            member.setPhone(com.example.save.utils.ValidationUtils.normalizePhone(member.getPhone()));
        }
        executor.execute(() -> {
            try {
                MemberEntity entity = convertModelToEntity(member);
                entity.setContributionTarget(contributionTarget);
                memberDao.insert(entity);
                if (callback != null) {
                    // Post success to main thread if needed, or let ViewModel handle it
                    // Ideally, we run callback on the original thread or main thread
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(true, null));
                }
            } catch (Exception e) {
                if (callback != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper())
                            .post(() -> callback.onResult(false, e.getMessage()));
                }
            }
        });
    }

    public interface MemberAddCallback {
        void onResult(boolean success, String message);
    }

    public void removeMember(Member member) {
        if (member != null) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    memberDao.delete(entity);
                }
            });
        }
    }

    // Password reset - Now uses backend API
    // NOTE: OTP generation MUST be done on backend for security
    @Deprecated
    public String resetPassword(Member member) {
        // SECURITY ISSUE: OTP generation should be on backend
        // This method is deprecated - use backend API instead
        // Backend will generate secure OTP and send via SMS/Email
        String newOtp = com.example.save.utils.ValidationUtils.generateOTP();
        // TODO: Call backend API: POST /auth/reset-password
        // Backend will: generate OTP, hash password, update member, send OTP
        return newOtp;
    }

    /**
     * Change member password - Now uses backend API
     * NOTE: Password hashing MUST be done on backend
     */
    public void changePassword(String email, String currentPassword, String newPassword,
            PasswordChangeCallback callback) {
        if (email == null || newPassword == null) {
            if (callback != null)
                callback.onResult(false, "Invalid input");
            return;
        }

        com.example.save.data.network.ChangePasswordRequest request = new com.example.save.data.network.ChangePasswordRequest(
                email, currentPassword, newPassword);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.changePassword(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (callback != null) {
                                callback.onResult(apiResponse.isSuccess(), apiResponse.getMessage());
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            if (callback != null)
                                callback.onResult(false, "Failed to change password");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public interface PasswordChangeCallback {
        void onResult(boolean success, String message);
    }

    public LiveData<List<Member>> searchMembers(String query) {
        // Current implementation is dummy search on list, but we should use DAO
        // However, converting LiveData<List<Entity>> to LiveData<List<Model>> requires
        // Transformations
        // For simplicity, we can load sync or assume the Fragment handles filtering if
        // the user wants memory search
        // But let's use the DAO search
        return androidx.lifecycle.Transformations.map(memberDao.searchMembers(query), entities -> {
            List<Member> models = new java.util.ArrayList<>();
            for (MemberEntity e : entities) {
                models.add(convertEntityToModel(e));
            }
            return models;
        });
    }

    public void updateMember(int position, Member member) {
        // Position is not reliable with database, use email/ID to identify member
        executor.execute(() -> {
            MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
            if (entity != null) {
                updateEntityFromModel(entity, member);
                memberDao.update(entity);
            }
        });
    }

    public void enableAutoPay(Member member, double amount, int day) {
        if (member != null) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setAutoPayEnabled(true);
                    entity.setAutoPayAmount(amount);
                    entity.setAutoPayDay(day);
                    memberDao.update(entity);
                }
            });
        }
    }

    public List<Member> getMembersWithShortfalls() {
        List<MemberEntity> entities = memberDao.getMembersWithShortfalls();
        return convertEntitiesToModels(entities);
    }

    public List<Member> getAdmins() {
        List<MemberEntity> entities = memberDao.getAdmins();
        return convertEntitiesToModels(entities);
    }

    public int getActiveMemberCount() {
        return memberDao.getActiveMemberCount();
    }

    public int getAdminCount() {
        return memberDao.getAdminCount();
    }

    public LiveData<Integer> getAdminCountLive() {
        return memberDao.getAdminCountLive();
    }

    public int getTotalMemberCount() {
        return memberDao.getMemberCount();
    }

    public LiveData<Double> getGroupBalance() {
        return groupBalance;
    }

    public Member getNextPayoutRecipient() {
        // Sync version for background threads
        List<MemberEntity> entities = memberDao.getActiveMembers();
        for (MemberEntity entity : entities) {
            if (!entity.isHasReceivedPayout()) {
                return convertEntityToModel(entity);
            }
        }
        return null;
    }

    // Payout execution - Now uses backend API
    // Note: Balance checks, calculations, and transaction logging are handled by
    // backend
    public void executePayout(Member member, double amount, boolean deferRemaining, PayoutCallback callback) {
        if (member == null) {
            if (callback != null)
                callback.onResult(false, "Member not found");
            return;
        }

        com.example.save.data.network.PayoutRequest payoutRequest = new com.example.save.data.network.PayoutRequest(
                member.getEmail(), amount, deferRemaining);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.executePayout(payoutRequest)
                .enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                if (callback != null)
                                    callback.onResult(true, apiResponse.getMessage());
                            } else {
                                if (callback != null)
                                    callback.onResult(false, apiResponse.getMessage());
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            if (callback != null)
                                callback.onResult(false, "Failed to execute payout");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public interface PayoutCallback {
        void onResult(boolean success, String message);
    }

    // Deprecated - balance checks should be done on backend
    @Deprecated
    public boolean canExecutePayout() {
        // This should query backend for balance and payout eligibility
        return true; // Placeholder - backend will validate
    }

    @Deprecated
    public double getBalanceShortfall() {
        // This should query backend for balance information
        return 0.0; // Placeholder - backend will calculate
    }

    // Shortfall resolution - Now uses backend API
    // Note: Balance checks and transaction logging are handled by backend
    public void resolveShortfall(Member member, ShortfallResolutionCallback callback) {
        if (member == null) {
            if (callback != null)
                callback.onResult(false, "Member not found");
            return;
        }

        // TODO: Add API endpoint for shortfall resolution
        // POST /members/{id}/resolve-shortfall
        // Backend will:
        // - Check if balance is sufficient
        // - Update member shortfall to 0
        // - Log shortfall resolution transaction
        // - Update group balance

        if (callback != null) {
            callback.onResult(false, "Shortfall resolution API not yet implemented");
        }
    }

    public interface ShortfallResolutionCallback {
        void onResult(boolean success, String message);
    }

    public Member getMemberByName(String name) {
        MemberEntity entity = memberDao.getMemberByName(name);
        return entity != null ? convertEntityToModel(entity) : null;
    }

    public Member getMemberByEmail(String email) {
        MemberEntity entity = memberDao.getMemberByEmail(email);
        return entity != null ? convertEntityToModel(entity) : null;
    }

    public Member getMemberByPhone(String phone) {
        String normalizedPhone = com.example.save.utils.ValidationUtils.normalizePhone(phone);
        MemberEntity entity = memberDao.getMemberByPhone(normalizedPhone);
        return entity != null ? convertEntityToModel(entity) : null;
    }

    // Synchronous method explicitly for background thread use
    public Member getMemberByNameSync(String identifier) {
        if (identifier == null || identifier.isEmpty())
            return null;

        MemberEntity entity = memberDao.getMemberByName(identifier);
        if (entity == null) {
            entity = memberDao.getMemberByEmail(identifier);
        }
        return entity != null ? convertEntityToModel(entity) : null;
    }

    // Payment/Contribution - Now uses backend API
    // Note: Balance updates, streak calculations, and transaction logging are
    // handled by backend
    public void makePayment(Member member, double amount, String phoneNumber, String paymentMethod,
            PaymentCallback callback) {
        if (member == null) {
            if (callback != null)
                callback.onResult(false, "Member not found");
            return;
        }

        com.example.save.data.network.TransactionRequest transactionRequest = new com.example.save.data.network.TransactionRequest(
                member.getName(), "CONTRIBUTION", amount,
                "Contribution from " + member.getName(), paymentMethod);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.createTransaction(transactionRequest).enqueue(
                new retrofit2.Callback<com.example.save.data.local.entities.TransactionEntity>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.local.entities.TransactionEntity> call,
                            retrofit2.Response<com.example.save.data.local.entities.TransactionEntity> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Backend processed payment, updated balance, calculated streak, logged
                            // transaction
                            if (callback != null)
                                callback.onResult(true, "Payment processed successfully");
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            if (callback != null)
                                callback.onResult(false, "Failed to process payment");
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<com.example.save.data.local.entities.TransactionEntity> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public interface PaymentCallback {
        void onResult(boolean success, String message);
    }

    public void deleteAllMembers() {
        executor.execute(() -> {
            memberDao.deleteAll();
        });
    }

    public List<Member> getMonthlyRecipients(int limit) {
        List<MemberEntity> allActive = memberDao.getActiveMembers();
        List<Member> recipients = new ArrayList<>();
        int count = 0;
        for (MemberEntity entity : allActive) {
            if (!entity.isHasReceivedPayout()) {
                recipients.add(convertEntityToModel(entity));
                count++;
                if (count >= limit)
                    break;
            }
        }
        return recipients;
    }

    public int getPendingPaymentsCount() {
        List<MemberEntity> allActive = memberDao.getActiveMembers();
        int pending = 0;
        for (MemberEntity entity : allActive) {
            if (entity.getContributionPaid() < entity.getContributionTarget()) {
                pending++;
            }
        }
        return pending;
    }

    public int getActiveMemberCountSync() {
        return memberDao.getActiveMemberCount();
    }

    public int getTotalMemberCountSync() {
        return memberDao.getMemberCount();
    }

    // Contribution Target Management
    public double getContributionTarget() {
        return contributionTarget;
    }

    public void setContributionTarget(double target) {
        if (target > 0) {
            this.contributionTarget = target;
            executor.execute(() -> {
                List<MemberEntity> allMembers = memberDao.getAllMembersSync();
                for (MemberEntity entity : allMembers) {
                    entity.setContributionTarget(target);
                    memberDao.update(entity);
                }
            });
        }
    }

    // Loan Configuration
    public double getMaxLoanAmount() {
        return maxLoanAmount;
    }

    public void setMaxLoanAmount(double amount) {
        this.maxLoanAmount = amount;
    }

    public double getLoanInterestRate() {
        return loanInterestRate;
    }

    public void setLoanInterestRate(double rate) {
        this.loanInterestRate = rate;
    }

    public int getMaxLoanDuration() {
        return maxLoanDuration;
    }

    public void setMaxLoanDuration(int duration) {
        this.maxLoanDuration = duration;
    }

    public boolean isGuarantorRequired() {
        return requireGuarantor;
    }

    public void setRequireGuarantor(boolean required) {
        this.requireGuarantor = required;
    }

    // Loan Requests Management - Now uses backend API
    // Note: Financial calculations (interest, eligibility) are handled by backend
    public void submitLoanRequest(com.example.save.data.models.LoanRequest request,
            LoanSubmissionCallback callback) {
        // Submit loan request to backend - backend will calculate interest, validate
        // eligibility, etc.
        // Context is stored in the repository
        android.content.Context context = null;
        try {
            // Get context from SharedPreferences (stored during initialization)
            java.lang.reflect.Field contextField = prefs.getClass().getDeclaredField("mContext");
            contextField.setAccessible(true);
            context = (android.content.Context) contextField.get(prefs);
        } catch (Exception e) {
            // Fallback: use application context if available
            if (memberDao != null) {
                // Try to get from database
            }
        }

        if (context == null) {
            if (callback != null)
                callback.onResult(false, "Context not available");
            return;
        }

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(context).create(com.example.save.data.network.ApiService.class);

        apiService.submitLoanRequest(request)
                .enqueue(new retrofit2.Callback<com.example.save.data.network.LoanResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.save.data.network.LoanResponse> call,
                            retrofit2.Response<com.example.save.data.network.LoanResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Backend calculated interest and created loan
                            if (callback != null)
                                callback.onResult(true, "Loan request submitted successfully");
                        } else {
                            String errorMsg = "Failed to submit loan request";
                            if (callback != null)
                                callback.onResult(false, errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.LoanResponse> call,
                            Throwable t) {
                        if (callback != null)
                            callback.onResult(false, "Network error: " + t.getMessage());
                    }
                });
    }

    public interface LoanSubmissionCallback {
        void onResult(boolean success, String message);
    }

    public LiveData<List<com.example.save.data.models.LoanRequest>> getLoanRequests() {
        // Return transformed LiveData from DB
        return Transformations.map(loanDao.getAllLoans(), entities -> {
            List<com.example.save.data.models.LoanRequest> requests = new ArrayList<>();
            for (LoanEntity entity : entities) {
                com.example.save.data.models.LoanRequest req = new com.example.save.data.models.LoanRequest(
                        entity.getMemberName(),
                        entity.getAmount(),
                        12, // Duration not stored in entity
                        "N/A", // Guarantor not in entity
                        "N/A",
                        entity.getReason());
                req.setId(entity.getId());
                req.setStatus(entity.getStatus());
                requests.add(req);
            }
            return requests;
        });
    }

    public List<com.example.save.data.models.LoanRequest> getPendingLoanRequests() {
        // Sync fetch for background threads
        List<LoanEntity> entities = loanDao.getPendingLoans();
        List<com.example.save.data.models.LoanRequest> requests = new ArrayList<>();
        for (LoanEntity entity : entities) {
            com.example.save.data.models.LoanRequest req = new com.example.save.data.models.LoanRequest(
                    entity.getMemberName(),
                    entity.getAmount(),
                    12,
                    "N/A",
                    "N/A",
                    entity.getReason());
            req.setId(entity.getId());
            req.setStatus(entity.getStatus());
            requests.add(req);
        }
        return requests;
    }

    public LoanEntity getActiveLoanForMember(String memberName) {
        return loanDao.getActiveLoanByMemberName(memberName);
    }

    // Loan approval - Now uses backend API
    // Note: Balance checks, approval counting, and loan finalization are handled by
    // backend
    public void initiateLoanApproval(String requestId, String adminEmail, ApprovalCallback callback) {
        com.example.save.data.network.ApprovalRequest approvalRequest = new com.example.save.data.network.ApprovalRequest(
                requestId, adminEmail);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.approveLoan(requestId, approvalRequest).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            postResult(callback, apiResponse.isSuccess(), apiResponse.getMessage());
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            postResult(callback, false, "Failed to approve loan");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        postResult(callback, false, "Network error");
                    }
                });
    }

    // Loan repayment - Now uses backend API
    // Note: Loan balance calculations, status updates, and transaction logging are
    // handled by backend
    public void repayLoan(String loanId, double amount, String paymentMethod, String phoneNumber,
            LoanRepaymentCallback callback) {
        com.example.save.data.network.LoanRepaymentRequest repaymentRequest = new com.example.save.data.network.LoanRepaymentRequest(
                amount, paymentMethod, phoneNumber);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.repayLoan(loanId, repaymentRequest).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (callback != null) {
                                callback.onResult(apiResponse.isSuccess(), apiResponse.getMessage());
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            if (callback != null)
                                callback.onResult(false, "Failed to process repayment");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public interface LoanRepaymentCallback {
        void onResult(boolean success, String message);
    }

    // Loan rejection - Now uses backend API
    // Note: Status updates and notifications are handled by backend
    public void rejectLoanRequest(String requestId, String reason, RejectionCallback callback) {
        com.example.save.data.network.RejectionRequest rejectionRequest = new com.example.save.data.network.RejectionRequest(
                reason);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.rejectLoan(requestId, rejectionRequest).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (callback != null) {
                                callback.onResult(apiResponse.isSuccess(), apiResponse.getMessage());
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            if (callback != null)
                                callback.onResult(false, "Failed to reject loan");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public interface RejectionCallback {
        void onResult(boolean success, String message);
    }

    // Conversion Methods
    private List<Member> convertEntitiesToModels(List<MemberEntity> entities) {
        List<Member> members = new ArrayList<>();
        for (MemberEntity entity : entities) {
            members.add(convertEntityToModel(entity));
        }
        return members;
    }

    private Member convertEntityToModel(MemberEntity entity) {
        Member member = new Member(entity.getName(), entity.getRole(), true, entity.getPhone(), entity.getEmail());
        member.setId(entity.getId());
        member.setPassword(entity.getPassword());
        member.setPayoutDate(entity.getPayoutDate());
        member.setPayoutAmount(entity.getPayoutAmount());
        member.setHasReceivedPayout(entity.isHasReceivedPayout());
        member.setShortfallAmount(entity.getShortfallAmount());
        member.setContributionTarget(entity.getContributionTarget());
        member.setContributionPaid(entity.getContributionPaid());
        member.setFirstLogin(entity.isFirstLogin()); // Sync isFirstLogin field
        member.setPaymentStreak(entity.getPaymentStreak());
        member.setNextPayoutDate(entity.getNextPayoutDate());
        member.setNextPaymentDueDate(entity.getNextPaymentDueDate());
        member.setAutoPayEnabled(entity.isAutoPayEnabled());
        member.setAutoPayAmount(entity.getAutoPayAmount());
        member.setAutoPayDay(entity.getAutoPayDay());
        return member;
    }

    private MemberEntity convertModelToEntity(Member member) {
        MemberEntity entity = new MemberEntity(member.getName(), member.getRole(), member.getEmail(),
                member.getPhone());
        entity.setPassword(member.getPassword());
        entity.setPayoutDate(member.getPayoutDate());
        entity.setPayoutAmount(member.getPayoutAmount());
        entity.setHasReceivedPayout(member.hasReceivedPayout());
        entity.setShortfallAmount(member.getShortfallAmount());
        entity.setContributionTarget(member.getContributionTarget());
        entity.setContributionPaid(member.getContributionPaid());
        entity.setFirstLogin(member.isFirstLogin()); // Sync isFirstLogin field
        entity.setPaymentStreak(member.getPaymentStreak());
        entity.setNextPayoutDate(member.getNextPayoutDate());
        entity.setNextPaymentDueDate(member.getNextPaymentDueDate());
        entity.setAutoPayEnabled(member.isAutoPayEnabled());
        entity.setAutoPayAmount(member.getAutoPayAmount());
        entity.setAutoPayDay(member.getAutoPayDay());
        return entity;
    }

    private void updateEntityFromModel(MemberEntity entity, Member model) {
        entity.setName(model.getName());
        entity.setRole(model.getRole());
        entity.setActive(model.isActive());
        entity.setPhone(model.getPhone());
        entity.setEmail(model.getEmail());
        entity.setPayoutDate(model.getPayoutDate());
        entity.setPayoutAmount(model.getPayoutAmount());
        entity.setHasReceivedPayout(model.hasReceivedPayout());
        entity.setShortfallAmount(model.getShortfallAmount());
        entity.setContributionTarget(model.getContributionTarget());
        entity.setContributionPaid(model.getContributionPaid());
        entity.setFirstLogin(model.isFirstLogin()); // Sync isFirstLogin field
        entity.setPaymentStreak(model.getPaymentStreak());
        entity.setNextPayoutDate(model.getNextPayoutDate());
        entity.setNextPaymentDueDate(model.getNextPaymentDueDate());
        entity.setAutoPayEnabled(model.isAutoPayEnabled());
        entity.setAutoPayAmount(model.getAutoPayAmount());
        entity.setAutoPayDay(model.getAutoPayDay());
        // Password is not copied from model to entity (managed separately)
    }

    // Transaction Management
    public LiveData<List<TransactionEntity>> getRecentTransactions() {
        return transactionDao.getRecentTransactions();
    }

    public LiveData<List<TransactionEntity>> getGenericTransactions() {
        // Just reusing recent for now, but ideally get ALL for trend
        return transactionDao.getRecentTransactions();
    }

    public LiveData<List<TransactionEntity>> getLatestMemberTransactions(String memberName) {
        return transactionDao.getLatestMemberTransactions(memberName);
    }

    // Payout Configuration
    public double getPayoutAmount() {
        return payoutAmount;
    }

    public void setPayoutAmount(double amount) {
        this.payoutAmount = amount;
        prefs.edit().putLong("payout_amount", Double.doubleToRawLongBits(amount)).apply();
    }

    public double getRetentionPercentage() {
        return retentionPercentage;
    }

    public void setRetentionPercentage(double percentage) {
        this.retentionPercentage = percentage;
        prefs.edit().putLong("retention_percentage", Double.doubleToRawLongBits(percentage)).apply();
    }

    @Deprecated
    public double getNetPayoutAmount() {
        // NOTE: This calculation should be done on backend
        // This is a placeholder - actual net payout should come from backend API
        return payoutAmount * (1 - (retentionPercentage / 100.0));
    }

    // --- Payout Improvements ---

    public void savePayoutRules(boolean autoPayoutEnabled, int dayOfMonth, double minReserve) {
        prefs.edit()
                .putBoolean("auto_payout_enabled", autoPayoutEnabled)
                .putInt("auto_payout_day", dayOfMonth)
                .putLong("auto_min_reserve", Double.doubleToRawLongBits(minReserve))
                .apply();
        // Here we would trigger the WorkManager if this was fully implemented
    }

    // Payout execution with approval workflow - Now uses backend API
    // Note: Approval counting, balance checks, and transaction logging are handled
    // by backend
    public void executePayout(Member member, double amount, boolean deferRemaining, String adminEmail,
            PayoutCallback callback) {
        if (member == null) {
            if (callback != null)
                callback.onResult(false, "Member not found");
            return;
        }

        com.example.save.data.network.PayoutRequest payoutRequest = new com.example.save.data.network.PayoutRequest(
                member.getEmail(), amount, deferRemaining);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.executePayout(payoutRequest)
                .enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (callback != null) {
                                callback.onResult(apiResponse.isSuccess(), apiResponse.getMessage());
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            if (callback != null)
                                callback.onResult(false, "Failed to execute payout");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        if (callback != null)
                            callback.onResult(false, "Network error");
                    }
                });
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getPendingTransactionsWithApproval(
            String adminEmail) {
        return transactionDao.getPendingTransactionsWithApproval(adminEmail);
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getPendingLoansWithApproval(
            String adminEmail) {
        return loanDao.getPendingLoansWithApproval(adminEmail);
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getMemberLoansWithApproval(String memberName) {
        return loanDao.getMemberLoansWithApproval(memberName);
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getMemberTransactionsWithApproval(
            String memberName) {
        return transactionDao.getMemberTransactionsWithApproval(memberName);
    }

    // NOTE: finalizePayout removed - this business logic should be handled by
    // backend
    // Backend will update member payout status, log transactions, and update
    // balances

    public void syncMembers() {
        com.example.save.data.network.RetrofitClient.getClient(appContext)
                .create(com.example.save.data.network.ApiService.class)
                .getMembers().enqueue(new retrofit2.Callback<List<MemberEntity>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<MemberEntity>> call,
                            retrofit2.Response<List<MemberEntity>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            executor.execute(() -> {
                                memberDao.insertAll(response.body());
                            });
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<MemberEntity>> call, Throwable t) {
                        // Keep using local Room cache - Offline-first
                    }
                });
    }

    // --- Multi-Admin Approval Logic ---

    public void approveTransaction(String txId, String adminEmail, ApprovalCallback callback) {
        executor.execute(() -> {
            synchronized (this) {
                try {
                    // Post approval to API
                    com.example.save.data.network.RetrofitClient.getClient(null)
                            .create(com.example.save.data.network.ApiService.class)
                            .approveTransaction(txId,
                                    new com.example.save.data.network.ApprovalRequest(txId, adminEmail))
                            .enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                                        retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        postResult(callback, response.body().isSuccess(), response.body().getMessage());
                                        // Trigger a sync to update local status
                                        syncMembers();
                                    } else {
                                        postResult(callback, false, "Server error");
                                    }
                                }

                                @Override
                                public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                                        Throwable t) {
                                    postResult(callback, false, "Connection error: " + t.getMessage());
                                }
                            });
                } catch (Exception e) {
                    postResult(callback, false, e.getMessage());
                }
            }
        });
    }

    // Loan approval by ID - Now uses backend API
    // Note: Approval counting, loan finalization, and transaction logging are
    // handled by backend
    public void approveLoan(String loanId, String adminEmail, ApprovalCallback callback) {
        com.example.save.data.network.ApprovalRequest approvalRequest = new com.example.save.data.network.ApprovalRequest(
                loanId, adminEmail);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(appContext).create(com.example.save.data.network.ApiService.class);

        apiService.approveLoan(loanId, approvalRequest).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            postResult(callback, apiResponse.isSuccess(), apiResponse.getMessage());

                            if (apiResponse.isSuccess()) {
                                notificationHelper.showNotification("Loan Approved",
                                        apiResponse.getMessage(),
                                        com.example.save.utils.NotificationHelper.CHANNEL_ID_LOANS);
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(appContext, response);
                            postResult(callback, false, "Failed to approve loan");
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(appContext, t);
                        postResult(callback, false, "Network error");
                    }
                });
    }

    private void postResult(ApprovalCallback callback, boolean success, String msg) {
        if (callback != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(success, msg));
        }
    }

    public interface ApprovalCallback {
        void onResult(boolean success, String message);
    }

    public LiveData<List<TransactionEntity>> getPendingTransactions() {
        return transactionDao.getPendingTransactions();
    }

    public int getApprovalCount(String type, String targetId) {
        return approvalDao.getApprovalCount(type, targetId);
    }

    public boolean hasAdminApproved(String type, String id, String adminEmail) {
        return approvalDao.getAdminApproval(type, id, adminEmail) != null;
    }
}
