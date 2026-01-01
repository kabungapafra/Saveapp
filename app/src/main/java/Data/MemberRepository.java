package Data;

import java.util.ArrayList;
import java.util.List;

import Models.Member;

public class MemberRepository {
    private static MemberRepository instance;
    private List<Member> members;
    private List<MemberChangeListener> listeners;

    private MemberRepository() {
        members = new ArrayList<>();
        listeners = new ArrayList<>();
        loadInitialData();
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
        members.add(new Member("Eve Adams", "Member", true));
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
