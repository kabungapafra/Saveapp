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

    private MemberRepository() {
        members = new ArrayList<>();
        membersLiveData = new MutableLiveData<>();
        loadInitialData();
        calculateInitialBalance();
        updateLiveData();
    }

    public static synchronized MemberRepository getInstance() {
        if (instance == null) {
            instance = new MemberRepository();
        }
        return instance;
    }

    private void loadInitialData() {
        // Load initial dummy data
        members.add(new Member("Alice Johnson", "Member", true));
        members.add(new Member("Bob Smith", "Secretary", true));
        members.add(new Member("Charlie Brown", "Member", false));
        members.add(new Member("David Lee", "Treasurer", true));
        Member eve = new Member("Eve Adams", "Member", true);
        eve.setShortfallAmount(50000); // 50k shortfall
        members.add(eve);
    }

    public LiveData<List<Member>> getMembers() {
        return membersLiveData;
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Member member) {
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
}
