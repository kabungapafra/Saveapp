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
import com.example.save.databinding.FragmentWizardFinalizeRulesBinding;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
public class WizardFinalizeRulesFragment extends Fragment {

    private FragmentWizardFinalizeRulesBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentWizardFinalizeRulesBinding.inflate(inflater, container, false);

        setupFrequencySpinner();
        setupNavigation();
        
        // Match mockup defaults
        binding.etLateFee.setHint("0.00");
        binding.etInterestRate.setText("5.0");
        binding.switchReminders.setChecked(true);

        return binding.getRoot();
    }


    private void setupFrequencySpinner() {
        if (getContext() == null) return;

        String[] frequencies = {"Every 1 Week", "Every 2 Weeks", "Every 3 Weeks", "Every 4 Weeks"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, frequencies);
        binding.actvPayoutFrequency.setAdapter(adapter);
        
        // Match mockup default
        binding.actvPayoutFrequency.setText(frequencies[0], false);
    }

    private void setupNavigation() {
        // No manual click listener needed for Exposed Dropdown Menu

        binding.btnBackCircle.setOnClickListener(v -> {
            if (getActivity() instanceof AdminSetupWizardActivity) {
                ((AdminSetupWizardActivity) getActivity()).onBackPressed();
            }
        });

        binding.btnLaunch.setOnClickListener(v -> {
            if (validateAndSave()) {
                if (getActivity() instanceof AdminSetupWizardActivity) {
                    ((AdminSetupWizardActivity) getActivity()).nextStep();
                }
            }
        });
    }

    public boolean validateAndSave() {
        if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
            String lateFeeStr = binding.etLateFee.getText().toString().trim();
            double lateFee = lateFeeStr.isEmpty() ? 0.0 : Double.parseDouble(lateFeeStr);
            String interestStr = binding.etInterestRate.getText().toString().trim();
            double interest = interestStr.isEmpty() ? 5.0 : Double.parseDouble(interestStr);
            String frequency = binding.actvPayoutFrequency.getText().toString();
            boolean reminders = binding.switchReminders.isChecked();
            String repaymentStr = binding.etRepaymentPeriod.getText().toString().trim();
            int repayment = repaymentStr.isEmpty() ? 12 : Integer.parseInt(repaymentStr);

            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setContributionSettings(frequency, lateFee);
                activity.setLoanRules(0.0, interest, repayment, reminders); 
            }
            return true;
        }

        String lateFeeStr = binding.etLateFee.getText().toString().trim();
        String interestStr = binding.etInterestRate.getText().toString().trim();
        String frequency = binding.actvPayoutFrequency.getText().toString();
        boolean reminders = binding.switchReminders.isChecked();

        try {
            double lateFee = lateFeeStr.isEmpty() ? 0.0 : Double.parseDouble(lateFeeStr);
            double interest = interestStr.isEmpty() ? 0.0 : Double.parseDouble(interestStr);
            String repaymentStr = binding.etRepaymentPeriod.getText().toString().trim();
            int repayment = repaymentStr.isEmpty() ? 0 : Integer.parseInt(repaymentStr);

            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setContributionSettings(frequency, lateFee);
                activity.setLoanRules(0.0, interest, repayment, reminders); 
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
