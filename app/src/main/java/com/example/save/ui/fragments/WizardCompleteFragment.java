package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class WizardCompleteFragment extends Fragment {

    private TextView tvSummaryGroupName;
    private TextView tvSummaryContribution;
    private TextView tvSummaryFrequency;
    private MaterialButton btnFinish;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_complete, container, false);

        tvSummaryGroupName = view.findViewById(R.id.tvSummaryGroupName);
        tvSummaryContribution = view.findViewById(R.id.tvSummaryContribution);
        tvSummaryFrequency = view.findViewById(R.id.tvSummaryFrequency);
        btnFinish = view.findViewById(R.id.btnFinish);

        // Load summary data
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();

            tvSummaryGroupName.setText(activity.getGroupName());

            NumberFormat currencyFormat = NumberFormat
                    .getCurrencyInstance(new Locale.Builder().setLanguage("en").setRegion("UG").build());
            tvSummaryContribution.setText(currencyFormat.format(activity.getContributionAmount()));

            tvSummaryFrequency.setText(activity.getContributionFrequency());
        }

        btnFinish.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish();
            }
        });

        return view;
    }
}
