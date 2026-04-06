package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class WizardFinancialRulesFragment extends Fragment {

    private TextInputEditText etPayoutAmount;
    private TextInputEditText etRetention;
    private TextInputEditText etMaxLoanAmount;
    private TextInputEditText etInterestRate;
    private TextInputEditText etRepaymentPeriod;
    private SwitchMaterial switchRequireGuarantor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_financial_rules, container, false);

        etPayoutAmount = view.findViewById(R.id.etPayoutAmount);
        etRetention = view.findViewById(R.id.etRetention);
        etMaxLoanAmount = view.findViewById(R.id.etMaxLoanAmount);
        etInterestRate = view.findViewById(R.id.etInterestRate);
        etRepaymentPeriod = view.findViewById(R.id.etRepaymentPeriod);
        switchRequireGuarantor = view.findViewById(R.id.switchRequireGuarantor);

        return view;
    }

    public boolean validateAndSave() {
        if (etPayoutAmount == null || etRetention == null || etMaxLoanAmount == null || 
            etInterestRate == null || etRepaymentPeriod == null) return false;

        String payoutStr = etPayoutAmount.getText().toString().trim();
        String retentionStr = etRetention.getText().toString().trim();
        String maxLoanStr = etMaxLoanAmount.getText().toString().trim();
        String interestStr = etInterestRate.getText().toString().trim();
        String periodStr = etRepaymentPeriod.getText().toString().trim();

        if (payoutStr.isEmpty()) {
            etPayoutAmount.setError("Payout amount is required");
            return false;
        }

        if (retentionStr.isEmpty()) {
            etRetention.setError("Retention percentage is required");
            return false;
        }

        if (maxLoanStr.isEmpty()) {
            etMaxLoanAmount.setError("Maximum loan amount is required");
            return false;
        }

        try {
            double payout = Double.parseDouble(payoutStr);
            double retention = Double.parseDouble(retentionStr);
            double maxLoan = Double.parseDouble(maxLoanStr);
            double interest = interestStr.isEmpty() ? 0.0 : Double.parseDouble(interestStr);
            int period = periodStr.isEmpty() ? 12 : Integer.parseInt(periodStr);
            boolean requireGuarantor = switchRequireGuarantor != null && switchRequireGuarantor.isChecked();

            if (retention < 0 || retention > 100) {
                etRetention.setError("Percentage must be between 0 and 100");
                return false;
            }

            // Save data to activity
            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setPayoutSettings(payout, retention);
                activity.setLoanRules(maxLoan, interest, period, requireGuarantor);
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
