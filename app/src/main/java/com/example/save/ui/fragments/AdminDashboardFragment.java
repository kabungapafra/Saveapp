package com.example.save.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.databinding.FragmentAdminDashboardBinding;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.ui.viewmodels.NotificationsViewModel;
import com.example.save.data.repository.MemberRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private MembersViewModel viewModel;
    private NotificationsViewModel notificationsViewModel;
    private String adminNameStr;
    private double totalBalanceValue = 0.0;
    private boolean isAdmin = true;

    public static AdminDashboardFragment newInstance(boolean isAdmin) {
        AdminDashboardFragment fragment = new AdminDashboardFragment();
        Bundle args = new Bundle();
        args.putBoolean("IS_ADMIN", isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean("IS_ADMIN", true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        loadAdminData();
        setupListeners();
        setupGrowthChart();
        loadDashboardData();
        updateScheduleUI();
        setupEntranceAnimations();
        observeViewModel();

        // Hide admin-only actions if member
        if (!isAdmin) {
            binding.btnEditSchedule.setVisibility(View.GONE);
            binding.btnMarkPaid1.setVisibility(View.GONE);
            binding.btnMarkPaid2.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        binding.btnNotifications.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).showNotifications();
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).showNotifications();
            }
        });


        binding.btnThemeToggle.setOnClickListener(v -> {
            applyClickAnimation(v);
            com.example.save.utils.ThemeUtils.toggleTheme(requireContext(), "admin");
        });
        binding.viewAllTargets.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Navigating to Targets", Toast.LENGTH_SHORT).show();
        });

        binding.viewAllLoans.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                // Future: Add Loans navigation if fragment exists
                Toast.makeText(getContext(), "Navigating to Loans", Toast.LENGTH_SHORT).show();
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(new LoansFragment());
            }
        });

        // MARK PAID BUTTONS
        binding.btnMarkPaid1.setOnClickListener(v -> handleMarkPaid(1));
        binding.btnMarkPaid2.setOnClickListener(v -> handleMarkPaid(2));

        // EDIT SCHEDULE
        binding.btnEditSchedule.setOnClickListener(v -> showEditScheduleDialog());
    }

    private void handleMarkPaid(int index) {
        if (index == 1) {
            binding.btnMarkPaid1.setText("PAID \u2714");
            binding.btnMarkPaid1.setTextColor(Color.parseColor("#10B981"));
            binding.btnMarkPaid1.setEnabled(false);
        } else {
            binding.btnMarkPaid2.setText("PAID \u2714");
            binding.btnMarkPaid2.setTextColor(Color.parseColor("#10B981"));
            binding.btnMarkPaid2.setEnabled(false);
        }
        Toast.makeText(getContext(), "Transaction recorded successfully", Toast.LENGTH_SHORT).show();
    }

    private void showEditScheduleDialog() {
        Calendar c = Calendar.getInstance();
        new android.app.DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String dateStr = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            requireActivity().getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("sched_contrib_date", dateStr)
                    .apply();
            updateScheduleUI();
            Toast.makeText(getContext(), "Schedule Updated", Toast.LENGTH_SHORT).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateScheduleUI() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE);
        String contrib = prefs.getString("sched_contrib_date", "");
        String payout = prefs.getString("sched_payout_date", "");

        if (contrib.isEmpty()) {
            binding.sectionPayoutSchedule.setVisibility(View.GONE);
        } else {
            binding.sectionPayoutSchedule.setVisibility(View.VISIBLE);
            binding.tvContributionDate.setText(contrib);
            binding.tvPayoutDate.setText(payout);
        }
        
        // Hide other demo sections by default for now
        binding.sectionSavingsTargets.setVisibility(View.GONE);
        binding.sectionActiveLoans.setVisibility(View.GONE);
        binding.sectionRecentPayouts.setVisibility(View.GONE);
        binding.sectionUpcomingPayouts.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                loadDashboardData();
            }
        });

        notificationsViewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            binding.notificationBadgeDot.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void loadAdminData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
        if (isAdmin) {
            adminNameStr = prefs.getString("admin_name", "Admin");
        } else {
            // Load Member Name from Session
            adminNameStr = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserEmail();
            if (adminNameStr != null && adminNameStr.contains("@")) {
                adminNameStr = adminNameStr.split("@")[0]; // Fallback if name not found
                adminNameStr = adminNameStr.substring(0, 1).toUpperCase() + adminNameStr.substring(1);
            } else {
                adminNameStr = "Member";
            }
            
            // Try to get real name from ViewModel
            String email = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserEmail();
            if (email != null) {
                viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                    if (member != null) {
                        adminNameStr = member.getName().split(" ")[0];
                        updateGreeting();
                    }
                });
            }
        }
        updateGreeting();
    }

    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 0 && hour < 12) greeting = "Good Morning,";
        else if (hour >= 12 && hour < 17) greeting = "Good Afternoon,";
        else greeting = "Good Evening,";

        binding.tvTopGreeting.setText(greeting);
        binding.tvTopName.setText(adminNameStr);
    }

    private void loadDashboardData() {
        if (isAdmin) {
            viewModel.getDashboardSummary((success, summaryObj, message) -> {
                if (success && isAdded() && summaryObj instanceof com.example.save.data.models.DashboardSummaryResponse) {
                    com.example.save.data.models.DashboardSummaryResponse summary = (com.example.save.data.models.DashboardSummaryResponse) summaryObj;
                    requireActivity().runOnUiThread(() -> {
                        totalBalanceValue = summary.getTotalBalance();
                        updateBalanceDisplay();
                        
                        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
                        binding.monthlyContrib.setText(ugFormat.format(summary.getMonthlyContributions()).replace("UGX", "UGX "));
                        binding.interestEarned.setText(ugFormat.format(summary.getInterestEarned()).replace("UGX", "UGX "));
                    });
                }
            });
        } else {
            // LOAD MEMBER SPECIFIC DATA
            binding.tvBalanceLabel.setText("My Total Savings");
            String email = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserEmail();
            if (email != null) {
                viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                    if (member != null) {
                        totalBalanceValue = member.getContributionPaid();
                        updateBalanceDisplay();
                        
                        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
                        binding.monthlyContrib.setText(ugFormat.format(member.getContributionTarget()).replace("UGX", "UGX "));
                        binding.interestEarned.setText(ugFormat.format(member.getLoanBalance()).replace("UGX", "UGX "));
                        binding.tvInterestLabel.setText("Active Loans");
                        binding.tvMonthlyLabel.setText("Savings Target");
                    }
                });
            }
        }
    }

    private void updateBalanceDisplay() {
        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
        String formatted = ugFormat.format(totalBalanceValue).replace("UGX", "").replace("USh", "").trim();
        if (formatted.contains(".")) {
            binding.currentBalance.setText("UGX " + formatted.substring(0, formatted.lastIndexOf(".")));
        } else {
            binding.currentBalance.setText("UGX " + formatted);
        }
    }

    private void setupGrowthChart() {
        BarChart chart = binding.growthBarChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setTextSize(10f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F1F5F9"));
        leftAxis.setTextColor(Color.parseColor("#94A3B8"));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        viewModel.getSavingsTrend().observe(getViewLifecycleOwner(), trendEntries -> {
            if (trendEntries == null || trendEntries.isEmpty()) {
                // If no real data, show a subtle empty state or flat line
                ArrayList<BarEntry> emptyEntries = new ArrayList<>();
                for(int i=0; i<7; i++) emptyEntries.add(new BarEntry(i, 0.1f));
                updateChartData(chart, emptyEntries);
                return;
            }

            ArrayList<BarEntry> barEntries = new ArrayList<>();
            for (com.github.mikephil.charting.data.Entry e : trendEntries) {
                barEntries.add(new BarEntry(e.getX(), e.getY()));
            }
            updateChartData(chart, barEntries);
        });
    }

    private void updateChartData(BarChart chart, ArrayList<BarEntry> entries) {
        BarDataSet dataSet = new BarDataSet(entries, "Growth");
        int count = entries.size();
        int[] colors = new int[count];
        int lightBlue = Color.parseColor("#DBEAFE");
        int primaryBlue = Color.parseColor("#2563EB");
        int accentOrange = Color.parseColor("#FF8A00");

        for (int i = 0; i < count; i++) {
            if (i == count - 1) colors[i] = accentOrange;
            else if (i == count - 2) colors[i] = primaryBlue;
            else colors[i] = lightBlue;
        }

        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        chart.setData(new BarData(dataSet));
        chart.getBarData().setBarWidth(0.6f);
        chart.setFitBars(true);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void setupEntranceAnimations() {
        binding.balanceContainer.setAlpha(0f);
        binding.balanceContainer.setTranslationY(80f);
        binding.balanceContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void applyClickAnimation(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore nav bar directly when returning to Admin dashboard
        if (getActivity() != null) {
            View navContainer = getActivity().findViewById(R.id.navContainer);
            if (navContainer != null) navContainer.setVisibility(View.VISIBLE);
            View navAction = getActivity().findViewById(R.id.navAction);
            if (navAction != null) navAction.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
