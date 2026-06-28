package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class WizardContributionFragment extends Fragment {

    private TextInputEditText etContributionAmount;
    private Spinner spinnerFrequency;
    private TextInputEditText etLatePenalty;
    private TextInputEditText etRetention;
    private TextInputEditText etMaxLoanAmount;
    private TextInputEditText etInterestRate;
    private TextInputEditText etRepaymentPeriod;
    private MaterialSwitch switchRequireGuarantor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_contribution, container, false);

        etContributionAmount = view.findViewById(R.id.etContributionAmount);
        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        etLatePenalty = view.findViewById(R.id.etLatePenalty);
        etRetention = view.findViewById(R.id.etRetention);
        etMaxLoanAmount = view.findViewById(R.id.etMaxLoanAmount);
        etInterestRate = view.findViewById(R.id.etInterestRate);
        etRepaymentPeriod = view.findViewById(R.id.etRepaymentPeriod);
        switchRequireGuarantor = view.findViewById(R.id.switchRequireGuarantor);

        setupFrequencySpinner();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();

            double amount = activity.getContributionAmount();
            if (amount > 0) etContributionAmount.setText(String.valueOf((int) amount));

            double penalty = activity.getLatePenalty();
            if (penalty > 0) etLatePenalty.setText(String.valueOf(penalty));

            double retention = activity.getRetentionPercentage();
            if (retention > 0) etRetention.setText(String.valueOf(retention));

            double maxLoan = activity.getMaxLoanAmount();
            if (maxLoan > 0) etMaxLoanAmount.setText(String.valueOf((int) maxLoan));

            double rate = activity.getInterestRate();
            if (rate > 0) etInterestRate.setText(String.valueOf(rate));

            int period = activity.getRepaymentPeriod();
            if (period > 0) etRepaymentPeriod.setText(String.valueOf(period));

            switchRequireGuarantor.setChecked(activity.isRequireGuarantor());

            String frequency = activity.getContributionFrequency();
            if (frequency != null && !frequency.isEmpty()) {
                for (int i = 0; i < spinnerFrequency.getCount(); i++) {
                    if (spinnerFrequency.getItemAtPosition(i).toString().equals(frequency)) {
                        spinnerFrequency.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void setupFrequencySpinner() {
        if (getContext() == null) return;

        List<String> frequencies = new ArrayList<>();
        frequencies.add("Weekly");
        frequencies.add("Bi-weekly");
        frequencies.add("Monthly");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, frequencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
        spinnerFrequency.setSelection(2);
    }

    public boolean validateAndSave() {
        if (etContributionAmount == null || etMaxLoanAmount == null) return false;

        String amountStr = etContributionAmount.getText().toString().trim();
        String penaltyStr = etLatePenalty.getText().toString().trim();
        String retentionStr = etRetention.getText().toString().trim();
        String maxLoanStr = etMaxLoanAmount.getText().toString().trim();
        String interestStr = etInterestRate.getText().toString().trim();
        String periodStr = etRepaymentPeriod.getText().toString().trim();

        if (amountStr.isEmpty()) {
            etContributionAmount.setError("Contribution amount is required");
            return false;
        }
        if (maxLoanStr.isEmpty()) {
            etMaxLoanAmount.setError("Max loan amount is required");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            double penalty = penaltyStr.isEmpty() ? 0.0 : Double.parseDouble(penaltyStr);
            double retention = retentionStr.isEmpty() ? 0.0 : Double.parseDouble(retentionStr);
            double maxLoan = Double.parseDouble(maxLoanStr);
            double interest = interestStr.isEmpty() ? 0.0 : Double.parseDouble(interestStr);
            int period = periodStr.isEmpty() ? 1 : Integer.parseInt(periodStr);
            boolean requireGuarantor = switchRequireGuarantor != null && switchRequireGuarantor.isChecked();
            String frequency = spinnerFrequency.getSelectedItem().toString();

            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setGroupInfo(activity.getGroupName(), "", activity.getCurrency(), amount);
                activity.setContributionSettings(frequency, penalty);
                activity.setRetentionPercentage(retention);
                activity.setLoanRules(maxLoan, interest, period, requireGuarantor);
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
