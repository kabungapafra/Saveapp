package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.DashboardSummaryResponse;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.NumberFormat;
import java.util.Locale;

public class SavingsTargetsFragment extends Fragment {

    private MembersViewModel viewModel;
    private TextView tvMonthlyAmount, tvYearlyAmount, tvPerMember, tvMemberCount;
    private TextView tvMonthlyPayoutAmount, tvRetentionKept, tvRetentionPct;
    private TextView tvMonthlyActual, tvYearlyActual;
    private LinearProgressIndicator pbMonthly, pbYearly, pbPayout;
    private View loadingIndicator;

    private double contributionAmount = 0;
    private double retentionPercentage = 0;
    private int memberCount = 0;
    private double actualMonthly = 0;
    private double actualYearly = 0;

    public static SavingsTargetsFragment newInstance() {
        return new SavingsTargetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_savings_targets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvMonthlyAmount   = view.findViewById(R.id.tvMonthlyAmount);
        tvYearlyAmount    = view.findViewById(R.id.tvYearlyAmount);
        tvPerMember       = view.findViewById(R.id.tvPerMember);
        tvMemberCount     = view.findViewById(R.id.tvMemberCount);
        tvMonthlyPayoutAmount = view.findViewById(R.id.tvMonthlyPayoutAmount);
        tvRetentionKept   = view.findViewById(R.id.tvRetentionKept);
        tvRetentionPct    = view.findViewById(R.id.tvRetentionPct);
        tvMonthlyActual   = view.findViewById(R.id.tvMonthlyActual);
        tvYearlyActual    = view.findViewById(R.id.tvYearlyActual);
        pbMonthly         = view.findViewById(R.id.pbMonthly);
        pbYearly          = view.findViewById(R.id.pbYearly);
        pbPayout          = view.findViewById(R.id.pbPayout);
        loadingIndicator  = view.findViewById(R.id.loadingIndicator);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        loadingIndicator.setVisibility(View.VISIBLE);

        // Config (contribution amount + retention %) from system config
        viewModel.fetchSystemConfig((success, config, message) -> {
            if (success && config != null) {
                contributionAmount  = config.getContributionAmount();
                retentionPercentage = config.getRetentionPercentage();
                requireActivity().runOnUiThread(this::updateProjections);
            } else {
                requireActivity().runOnUiThread(() -> loadingIndicator.setVisibility(View.GONE));
            }
        });

        // Actual collected + total_contributors (admin + members) from dashboard summary
        viewModel.getDashboardSummary((success, summaryObj, message) -> {
            if (success && summaryObj instanceof DashboardSummaryResponse) {
                DashboardSummaryResponse s = (DashboardSummaryResponse) summaryObj;
                actualMonthly = s.getMonthlyContributions();
                actualYearly  = s.getYearlyContributions();
                memberCount   = s.getTotalContributors();
                if (isAdded()) requireActivity().runOnUiThread(this::updateProjections);
            }
        });
    }

    private void updateProjections() {
        if (contributionAmount == 0 || memberCount == 0) return;

        loadingIndicator.setVisibility(View.GONE);

        double monthly = contributionAmount * memberCount;
        double yearly  = monthly * 12;
        double kept    = monthly * (retentionPercentage / 100.0);
        double payout  = monthly - kept;

        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));

        // Projected targets
        tvMonthlyAmount.setText(fmt.format(monthly).replace("USh", "UGX"));
        tvYearlyAmount.setText(fmt.format(yearly).replace("USh", "UGX"));
        tvPerMember.setText(fmt.format(contributionAmount).replace("USh", "UGX"));
        tvMemberCount.setText(String.valueOf(memberCount));

        // Payout card
        tvMonthlyPayoutAmount.setText(fmt.format(payout).replace("USh", "UGX"));
        tvRetentionKept.setText(fmt.format(kept).replace("USh", "UGX"));
        tvRetentionPct.setText(String.format(Locale.getDefault(), "%.0f%%", retentionPercentage));

        // Actual collected — updates incrementally as data arrives
        if (tvMonthlyActual != null) {
            tvMonthlyActual.setText(fmt.format(actualMonthly).replace("USh", "UGX"));
        }
        if (tvYearlyActual != null) {
            tvYearlyActual.setText(fmt.format(actualYearly).replace("USh", "UGX"));
        }

        // Progress bars: actual vs projected target
        int monthlyPct = monthly > 0 ? (int) Math.min(100, (actualMonthly / monthly) * 100) : 0;
        int yearlyPct  = yearly  > 0 ? (int) Math.min(100, (actualYearly  / yearly)  * 100) : 0;
        int payoutPct  = monthly > 0 ? (int) Math.round((payout / monthly) * 100) : 0;

        if (pbMonthly != null) pbMonthly.setProgress(monthlyPct);
        if (pbYearly  != null) pbYearly.setProgress(yearlyPct);
        if (pbPayout  != null) pbPayout.setProgress(payoutPct);
    }
}
