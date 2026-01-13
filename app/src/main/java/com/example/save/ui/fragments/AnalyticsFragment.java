package com.example.save.ui.fragments;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.FragmentAnalyticsBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.save.ui.adapters.AnalyticsActivityAdapter;
import com.example.save.data.models.ActivityModel;
import com.example.save.ui.viewmodels.LoansViewModel;
import com.example.save.ui.viewmodels.MembersViewModel;

public class AnalyticsFragment extends Fragment {

    private static final String ARG_IS_ADMIN = "is_admin";
    private boolean isAdmin = false;

    private FragmentAnalyticsBinding binding;

    // ViewModels
    private LoansViewModel loansViewModel;
    private MembersViewModel membersViewModel;

    public static AnalyticsFragment newInstance(boolean isAdmin) {
        AnalyticsFragment fragment = new AnalyticsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean(ARG_IS_ADMIN);
        }
        loansViewModel = new androidx.lifecycle.ViewModelProvider(this).get(LoansViewModel.class);
        membersViewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);

        binding.recyclerRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        updateUI();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // initViews removed as Binding handles it

    private void updateUI() {
        if (isAdmin) {
            setupAdminView();
        } else {
            setupMemberView();
        }
    }

    private void setupAdminView() {
        binding.tvAnalyticsTitle.setText("Group Overview");

        // Metrics
        binding.tvMetric1Label.setText("Total Savings");
        membersViewModel.getGroupBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                binding.tvMetric1Value.setText(formatCurrency(balance));
            }
        });

        binding.tvMetric2Label.setText("Active Loans");
        binding.tvMetric2Value.setText(formatCurrency(loansViewModel.getTotalOutstanding()));

        binding.tvMetric3Label.setText("Revenue");
        binding.tvMetric3Value.setText(formatCurrency(loansViewModel.getTotalInterestEarned()));

        binding.tvMetric4Label.setText("Members");
        // Load member count on background thread
        new Thread(() -> {
            try {
                int memberCount = membersViewModel.getTotalMemberCountSync();
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.tvMetric4Value.setText(String.valueOf(memberCount));
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        binding.tvMetric4Value.setText("0");
                    }
                });
            }
        }).start();

        // Chart
        binding.tvChartTitle.setText("Weekly Performance");
        setupChart(true);

        // Loan Health (Visible)
        binding.cardLoanHealth.setVisibility(View.VISIBLE);
        // Mock data
        binding.tvHealthyLoansCount.setText("12");
        binding.tvAtRiskLoansCount.setText("2");
        binding.tvOverdueLoansCount.setText("1");

        // Recent Activity (Group)
        List<ActivityModel> activities = new ArrayList<>();
        activities.add(new ActivityModel("Loan Repayment - Jane", "10 mins ago", 50000, true));
        activities.add(new ActivityModel("New Loan - John", "2 hrs ago", 200000, false));
        activities.add(new ActivityModel("Contribution - Sarah", "5 hrs ago", 20000, true));
        activities.add(new ActivityModel("Contribution - Mike", "1 day ago", 20000, true));

        binding.recyclerRecentActivity.setAdapter(new AnalyticsActivityAdapter(activities));
    }

    private void setupMemberView() {
        binding.tvAnalyticsTitle.setText("My Financials");

        // Metrics
        binding.tvMetric1Label.setText("My Savings");
        binding.tvMetric1Value.setText(formatCurrency(350000));

        binding.tvMetric2Label.setText("Active Loan");
        binding.tvMetric2Value.setText(formatCurrency(0));

        binding.tvMetric3Label.setText("Dividends Earned");
        binding.tvMetric3Value.setText(formatCurrency(15000));

        binding.tvMetric4Label.setText("Next Contribution");
        binding.tvMetric4Value.setText("Due in 5 days");

        // Chart
        binding.tvChartTitle.setText("Weekly Stash Growth");
        setupChart(false);

        // Loan Health (Hidden)
        binding.cardLoanHealth.setVisibility(View.GONE);

        // Recent Activity (Personal)
        List<ActivityModel> activities = new ArrayList<>();
        activities.add(new ActivityModel("Monthly Contribution", "5 days ago", 20000, true));
        activities.add(new ActivityModel("Loan Repayment", "2 weeks ago", 50000, false));
        activities.add(new ActivityModel("Dividend Payout", "1 month ago", 15000, true));

        binding.recyclerRecentActivity.setAdapter(new AnalyticsActivityAdapter(activities));
    }

    private void setupChart(boolean isAdmin) {
        com.github.mikephil.charting.charts.PieChart chart = binding.pieChart;
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(android.graphics.Color.WHITE);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.setDrawCenterText(true);
        chart.setCenterText("This Month");
        chart.setCenterTextSize(12f);

        // Improve Legend
        com.github.mikephil.charting.components.Legend legend = chart.getLegend();
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(11f);
        legend.setTextColor(android.graphics.Color.DKGRAY);

        List<com.github.mikephil.charting.data.PieEntry> entries = new ArrayList<>();

        // Mock Weekly Data
        if (isAdmin) {
            entries.add(new com.github.mikephil.charting.data.PieEntry(35f, "Week 1"));
            entries.add(new com.github.mikephil.charting.data.PieEntry(25f, "Week 2"));
            entries.add(new com.github.mikephil.charting.data.PieEntry(20f, "Week 3"));
            entries.add(new com.github.mikephil.charting.data.PieEntry(20f, "Week 4"));
        } else {
            entries.add(new com.github.mikephil.charting.data.PieEntry(40f, "Week 1"));
            entries.add(new com.github.mikephil.charting.data.PieEntry(30f, "Week 2"));
            entries.add(new com.github.mikephil.charting.data.PieEntry(15f, "Week 3"));
            entries.add(new com.github.mikephil.charting.data.PieEntry(15f, "Week 4"));
        }

        com.github.mikephil.charting.data.PieDataSet dataSet = new com.github.mikephil.charting.data.PieDataSet(entries,
                "");

        // Vibrant Colors for Weeks
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.deep_blue));
        colors.add(android.graphics.Color.parseColor("#42A5F5")); // Blue Light
        colors.add(android.graphics.Color.parseColor("#66BB6A")); // Green Light
        colors.add(android.graphics.Color.parseColor("#FFA726")); // Orange Light
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        com.github.mikephil.charting.data.PieData data = new com.github.mikephil.charting.data.PieData(dataSet);
        chart.setData(data);
        chart.invalidate(); // refresh

        // Animate
        chart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "UGX %,.0f", amount);
    }
}
