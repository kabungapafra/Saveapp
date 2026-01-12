package com.example.save.data.repository;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.save.data.models.Member;
import java.util.ArrayList;
import java.util.List;

import com.example.save.data.models.Member;

public class MemberRepository {
    private static MemberRepository instance;
    private List<Member> members;
    private MutableLiveData<List<Member>> membersLiveData;
    private double groupBalance;
    private double contributionTarget; // Admin-configurable contribution target for all members

    private MemberRepository() {
        members = new ArrayList<>();
        membersLiveData = new MutableLiveData<>();
        contributionTarget = 1000000; // Default: 1M UGX

        // Simulate network delay for loading state demonstration
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loadInitialData();
            calculateInitialBalance();
            updateLiveData();
        }, 1500); // 1.5 second delay
    }

    public static synchronized MemberRepository getInstance() {
        if (instance == null) {
            instance = new MemberRepository();
        }
        return instance;
    }

    private void loadInitialData() {
        // Load initial dummy data
        Member alice = new Member("Alice Johnson", "Member", true);
        alice.setContributionTarget(contributionTarget);
        members.add(alice);

        Member bob = new Member("Bob Smith", "Secretary", true);
        bob.setContributionTarget(contributionTarget);
        members.add(bob);

        Member charlie = new Member("Charlie Brown", "Member", false);
        charlie.setContributionTarget(contributionTarget);
        members.add(charlie);

        Member david = new Member("David Lee", "Treasurer", true);
        david.setContributionTarget(contributionTarget);
        members.add(david);

        Member eve = new Member("Eve Adams", "Member", true);
        eve.setShortfallAmount(50000); // 50k shortfall
        eve.setContributionTarget(contributionTarget);
        members.add(eve);

        Member admin = new Member("Admin", "Administrator", true);
        admin.setContributionTarget(contributionTarget);
        members.add(admin); // Add Admin for payment features
    }

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Member member) {
        member.setContributionTarget(contributionTarget); // Set target for new member
        members.add(0, member);
        updateLiveData();
    }

    public void removeMember(int position) {
        if (position >= 0 && position < members.size()) {
            members.remove(position);
            updateLiveData();
        }
    }

    public List<Member> getMembersWithShortfalls() {
        List<Member> shortfalls = new ArrayList<>();
        for (Member member : members) {
            if (member.getShortfallAmount() > 0) {
                shortfalls.add(member);
            }
        }
        return shortfalls;
    }

    public List<Member> getAdmins() {
        List<Member> admins = new ArrayList<>();
        for (Member member : members) {
            if (member.getRole() != null && (member.getRole().equalsIgnoreCase("Administrator")
                    || member.getRole().equalsIgnoreCase("Admin"))) {
                admins.add(member);
            }
        }
        return admins;
    }

    public void updateMember(int position, Member member) {
        if (position >= 0 && position < members.size()) {
            members.set(position, member);
            updateLiveData();
        }
    }

    public int getActiveMemberCount() {
        int count = 0;
        for (Member member : members) {
            if (member.isActive()) {
                count++;
            }
        }
        return count;
    }

    public int getTotalMemberCount() {
        return members.size();
    }

    private void calculateInitialBalance() {
        // Dummy logic: mostly just a static starting point
        groupBalance = 1500000;
        // Adjust for payouts if we had real data
    }

    public double getGroupBalance() {
        return groupBalance;
    }

    public void addToBalance(double amount) {
        groupBalance += amount;
        updateLiveData();
    }

    public Member getNextPayoutRecipient() {
        // Simple round-robin or first eligible member logic
        for (Member member : members) {
            if (!member.hasReceivedPayout() && member.isActive()) {
                return member;
            }
        }
        return null; // All have received or no active members
    }

    public boolean executePayout(Member member) {
        if (member == null)
            return false;

        double payoutAmount = 500000; // Fixed dummy amount for now

        if (groupBalance >= payoutAmount) {
            groupBalance -= payoutAmount;
            member.setHasReceivedPayout(true);
            member.setPayoutAmount(String.valueOf(payoutAmount));
            member.setPayoutDate(java.text.DateFormat.getDateInstance().format(new java.util.Date()));
            updateLiveData();
            return true;
        }
        return false;
    }

    public void resolveShortfall(Member member) {
        if (member != null && member.getShortfallAmount() > 0) {
            // Logic: Deduct from group balance to cover the member's shortfall
            // Ideally we'd have a separate "Reserve" fund, but for now we use groupBalance
            if (groupBalance >= member.getShortfallAmount()) {
                groupBalance -= member.getShortfallAmount();
                member.setShortfallAmount(0);
                updateLiveData();
            }
        }
    }

    private void updateLiveData() {
        membersLiveData.setValue(new ArrayList<>(members));
    }

    public Member getMemberByName(String name) {
        for (Member member : members) {
            if (member.getName().equalsIgnoreCase(name)) {
                return member;
            }
        }
        return null;
    }

    public void makePayment(Member member, double amount) {
        if (member != null) {
            member.setContributionPaid(member.getContributionPaid() + amount);
            addToBalance(amount);
            // In a real app, we would record the transaction date/type here
            updateLiveData();
        }
    }

    // Contribution Target Management
    public double getContributionTarget() {
        return contributionTarget;
    }

    public void setContributionTarget(double target) {
        if (target > 0) {
            this.contributionTarget = target;
            updateAllMembersTarget(target);
        }
    }

    private void updateAllMembersTarget(double newTarget) {
        for (Member member : members) {
            member.setContributionTarget(newTarget);
        }
        updateLiveData();
    }

    // Loan Configuration
    private double maxLoanAmount = 500000;
    private double loanInterestRate = 5.0;
    private int maxLoanDuration = 12;
    private boolean requireGuarantor = true;

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
    private List<com.example.save.data.models.LoanRequest> loanRequests = new ArrayList<>();
    private MutableLiveData<List<com.example.save.data.models.LoanRequest>> loanRequestsLiveData = new MutableLiveData<>();

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
                request.setStatus("APPROVED");
                loanRequestsLiveData.setValue(new ArrayList<>(loanRequests));
                break;
            }
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
}
