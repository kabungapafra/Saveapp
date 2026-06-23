package com.example.save.ui.fragments;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.FragmentAnalyticsBinding;
import com.example.save.databinding.ItemMemberPerformanceBinding;
import com.example.save.databinding.ItemTransactionLedgerBinding;
import com.example.save.ui.viewmodels.LoansViewModel;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.data.models.DashboardSummaryResponse;
import com.example.save.data.models.TransactionEntity;
import com.example.save.data.models.Loan;
import com.example.save.data.models.Member;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.animation.Easing;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private MembersViewModel membersViewModel;
    private LoansViewModel loansViewModel;
    private MemberPerformanceAdapter performanceAdapter;
    private TransactionLedgerAdapter ledgerAdapter;

    // Cached values — updated incrementally from each data source
    private double cachedTotalBalance = 0;
    private double cachedActiveLoans = 0;
    private int cachedConsistency = 0;
    private int cachedRiskMitigation = 0;
    private double cachedContribAmount = 0;
    private int cachedTotalContributors = 0;

    public static AnalyticsFragment newInstance(boolean isAdmin) {
        return newInstance(isAdmin, true);
    }

    public static AnalyticsFragment newInstance(boolean isAdmin, boolean showBackButton) {
        AnalyticsFragment fragment = new AnalyticsFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_admin", isAdmin);
        args.putBoolean("show_back", showBackButton);
        fragment.setArguments(args);
        return fragment;
    }

    public static AnalyticsFragment newInstance(boolean isAdmin, String memberEmail) {
        AnalyticsFragment fragment = new AnalyticsFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_admin", isAdmin);
        args.putString("member_email", memberEmail);
        args.putBoolean("show_back", true);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            boolean showBack = getArguments().getBoolean("show_back", true);
            binding.backButton.setVisibility(showBack ? View.VISIBLE : View.GONE);
        }

        membersViewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        loansViewModel = new ViewModelProvider(requireActivity()).get(LoansViewModel.class);

        setupRecyclerViews();
        setupClickListeners();
        setupBarChart();
        updateUI();

        return binding.getRoot();
    }

    private void setupRecyclerViews() {
        performanceAdapter = new MemberPerformanceAdapter();
        binding.rvMemberPerformance.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvMemberPerformance.setAdapter(performanceAdapter);

        ledgerAdapter = new TransactionLedgerAdapter();
        binding.rvTransactionalLedger.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTransactionalLedger.setAdapter(ledgerAdapter);
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        binding.btnExportPdf.setOnClickListener(v -> exportReport("PDF"));
        binding.btnExcelReport.setOnClickListener(v -> exportReport("EXCEL"));
        binding.btnViewAllMembers.setOnClickListener(v -> navigateToMemberSummary());
        binding.cardMemberPerformance.setOnClickListener(v -> navigateToMemberSummary());

        if (binding.cardPayoutPerformance != null) {
            binding.cardPayoutPerformance.setOnClickListener(v ->
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, PayoutAuditFragment.newInstance())
                    .addToBackStack(null).commit());
        }

        binding.cardLiquiditySummary.setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, LiquidityDashboardFragment.newInstance())
                .addToBackStack(null).commit());

        binding.swipeRefresh.setOnRefreshListener(() -> {
            membersViewModel.syncMembers();
            loansViewModel.refresh();
            loadSummaryData();
        });
    }

    private void navigateToMemberSummary() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, MemberSummaryFragment.newInstance())
                .addToBackStack(null).commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateLiquidityDisplay() {
        if (binding == null) return;
        double available = Math.max(0, cachedTotalBalance - cachedActiveLoans);
        binding.tvCurrentLiquidity.setText(formatCurrencyCompact(available));
    }

    private void updateUI() {
        if (binding == null) return;

        // 1. Dashboard summary → balance, gross contributions, interest, consistency
        loadSummaryData();

        // 2. Loans → active loans total + risk mitigation score
        loansViewModel.getLoans().observe(getViewLifecycleOwner(), loans -> {
            if (loans == null || binding == null) return;
            double activeLoansTotal = 0;
            int healthy = 0, total = 0;
            for (Loan l : loans) {
                String st = l.getStatus();
                if ("ACTIVE".equalsIgnoreCase(st) || "APPROVED".equalsIgnoreCase(st)) {
                    activeLoansTotal += l.getAmount();
                    total++;
                    if ("HEALTHY".equals(calculateLoanHealth(l))) healthy++;
                }
            }
            cachedActiveLoans = activeLoansTotal;
            cachedRiskMitigation = total > 0 ? (int) ((healthy / (double) total) * 100) : 100;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                updateLiquidityDisplay();
                updateHealthIndex();
            });
        });

        // 3. Members → performance table
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members == null || binding == null) return;
            performanceAdapter.setMembers(members);
            if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
        });

        // 4. Transactions → ledger
        membersViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || binding == null) return;
            ledgerAdapter.setTransactions(transactions);
        });
    }

    private void loadSummaryData() {
        membersViewModel.getDashboardSummary((success, summaryObj, message) -> {
            if (!isAdded() || binding == null) return;
            if (!success || !(summaryObj instanceof DashboardSummaryResponse)) {
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
                return;
            }
            DashboardSummaryResponse s = (DashboardSummaryResponse) summaryObj;
            cachedTotalBalance = s.getTotalBalance();
            cachedContribAmount = s.getContributionAmount();
            cachedTotalContributors = s.getTotalContributors();

            double expectedMonthly = s.getContributionAmount() * s.getTotalContributors();
            cachedConsistency = expectedMonthly > 0
                    ? (int) Math.min(100, (s.getMonthlyContributions() / expectedMonthly) * 100) : 0;

            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;

                // Hero card — total group balance
                animateNumber(binding.tvTotalLiquidity, s.getTotalBalance(), "UGX %,.0f");

                // Gross contributions (all approved contributions this year)
                binding.tvGrossContributions.setText(formatCurrencyCompact(s.getYearlyContributions()));
                double yearlyTarget = s.getContributionAmount() * s.getTotalContributors() * 12;
                int grossPct = yearlyTarget > 0
                        ? (int) Math.min(100, (s.getYearlyContributions() / yearlyTarget) * 100) : 0;
                animateProgressBar(binding.progressGross, grossPct);

                // Interest earned (from approved/active/completed loans)
                binding.tvInterestEarned.setText(formatCurrencyCompact(s.getInterestEarned()));

                updateLiquidityDisplay();
                updateHealthIndex();
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
            });

            loadForecastChart();
        });
    }

    private void loadForecastChart() {
        if (!isAdded() || binding == null) return;
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        // Build last 4 months buckets
        LinkedHashMap<String, Float> monthMap = new LinkedHashMap<>();
        SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        for (int i = 3; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -i);
            monthMap.put(keyFmt.format(c.getTime()), 0f);
        }

        api.getTransactions(100, 0).enqueue(new Callback<PaginatedResponse<TransactionEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<TransactionEntity>> call,
                    Response<PaginatedResponse<TransactionEntity>> response) {
                if (!isAdded() || binding == null || !response.isSuccessful() || response.body() == null) return;
                for (TransactionEntity tx : response.body().getData()) {
                    if (!"CONTRIBUTION".equalsIgnoreCase(tx.getType())) continue;
                    if (!"APPROVED".equalsIgnoreCase(tx.getStatus())) continue;
                    if (tx.getDate() == null) continue;
                    String key = keyFmt.format(tx.getDate());
                    if (monthMap.containsKey(key)) monthMap.put(key, monthMap.get(key) + (float) tx.getAmount());
                }
                List<BarEntry> entries = new ArrayList<>();
                int idx = 0;
                float lastActual = 0;
                for (float val : monthMap.values()) {
                    entries.add(new BarEntry(idx++, val));
                    if (val > 0) lastActual = val;
                }
                // 5th bar: projected next month
                float projected = (float) (cachedContribAmount * cachedTotalContributors);
                if (projected <= 0) projected = lastActual;
                entries.add(new BarEntry(idx, projected));
                requireActivity().runOnUiThread(() -> updateForecastChart(entries));
            }
            @Override
            public void onFailure(Call<PaginatedResponse<TransactionEntity>> call, Throwable t) {}
        });
    }

    private void updateHealthIndex() {
        if (binding == null) return;
        int reserveRatio = cachedTotalBalance > 0
                ? (int) Math.min(100, Math.max(0, ((cachedTotalBalance - cachedActiveLoans) / cachedTotalBalance) * 100))
                : 0;
        int score = (cachedConsistency + cachedRiskMitigation + reserveRatio) / 3;

        binding.tvConsistencyPct.setText(cachedConsistency + "%");
        animateProgressBar(binding.progressConsistency, cachedConsistency);
        binding.tvRiskPct.setText(cachedRiskMitigation + "%");
        animateProgressBar(binding.progressRisk, cachedRiskMitigation);
        binding.tvReservePct.setText(reserveRatio + "%");
        animateProgressBar(binding.progressReserve, reserveRatio);
        binding.tvHealthScore.setText(score + "%");
        animateProgressBar(binding.progressLiquidity, score);
    }

    private void setupBarChart() {
        if (binding == null) return;
        BarChart chart = binding.forecastChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setScaleEnabled(false);
        chart.setRenderer(new com.example.save.ui.views.RoundedBarChartRenderer(
                chart, chart.getAnimator(), chart.getViewPortHandler(), 20f));
    }

    private void updateForecastChart(List<BarEntry> barEntries) {
        if (binding == null) return;
        if (barEntries == null || barEntries.isEmpty()) {
            barEntries = new ArrayList<>();
            barEntries.add(new BarEntry(0, 0f));
        }

        BarDataSet set = new BarDataSet(barEntries, "Forecast");
        // 5 shades from lightest (oldest) to orange (projection)
        int[] colors = new int[]{
            android.graphics.Color.parseColor("#BFDBFE"),
            android.graphics.Color.parseColor("#93C5FD"),
            android.graphics.Color.parseColor("#60A5FA"),
            android.graphics.Color.parseColor("#3B82F6"),
            android.graphics.Color.parseColor("#FF8A00")  // forecast bar is orange
        };
        // Assign colors cyclically in case fewer bars
        int[] assignedColors = new int[barEntries.size()];
        for (int i = 0; i < barEntries.size(); i++) {
            assignedColors[i] = colors[Math.min(i, colors.length - 1)];
        }
        set.setColors(assignedColors);
        set.setDrawValues(false);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        binding.forecastChart.setData(data);
        binding.forecastChart.invalidate();
        binding.forecastChart.animateY(1200, Easing.EaseOutCubic);
    }

    private void animateNumber(TextView view, double target, String format) {
        if (view == null) return;
        Handler handler = new Handler(Looper.getMainLooper());
        final long duration = 1200;
        final long startTime = System.currentTimeMillis();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (view == null || binding == null) return;
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
        ObjectAnimator anim = ObjectAnimator.ofInt(bar, "progress", 0, target);
        anim.setDuration(900);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setStartDelay(300);
        anim.start();
    }

    private void animateProgressBar(com.google.android.material.progressindicator.CircularProgressIndicator bar, int target) {
        if (bar == null) return;
        ObjectAnimator anim = ObjectAnimator.ofInt(bar, "progress", 0, target);
        anim.setDuration(900);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setStartDelay(300);
        anim.start();
    }

    private String calculateLoanHealth(Loan loan) {
        if (loan.getDueDate() == null) return "HEALTHY";
        long diff = loan.getDueDate().getTime() - new Date().getTime();
        long daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        if (daysDiff < 0) return "OVERDUE";
        else if (daysDiff <= 7) return "AT_RISK";
        else return "HEALTHY";
    }

    private void exportReport(String type) {
        membersViewModel.getComprehensiveReport((success, report, message) -> {
            if (success && report != null) {
                com.example.save.utils.ReportUtils.generateAndShareReport(getContext(), report);
            }
        });
    }

    private String formatCurrencyCompact(double amount) {
        if (amount >= 1_000_000) return String.format(Locale.getDefault(), "UGX %.1fM", amount / 1_000_000);
        if (amount >= 1_000) return String.format(Locale.getDefault(), "UGX %.1fK", amount / 1_000);
        return String.format(Locale.getDefault(), "UGX %.0f", amount);
    }

    // ─── INNER ADAPTERS ───────────────────────────────────────────────────────

    private class MemberPerformanceAdapter extends RecyclerView.Adapter<MemberPerformanceAdapter.ViewHolder> {
        private List<Member> members = new ArrayList<>();

        public void setMembers(List<Member> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemMemberPerformanceBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member m = members.get(position);
            holder.binding.tvMemberName.setText(m.getName() != null ? m.getName() : "—");
            if (m.getName() != null && !m.getName().isEmpty()) {
                holder.binding.tvAvatarInitials.setText(
                        m.getName().substring(0, Math.min(2, m.getName().length())).toUpperCase());
            }

            // Use yearly per-member target from live summary (cachedContribAmount * 12)
            double yearlyTarget = cachedContribAmount > 0
                    ? cachedContribAmount * 12
                    : membersViewModel.getContributionTarget();
            int progress = (yearlyTarget > 0)
                    ? (int) Math.min(100, Math.max(0, (m.getContributionPaid() / yearlyTarget) * 100))
                    : 0;

            holder.binding.progressEfficiency.setProgress(progress);
            holder.binding.tvEfficiencyPct.setText(progress + "%");
            holder.binding.tvEfficiencyValue.setText(
                    String.format(Locale.getDefault(), "%.1f", m.getContributionPaid() / 1_000_000.0));
            holder.binding.tvMemberSub.setText("Rank #" + (position + 1));
        }

        @Override
        public int getItemCount() { return Math.min(5, members.size()); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemMemberPerformanceBinding binding;
            ViewHolder(ItemMemberPerformanceBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }

    private class TransactionLedgerAdapter extends RecyclerView.Adapter<TransactionLedgerAdapter.ViewHolder> {
        private List<TransactionEntity> txs = new ArrayList<>();

        public void setTransactions(List<TransactionEntity> transactions) {
            this.txs = transactions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemTransactionLedgerBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TransactionEntity tx = txs.get(position);

            // Show member name; fall back to description if name is absent
            String displayName = (tx.getMemberName() != null && !tx.getMemberName().isEmpty())
                    ? tx.getMemberName()
                    : (tx.getDescription() != null ? tx.getDescription() : "—");
            holder.binding.tvTxMemberName.setText(displayName);

            if (tx.getDate() != null) {
                holder.binding.tvTxDateRef.setText(
                        new SimpleDateFormat("MMM dd", Locale.getDefault()).format(tx.getDate())
                                + " • REF# " + (tx.getId() != null && tx.getId().length() > 4
                                    ? tx.getId().substring(0, 4) : tx.getId()));
            }

            // Derive type from tx.getType() — isPositive() is unreliable (not sent by backend)
            String txType = tx.getType() != null ? tx.getType().toUpperCase(Locale.getDefault()) : "";
            switch (txType) {
                case "CONTRIBUTION":
                    holder.binding.tvTxTypeBadge.setText("Contribution");
                    holder.binding.tvTxTypeBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#DCFCE7")));
                    holder.binding.tvTxTypeBadge.setTextColor(android.graphics.Color.parseColor("#16A34A"));
                    holder.binding.tvTxAmount.setText("+" + String.format(Locale.getDefault(), "%,.0f", tx.getAmount()));
                    holder.binding.tvTxAmount.setTextColor(android.graphics.Color.parseColor("#10B981"));
                    break;
                case "LOAN_REPAYMENT":
                    holder.binding.tvTxTypeBadge.setText("Repayment");
                    holder.binding.tvTxTypeBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#EEF2FF")));
                    holder.binding.tvTxTypeBadge.setTextColor(android.graphics.Color.parseColor("#2563EB"));
                    holder.binding.tvTxAmount.setText("+" + String.format(Locale.getDefault(), "%,.0f", tx.getAmount()));
                    holder.binding.tvTxAmount.setTextColor(android.graphics.Color.parseColor("#10B981"));
                    break;
                default:
                    boolean isYield = tx.getDescription() != null
                            && tx.getDescription().toUpperCase().contains("YIELD");
                    holder.binding.tvTxTypeBadge.setText(isYield ? "Yield Payout" : "Disbursement");
                    String bgColor = isYield ? "#EEF2FF" : "#FEF3C7";
                    String textColor = isYield ? "#2563EB" : "#F59E0B";
                    holder.binding.tvTxTypeBadge.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(bgColor)));
                    holder.binding.tvTxTypeBadge.setTextColor(android.graphics.Color.parseColor(textColor));
                    holder.binding.tvTxAmount.setText("-" + String.format(Locale.getDefault(), "%,.0f", Math.abs(tx.getAmount())));
                    holder.binding.tvTxAmount.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                    break;
            }
        }

        @Override
        public int getItemCount() { return Math.min(10, txs.size()); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemTransactionLedgerBinding binding;
            ViewHolder(ItemTransactionLedgerBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }
}
