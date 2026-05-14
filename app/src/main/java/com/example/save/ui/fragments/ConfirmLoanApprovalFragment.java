package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class ConfirmLoanApprovalFragment extends Fragment {

    private MaterialSwitch switchConfirm;
    private LinearLayout btnConfirmApproval;
    private String loanId, borrowerName, amountStr;

    public static ConfirmLoanApprovalFragment newInstance(String loanId, String borrower, String amount) {
        ConfirmLoanApprovalFragment fragment = new ConfirmLoanApprovalFragment();
        Bundle args = new Bundle();
        args.putString("loan_id", loanId);
        args.putString("borrower", borrower);
        args.putString("amount", amount);
        fragment.setArguments(args);
        return fragment;
    }

    public static ConfirmLoanApprovalFragment newInstance() {
        return new ConfirmLoanApprovalFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_loan_approval, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            loanId = getArguments().getString("loan_id");
            borrowerName = getArguments().getString("borrower");
            amountStr = getArguments().getString("amount");
        }

        initViews(view);
        setupSwitch();
        setupButtons(view);
        populateData();
    }

    private void initViews(View view) {
        switchConfirm = view.findViewById(R.id.switchConfirm);
        btnConfirmApproval = view.findViewById(R.id.btnConfirmApproval);
    }

    private void populateData() {
        if (borrowerName != null) {
            ((TextView) getView().findViewById(R.id.tvBorrowerNameSummary)).setText(borrowerName);
        }
        if (amountStr != null) {
            ((TextView) getView().findViewById(R.id.tvAmountSummary)).setText(amountStr);
        }
    }

    private void setupSwitch() {
        switchConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                btnConfirmApproval.setAlpha(1.0f);
                btnConfirmApproval.setClickable(true);
                btnConfirmApproval.setFocusable(true);
            } else {
                btnConfirmApproval.setAlpha(0.5f);
                btnConfirmApproval.setClickable(false);
                btnConfirmApproval.setFocusable(false);
            }
        });
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        view.findViewById(R.id.btnGoBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        view.findViewById(R.id.btnDeclineRequest).setOnClickListener(v -> {
            if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                        .loadFragment(DeclineLoanRequestFragment.newInstance(loanId, borrowerName, amountStr, "Loan Request"), true);
            }
        });

        btnConfirmApproval.setOnClickListener(v -> {
            com.example.save.ui.viewmodels.MembersViewModel viewModel = 
                new androidx.lifecycle.ViewModelProvider(requireActivity()).get(com.example.save.ui.viewmodels.MembersViewModel.class);
            
            com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
            
            if (loanId != null) {
                viewModel.approveLoan(loanId, session.getUserEmail(), (success, message) -> {
                    if (!isAdded() || getContext() == null) return;
                    if (success) {
                        navigateToSuccess();
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                navigateToSuccess();
            }
        });
    }

    private void navigateToSuccess() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, LoanApprovedSuccessFragment.newInstance(borrowerName, amountStr))
                    .addToBackStack(null)
                    .commit();
        }
    }
}
