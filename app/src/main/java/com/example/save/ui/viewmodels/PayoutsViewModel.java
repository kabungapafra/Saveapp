package com.example.save.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.save.data.models.Member;
import com.example.save.data.repository.MemberRepository;
import java.util.List;

public class PayoutsViewModel extends ViewModel {
    private final MemberRepository repository;

    public PayoutsViewModel() {
        this.repository = MemberRepository.getInstance();
    }

    public LiveData<List<Member>> getMembers() {
        return repository.getMembers();
    }

    public Member getNextPayoutRecipient() {
        return repository.getNextPayoutRecipient();
    }

    public boolean executePayout(Member member) {
        return repository.executePayout(member);
    }

    public void resolveShortfall(Member member) {
        repository.resolveShortfall(member);
    }
}
