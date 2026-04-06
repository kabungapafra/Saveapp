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

import java.text.NumberFormat;
import java.util.Locale;

public class WizardCompleteFragment extends Fragment {

    private TextView tvSummaryGroupName;
    private TextView tvSummaryContribution;
    private TextView tvSummaryFrequency;
    private TextView tvSummaryPayout;
    private TextView tvSummaryRetention;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_complete, container, false);

        tvSummaryGroupName = view.findViewById(R.id.tvSummaryGroupName);
        tvSummaryContribution = view.findViewById(R.id.tvSummaryContribution);
        tvSummaryFrequency = view.findViewById(R.id.tvSummaryFrequency);
        tvSummaryPayout = view.findViewById(R.id.tvSummaryPayout);
        tvSummaryRetention = view.findViewById(R.id.tvSummaryRetention);

        populateSummary();

        return view;
    }

    private void populateSummary() {
        if (getActivity() instanceof AdminSetupWizardActivity) {
            AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();

            String currency = activity.getCurrency();
            // Extract symbol if needed, e.g. "USD ($)" -> "$"
            String symbol = "$";
            if (currency.contains("UGX")) symbol = "UGX";
            else if (currency.contains("KES")) symbol = "KES";
            else if (currency.contains("EUR")) symbol = "€";
            else if (currency.contains("GBP")) symbol = "£";

            tvSummaryGroupName.setText(activity.getGroupName());
            
            tvSummaryContribution.setText(String.format(Locale.getDefault(), "%s %,.0f", symbol, activity.getContributionAmount()));
            tvSummaryFrequency.setText(activity.getContributionFrequency());
            tvSummaryPayout.setText(String.format(Locale.getDefault(), "%s %,.0f", symbol, activity.getPayoutAmount()));
            tvSummaryRetention.setText(String.format(Locale.getDefault(), "%.1f%%", activity.getRetentionPercentage()));
        }
    }
}
