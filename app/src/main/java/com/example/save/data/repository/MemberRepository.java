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
    private final android.content.SharedPreferences prefs;
    private LiveData<List<Member>> membersLiveData;
    private LiveData<Double> groupBalance;
    private double contributionTarget;
    private double payoutAmount;
    private double retentionPercentage;

    // Loan Configuration
    private double maxLoanAmount = 500000;
    private double loanInterestRate = 5.0;
    private int maxLoanDuration = 12;
    private boolean requireGuarantor = true;

    private MemberRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        memberDao = database.memberDao();
        transactionDao = database.transactionDao();
        loanDao = database.loanDao();
        executor = Executors.newSingleThreadExecutor();
        prefs = context.getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);

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

    public boolean executePayout(Member member) {
        if (member == null)
            return false;

        if (member == null)
            return false;

        double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;
        double netPayout = getNetPayoutAmount();

        if (currentBalance >= netPayout) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setHasReceivedPayout(true);
                    entity.setPayoutAmount(String.valueOf(payoutAmount)); // Store Gross Amount or Net? Logic usually
                                                                          // implies what they got. Storing Gross for
                                                                          // records.
                    entity.setPayoutDate(java.text.DateFormat.getDateInstance().format(new java.util.Date()));
                    memberDao.update(entity);

                    // Log Transaction for Payout (Net Amount Leaving)
                    TransactionEntity tx = new TransactionEntity(
                            member.getName(),
                            "MEMBER_PAYOUT",
                            netPayout,
                            "Member payout to " + member.getName(),
                            new java.util.Date(),
                            false, // Money leaving the group
                            "Bank" // Default
                    );
                    transactionDao.insert(tx);
                }
            });
            return true;
        }
        return false;
    }

    public boolean canExecutePayout() {
        double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;
        return currentBalance >= getNetPayoutAmount();
    }

    public double getBalanceShortfall() {
        double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;
        return Math.max(0, getNetPayoutAmount() - currentBalance);
    }

    public void resolveShortfall(Member member) {
        if (member != null && member.getShortfallAmount() > 0) {
            double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;

            if (currentBalance >= member.getShortfallAmount()) {
                executor.execute(() -> {
                    MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                    if (entity != null) {
                        entity.setShortfallAmount(0);
                        memberDao.update(entity);

                        // Log Transaction for Shortfall Resolution
                        TransactionEntity tx = new TransactionEntity(
                                member.getName(),
                                "SHORTFALL_RESOLUTION",
                                member.getShortfallAmount(),
                                "Shortfall resolution for " + member.getName(),
                                new java.util.Date(),
                                false, // Money leaving the group
                                "System");
                        transactionDao.insert(tx);
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

    public void makePayment(Member member, double amount, String phoneNumber, String paymentMethod) {
        if (member != null) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    entity.setContributionPaid(entity.getContributionPaid() + amount);

                    // Update Streak (Simple logic: +1 per payment for now)
                    entity.setPaymentStreak(entity.getPaymentStreak() + 1);

                    memberDao.update(entity);

                    // Log Transaction
                    TransactionEntity tx = new TransactionEntity(
                            member.getName(),
                            "CONTRIBUTION",
                            amount,
                            "Contribution from " + member.getName(),
                            new java.util.Date(),
                            true, // Money coming in
                            paymentMethod);
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
        executor.execute(() -> {
            // Create LoanEntity from LoanRequest
            LoanEntity entity = new LoanEntity();
            entity.setExternalId(java.util.UUID.randomUUID().toString());

            // Find member by name to get ID
            MemberEntity member = memberDao.getMemberByName(request.getMemberName());
            if (member != null) {
                entity.setMemberId(String.valueOf(member.getId()));
            } else {
                entity.setMemberId("0"); // Fallback
            }

            entity.setMemberName(request.getMemberName());
            entity.setAmount(request.getAmount());

            // Calculate interest amount
            double interestAmount = request.getAmount() * loanInterestRate / 100.0;
            entity.setInterest(interestAmount);

            entity.setReason(request.getReason());
            entity.setDateRequested(new java.util.Date());
            entity.setStatus("PENDING");
            entity.setDueDate(new java.util.Date(
                    System.currentTimeMillis() + (long) request.getDurationMonths() * 30L * 24L * 60L * 60L * 1000L));

            loanDao.insert(entity);
        });
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
                req.setId(entity.getExternalId());
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
            req.setId(entity.getExternalId());
            req.setStatus(entity.getStatus());
            requests.add(req);
        }
        return requests;
    }

    public LoanEntity getActiveLoanForMember(String memberName) {
        return loanDao.getActiveLoanByMemberName(memberName);
    }

    public boolean approveLoanRequest(String requestId) {
        LoanEntity entity = loanDao.getLoanByExternalId(requestId);
        if (entity == null || !"PENDING".equals(entity.getStatus())) {
            return false;
        }

        double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;
        if (currentBalance >= entity.getAmount()) {
            executor.execute(() -> {
                // Log Transaction
                TransactionEntity tx = new TransactionEntity(
                        entity.getMemberName(),
                        "LOAN_PAYOUT",
                        entity.getAmount(),
                        "Loan payout to " + entity.getMemberName(),
                        new java.util.Date(),
                        false, // Money leaving the group
                        "Bank");
                transactionDao.insert(tx);

                // Update Loan Status in DB
                entity.setStatus("ACTIVE");
                // Set due date (simple 1 month logic if not specified)
                entity.setDueDate(new java.util.Date(
                        System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L));
                loanDao.update(entity);
            });
            return true;
        }
        return false;
    }

    public void repayLoan(Member member, double amount) {
        if (member != null) {
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
                        member.getName(),
                        "LOAN_REPAYMENT",
                        amount,
                        "Loan repayment from " + member.getName(),
                        new java.util.Date(),
                        true, // Money coming in
                        "Phone");
                transactionDao.insert(tx);
            });
        }
    }

    public void rejectLoanRequest(String requestId) {
        executor.execute(() -> {
            LoanEntity entity = loanDao.getLoanByExternalId(requestId);
            if (entity != null && "PENDING".equals(entity.getStatus())) {
                entity.setStatus("REJECTED");
                loanDao.update(entity);
            }
        });
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

    public double getNetPayoutAmount() {
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

    public boolean executePayout(Member member, double amount, boolean deferRemaining) {
        if (member == null)
            return false;

        double currentBalance = groupBalance.getValue() != null ? groupBalance.getValue() : 0.0;

        // Check balance logic (using amount requested)
        if (currentBalance >= amount) {
            executor.execute(() -> {
                MemberEntity entity = memberDao.getMemberByEmail(member.getEmail());
                if (entity != null) {
                    // Update Member Payout Info
                    entity.setHasReceivedPayout(true);
                    entity.setPayoutAmount(String.valueOf(amount));
                    entity.setPayoutDate(java.text.DateFormat.getDateInstance().format(new java.util.Date()));

                    if (deferRemaining) {
                        // Defer logic: calculate remainder (Net Payout - Amount Paid)
                        double fullNet = getNetPayoutAmount();
                        double remainder = fullNet - amount;
                        if (remainder > 0) {
                            // Where do we store deferrals?
                            // For V1, we might just note it in a separate field or just add to 'shortfall'
                            // (negative shortfall = credit?)
                            // OR, create a new "Deferred Payout" transaction/record.
                            // Simplified: Just log the partial payout.
                            // The system keeps track that they HAVE received *a* payout.
                            // If we want to pay remainder later, we need to track it.
                            // Let's assume for now deferrals are manual tracking or we add a
                            // "PayoutBalance" field later.
                            // Logging it in transaction description for now.
                        }
                    }

                    memberDao.update(entity);

                    // Log Transaction
                    TransactionEntity tx = new TransactionEntity(
                            member.getName(),
                            "MEMBER_PAYOUT",
                            amount,
                            "Payout to " + member.getName() + (deferRemaining ? " (Partial)" : ""),
                            new java.util.Date(),
                            false, // Money leaving
                            "Bank");
                    transactionDao.insert(tx);
                }
            });
            return true;
        }
        return false;
    }
}
