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

    public void makePayment(Member member, double amount, String phoneNumber, String paymentMethod) {
        repository.makePayment(member, amount, phoneNumber, paymentMethod);
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

    // Loan Requests
    public void submitLoanRequest(com.example.save.data.models.LoanRequest request) {
        repository.submitLoanRequest(request);
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

    public void rejectLoanRequest(String requestId) {
        repository.rejectLoanRequest(requestId);
    }

    public void repayLoan(Member member, double amount) {
        repository.repayLoan(member, amount);
    }

    /**
     * Change password after first OTP login
     */
    public void changePassword(Member member, String newPassword) {
        repository.changePassword(member, newPassword);
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
    public double getLoanEligibility(Member member) {
        if (member == null)
            return 0;
        // Logic: 3x Savings
        return member.getContributionPaid() * 3;
    }

    public int calculateCreditScore(Member member) {
        if (member == null)
            return 300;

        int baseScore = 0;
        int maxScore = 850;

        // 1. Payment Streak: +10 points per month
        int streakPoints = member.getPaymentStreak() * 10;

        // 2. Savings: +1 point per 10,000 UGX
        int savingsPoints = (int) (member.getContributionPaid() / 10000);

        // 3. Loans: -50 points if shortfall exists (penalty)
        int penalty = member.getShortfallAmount() > 0 ? 50 : 0;

        int totalScore = baseScore + streakPoints + savingsPoints - penalty;

        return Math.min(totalScore, maxScore);
    }

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

    private void combineApprovals(
            androidx.lifecycle.MutableLiveData<List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem>> mediator,
            List<com.example.save.data.models.TransactionWithApproval> transactions,
            List<com.example.save.data.models.LoanWithApproval> loans) {

        java.util.List<com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem> combined = new java.util.ArrayList<>();

        if (transactions != null) {
            for (com.example.save.data.models.TransactionWithApproval item : transactions) {
                // Skip if current admin already approved
                if (!item.isApprovedByAdmin) {
                    combined.add(new ApprovalItemImpl(
                            item.transaction.getId(),
                            "PAYOUT",
                            item.transaction.getMemberName(),
                            item.transaction.getAmount(),
                            item.transaction.getDescription(),
                            item.transaction.getDate(),
                            item.approvalCount,
                            item.isApprovedByAdmin));
                }
            }
        }

        if (loans != null) {
            for (com.example.save.data.models.LoanWithApproval item : loans) {
                if (!item.isApprovedByAdmin) {
                    combined.add(new ApprovalItemImpl(
                            item.loan.getId(),
                            "LOAN",
                            item.loan.getMemberName(),
                            item.loan.getAmount(),
                            item.loan.getReason(),
                            item.loan.getDateRequested(),
                            item.approvalCount,
                            item.isApprovedByAdmin));
                }
            }
        }

        // Sort by date (newest first)
        java.util.Collections.sort(combined, (a, b) -> b.getDate().compareTo(a.getDate()));

        mediator.setValue(combined);
    }

    // Static implementation to avoid leaks
    private static class ApprovalItemImpl implements com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem {
        private final long id;
        private final String type, title, description;
        private final double amount;
        private final java.util.Date date;
        private final int approvalCount;
        private final boolean hasApproved;

        public ApprovalItemImpl(long id, String type, String title, double amount, String description,
                java.util.Date date, int approvalCount, boolean hasApproved) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.amount = amount;
            this.description = description;
            this.date = date;
            this.approvalCount = approvalCount;
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

        public int getApprovalCount() {
            return approvalCount;
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
