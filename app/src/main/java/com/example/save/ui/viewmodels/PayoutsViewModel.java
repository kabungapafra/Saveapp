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

    public boolean canExecutePayout() {
        return repository.canExecutePayout();
    }

    public double getBalanceShortfall() {
        return repository.getBalanceShortfall();
    }

    public void sendPaymentReminders(java.util.List<com.example.save.data.models.Member> members,
            android.content.Context context) {
        com.example.save.utils.NotificationHelper notificationHelper = new com.example.save.utils.NotificationHelper(
                context);
        for (com.example.save.data.models.Member member : members) {
            String title = "Payment Reminder";
            String body = "Hi " + member.getName() + ", you have a shortfall of UGX " +
                    String.format(java.util.Locale.US, "%,.0f", member.getShortfallAmount()) +
                    ". Please make your contribution soon.";
            notificationHelper.showNotification(title, body,
                    com.example.save.utils.NotificationHelper.CHANNEL_ID_PAYMENTS);
        }
    }

    public void resolveShortfall(Member member) {
        repository.resolveShortfall(member);
    }

    public double getPayoutAmount() {
        return repository.getPayoutAmount();
    }

    public double getRetentionPercentage() {
        return repository.getRetentionPercentage();
    }

    public double getNetPayoutAmount() {
        return repository.getNetPayoutAmount();
    }

    // --- Payout Improvements ---

    public void savePayoutRules(boolean autoPayoutEnabled, int dayOfMonth, double minReserve) {
        repository.savePayoutRules(autoPayoutEnabled, dayOfMonth, minReserve);
    }

    public boolean executePayout(Member member, double amount, boolean deferRemaining) {
        return repository.executePayout(member, amount, deferRemaining);
    }

    // Overload for backward compatibility if needed, using default amount
    // @Override // Remove @Override if it was checking against a superclass method
    // that doesn't exist or just fix duplication
    public boolean executePayout(Member member) {
        return repository.executePayout(member, getNetPayoutAmount(), false);
    }
}
