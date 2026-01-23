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

public class WizardGroupInfoFragment extends Fragment {

    private TextInputEditText etGroupName;
    private TextInputEditText etGroupDescription;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_group_info, container, false);

        etGroupName = view.findViewById(R.id.etGroupName);
        etGroupDescription = view.findViewById(R.id.etGroupDescription);

        return view;
    }

    public boolean validateAndSave() {
        String groupName = etGroupName.getText().toString().trim();

        if (groupName.isEmpty()) {
            etGroupName.setError("Group name is required");
            return false;
        }

        // Save data to activity
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();
            String description = etGroupDescription.getText().toString().trim();
            activity.setGroupInfo(groupName, description);
        }

        return true;
    }
}
