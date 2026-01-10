package com.example.save.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.save.data.models.Member;
import com.example.save.data.repository.MemberRepository;
import java.util.List;

public class MembersViewModel extends ViewModel {
    private final MemberRepository repository;

    public MembersViewModel() {
        this.repository = MemberRepository.getInstance();
    }

    public LiveData<List<Member>> getMembers() {
        return repository.getMembers();
    }

    public void addMember(Member member) {
        repository.addMember(member);
    }

    public double getGroupBalance() {
        return repository.getGroupBalance();
    }

    public int getActiveMemberCount() {
        return repository.getActiveMemberCount();
    }

    public int getTotalMemberCount() {
        return repository.getTotalMemberCount();
    }
}
