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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class WizardContributionFragment extends Fragment {

    private Spinner spinnerFrequency;
    private TextInputEditText etLatePenalty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_contribution, container, false);

        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        etLatePenalty = view.findViewById(R.id.etLatePenalty);

        setupFrequencySpinner();

        return view;
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
        
        // Default to Monthly
        spinnerFrequency.setSelection(2);
    }

    public boolean validateAndSave() {
        if (etLatePenalty == null) return false;

        String penaltyStr = etLatePenalty.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();

        try {
            double penalty = penaltyStr.isEmpty() ? 0.0 : Double.parseDouble(penaltyStr);

            // Save data to activity
            if (getActivity() instanceof AdminSetupWizardActivity) {
                AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
                activity.setContributionSettings(frequency, penalty);
            }
            return true;
        } catch (NumberFormatException e) {
            etLatePenalty.setError("Invalid penalty percentage");
            return false;
        }
    }
}
