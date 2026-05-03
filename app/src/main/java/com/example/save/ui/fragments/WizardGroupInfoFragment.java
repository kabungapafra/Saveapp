package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class WizardGroupInfoFragment extends Fragment {

    private EditText etGroupName;
    private Spinner spinnerCurrency;
    private TextInputEditText etMonthlyContribution;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_group_info, container, false);

        etGroupName = view.findViewById(R.id.etGroupName);
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency);
        etMonthlyContribution = view.findViewById(R.id.etMonthlyContribution);

        setupCurrencySpinner();

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Restore data from activity if available
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
            
            String groupName = activity.getGroupName();
            if (groupName != null && !groupName.isEmpty()) {
                etGroupName.setText(groupName);
            }
            
            double amount = activity.getContributionAmount();
            if (amount > 0) {
                etMonthlyContribution.setText(String.valueOf((int)amount));
            }
            
            String currency = activity.getCurrency();
            if (currency != null && !currency.isEmpty()) {
                for (int i = 0; i < spinnerCurrency.getCount(); i++) {
                    if (spinnerCurrency.getItemAtPosition(i).toString().equals(currency)) {
                        spinnerCurrency.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void setupCurrencySpinner() {
        if (getContext() == null) return;
        
        List<String> currencies = new ArrayList<>();
        currencies.add("UGX - Uganda Shillings");
        currencies.add("USD ($)");
        currencies.add("KES (KSh)");
        currencies.add("EUR (€)");
        currencies.add("GBP (£)");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
        
        // Default to UGX
        spinnerCurrency.setSelection(0); // UGX
    }

    public boolean validateAndSave() {
        if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
            String groupName = etGroupName.getText().toString().trim();
            if (groupName.isEmpty()) groupName = "Design Group";
            
            String contributionStr = etMonthlyContribution.getText().toString().trim();
            double amount = 0;
            if (!contributionStr.isEmpty()) {
                try { amount = Double.parseDouble(contributionStr); } catch (Exception e) {}
            }
            
            String currency = spinnerCurrency.getSelectedItem().toString();

            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setGroupInfo(groupName, "", currency, amount);
            }
            return true;
        }

        if (etGroupName == null || etMonthlyContribution == null) return false;

        String groupName = etGroupName.getText().toString().trim();
        String contributionStr = etMonthlyContribution.getText().toString().trim();

        if (groupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            return false;
        }

        if (contributionStr.isEmpty()) {
            etMonthlyContribution.setError("Monthly contribution is required");
            return false;
        }

        try {
            double amount = Double.parseDouble(contributionStr);
            String currency = spinnerCurrency.getSelectedItem().toString();

            // Save data to activity
            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setGroupInfo(groupName, "", currency, amount);
            }
            return true;
        } catch (NumberFormatException e) {
            etMonthlyContribution.setError("Invalid amount");
            return false;
        }
    }
}
