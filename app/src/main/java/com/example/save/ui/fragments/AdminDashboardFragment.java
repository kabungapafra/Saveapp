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
import com.example.save.data.network.DashboardSummaryResponse;
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
    }

    private void setupListeners() {
        binding.btnNotifications.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).showNotifications();
            }
        });

        binding.profileIcon.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Profile settings coming soon", Toast.LENGTH_SHORT).show();
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
        String contrib = prefs.getString("sched_contrib_date", "Dec 05 & Dec 19");
        binding.tvContributionDate.setText(contrib);
        
        String payout = prefs.getString("sched_payout_date", "Jan 02 & Jan 16");
        binding.tvPayoutDate.setText(payout);
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
        adminNameStr = prefs.getString("admin_name", "Admin");
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
        viewModel.getDashboardSummary(new MemberRepository.SummaryCallback() {
            @Override
            public void onResult(boolean success, DashboardSummaryResponse summary, String message) {
                if (success && summary != null && isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        totalBalanceValue = summary.getTotalBalance();
                        updateBalanceDisplay();

                        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
                        binding.monthlyContrib.setText(ugFormat.format(summary.getMonthlyContributions()).replace("UGX", "UGX "));
                        binding.interestEarned.setText(ugFormat.format(summary.getInterestEarned()).replace("UGX", "UGX "));
                    });
                }
            }
        });
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

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 4.5f));
        entries.add(new BarEntry(1, 6.2f));
        entries.add(new BarEntry(2, 5.1f));
        entries.add(new BarEntry(3, 8.4f));
        entries.add(new BarEntry(4, 7.8f));
        entries.add(new BarEntry(5, 11.2f));
        entries.add(new BarEntry(6, 13.5f));

        BarDataSet dataSet = new BarDataSet(entries, "Growth");
        int[] colors = new int[7];
        int lightBlue = Color.parseColor("#DBEAFE");
        int primaryBlue = Color.parseColor("#2563EB");
        int accentOrange = Color.parseColor("#FF8A00");

        for (int i = 0; i < 5; i++) colors[i] = lightBlue;
        colors[5] = primaryBlue;
        colors[6] = accentOrange;

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
            View bottomNavWrapper = getActivity().findViewById(R.id.bottomNavWrapper);
            if (bottomNavWrapper != null) bottomNavWrapper.setVisibility(View.VISIBLE);
            View fabAction = getActivity().findViewById(R.id.fabAction);
            if (fabAction != null) fabAction.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
