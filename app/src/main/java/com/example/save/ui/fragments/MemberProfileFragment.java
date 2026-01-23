package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.R;
import com.example.save.data.models.Badge;
import com.example.save.data.models.Member;
import com.example.save.data.models.Transaction;
import com.example.save.databinding.FragmentMemberProfileBinding;
import com.example.save.ui.adapters.BadgeAdapter;
import com.example.save.ui.adapters.TransactionAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.ui.activities.MemberMainActivity;

import java.util.ArrayList;
import java.util.List;

public class MemberProfileFragment extends Fragment {

    private static final String ARG_EMAIL = "member_email";

    private FragmentMemberProfileBinding binding;
    private MembersViewModel viewModel;
    private String memberEmail;

    public static MemberProfileFragment newInstance(String email) {
        MemberProfileFragment fragment = new MemberProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberEmail = getArguments().getString(ARG_EMAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        setupToolbar();
        loadMemberData();

        return binding.getRoot();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });
    }

    private void loadMemberData() {
        if (memberEmail == null)
            return;

        viewModel.getMemberByEmailLive(memberEmail).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                updateUI(member);
                loadTransactions(member.getName());
            }
        });
    }

    private void updateUI(Member member) {
        binding.tvMemberName.setText(member.getName());
        binding.tvMemberRole.setText(member.getRole() + " • Joined Jan 2024");

        // Initials
        if (member.getName() != null && !member.getName().isEmpty()) {
            String[] parts = member.getName().split(" ");
            String initials = "";
            if (parts.length > 0)
                initials += parts[0].charAt(0);
            if (parts.length > 1)
                initials += parts[1].charAt(0);
            binding.tvProfileInitials.setText(initials.toUpperCase());
            binding.tvProfileInitials.setVisibility(View.VISIBLE);
            binding.ivProfileAvatar.setVisibility(View.GONE);
        }

        binding.tvEmail.setText(member.getEmail());
        binding.tvPhone.setText(member.getPhone());

        // Stats
        int streak = member.getPaymentStreak();
        binding.tvStreak.setText(String.valueOf(streak));

        // Use real calculation from ViewModel
        int score = viewModel.calculateCreditScore(member);
        binding.tvCreditScore.setText(String.valueOf(score));

        // Reliability (Mock calc: streak / (streak + missed)?)
        // Let's assume 98% base + small variance or perfect if streak high
        int reliability = 90 + Math.min(10, streak);
        binding.tvReliability.setText(reliability + "%");

        // Badges
        List<Badge> badges = new ArrayList<>();
        badges.add(new Badge("Bronze Saver", R.drawable.ic_stars, streak >= 1));
        badges.add(new Badge("Silver Saver", R.drawable.ic_stars, streak >= 6));
        badges.add(new Badge("Gold Saver", R.drawable.ic_stars, streak >= 12));

        BadgeAdapter badgeAdapter = new BadgeAdapter(badges);
        binding.rvBadges.setAdapter(badgeAdapter);
    }

    private void loadTransactions(String memberName) {
        viewModel.getLatestMemberTransactions(memberName).observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                List<Transaction> transactions = new ArrayList<>();
                for (com.example.save.data.local.entities.TransactionEntity entity : entities) {
                    int iconRes = entity.isPositive() ? R.drawable.ic_money : R.drawable.ic_loan;
                    int color = entity.isPositive() ? 0xFF4CAF50 : 0xFFF44336;

                    transactions.add(new Transaction(
                            entity.getDescription(),
                            new java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
                                    .format(entity.getDate()),
                            entity.getAmount(),
                            entity.isPositive(),
                            iconRes,
                            color));
                }
                TransactionAdapter adapter = new TransactionAdapter(transactions);
                binding.rvTransactions.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
