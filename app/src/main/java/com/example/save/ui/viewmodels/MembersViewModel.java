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

    public List<Member> getAdmins() {
        return repository.getAdmins();
    }

    public void addMember(Member member) {
        repository.addMember(member);
    }

    public void updateMember(int position, Member member) {
        repository.updateMember(position, member);
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

    public Member getMemberByName(String name) {
        return repository.getMemberByName(name);
    }

    public void makePayment(Member member, double amount) {
        repository.makePayment(member, amount);
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

    // Loan Requests
    public void submitLoanRequest(com.example.save.data.models.LoanRequest request) {
        repository.submitLoanRequest(request);
    }

    public LiveData<List<com.example.save.data.models.LoanRequest>> getLoanRequests() {
        return repository.getLoanRequests();
    }

    public List<com.example.save.data.models.LoanRequest> getPendingLoanRequests() {
        return repository.getPendingLoanRequests();
    }

    public void approveLoanRequest(String requestId) {
        repository.approveLoanRequest(requestId);
    }

    public void rejectLoanRequest(String requestId) {
        repository.rejectLoanRequest(requestId);
    }
}
