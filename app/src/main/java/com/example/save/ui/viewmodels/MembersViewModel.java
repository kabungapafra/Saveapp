package com.example.save.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.save.data.models.Member;
import com.example.save.data.repository.MemberRepository;

import java.util.List;

public class MembersViewModel extends AndroidViewModel {
    private final MemberRepository repository;

    public MembersViewModel(@NonNull Application application) {
        super(application);
        this.repository = MemberRepository.getInstance(application);
    }

    public LiveData<List<Member>> getMembers() {
        return repository.getMembers();
    }

    public LiveData<List<Member>> searchMembers(String query) {
        return repository.searchMembers(query);
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

    public void addMember(Member member, MemberRepository.MemberAddCallback callback) {
        repository.addMember(member, callback);
    }

    public void removeMember(Member member) {
        repository.removeMember(member);
    }

    public String resetPassword(Member member) {
        return repository.resetPassword(member);
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

    public Member getMemberByEmail(String email) {
        return repository.getMemberByEmail(email);
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
        repository.makePayment(member, amount, phoneNumber, paymentMethod, callback);
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

    public LiveData<List<com.example.save.data.models.LoanRequest>> getLoanRequests() {
        return repository.getLoanRequests();
    }

    public com.example.save.data.local.entities.LoanEntity getActiveLoanForMember(String memberName) {
        return repository.getActiveLoanForMember(memberName);
    }

    public List<com.example.save.data.models.LoanRequest> getPendingLoanRequests() {
        return repository.getPendingLoanRequests();
    }

    public void initiateLoanApproval(String requestId, String adminEmail, MemberRepository.ApprovalCallback callback) {
        repository.initiateLoanApproval(requestId, adminEmail, callback);
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

    public LiveData<List<com.example.save.data.local.entities.TransactionEntity>> getRecentTransactions() {
        return repository.getRecentTransactions();
    }

    public LiveData<List<com.example.save.data.local.entities.TransactionEntity>> getLatestMemberTransactions(
            String memberName) {
        return repository.getLatestMemberTransactions(memberName);
    }

    public LiveData<List<com.example.save.data.models.TransactionWithApproval>> getMemberTransactionsWithApproval(
            String memberName) {
        return repository.getMemberTransactionsWithApproval(memberName);
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
    public LiveData<Member> getMemberByEmailLive(String email) {
        androidx.lifecycle.MutableLiveData<Member> liveData = new androidx.lifecycle.MutableLiveData<>();
        repository.getExecutor().execute(() -> {
            Member member = repository.getMemberByEmail(email);
            liveData.postValue(member);
        });
        return liveData;
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

    // TODO: Add method to fetch loan eligibility from backend
    // public void getLoanEligibilityFromBackend(String memberEmail,
    // LoanEligibilityCallback callback)

    public LiveData<List<com.github.mikephil.charting.data.Entry>> getSavingsTrend() {
        return androidx.lifecycle.Transformations.map(repository.getGenericTransactions(), transactions -> {
            List<com.github.mikephil.charting.data.Entry> entries = new java.util.ArrayList<>();
            double cumulative = 0;
            // Sort by date just in case
            java.util.Collections.sort(transactions, (a, b) -> a.getDate().compareTo(b.getDate()));

            int index = 0;
            for (com.example.save.data.local.entities.TransactionEntity tx : transactions) {
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

    public void approveTransaction(long txId, String adminEmail, MemberRepository.ApprovalCallback callback) {
        repository.approveTransaction(txId, adminEmail, callback);
    }

    public void approveLoan(long loanId, String adminEmail, MemberRepository.ApprovalCallback callback) {
        repository.approveLoan(loanId, adminEmail, callback);
    }

    public LiveData<List<com.example.save.data.local.entities.TransactionEntity>> getPendingTransactions() {
        return repository.getPendingTransactions();
    }

    // New optimized reactive method
    public LiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> getCombinedApprovals(
            String adminEmail) {
        androidx.lifecycle.MediatorLiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> mediator = new androidx.lifecycle.MediatorLiveData<>();

        LiveData<List<com.example.save.data.models.TransactionWithApproval>> txSource = repository
                .getPendingTransactionsWithApproval(adminEmail);
        LiveData<List<com.example.save.data.models.LoanWithApproval>> loanSource = repository
                .getPendingLoansWithApproval(adminEmail);

        mediator.addSource(txSource, txs -> combineApprovals(mediator, txs, loanSource.getValue()));
        mediator.addSource(loanSource, loans -> combineApprovals(mediator, txSource.getValue(), loans));

        return mediator;
    }

    public LiveData<List<com.example.save.data.models.LoanWithApproval>> getMemberLoansWithApproval(String memberName) {
        return repository.getMemberLoansWithApproval(memberName);
    }

    private void combineApprovals(
            androidx.lifecycle.MutableLiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> mediator,
            List<com.example.save.data.models.TransactionWithApproval> transactions,
            List<com.example.save.data.models.LoanWithApproval> loans) {

        java.util.List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem> combined = new java.util.ArrayList<>();

        if (transactions != null) {
            for (com.example.save.data.models.TransactionWithApproval item : transactions) {
                // Include all pending items, even if already approved by this admin
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
                // Include all pending items, even if already approved by this admin
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

        // Sort by date (newest first)
        java.util.Collections.sort(combined, (a, b) -> b.getDate().compareTo(a.getDate()));

        mediator.setValue(combined);
    }

    // Static implementation to avoid leaks
    private static class ApprovalItemImpl implements com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem {
        private final long id;
        private final String type, title, description, status; // Keep status field
        private final double amount;
        private final java.util.Date date;
        private final boolean hasApproved;

        public ApprovalItemImpl(long id, String type, String title, double amount, String description,
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

        public long getId() {
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

    public boolean hasAdminApproved(String type, long id, String adminEmail) {
        return repository.hasAdminApproved(type, id, adminEmail);
    }

    public int getAdminCount() {
        return repository.getAdminCount();
    }

    public LiveData<Integer> getAdminCountLive() {
        return repository.getAdminCountLive();
    }
}
