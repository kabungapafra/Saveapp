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
import com.google.android.material.textfield.TextInputEditText;

public class WizardPayoutFragment extends Fragment {

    private TextInputEditText etPayoutAmount;
    private TextInputEditText etRetention;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_payout, container, false);
        
        etPayoutAmount = view.findViewById(R.id.etPayoutAmount);
        etRetention = view.findViewById(R.id.etRetention);
        
        return view;
    }

    public boolean validateAndSave() {
        if (etPayoutAmount == null || etRetention == null) return false;

        String payoutStr = etPayoutAmount.getText().toString().trim();
        String retentionStr = etRetention.getText().toString().trim();

        if (payoutStr.isEmpty()) {
            etPayoutAmount.setError("Payout amount is required");
            return false;
        }

        if (retentionStr.isEmpty()) {
            etRetention.setError("Retention percentage is required");
            return false;
        }

        try {
            double payout = Double.parseDouble(payoutStr);
            double retention = Double.parseDouble(retentionStr);

            if (retention < 0 || retention > 100) {
                etRetention.setError("Percentage must be between 0 and 100");
                return false;
            }

            if (getActivity() instanceof AdminSetupWizardActivity) {
                ((AdminSetupWizardActivity) getActivity()).setPayoutSettings(payout, retention);
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
