package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

public class WizardContributionFragment extends Fragment {

    private TextInputEditText etContributionAmount;
    private ChipGroup chipGroupFrequency;
    private TextInputEditText etLatePenalty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_contribution, container, false);

        etContributionAmount = view.findViewById(R.id.etContributionAmount);
        chipGroupFrequency = view.findViewById(R.id.chipGroupFrequency);
        etLatePenalty = view.findViewById(R.id.etLatePenalty);

        return view;
    }

    public boolean validateAndSave() {
        String amountStr = etContributionAmount.getText().toString().trim();
        String penaltyStr = etLatePenalty.getText().toString().trim();

        if (amountStr.isEmpty()) {
            etContributionAmount.setError("Contribution amount is required");
            return false;
        }

        double amount = Double.parseDouble(amountStr);
        double penalty = penaltyStr.isEmpty() ? 5.0 : Double.parseDouble(penaltyStr);

        // Get selected frequency
        int selectedId = chipGroupFrequency.getCheckedChipId();
        String frequency = "Monthly";
        if (selectedId == R.id.chipWeekly) {
            frequency = "Weekly";
        } else if (selectedId == R.id.chipBiweekly) {
            frequency = "Bi-weekly";
        }

        // Save data to activity
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
            activity.setContributionSettings(amount, frequency, penalty);
        }

        return true;
    }
}
