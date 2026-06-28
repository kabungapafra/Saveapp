package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;

import java.util.ArrayList;
import java.util.List;

public class WizardGroupInfoFragment extends Fragment {

    private EditText etGroupName;
    private Spinner spinnerCurrency;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_group_info, container, false);

        etGroupName = view.findViewById(R.id.etGroupName);
        spinnerCurrency = view.findViewById(R.id.spinnerCurrency);

        setupCurrencySpinner();

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

            String groupName = activity.getGroupName();
            if (groupName != null && !groupName.isEmpty()) {
                etGroupName.setText(groupName);
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
        spinnerCurrency.setSelection(0);
    }

    public boolean validateAndSave() {
        if (etGroupName == null) return false;

        String groupName = etGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            return false;
        }

        String currency = spinnerCurrency.getSelectedItem().toString();
        if (getActivity() instanceof AdminSetupWizardActivity) {
            ((AdminSetupWizardActivity) getActivity()).setGroupInfo(groupName, "", currency, 0);
        }
        return true;
    }
}
