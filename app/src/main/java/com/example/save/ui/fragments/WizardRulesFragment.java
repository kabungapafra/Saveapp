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

public class WizardRulesFragment extends Fragment {

    private TextInputEditText etMaxLoanAmount;
    private TextInputEditText etInterestRate;
    private TextInputEditText etRepaymentPeriod;
    private SwitchMaterial switchRequireGuarantor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_rules, container, false);

        etMaxLoanAmount = view.findViewById(R.id.etMaxLoanAmount);
        etInterestRate = view.findViewById(R.id.etInterestRate);
        etRepaymentPeriod = view.findViewById(R.id.etRepaymentPeriod);
        switchRequireGuarantor = view.findViewById(R.id.switchRequireGuarantor);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore data from activity if available
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();

            double maxAmount = activity.getMaxLoanAmount();
            if (maxAmount > 0) {
                etMaxLoanAmount.setText(String.valueOf(maxAmount));
            }

            double rate = activity.getInterestRate();
            if (rate > 0) {
                etInterestRate.setText(String.valueOf(rate));
            }

            int period = activity.getRepaymentPeriod();
            if (period > 0) {
                etRepaymentPeriod.setText(String.valueOf(period));
            }

            switchRequireGuarantor.setChecked(activity.isRequireGuarantor());
        }
    }

    public boolean validateAndSave() {
        if (etMaxLoanAmount == null || etInterestRate == null || etRepaymentPeriod == null) return false;

        String maxAmountStr = etMaxLoanAmount.getText().toString().trim();
        String interestStr = etInterestRate.getText().toString().trim();
        String periodStr = etRepaymentPeriod.getText().toString().trim();

        if (maxAmountStr.isEmpty()) {
            etMaxLoanAmount.setError("Maximum loan amount is required");
            return false;
        }

        try {
            double maxAmount = Double.parseDouble(maxAmountStr);
            double interest = interestStr.isEmpty() ? 0.0 : Double.parseDouble(interestStr);
            int period = periodStr.isEmpty() ? 1 : Integer.parseInt(periodStr);
            boolean requireGuarantor = switchRequireGuarantor != null && switchRequireGuarantor.isChecked();

            // Save data to activity
            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setLoanRules(maxAmount, interest, period, requireGuarantor);
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
