package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.databinding.FragmentLoansBinding;
import com.example.save.data.models.Loan;
import com.example.save.ui.adapters.LoanHistoryAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.ui.viewmodels.LoansViewModel;

import java.util.ArrayList;
import java.util.List;

public class LoansFragment extends Fragment {

    private static final String ARG_MEMBER_EMAIL = "member_email";

    private FragmentLoansBinding binding;
    private LoansViewModel loansViewModel;
    private MembersViewModel membersViewModel;
    private LoanHistoryAdapter historyAdapter;
    private String memberEmail;
    private List<com.example.save.data.models.LoanWithApproval> allLoans = new ArrayList<>();

    public static LoansFragment newInstance(String memberEmail) {
        LoansFragment fragment = new LoansFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEMBER_EMAIL, memberEmail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberEmail = getArguments().getString(ARG_MEMBER_EMAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loansViewModel = new ViewModelProvider(this).get(LoansViewModel.class);
        membersViewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        setupListeners();
        loadLoans();
    }

    private void setupRecyclerView() {
        historyAdapter = new LoanHistoryAdapter();
        binding.rvLoanHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvLoanHistory.setAdapter(historyAdapter);
    }

    private void loadLoans() {
        if (memberEmail != null) {
            // Reactive chain:
            // 1. Observe Member (to get name) ->
            // 2. Observe Admin Count ->
            // 3. Observe Loans with Approval Stats

            membersViewModel.getMemberByEmailLive(memberEmail).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    final String memberName = member.getName();
                    observeLoans(memberName);
                }
            });
        }
    }

    private void observeLoans(String memberName) {
        // Observe Admin Count first
        membersViewModel.getAdminCountLive().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                historyAdapter.setAdminCount(count);
            }
        });

        // Observe Loans
        // Observe Loans
        membersViewModel.getMemberLoansWithApproval(memberName).observe(getViewLifecycleOwner(), loans -> {
            if (loans != null) {
                allLoans.clear();
                allLoans.addAll(loans);
                filterLoans(binding.etSearch.getText().toString());
            }
        });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.cardRequestLoan.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(com.example.save.R.id.fragment_container, new LoanApplicationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.cardPayLoan.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(com.example.save.R.id.fragment_container, new LoanPaymentFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLoans(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterLoans(String query) {
        List<com.example.save.data.models.LoanWithApproval> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (com.example.save.data.models.LoanWithApproval loanWithApproval : allLoans) {
            if (loanWithApproval.loan != null) {
                String status = loanWithApproval.loan.getStatus() != null ? loanWithApproval.loan.getStatus().toLowerCase() : "";
                String amount = String.valueOf((int) loanWithApproval.loan.getAmount());
                if (status.contains(lowerQuery) || amount.contains(lowerQuery)) {
                    filtered.add(loanWithApproval);
                }
            }
        }

        historyAdapter.setLoans(filtered);

        if (filtered.isEmpty()) {
            binding.rvLoanHistory.setVisibility(View.GONE);
            binding.emptyStateLayout.getRoot().setVisibility(View.VISIBLE);
            binding.emptyStateLayout.tvEmptyTitle.setText("No loans found");
            binding.emptyStateLayout.tvEmptyMessage.setText(query.isEmpty() ? "Apply for a loan to get started" : "Try adjusting your search");
        } else {
            binding.rvLoanHistory.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
