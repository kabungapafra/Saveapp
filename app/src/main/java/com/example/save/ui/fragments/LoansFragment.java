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
        // We need the member's name to filter loans, so first we might need to get the
        // member by email
        // if we only have email. Or we can rely on email if loans stored email (they
        // store memberId usually, or name).
        // LoanEntity has memberId and memberName.

        if (memberEmail != null) {
            new Thread(() -> {
                com.example.save.data.models.Member member = membersViewModel.getMemberByEmail(memberEmail);
                if (member != null) {
                    final String memberName = member.getName();
                    getActivity().runOnUiThread(() -> {
                        observeLoans(memberName);
                    });
                }
            }).start();
        } else {
            // Fallback or show all if admin (logic depends on requirements, assuming empty
            // for now)
        }
    }

    private void observeLoans(String memberName) {
        loansViewModel.getLoans().observe(getViewLifecycleOwner(), loans -> {
            if (loans != null) {
                List<Loan> paidLoans = new ArrayList<>();
                for (Loan loan : loans) {
                    // Filter by member name and status
                    // Status matching logic: Check for "PAID" or "PAID_OFF"
                    if (loan.getMemberName() != null && loan.getMemberName().equals(memberName) &&
                            ("PAID".equalsIgnoreCase(loan.getStatus())
                                    || "PAID_OFF".equalsIgnoreCase(loan.getStatus()))) {
                        paidLoans.add(loan);
                    }
                }
                historyAdapter.setLoans(paidLoans);
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
