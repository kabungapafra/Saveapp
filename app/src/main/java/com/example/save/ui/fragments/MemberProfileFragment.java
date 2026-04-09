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
import com.example.save.data.models.Member;
import com.example.save.data.models.Transaction;
import com.example.save.databinding.FragmentMemberProfileBinding;
import com.example.save.ui.adapters.MemberProfileHistoryAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.DesignMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        
        if (DesignMode.IS_DESIGN_MODE || (memberEmail != null && memberEmail.endsWith("@design.com"))) {
            // Display already matches perfectly via XML for design mode
            setupDesignMock();
        } else {
            loadMemberData();
        }

        return binding.getRoot();
    }

    private void setupDesignMock() {
        // Init mock transaction history for design parity
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction("Monthly Contribution", "Oct 10, 2023", 500, true, R.drawable.ic_money, 0));
        transactions.add(new Transaction("Loan Repayment", "Sep 15, 2023", 1300, false, R.drawable.ic_loan, 0));
        transactions.add(new Transaction("Monthly Contribution", "Sep 10, 2023", 500, true, R.drawable.ic_money, 0));
        transactions.add(new Transaction("Loan Repayment", "Aug 15, 2023", 1300, false, R.drawable.ic_loan, 0));

        MemberProfileHistoryAdapter adapter = new MemberProfileHistoryAdapter(transactions);
        binding.rvTransactions.setAdapter(adapter);
    }

    private void setupToolbar() {
        binding.toolbar.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });
    }

    private void loadMemberData() {
        if (memberEmail == null) return;

        viewModel.getMemberByEmailLive(memberEmail).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                updateUI(member);
                loadTransactions(member.getName());
            }
        });
    }

    private void updateUI(Member member) {
        binding.tvMemberName.setText(member.getName());
        binding.tvMemberRole.setText("Member since January 2024");
        
        // Initials
        if (member.getName() != null && !member.getName().isEmpty()) {
            String[] parts = member.getName().split(" ");
            String initials = "";
            if (parts.length > 0 && !parts[0].isEmpty())
                initials += parts[0].charAt(0);
            if (parts.length > 1 && !parts[1].isEmpty())
                initials += parts[1].charAt(0);
            binding.tvProfileInitials.setText(initials.toUpperCase());
            binding.tvProfileInitials.setVisibility(View.VISIBLE);
            binding.ivProfileAvatar.setVisibility(View.GONE);
        }

        double balance = 1550.00; // default for mock if real data not present
        if (member.getContributionPaid() > 0) {
            balance = member.getContributionPaid();
        }
        binding.tvSavingsBalance.setText(String.format(Locale.getDefault(), "$%,.2f", balance));
        
        binding.tvTotalContributed.setText(String.format(Locale.getDefault(), "$%,.2f", balance + 2650.00));
        
        // Active Loans
        int loans = member.getShortfallAmount() > 0 ? 1 : 0;
        binding.tvStreak.setText(String.valueOf(loans)); // Note: ID is tvStreak mapped to Active Loans count in design
    }

    private void loadTransactions(String memberName) {
        viewModel.getLatestMemberTransactions(memberName).observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                List<Transaction> transactions = new ArrayList<>();
                for (com.example.save.data.local.entities.TransactionEntity entity : entities) {
                    transactions.add(new Transaction(
                            entity.getDescription(),
                            new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                    .format(entity.getDate()),
                            entity.getAmount(),
                            entity.isPositive(),
                            entity.isPositive() ? R.drawable.ic_money : R.drawable.ic_loan,
                            0));
                }

                MemberProfileHistoryAdapter adapter = new MemberProfileHistoryAdapter(transactions);
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
