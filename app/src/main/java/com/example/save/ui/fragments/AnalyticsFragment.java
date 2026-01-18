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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.example.save.ui.adapters.AnalyticsActivityAdapter;
import com.example.save.data.models.ActivityModel;
import com.example.save.data.models.Loan;
import com.example.save.data.models.Member;
import com.example.save.ui.viewmodels.LoansViewModel;
import com.example.save.ui.viewmodels.MembersViewModel;

public class AnalyticsFragment extends Fragment {

    private static final String ARG_IS_ADMIN = "is_admin";
    private static final String ARG_MEMBER_EMAIL = "member_email";

    private boolean isAdmin = false;
    private String memberEmail;

    private FragmentAnalyticsBinding binding;

    // ViewModels
    private LoansViewModel loansViewModel;
    private MembersViewModel membersViewModel;

    public static AnalyticsFragment newInstance(boolean isAdmin) {
        return newInstance(isAdmin, null);
    }

    public static AnalyticsFragment newInstance(boolean isAdmin, String memberEmail) {
        AnalyticsFragment fragment = new AnalyticsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        if (memberEmail != null) {
            args.putString(ARG_MEMBER_EMAIL, memberEmail);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean(ARG_IS_ADMIN);
            memberEmail = getArguments().getString(ARG_MEMBER_EMAIL);
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

    private void updateUI() {
        if (isAdmin) {
            setupAdminView();
        } else {
            setupMemberView();
        }
    }

    private void setupAdminView() {
        binding.tvAnalyticsTitle.setText("Group Overview");

        // Metric 1: Total Savings
        binding.tvMetric1Label.setText("Total Savings");
        membersViewModel.getGroupBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                binding.tvMetric1Value.setText(formatCurrency(balance));
            } else {
                binding.tvMetric1Value.setText(formatCurrency(0));
            }
        });

        // Metrics for Loans and Revenue
        loansViewModel.getLoans().observe(getViewLifecycleOwner(), loans -> {
            double activeLoansAmount = 0;
            double revenueInterest = 0;
            int healthy = 0;
            int atRisk = 0;
            int overdue = 0;
            int activeCount = 0;

            // Pie Chart Data
            int pendingCount = 0;
            int activeLoanCount = 0;
            int paidCount = 0;
            int rejectedCount = 0;

            if (loans != null) {
                for (Loan loan : loans) {
                    if (Loan.STATUS_ACTIVE.equals(loan.getStatus())) {
                        // Active Loan Amount (Outstanding)
                        double outstanding = loan.getTotalDue() - loan.getRepaidAmount();
                        activeLoansAmount += outstanding;
                        activeCount++;

                        // Calculate Revenue (Interest Earned so far is simplistic, let's use total
                        // interest for active/paid)
                        // Or better: Revenue is realized when paid. For now, let's project Total
                        // Interest of all Active+Paid loans
                        revenueInterest += loan.getInterest(); // Simple projection

                        // Loan Health
                        String health = calculateLoanHealth(loan);
                        if ("HEALTHY".equals(health))
                            healthy++;
                        else if ("AT_RISK".equals(health))
                            atRisk++;
                        else if ("OVERDUE".equals(health))
                            overdue++;

                        activeLoanCount++;
                    } else if (Loan.STATUS_PAID.equals(loan.getStatus())) {
                        revenueInterest += loan.getInterest(); // Realized revenue
                        paidCount++;
                    } else if (Loan.STATUS_PENDING.equals(loan.getStatus())) {
                        pendingCount++;
                    } else if (Loan.STATUS_REJECTED.equals(loan.getStatus())) {
                        rejectedCount++;
                    }
                }
            }

            binding.tvMetric2Label.setText("Active Loans");
            binding.tvMetric2Value.setText(formatCurrency(activeLoansAmount));

            binding.tvMetric3Label.setText("Projected Revenue");
            binding.tvMetric3Value.setText(formatCurrency(revenueInterest));

            // Loan Health Card
            binding.tvHealthyLoansCount.setText(String.valueOf(healthy));
            binding.tvAtRiskLoansCount.setText(String.valueOf(atRisk));
            binding.tvOverdueLoansCount.setText(String.valueOf(overdue));
            binding.cardLoanHealth.setVisibility(View.VISIBLE);

            // Chart Data - Loan Status Distribution
            setupChart(activeLoanCount, paidCount, pendingCount, rejectedCount);
            binding.tvChartTitle.setText("Loan Status Distribution");

            // Recent Activity (Loans as proxy for now)
            populateRecentActivity(loans, null);
        });

        binding.tvMetric4Label.setText("Members");
        // Load member count on background thread
        new Thread(() -> {
            try {
                int memberCount = membersViewModel.getTotalMemberCountSync();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null) {
                            binding.tvMetric4Value.setText(String.valueOf(memberCount));
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null) {
                            binding.tvMetric4Value.setText("0");
                        }
                    });
                }
            }
        }).start();

    }

    private String calculateLoanHealth(Loan loan) {
        if (loan.getDueDate() == null)
            return "HEALTHY";

        long diff = loan.getDueDate().getTime() - new Date().getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        if (daysDiff < 0) {
            return "OVERDUE";
        } else if (daysDiff <= 7) { // Due within a week
            return "AT_RISK";
        } else {
            return "HEALTHY";
        }
    }

    private void setupMemberView() {
        binding.tvAnalyticsTitle.setText("My Financials");

        // Hide Admin Sections
        binding.cardLoanHealth.setVisibility(View.GONE);

        if (memberEmail == null) {
            // Fallback for demo or error
            binding.tvMetric1Value.setText("Error");
            return;
        }

        // Fetch Member Logic
        new Thread(() -> {
            Member member = membersViewModel.getMemberByEmail(memberEmail);
            if (member != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding == null)
                        return;

                    // Metric 1: My Savings
                    binding.tvMetric1Label.setText("My Savings");
                    binding.tvMetric1Value.setText(formatCurrency(member.getContributionPaid()));

                    // Metric 3: Dividends (Mock calculation based on savings 5%)
                    binding.tvMetric3Label.setText("Est. Dividends");
                    double dividends = member.getContributionPaid() * 0.05;
                    binding.tvMetric3Value.setText(formatCurrency(dividends));

                    // Metric 4: Next Contribution
                    binding.tvMetric4Label.setText("Next Contribution");
                    // Simple monthly logic
                    binding.tvMetric4Value.setText("Due Monthly");

                    // Metric 2: Active Loan & Chart & Recent Activity needs Loan Data
                    loansViewModel.getLoans().observe(getViewLifecycleOwner(), allLoans -> {
                        double myActiveLoanStr = 0;
                        List<Loan> myLoans = new ArrayList<>();

                        // Filter
                        if (allLoans != null) {
                            for (Loan l : allLoans) {
                                // Match by Name as ID might be tricky, but ideally Member ID
                                // Falling back to Name if ID match fails or using Name directly
                                if (member.getName().equals(l.getMemberName())) {
                                    myLoans.add(l);
                                    if (Loan.STATUS_ACTIVE.equals(l.getStatus())) {
                                        myActiveLoanStr += (l.getTotalDue() - l.getRepaidAmount());
                                    }
                                }
                            }
                        }

                        binding.tvMetric2Label.setText("My Active Loan");
                        binding.tvMetric2Value.setText(formatCurrency(myActiveLoanStr));

                        // Chart - My Spending vs Savings? Or Loan Repayment?
                        // Let's show Savings Growth (Mocked as weekly increments)
                        setupMockSavingsChart();
                        binding.tvChartTitle.setText("Savings Growth (Est)");

                        // Recent Activity
                        populateRecentActivity(myLoans, member.getName());
                    });
                });
            }
        }).start();
    }

    private void populateRecentActivity(List<Loan> loans, String filterMemberName) {
        List<ActivityModel> activities = new ArrayList<>();

        if (loans != null) {
            // Sort by date requested desc
            Collections.sort(loans, (l1, l2) -> {
                if (l1.getDateRequested() == null || l2.getDateRequested() == null)
                    return 0;
                return l2.getDateRequested().compareTo(l1.getDateRequested());
            });

            for (Loan l : loans) {
                // If filtering for member view
                if (filterMemberName != null && !filterMemberName.equals(l.getMemberName())) {
                    continue;
                }

                String title = l.getStatus().equals("ACTIVE") ? "Loan Approved" : "Loan Request: " + l.getStatus();
                if (l.getStatus().equals("PAID"))
                    title = "Loan Repayed";

                String time = "Recently";
                if (l.getDateRequested() != null) {
                    time = new java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(l.getDateRequested());
                }

                boolean isPositive = !l.getStatus().equals("ACTIVE"); // Debt is negative-ish visually? Or just use
                                                                      // color

                activities.add(new ActivityModel(
                        title + (filterMemberName == null ? " - " + l.getMemberName() : ""),
                        time,
                        l.getAmount(),
                        isPositive));

                if (activities.size() >= 10)
                    break; // Limit to 10
            }
        }

        if (activities.isEmpty()) {
            activities.add(new ActivityModel("No recent activity", "", 0, true));
        }

        if (binding != null) {
            binding.recyclerRecentActivity.setAdapter(new AnalyticsActivityAdapter(activities));
        }
    }

    private void setupChart(int active, int paid, int pending, int rejected) {
        if (binding == null)
            return;

        com.github.mikephil.charting.charts.PieChart chart = binding.pieChart;
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(android.graphics.Color.WHITE);
        chart.setHoleRadius(50f);
        chart.setTransparentCircleRadius(55f);
        chart.setDrawCenterText(true);
        chart.setCenterText("Loans");
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

        if (active > 0)
            entries.add(new com.github.mikephil.charting.data.PieEntry((float) active, "Active"));
        if (paid > 0)
            entries.add(new com.github.mikephil.charting.data.PieEntry((float) paid, "Paid"));
        if (pending > 0)
            entries.add(new com.github.mikephil.charting.data.PieEntry((float) pending, "Pending"));
        if (rejected > 0)
            entries.add(new com.github.mikephil.charting.data.PieEntry((float) rejected, "Rejected"));

        if (entries.isEmpty()) {
            entries.add(new com.github.mikephil.charting.data.PieEntry(1f, "No Data"));
        }

        com.github.mikephil.charting.data.PieDataSet dataSet = new com.github.mikephil.charting.data.PieDataSet(entries,
                "");

        // Vibrant Colors
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.deep_blue)); // Active/Main
        colors.add(android.graphics.Color.parseColor("#66BB6A")); // Green (Paid)
        colors.add(android.graphics.Color.parseColor("#FFA726")); // Orange (Pending)
        colors.add(android.graphics.Color.parseColor("#EF5350")); // Red (Rejected)
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        com.github.mikephil.charting.data.PieData data = new com.github.mikephil.charting.data.PieData(dataSet);
        chart.setData(data);
        chart.invalidate(); // refresh
        chart.animateY(1000, com.github.mikephil.charting.animation.Easing.EaseInOutQuad);
    }

    private void setupMockSavingsChart() {
        // Keep the aesthetic mock chart for individual savings growth as we don't have
        // historical transaction data yet
        if (binding == null)
            return;

        com.github.mikephil.charting.charts.PieChart chart = binding.pieChart;
        chart.setCenterText("Growth");

        List<com.github.mikephil.charting.data.PieEntry> entries = new ArrayList<>();
        entries.add(new com.github.mikephil.charting.data.PieEntry(40f, "Week 1"));
        entries.add(new com.github.mikephil.charting.data.PieEntry(30f, "Week 2"));
        entries.add(new com.github.mikephil.charting.data.PieEntry(15f, "Week 3"));
        entries.add(new com.github.mikephil.charting.data.PieEntry(15f, "Week 4"));

        com.github.mikephil.charting.data.PieDataSet dataSet = new com.github.mikephil.charting.data.PieDataSet(entries,
                "");
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.deep_blue));
        colors.add(android.graphics.Color.parseColor("#42A5F5"));
        colors.add(android.graphics.Color.parseColor("#66BB6A"));
        colors.add(android.graphics.Color.parseColor("#FFA726"));
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);

        com.github.mikephil.charting.data.PieData data = new com.github.mikephil.charting.data.PieData(dataSet);
        chart.setData(data);
        chart.invalidate();
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "UGX %,.0f", amount);
    }
}
