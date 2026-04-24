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
        initViews(view);
        setupSwitch();
        setupButtons(view);
    }

    private void initViews(View view) {
        switchConfirm = view.findViewById(R.id.switchConfirm);
        btnConfirmApproval = view.findViewById(R.id.btnConfirmApproval);
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

        btnConfirmApproval.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Loan Approved Successfully", Toast.LENGTH_SHORT).show();
            // Implement actual approval logic here
        });
    }
}
