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
            if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).switchToDashboard();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Initialize LineChart
        setupLineChart(null); // Init with empty data or styling

        // Export Button
        binding.btnExport.setOnClickListener(v -> exportReport());

        // Setup SwipeRefresh
        binding.swipeRefresh.setOnRefreshListener(() -> {
            updateUI();
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
        // Standardize: Everyone sees Group Performance
        setupGroupView();

        // Setup Line Chart Data
        membersViewModel.getSavingsTrend().observe(getViewLifecycleOwner(), entries -> {
            setupLineChart(entries);
            if (binding != null && binding.swipeRefresh != null) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void setupGroupView() {
        binding.tvAnalyticsTitle.setText("Group Performance");

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

                        // Calculate Revenue
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

            binding.tvMetric3Label.setText("Revenue");
            binding.tvMetric3Value.setText(formatCurrency(revenueInterest));

            // Loan Health Card
            binding.tvHealthyLoansCount.setText(String.valueOf(healthy));
            binding.tvAtRiskLoansCount.setText(String.valueOf(atRisk));
            binding.tvOverdueLoansCount.setText(String.valueOf(overdue));
            binding.cardLoanHealth.setVisibility(View.VISIBLE);

            // Chart Data - Loan Status Distribution
            setupChart(activeLoanCount, paidCount, pendingCount, rejectedCount);
            // Title is now fixed in XML as "Portfolio Status" to be cleaner
            // binding.tvChartTitle.setText("Loan Status Distribution");
        });

        // Recent Activity - Now from Transactions
        membersViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            populateRecentActivity(transactions, null);
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

    private void populateRecentActivity(List<com.example.save.data.local.entities.TransactionEntity> transactions,
            String filterMemberName) {
        List<ActivityModel> activities = new ArrayList<>();

        if (transactions != null) {
            for (com.example.save.data.local.entities.TransactionEntity tx : transactions) {
                // Filter by name if provided
                if (filterMemberName != null) {
                    if (tx.getDescription() == null || !tx.getDescription().contains(filterMemberName)) {
                        continue;
                    }
                }

                String time = "Recently";
                if (tx.getDate() != null) {
                    time = new java.text.SimpleDateFormat("MMM dd", Locale.getDefault()).format(tx.getDate());
                }

                // If filter is active (Member view), description is self-explanatory or we can
                // trim details if needed.
                // If filter is null (Admin view), description usually contains the name
                // "Contribution from X", so it's good.

                activities.add(new ActivityModel(
                        tx.getDescription(),
                        time,
                        tx.getAmount(),
                        tx.isPositive()));

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
        chart.setHoleRadius(58f); // Slightly larger hole
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setCenterText("Loans");
        chart.setCenterTextSize(14f);
        chart.setCenterTextColor(android.graphics.Color.parseColor("#9E9E9E"));

        // Improve Legend
        com.github.mikephil.charting.components.Legend legend = chart.getLegend();
        legend.setVerticalAlignment(com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);
        legend.setTextColor(android.graphics.Color.parseColor("#757575"));
        legend.setYOffset(10f); // Add spacing below chart

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

        // Vibrant, Premium Colors matching the gradient cards
        ArrayList<Integer> colors = new ArrayList<>();
        // Card 1: Blue/Purple (Active) - using a solid representation
        colors.add(android.graphics.Color.parseColor("#4A00E0"));
        // Card 3: Green/Teal (Paid)
        colors.add(android.graphics.Color.parseColor("#11998e"));
        // Card 4: Orange (Pending)
        colors.add(android.graphics.Color.parseColor("#FF8008"));
        // Card 2: Red (Rejected/AtRisk)
        colors.add(android.graphics.Color.parseColor("#cb2d3e"));
        dataSet.setColors(colors);

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(8f); // More pop on selection
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        // Hide values if they are 0 (Handled by logic above effectively)

        com.github.mikephil.charting.data.PieData data = new com.github.mikephil.charting.data.PieData(dataSet);
        chart.setData(data);
        chart.invalidate(); // refresh
        chart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutCubic); // Smoother animation
    }

    private void setupLineChart(List<com.github.mikephil.charting.data.Entry> entries) {
        if (binding == null)
            return;

        com.github.mikephil.charting.charts.LineChart chart = binding.lineChart;
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        // Clean up axes
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setTextColor(android.graphics.Color.parseColor("#9E9E9E"));

        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(true); // Keep horizontal grid lines for reference
        chart.getAxisLeft().setGridColor(android.graphics.Color.parseColor("#F5F5F5"));
        chart.getAxisLeft().setDrawAxisLine(false);
        chart.getAxisLeft().setTextColor(android.graphics.Color.parseColor("#9E9E9E"));

        chart.getLegend().setEnabled(false); // Hide legend as we have one series

        if (entries != null && !entries.isEmpty()) {
            com.github.mikephil.charting.data.LineDataSet set1;
            if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
                set1 = (com.github.mikephil.charting.data.LineDataSet) chart.getData().getDataSetByIndex(0);
                set1.setValues(entries);
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                set1 = new com.github.mikephil.charting.data.LineDataSet(entries, "Savings Growth");

                set1.setMode(com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER); // Smooth curve
                set1.setDrawIcons(false);
                // Deep Purple/Blue for line
                set1.setColor(android.graphics.Color.parseColor("#8E2DE2"));
                set1.setCircleColor(android.graphics.Color.parseColor("#4A00E0"));
                set1.setLineWidth(3f);
                set1.setCircleRadius(5f);
                set1.setDrawCircleHole(true);
                set1.setCircleHoleColor(android.graphics.Color.WHITE);
                set1.setValueTextSize(9f);
                set1.setDrawFilled(true);
                set1.setDrawValues(false); // Clean look

                // Fade gradient for fill
                if (androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.fade_blue) != null) {
                    set1.setFillDrawable(
                            androidx.core.content.ContextCompat.getDrawable(getContext(), R.drawable.fade_blue));
                } else {
                    set1.setFillColor(android.graphics.Color.parseColor("#8E2DE2"));
                    set1.setFillAlpha(40);
                }

                java.util.ArrayList<com.github.mikephil.charting.interfaces.datasets.ILineDataSet> dataSets = new java.util.ArrayList<>();
                dataSets.add(set1);
                com.github.mikephil.charting.data.LineData data = new com.github.mikephil.charting.data.LineData(
                        dataSets);
                chart.setData(data);
            }
            chart.invalidate();
            chart.animateX(1000); // Animation
        } else {
            chart.setNoDataText("No savings history yet.");
            chart.invalidate();
        }
    }

    private void exportReport() {
        // Simpler approach for demo:
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            membersViewModel.getGroupBalance().observe(getViewLifecycleOwner(), balance -> {
                loansViewModel.getLoans().observe(getViewLifecycleOwner(), loans -> {
                    double activeLoans = 0;
                    if (loans != null) {
                        for (Loan l : loans) {
                            if (Loan.STATUS_ACTIVE.equals(l.getStatus())) {
                                activeLoans += (l.getTotalDue() - l.getRepaidAmount());
                            }
                        }
                    }
                    com.example.save.utils.ReportUtils.generateAndShareReport(getContext(), members,
                            balance != null ? balance : 0, activeLoans);
                });
            });
        });
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "UGX %,.0f", amount);
    }
}
