package com.example.save.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.save.data.models.Member;
import com.example.save.data.repository.MemberRepository;

import java.util.List;

public class PayoutsViewModel extends AndroidViewModel {
    private final MemberRepository repository;

    public PayoutsViewModel(@NonNull Application application) {
        super(application);
        this.repository = MemberRepository.getInstance(application);
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
