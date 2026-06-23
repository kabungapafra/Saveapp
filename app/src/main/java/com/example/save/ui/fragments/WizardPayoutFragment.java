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

    private TextInputEditText etRetention;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_payout, container, false);
        
        etRetention = view.findViewById(R.id.etRetention);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore data from activity if available
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
            double retention = activity.getRetentionPercentage();
            if (retention > 0) {
                etRetention.setText(String.valueOf(retention));
            }
        }
    }

    public boolean validateAndSave() {
        if (etRetention == null) return false;

        String retentionStr = etRetention.getText().toString().trim();

        if (retentionStr.isEmpty()) {
            etRetention.setError("Retention percentage is required");
            return false;
        }

        try {
            double retention = Double.parseDouble(retentionStr);

            if (retention < 0 || retention > 100) {
                etRetention.setError("Percentage must be between 0 and 100");
                return false;
            }

            if (getActivity() instanceof AdminSetupWizardActivity) {
                ((AdminSetupWizardActivity) getActivity()).setRetentionPercentage(retention);
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
