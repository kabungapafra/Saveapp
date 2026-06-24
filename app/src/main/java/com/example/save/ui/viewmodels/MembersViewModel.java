package com.example.save.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.save.data.models.Member;
import com.example.save.data.repository.MemberRepository;

import java.util.List;

@SuppressWarnings("ALL")
public class MembersViewModel extends AndroidViewModel {
    private final MemberRepository repository;
    private final androidx.lifecycle.MutableLiveData<Long> depositEvent = new androidx.lifecycle.MutableLiveData<>(0L);
    private final androidx.lifecycle.MutableLiveData<com.example.save.data.models.DashboardSummaryResponse> dashboardCache = new androidx.lifecycle.MutableLiveData<>();
    private final androidx.lifecycle.MutableLiveData<java.util.List<Member>> payoutQueueCache = new androidx.lifecycle.MutableLiveData<>();
    private final androidx.lifecycle.MutableLiveData<java.util.List<com.example.save.data.models.TransactionEntity>> recentTransactionsCache = new androidx.lifecycle.MutableLiveData<>();

    public androidx.lifecycle.LiveData<com.example.save.data.models.DashboardSummaryResponse> getDashboardCache() {
        return dashboardCache;
    }

    public androidx.lifecycle.LiveData<java.util.List<Member>> getPayoutQueueCache() {
        return payoutQueueCache;
    }

    public void setPayoutQueueCache(java.util.List<Member> list) {
        payoutQueueCache.postValue(list);
    }

    public androidx.lifecycle.LiveData<java.util.List<com.example.save.data.models.TransactionEntity>> getRecentTransactionsCache() {
        return recentTransactionsCache;
    }

    public void setRecentTransactionsCache(java.util.List<com.example.save.data.models.TransactionEntity> list) {
        recentTransactionsCache.postValue(list);
    }

    public MembersViewModel(@NonNull Application application) {
        super(application);
        this.repository = MemberRepository.getInstance(application);
    }

    public androidx.lifecycle.LiveData<Long> getDepositEvent() {
        return depositEvent;
    }

    public void postDepositEvent() {
        Long current = depositEvent.getValue();
        if (current == null) current = 0L;
        depositEvent.postValue(current + 1);
    }

    public LiveData<List<Member>> getMembers() {
        return repository.getMembers();
    }

    public void syncMembers() {
        repository.syncMembers();
    }

    public LiveData<List<Member>> searchMembers(String query) {
        androidx.lifecycle.MutableLiveData<List<Member>> liveData = new androidx.lifecycle.MutableLiveData<>();
        liveData.setValue(repository.searchMembers(query));
        return liveData;
    }

    public List<Member> getAdmins() {
        return repository.getAdmins();
    }

    public void deleteAllMembers() {
        repository.deleteAllMembers();
    }

    public List<Member> getMonthlyRecipients(int limit) {
        return repository.getMonthlyRecipients(limit);
    }

    public int getPendingPaymentsCount() {
        return repository.getPendingPaymentsCount();
    }

    public void addMember(Member member, MemberRepository.MemberRegistrationCallback callback) {
        repository.addMember(member, callback);
    }

    public void sendInvite(Member member, MemberRepository.MemberAddCallback callback) {
        repository.sendMemberInvite(member, callback);
    }

    public void removeMember(Member member, MemberRepository.MemberAddCallback callback) {
        repository.deleteMember(member, callback);
    }

    public void resetPassword(String email, String newPassword, MemberRepository.PasswordChangeCallback callback) {
        repository.resetPassword(email, newPassword, callback);
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public String resetPassword(Member member) {
        // MOCK: No Database, No Network
        return com.example.save.utils.ValidationUtils.generateOTP();
    }

    public void updateMember(int position, Member member) {
        repository.updateMember(position, member);
    }

    public androidx.lifecycle.LiveData<Double> getGroupBalance() {
        return repository.getGroupBalance();
    }

    public int getActiveMemberCount() {
        return repository.getActiveMemberCount();
    }

    public int getTotalMemberCount() {
        return repository.getTotalMemberCount();
    }

    public Member getMemberByName(String name) {
        return repository.getMemberByName(name);
    }



    public Member getMemberByPhone(String phone) {
        return repository.getMemberByPhone(phone);
    }

    // Synchronous method for background thread calls
    public Member getMemberByNameSync(String identifier) {
        return repository.getMemberByNameSync(identifier);
    }

    // Payment/Contribution - Now uses backend API with callback
    public void makePayment(Member member, double amount, String phoneNumber, String paymentMethod,
            MemberRepository.PaymentCallback callback) {
        // Wrap the original callback to emit a deposit event on success
        repository.makePayment(member, amount, phoneNumber, paymentMethod, new MemberRepository.PaymentCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (success) {
                    // Notify observers that a deposit has been completed
                    postDepositEvent();
                }
                if (callback != null) {
                    callback.onResult(success, message);
                }
            }
        });
    }

    public void refreshMembers(MemberRepository.MemberAddCallback callback) {
        repository.refreshMembers(callback);
    }

    public void deleteMember(Member member, MemberRepository.MemberAddCallback callback) {
        repository.deleteMember(member, callback);
    }

    public double getContributionTarget() {
        return repository.getContributionTarget();
    }

    public void setContributionTarget(double target) {
        repository.setContributionTarget(target);
    }

    // Loan Configuration
    public double getMaxLoanAmount() {
        return repository.getMaxLoanAmount();
    }

    public double getLoanInterestRate() {
        return repository.getLoanInterestRate();
    }

    public int getMaxLoanDuration() {
        return repository.getMaxLoanDuration();
    }

    public boolean isGuarantorRequired() {
        return repository.isGuarantorRequired();
    }

    // Synchronous methods for background thread calls
    public int getActiveMemberCountSync() {
        return repository.getActiveMemberCountSync();
    }

    public int getTotalMemberCountSync() {
        return repository.getTotalMemberCountSync();
    }

    // Loan Requests - Now uses backend API with callback
    public void submitLoanRequest(com.example.save.data.models.LoanRequest request,
            MemberRepository.LoanSubmissionCallback callback) {
        repository.submitLoanRequest(request, callback);
    }

    public LiveData<List<com.example.save.data.models.LoanEntity>> getLoanRequests() {
        return repository.getLoanRequests();
    }

    public com.example.save.data.models.LoanEntity getActiveLoanForMember(String memberName) {
        return repository.getActiveLoanForMember(memberName);
    }

    public List<com.example.save.data.models.LoanRequest> getPendingLoanRequests() {
        return repository.getPendingLoanRequests();
    }

    public void initiateLoanApproval(String requestId, String adminPhone, MemberRepository.ApprovalCallback callback) {
        repository.initiateLoanApproval(requestId, adminPhone, callback);
    }

    // Loan rejection - Now uses backend API with callback
    public void rejectLoanRequest(String requestId, String reason, MemberRepository.RejectionCallback callback) {
        repository.rejectLoanRequest(requestId, reason, callback);
    }

    // Loan repayment - Now uses backend API with callback
    // Note: Requires loanId, paymentMethod, and phoneNumber - these should be
    // provided by UI
    public void repayLoan(String loanId, double amount, String paymentMethod, String phoneNumber,
            MemberRepository.LoanRepaymentCallback callback) {
        repository.repayLoan(loanId, amount, paymentMethod, phoneNumber, callback);
    }

    /**
     * Change password - Now uses backend API
     * NOTE: Password hashing is done on backend
     */
    public void changePassword(String email, String currentPassword, String newPassword,
            MemberRepository.PasswordChangeCallback callback) {
        repository.changePassword(email, currentPassword, newPassword, callback);
    }

    public void enableAutoPay(Member member, double amount, int day) {
        repository.enableAutoPay(member, amount, day);
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getRecentTransactions() {
        return repository.getRecentTransactions();
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getLatestMemberTransactions(
            String memberName) {
        return repository.getLatestMemberTransactions(memberName);
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getMemberTransactionsWithApproval(
            String memberName) {
        return repository.getMemberTransactionsWithApproval(memberName);
    }

    public LiveData<List<com.example.save.data.models.PayoutEntity>> getMemberPayoutHistory(String memberName) {
        return repository.getMemberPayoutHistory(memberName);
    }

    // Payout Configuration
    public double getPayoutAmount() {
        return repository.getPayoutAmount();
    }

    public void setPayoutAmount(double amount) {
        repository.setPayoutAmount(amount);
    }

    public double getRetentionPercentage() {
        return repository.getRetentionPercentage();
    }

    public void setRetentionPercentage(double percentage) {
        repository.setRetentionPercentage(percentage);
    }

    // LiveData wrappers for async operations (replaces manual threads)


    public LiveData<Member> getMemberByPhoneLive(String phone) {
        return androidx.lifecycle.Transformations.map(repository.getMembers(), members -> {
            if (members != null && phone != null) {
                for (Member m : members) {
                    if (phone.equals(m.getPhone())) {
                        return m;
                    }
                }
            }
            return null;
        });
    }

    public LiveData<Member> getMemberByNameLive(String name) {
        return androidx.lifecycle.Transformations.map(repository.getMembers(), members -> {
            if (members != null && name != null) {
                for (Member m : members) {
                    if (name.equals(m.getName())) {
                        return m;
                    }
                }
            }
            return null;
        });
    }

    public LiveData<List<Member>> getMonthlyRecipientsLive(int limit) {
        androidx.lifecycle.MutableLiveData<List<Member>> liveData = new androidx.lifecycle.MutableLiveData<>();
        repository.getExecutor().execute(() -> {
            List<Member> recipients = repository.getMonthlyRecipients(limit);
            liveData.postValue(recipients);
        });
        return liveData;
    }

    public LiveData<Integer> getPendingPaymentsCountLive() {
        androidx.lifecycle.MutableLiveData<Integer> liveData = new androidx.lifecycle.MutableLiveData<>();
        repository.getExecutor().execute(() -> {
            int count = repository.getPendingPaymentsCount();
            liveData.postValue(count);
        });
        return liveData;
    }

    public LiveData<Integer> getActiveMemberCountLive() {
        androidx.lifecycle.MutableLiveData<Integer> liveData = new androidx.lifecycle.MutableLiveData<>();
        repository.getExecutor().execute(() -> {
            int count = repository.getActiveMemberCountSync();
            liveData.postValue(count);
        });
        return liveData;
    }

    public LiveData<Integer> getTotalMemberCountLive() {
        androidx.lifecycle.MutableLiveData<Integer> liveData = new androidx.lifecycle.MutableLiveData<>();
        repository.getExecutor().execute(() -> {
            int count = repository.getTotalMemberCountSync();
            liveData.postValue(count);
        });
        return liveData;
    }

    // Advanced Features
    @Deprecated
    public double getLoanEligibility(Member member) {
        // NOTE: Loan eligibility calculation should be done on backend
        // Backend will consider: savings amount, existing loans, payment history, etc.
        // This is a placeholder - actual eligibility should come from backend API
        if (member == null)
            return 0;
        // Placeholder logic: 3x Savings (backend will have more complex rules)
        return member.getContributionPaid() * 3;
    }

    public LiveData<List<com.github.mikephil.charting.data.Entry>> getSavingsTrend() {
        return androidx.lifecycle.Transformations.map(repository.getGenericTransactions(), transactions -> {
            List<com.github.mikephil.charting.data.Entry> entries = new java.util.ArrayList<>();
            double cumulative = 0;
            // Sort by date just in case
            java.util.Collections.sort(transactions, (a, b) -> a.getDate().compareTo(b.getDate()));

            int index = 0;
            for (com.example.save.data.models.TransactionEntity tx : transactions) {
                if (tx.isPositive()) {
                    cumulative += tx.getAmount();
                } else {
                    cumulative -= tx.getAmount();
                }
                // Use index as X axis for simplicity, or Date conversion
                entries.add(new com.github.mikephil.charting.data.Entry(index++, (float) cumulative));
            }
            return entries;
        });
    }

    public void approveTransaction(String txId, String adminPhone, MemberRepository.ApprovalCallback callback) {
        repository.approveTransaction(txId, adminPhone, callback);
    }

    public void approveLoan(String loanId, String adminPhone, MemberRepository.ApprovalCallback callback) {
        repository.approveLoan(loanId, adminPhone, callback);
    }

    public LiveData<List<com.example.save.data.models.TransactionEntity>> getPendingTransactions() {
        return repository.getPendingTransactions();
    }

    // New optimized reactive method
    public LiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> getCombinedApprovals(
            String adminPhone) {
        androidx.lifecycle.MediatorLiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> mediator = new androidx.lifecycle.MediatorLiveData<>();

        LiveData<List<com.example.save.data.models.TransactionWithApproval>> txSource = repository
                .getPendingTransactionsWithApproval(adminPhone);
        LiveData<List<com.example.save.data.models.LoanWithApproval>> loanSource = repository
                .getPendingLoansWithApproval(adminPhone);
        LiveData<List<com.example.save.data.models.ApprovalRequest>> payoutSource = repository
                .getPendingPayoutApprovals();

        mediator.addSource(txSource, txs -> combineApprovals(mediator, txs, loanSource.getValue(), payoutSource.getValue()));
        mediator.addSource(loanSource, loans -> combineApprovals(mediator, txSource.getValue(), loans, payoutSource.getValue()));
        mediator.addSource(payoutSource, payouts -> combineApprovals(mediator, txSource.getValue(), loanSource.getValue(), payouts));

        return mediator;
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getMemberLoansWithApproval(String memberName) {
        return repository.getMemberLoansWithApproval(memberName);
    }

    private void combineApprovals(
            androidx.lifecycle.MutableLiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> mediator,
            List<com.example.save.data.models.TransactionWithApproval> transactions,
            List<com.example.save.data.models.LoanWithApproval> loans,
            List<com.example.save.data.models.ApprovalRequest> payouts) {

        java.util.List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem> combined = new java.util.ArrayList<>();

        if (transactions != null) {
            for (com.example.save.data.models.TransactionWithApproval item : transactions) {
                combined.add(new ApprovalItemImpl(
                        item.transaction.getId(),
                        "PAYOUT",
                        item.transaction.getMemberName(),
                        item.transaction.getAmount(),
                        item.transaction.getDescription(),
                        item.transaction.getDate(),
                        item.transaction.getStatus(),
                        item.isApprovedByAdmin));
            }
        }

        if (loans != null) {
            for (com.example.save.data.models.LoanWithApproval item : loans) {
                combined.add(new ApprovalItemImpl(
                        item.loan.getId(),
                        "LOAN",
                        item.loan.getMemberName(),
                        item.loan.getAmount(),
                        item.loan.getReason(),
                        item.loan.getDateRequested(),
                        item.loan.getStatus(),
                        item.isApprovedByAdmin));
            }
        }

        if (payouts != null) {
            for (com.example.save.data.models.ApprovalRequest p : payouts) {
                combined.add(new ApprovalItemImpl(
                        p.getId(),
                        "DISBURSEMENT",
                        p.getTitle(),
                        p.getAmount(),
                        p.getDescription(),
                        p.getDate(),
                        p.getStatus(),
                        p.hasApproved()));
            }
        }

        // Sort by date (newest first) — items with null date go to end
        java.util.Collections.sort(combined, (a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });

        mediator.setValue(combined);
    }

    // Static implementation to avoid leaks
    private static class ApprovalItemImpl implements com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem {
        private final String id;
        private final String type, title, description, status; // Keep status field
        private final double amount;
        private final java.util.Date date;
        private final boolean hasApproved;

        public ApprovalItemImpl(String id, String type, String title, double amount, String description,
                java.util.Date date, String status, boolean hasApproved) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.amount = amount;
            this.description = description;
            this.date = date;
            this.status = status;
            this.hasApproved = hasApproved;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public double getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public java.util.Date getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }

        public boolean hasApproved() {
            return hasApproved;
        }
    }

    public boolean hasAdminApproved(String type, String id, String adminEmail) {
        return repository.hasAdminApproved(type, id, adminEmail);
    }

    public int getAdminCount() {
        return repository.getAdminCount();
    }

    public LiveData<Integer> getAdminCountLive() {
        return repository.getAdminCountLive();
    }

    public void getComprehensiveReport(MemberRepository.ReportCallback callback) {
        repository.getComprehensiveReport(callback);
    }

    // System Config & Logic
    public void updateSystemConfig(Object update, MemberRepository.ConfigCallback callback) {
        repository.updateSystemConfig(update, callback);
    }

    public void fetchSystemConfig(MemberRepository.ConfigCallback callback) {
        repository.fetchSystemConfig(callback);
    }

    public void checkLoanEligibility(double amount, int duration, MemberRepository.EligibilityCallback callback) {
        repository.checkLoanEligibility(amount, duration, callback);
    }

    public void getRepaymentSchedule(double amount, int duration, MemberRepository.RepaymentScheduleCallback callback) {
        repository.getRepaymentSchedule(amount, duration, callback);
    }

    public void getDashboardSummary(MemberRepository.SummaryCallback callback) {
        repository.getDashboardSummary((success, data, message) -> {
            if (success && data instanceof com.example.save.data.models.DashboardSummaryResponse) {
                dashboardCache.postValue((com.example.save.data.models.DashboardSummaryResponse) data);
            }
            callback.onResult(success, data, message);
        });
    }

    // --- Decentralized Approvals Implementation ---

    public void approvePayout(String payoutId, MemberRepository.ApprovalCallback callback) {
        repository.approvePayout(payoutId, callback);
    }

    public void processApproval(com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem item, boolean approve, MemberRepository.ApprovalCallback callback) {
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(getApplication());
        String adminPhone = session.getUserPhone();

        if ("LOAN".equalsIgnoreCase(item.getType())) {
            if (approve) {
                repository.initiateLoanApproval(item.getId(), adminPhone, callback);
            } else {
                repository.rejectLoanRequest(item.getId(), "Rejected by Admin", (success, message) -> callback.onResult(success, message));
            }
        } else if ("DISBURSEMENT".equalsIgnoreCase(item.getType())) {
            if (approve) {
                repository.approvePayout(item.getId(), callback);
            } else {
                callback.onResult(false, "Payout rejection not supported");
            }
        } else {
            if (approve) {
                repository.approveTransaction(item.getId(), adminPhone, callback);
            } else {
                callback.onResult(false, "Rejection not implemented for transactions yet");
            }
        }
    }

    public LiveData<List<com.example.save.data.models.ApprovalRequest>> getPendingApprovals() {
        String adminPhone = com.example.save.utils.SessionManager.getInstance(getApplication()).getUserPhone();
        androidx.lifecycle.MediatorLiveData<List<com.example.save.data.models.ApprovalRequest>> mediator = new androidx.lifecycle.MediatorLiveData<>();

        LiveData<List<com.example.save.data.models.TransactionWithApproval>> txSource = repository.getPendingTransactionsWithApproval(adminPhone);
        LiveData<List<com.example.save.data.models.LoanWithApproval>> loanSource = repository.getPendingLoansWithApproval(adminPhone);
        LiveData<List<com.example.save.data.models.ApprovalRequest>> payoutSource = repository.getPendingPayoutApprovals();

        mediator.addSource(txSource, txs -> combineToApprovalRequests(mediator, txs, loanSource.getValue(), payoutSource.getValue()));
        mediator.addSource(loanSource, loans -> combineToApprovalRequests(mediator, txSource.getValue(), loans, payoutSource.getValue()));
        mediator.addSource(payoutSource, payouts -> combineToApprovalRequests(mediator, txSource.getValue(), loanSource.getValue(), payouts));

        return mediator;
    }

    private void combineToApprovalRequests(
            androidx.lifecycle.MutableLiveData<List<com.example.save.data.models.ApprovalRequest>> mediator,
            List<com.example.save.data.models.TransactionWithApproval> transactions,
            List<com.example.save.data.models.LoanWithApproval> loans,
            List<com.example.save.data.models.ApprovalRequest> payouts) {

        java.util.List<com.example.save.data.models.ApprovalRequest> combined = new java.util.ArrayList<>();

        if (transactions != null) {
            for (com.example.save.data.models.TransactionWithApproval item : transactions) {
                combined.add(new com.example.save.data.models.ApprovalRequest(
                        item.transaction.getId(),
                        "PAYOUT",
                        item.transaction.getMemberName(),
                        item.transaction.getAmount(),
                        item.transaction.getDescription(),
                        item.transaction.getDate(),
                        item.transaction.getStatus(),
                        item.isApprovedByAdmin));
            }
        }

        if (loans != null) {
            for (com.example.save.data.models.LoanWithApproval item : loans) {
                combined.add(new com.example.save.data.models.ApprovalRequest(
                        item.loan.getId(),
                        "LOAN",
                        item.loan.getMemberName(),
                        item.loan.getAmount(),
                        item.loan.getReason(),
                        item.loan.getDateRequested(),
                        item.loan.getStatus(),
                        item.isApprovedByAdmin));
            }
        }

        if (payouts != null) {
            combined.addAll(payouts);
        }

        java.util.Collections.sort(combined, (a, b) -> {
            if (a.getDate() == null && b.getDate() == null) return 0;
            if (a.getDate() == null) return 1;
            if (b.getDate() == null) return -1;
            return b.getDate().compareTo(a.getDate());
        });
        mediator.setValue(combined);
    }
}
