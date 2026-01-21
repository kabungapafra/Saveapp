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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MemberRepository {
    private static MemberRepository instance;
    private final MemberDao memberDao;
    private final TransactionDao transactionDao;
    private final LoanDao loanDao;
    private final Executor executor;
    private LiveData<List<Member>> membersLiveData;
    private MutableLiveData<Double> groupBalance;
    private double contributionTarget;

    // Loan Configuration
    private double maxLoanAmount = 500000;
    private double loanInterestRate = 5.0;
    private int maxLoanDuration = 12;
    private boolean requireGuarantor = true;

    // Loan Requests Management
    private List<com.example.save.data.models.LoanRequest> loanRequests = new ArrayList<>();
    private MutableLiveData<List<com.example.save.data.models.LoanRequest>> loanRequestsLiveData = new MutableLiveData<>();

    private MemberRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        memberDao = database.memberDao();
        transactionDao = database.transactionDao();
        loanDao = database.loanDao();
        executor = Executors.newSingleThreadExecutor();
        contributionTarget = 1000000; // Default: 1M UGX
        groupBalance = new MutableLiveData<>(1500000.0); // Initialize LiveData

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

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public List<Member> getAllMembers() {
        // This needs to run on background thread
        List<MemberEntity> entities = memberDao.getAllMembersSync();
        return convertEntitiesToModels(entities);
    }

    public void addMember(Member member) {
        executor.execute(() -> {
            MemberEntity entity = convertModelToEntity(member);
            entity.setContributionTarget(contributionTarget);
            memberDao.insert(entity);
        });
    }

    private void calculateInitialBalance() {
        executor.execute(() -> {
            // Simple logic: Sum all contributionPaid from members
            List<MemberEntity> allMembers = memberDao.getAllMembersSync();
            double total = 0;
            for (MemberEntity m : allMembers) {
                total += m.getContributionPaid();
            }
            groupBalance.postValue(total);
        });
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

    public String resetPassword(Member member) {
        String newOtp = com.example.save.utils.ValidationUtils.generateOTP();
        if (member != null) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setPassword(newOtp); // In real app, hash this
                    entity.setFirstLogin(true); // Reset to first login since it's a new OTP
                    memberDao.update(entity);
                }
            });
        }
        return newOtp;
    }

    /**
     * Change member password after first OTP login
     * Marks isFirstLogin as false so OTP expires
     */
    public void changePassword(Member member, String newPassword) {
        if (member != null && newPassword != null) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setPassword(newPassword); // In production, hash this
                    entity.setFirstLogin(false); // Mark as no longer first login
                    memberDao.update(entity);
                }
            });
        }
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

    public int getTotalMemberCount() {
        return memberDao.getMemberCount();
    }

    public LiveData<Double> getGroupBalance() {
        return groupBalance;
    }

    public void addToBalance(double amount) {
        double current = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;
        groupBalance.postValue(current + amount);
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

    public boolean executePayout(Member member) {
        if (member == null)
            return false;

        double payoutAmount = 500000;
        double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;

        if (currentBalance >= payoutAmount) {
            groupBalance.postValue(currentBalance - payoutAmount);
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setHasReceivedPayout(true);
                    entity.setPayoutAmount(String.valueOf(payoutAmount));
                    entity.setPayoutDate(java.text.DateFormat.getDateInstance().format(new java.util.Date()));
                    memberDao.update(entity);
                }
            });
            return true;
        }
        return false;
    }

    public void resolveShortfall(Member member) {
        if (member != null && member.getShortfallAmount() > 0) {
            double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;

            if (currentBalance >= member.getShortfallAmount()) {
                groupBalance.postValue(currentBalance - member.getShortfallAmount());
                executor.execute(() -> {
                    MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                    if (entity != null) {
                        entity.setShortfallAmount(0);
                        memberDao.update(entity);
                    }
                });
            }
        }
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
        MemberEntity entity = memberDao.getMemberByPhone(phone);
        return entity != null ? convertEntityToModel(entity) : null;
    }

    // Synchronous method explicitly for background thread use
    public Member getMemberByNameSync() {
        MemberEntity entity = memberDao.getMemberByName("Admin");
        return entity != null ? convertEntityToModel(entity) : null;
    }

    public void makePayment(Member member, double amount, String phoneNumber) {
        if (member != null) {
            addToBalance(amount);
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setContributionPaid(entity.getContributionPaid() + amount);
                    memberDao.update(entity);

                    // Log Transaction
                    TransactionEntity tx = new TransactionEntity(
                            "CONTRIBUTION",
                            amount,
                            "Contribution from " + member.getName(),
                            new java.util.Date(),
                            true // Money coming in
                    );
                    transactionDao.insert(tx);
                }
            });
        }
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

    // Loan Requests Management
    public void submitLoanRequest(com.example.save.data.models.LoanRequest request) {
        request.setInterestRate(loanInterestRate);
        request.setTotalRepayment(request.getAmount() + (request.getAmount() * loanInterestRate / 100));
        loanRequests.add(0, request);
        loanRequestsLiveData.setValue(new ArrayList<>(loanRequests));
    }

    public LiveData<List<com.example.save.data.models.LoanRequest>> getLoanRequests() {
        return loanRequestsLiveData;
    }

    public List<com.example.save.data.models.LoanRequest> getPendingLoanRequests() {
        List<com.example.save.data.models.LoanRequest> pending = new ArrayList<>();
        for (com.example.save.data.models.LoanRequest request : loanRequests) {
            if ("PENDING".equals(request.getStatus())) {
                pending.add(request);
            }
        }
        return pending;
    }

    public void approveLoanRequest(String requestId) {
        for (com.example.save.data.models.LoanRequest request : loanRequests) {
            if (request.getId().equals(requestId)) {
                if ("PENDING".equals(request.getStatus())) {
                    double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;
                    if (currentBalance >= request.getAmount()) {
                        groupBalance.postValue(currentBalance - request.getAmount());

                        // Log Transaction
                        executor.execute(() -> {
                            TransactionEntity tx = new TransactionEntity(
                                    "LOAN_PAYOUT",
                                    request.getAmount(),
                                    "Loan payout to " + request.getMemberName(),
                                    new java.util.Date(),
                                    false // Money leaving the group
                            );
                            transactionDao.insert(tx);
                        });

                        request.setStatus("APPROVED");
                        loanRequestsLiveData.setValue(new ArrayList<>(loanRequests));
                    }
                }
                break;
            }
        }
    }

    public void repayLoan(Member member, double amount) {
        if (member != null) {
            addToBalance(amount);
            executor.execute(() -> {
                // 1. Update Loan Entity
                // Find active loan for this member
                List<LoanEntity> activeLoans = loanDao.getActiveLoans(); // Assumes getActiveLoans filters by status
                for (LoanEntity loan : activeLoans) {
                    // Match by name or ID. System seems to use Name predominantly for member
                    // identification in loans
                    if (loan.getMemberName() != null && loan.getMemberName().equals(member.getName())) {
                        double newRepaid = loan.getRepaidAmount() + amount;
                        loan.setRepaidAmount(newRepaid);

                        // Check if fully paid
                        double totalDue = loan.getAmount() + loan.getInterest();
                        if (newRepaid >= totalDue) {
                            loan.setStatus("PAID");
                        }

                        loanDao.update(loan);
                        break; // Pay off one loan at a time
                    }
                }

                // 2. Log Transaction
                TransactionEntity tx = new TransactionEntity(
                        "LOAN_REPAYMENT",
                        amount,
                        "Loan repayment from " + member.getName(),
                        new java.util.Date(),
                        true // Money coming in
                );
                transactionDao.insert(tx);
            });
        }
    }

    public void rejectLoanRequest(String requestId) {
        for (com.example.save.data.models.LoanRequest request : loanRequests) {
            if (request.getId().equals(requestId)) {
                request.setStatus("REJECTED");
                loanRequestsLiveData.setValue(new ArrayList<>(loanRequests));
                break;
            }
        }
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
        member.setPassword(entity.getPassword());
        member.setPayoutDate(entity.getPayoutDate());
        member.setPayoutAmount(entity.getPayoutAmount());
        member.setHasReceivedPayout(entity.isHasReceivedPayout());
        member.setShortfallAmount(entity.getShortfallAmount());
        member.setContributionTarget(entity.getContributionTarget());
        member.setContributionPaid(entity.getContributionPaid());
        member.setFirstLogin(entity.isFirstLogin()); // Sync isFirstLogin field
        member.setPaymentStreak(entity.getPaymentStreak());
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
        // Password is not copied from model to entity (managed separately)
    }

    // Transaction Management
    public LiveData<List<TransactionEntity>> getRecentTransactions() {
        return transactionDao.getRecentTransactions();
    }
}
