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

import java.util.Locale;

public class WizardCompleteFragment extends Fragment {

    private TextView tvSummaryGroupName;
    private TextView tvSummaryCurrency;
    private TextView tvSummaryContribution;
    private TextView tvSummaryFrequency;
    private TextView tvSummaryPayout;
    private TextView tvSummaryRetention;
    private TextView tvSummaryMaxLoan;
    private TextView tvSummaryInterest;
    private TextView tvSummaryRepayment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_complete, container, false);

        tvSummaryGroupName = view.findViewById(R.id.tvSummaryGroupName);
        tvSummaryCurrency = view.findViewById(R.id.tvSummaryCurrency);
        tvSummaryContribution = view.findViewById(R.id.tvSummaryContribution);
        tvSummaryFrequency = view.findViewById(R.id.tvSummaryFrequency);
        tvSummaryPayout = view.findViewById(R.id.tvSummaryPayout);
        tvSummaryRetention = view.findViewById(R.id.tvSummaryRetention);
        tvSummaryMaxLoan = view.findViewById(R.id.tvSummaryMaxLoan);
        tvSummaryInterest = view.findViewById(R.id.tvSummaryInterest);
        tvSummaryRepayment = view.findViewById(R.id.tvSummaryRepayment);

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) getActivity().onBackPressed();
            });
        }

        populateSummary();
        return view;
    }

    private void populateSummary() {
        if (!(getActivity() instanceof AdminSetupWizardActivity)) return;
        AdminSetupWizardActivity activity = (AdminSetupWizardActivity) getActivity();

        String currency = activity.getCurrency();
        String symbol = "UGX";
        if (currency != null) {
            if (currency.contains("USD")) symbol = "USD";
            else if (currency.contains("KES")) symbol = "KES";
            else if (currency.contains("EUR")) symbol = "€";
            else if (currency.contains("GBP")) symbol = "£";
        }

        tvSummaryGroupName.setText(activity.getGroupName() != null ? activity.getGroupName() : "-");
        tvSummaryCurrency.setText(currency != null ? currency : "-");
        tvSummaryContribution.setText(String.format(Locale.getDefault(), "%s %,.0f", symbol, activity.getContributionAmount()));
        tvSummaryFrequency.setText(activity.getContributionFrequency() != null ? activity.getContributionFrequency() : "-");
        tvSummaryPayout.setText(String.format(Locale.getDefault(), "%.1f%%", activity.getLatePenalty()));
        tvSummaryRetention.setText(String.format(Locale.getDefault(), "%.1f%%", activity.getRetentionPercentage()));
        tvSummaryMaxLoan.setText(String.format(Locale.getDefault(), "%s %,.0f", symbol, activity.getMaxLoanAmount()));
        tvSummaryInterest.setText(String.format(Locale.getDefault(), "%.1f%%", activity.getInterestRate()));
        tvSummaryRepayment.setText(String.format(Locale.getDefault(), "%d months", activity.getRepaymentPeriod()));
    }
}
