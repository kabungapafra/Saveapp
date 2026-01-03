package Data;

import java.util.ArrayList;
import java.util.List;

import Models.Member;

public class MemberRepository {
    private static MemberRepository instance;
    private List<Member> members;
    private List<MemberChangeListener> listeners;
    private double groupBalance = 0;

    private MemberRepository() {
        members = new ArrayList<>();
        listeners = new ArrayList<>();
        loadInitialData();
        calculateInitialBalance();
    }

    public static MemberRepository getInstance() {
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

    public List<Member> getAllMembers() {
        return new ArrayList<>(members);
    }

    public void addMember(Member member) {
        members.add(0, member);
        notifyListeners();
    }

    public void removeMember(int position) {
        if (position >= 0 && position < members.size()) {
            members.remove(position);
            notifyListeners();
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
            notifyListeners();
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
        notifyListeners();
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
            notifyListeners();
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
                notifyListeners();
            }
        }
    }

    // Listener pattern for updates
    public void addListener(MemberChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(MemberChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (MemberChangeListener listener : listeners) {
            listener.onMembersChanged();
        }
    }

    public interface MemberChangeListener {
        void onMembersChanged();
    }
}
