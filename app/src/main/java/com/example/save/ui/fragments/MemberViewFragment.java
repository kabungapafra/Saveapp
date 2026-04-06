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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.FragmentMemberViewBinding;
import com.example.save.databinding.ItemMemberSimpleBinding;
import com.example.save.ui.viewmodels.MembersViewModel; // Added import
import com.example.save.data.models.Member;
import androidx.lifecycle.ViewModelProvider; // Added import

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.example.save.ui.viewmodels.LoansViewModel;
import com.example.save.data.models.Loan;

public class MemberViewFragment extends Fragment {

    private FragmentMemberViewBinding binding;
    private MembersViewModel viewModel;
    private LoansViewModel loansViewModel;
    private List<Member> memberList;
    private MemberAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberViewBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
        loansViewModel = new ViewModelProvider(this).get(LoansViewModel.class);

        setupRecyclerView();
        observeViewModel();
        setupAnalytics();

        // Initial sync to ensure data is fresh
        viewModel.syncMembers();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private MemberAdapter adminsAdapter;

    private void setupRecyclerView() {
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        memberList = new ArrayList<>();
        adapter = new MemberAdapter(memberList);
        binding.membersRecyclerView.setAdapter(adapter);

        // Admins setup
        adminsAdapter = new MemberAdapter(new ArrayList<>());
        binding.rvAdmins.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvAdmins.setAdapter(adminsAdapter);
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberList.clear();
                memberList.addAll(members);
                updateMemberCount();
                adapter.notifyDataSetChanged();
                refreshAdmins();
            }
        });
    }

    private void refreshAdmins() {
        if (binding == null)
            return;

        new Thread(() -> {
            try {
                List<Member> admins = viewModel.getAdmins();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null && adminsAdapter != null && admins != null) {
                            adminsAdapter.updateList(admins);
                            if (binding.tvAdminCount != null) {
                                binding.tvAdminCount.setText(String.valueOf(admins.size()));
                            }
                            if (binding.cvAdmins != null) {
                                binding.cvAdmins.setVisibility(admins.isEmpty() ? View.GONE : View.VISIBLE);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null && binding.cvAdmins != null) {
                            binding.cvAdmins.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    private void updateMemberCount() {
        if (binding != null && binding.tvMemberCount != null) {
            binding.tvMemberCount.setText(memberList.size() + " Active Members");
        }
    }

    private void setupAnalytics() {
        setupBarChart();
        updateAnalyticsUI();
    }

    private void updateAnalyticsUI() {
        if (binding == null) return;

        // Portfolio Liquidity
        viewModel.getGroupBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null && binding != null) {
                animateNumber(binding.tvTotalLiquidity, balance != null ? balance : 45200000.0, "UGX %,.0f");
                binding.tvCurrentLiquidity.setText(formatCurrencyCompact(balance != null ? balance : 12200000.0));
                animateProgressBar(binding.progressLiquidity, 40);
            }
        });

        // Contributions & Metrics
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null && !members.isEmpty() && binding != null) {
                double totalGross = 0;
                for (Member m : members) {
                    totalGross += m.getContributionPaid();
                }
                binding.tvGrossContributions.setText(formatCurrencyCompact(totalGross > 0 ? totalGross : 38400000.0));
                updateHealthIndex(members, null);
                animateProgressBar(binding.progressGross, 85);
            }
        });

        // Interests & Loans
        loansViewModel.getLoans().observe(getViewLifecycleOwner(), loans -> {
            if (loans != null && binding != null) {
                double totalInterest = 0;
                for (Loan l : loans) {
                    totalInterest += l.getInterest();
                }
                binding.tvInterestEarned.setText(formatCurrencyCompact(totalInterest > 0 ? totalInterest : 6800000.0));
                updateHealthIndex(null, loans);
                animateProgressBar(binding.progressInterest, 55);
            }
        });

        updateForecastChart();
    }

    private void updateHealthIndex(List<Member> members, List<Loan> loans) {
        if (binding == null) return;

        int consistency = 68;
        int riskMitigation = 82;
        int reserveRatio = 91;

        if (members != null && !members.isEmpty()) {
            double totalPaid = 0;
            double totalTarget = members.size() * 1000000.0;
            for(Member m : members) totalPaid += m.getContributionPaid();
            if(totalTarget > 0) consistency = (int)((totalPaid / totalTarget) * 100);
            consistency = Math.min(100, Math.max(0, consistency));
        }

        int score = (consistency + riskMitigation + reserveRatio) / 3;
        binding.tvConsistencyPct.setText(consistency + "%");
        animateProgressBar(binding.progressConsistency, consistency);
        
        binding.tvRiskPct.setText(riskMitigation + "%");
        animateProgressBar(binding.progressRisk, riskMitigation);
        
        binding.tvReservePct.setText(reserveRatio + "%");
        animateProgressBar(binding.progressReserve, reserveRatio);

        binding.tvHealthScore.setText(score + "%");
        animateProgressBar(binding.progressHealth, score);
    }

    private void setupBarChart() {
        BarChart chart = binding.forecastChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setScaleEnabled(false);
    }

    private void updateForecastChart() {
        if (binding == null) return;
        
        List<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0, 45f));
        barEntries.add(new BarEntry(1, 58f));
        barEntries.add(new BarEntry(2, 72f));
        barEntries.add(new BarEntry(3, 86f));
        barEntries.add(new BarEntry(4, 100f));

        BarDataSet set = new BarDataSet(barEntries, "Forecast");
        int[] colors = new int[]{
            android.graphics.Color.parseColor("#BFDBFE"),
            android.graphics.Color.parseColor("#93C5FD"),
            android.graphics.Color.parseColor("#60A5FA"),
            android.graphics.Color.parseColor("#3B82F6"),
            android.graphics.Color.parseColor("#2563EB")
        };
        set.setColors(colors);
        set.setDrawValues(false);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        binding.forecastChart.setData(data);
        binding.forecastChart.invalidate();
        binding.forecastChart.animateY(1200);
    }

    private void animateNumber(TextView view, double target, String format) {
        Handler handler = new Handler(Looper.getMainLooper());
        final long duration = 1200;
        final long startTime = System.currentTimeMillis();
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (view == null) return;
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.min(1f, (float) elapsed / duration);
                float easedProgress = 1 - (float) Math.pow(1 - progress, 3);
                double current = easedProgress * target;
                view.setText(String.format(Locale.getDefault(), format, current));
                if (progress < 1) handler.postDelayed(this, 16);
                else view.setText(String.format(Locale.getDefault(), format, target));
            }
        });
    }

    private void animateProgressBar(com.google.android.material.progressindicator.LinearProgressIndicator bar, int target) {
        if (bar == null) return;
        android.animation.ObjectAnimator animation = android.animation.ObjectAnimator.ofInt(bar, "progress", 0, target);
        animation.setDuration(900);
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animation.setStartDelay(300);
        animation.start();
    }

    private String formatCurrencyCompact(double amount) {
        if (amount >= 1000000) return String.format(Locale.getDefault(), "UGX %.1fM", amount / 1000000);
        if (amount >= 1000) return String.format(Locale.getDefault(), "UGX %.1fK", amount / 1000);
        return String.format(Locale.getDefault(), "UGX %.0f", amount);
    }
}
