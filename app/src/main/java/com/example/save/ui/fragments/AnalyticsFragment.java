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
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.data.models.DashboardSummaryResponse;
import com.example.save.data.models.TransactionEntity;
import com.example.save.data.models.Member;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private MembersViewModel membersViewModel;
    private MemberPerformanceAdapter performanceAdapter;
    private TransactionLedgerAdapter ledgerAdapter;

    // Cached values from the dashboard summary — all real, backend-provided figures
    private double cachedTotalBalance = 0;
    private double cachedLoanBalance = 0;
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

        setupRecyclerViews();
        setupClickListeners();
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

        binding.cardPayoutPerformance.setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, PayoutAuditFragment.newInstance())
                .addToBackStack(null).commit());

        binding.swipeRefresh.setOnRefreshListener(() -> {
            membersViewModel.syncMembers();
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
        double available = Math.max(0, cachedTotalBalance - cachedLoanBalance);
        binding.tvCurrentLiquidity.setText(formatCurrencyCompact(available));
    }

    private void updateUI() {
        if (binding == null) return;

        // 1. Dashboard summary → balance, contributions, interest, outstanding loans, liquidity
        loadSummaryData();

        // 2. Members → top contributors table
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members == null || binding == null) return;
            performanceAdapter.setMembers(members);
            if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
        });

        // 3. Transactions → recent ledger
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
            cachedLoanBalance = s.getLoanBalance();
            cachedContribAmount = s.getContributionAmount();
            cachedTotalContributors = s.getTotalContributors();

            double monthlyTarget = s.getContributionAmount() * s.getTotalContributors();
            int monthlyPct = monthlyTarget > 0
                    ? (int) Math.min(100, (s.getMonthlyContributions() / monthlyTarget) * 100) : 0;

            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;

                // Hero — total group balance
                animateNumber(binding.tvTotalLiquidity, s.getTotalBalance(), "UGX %,.0f");

                // Contributions this month + year to date
                binding.tvMonthlyContributions.setText(formatCurrencyCompact(s.getMonthlyContributions()));
                animateProgressBar(binding.progressMonthly, monthlyPct);
                binding.tvMonthlyTargetLabel.setText(monthlyPct + "% of monthly target");
                binding.tvYearlyContributions.setText(formatCurrencyCompact(s.getYearlyContributions()));

                // Outstanding loans + interest earned
                binding.tvOutstandingLoans.setText(formatCurrencyCompact(s.getLoanBalance()));
                binding.tvInterestEarned.setText(formatCurrencyCompact(s.getInterestEarned()));

                updateLiquidityDisplay();
                if (binding.swipeRefresh != null) binding.swipeRefresh.setRefreshing(false);
            });
        });
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

    private void exportReport(String type) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(),
                    "Generating " + ("EXCEL".equalsIgnoreCase(type) ? "Excel report" : "PDF") + "…",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
        membersViewModel.getComprehensiveReport((success, report, message) -> {
            if (!isAdded() || getContext() == null) return;
            if (success && report != null) {
                if ("EXCEL".equalsIgnoreCase(type)) {
                    com.example.save.utils.ReportUtils.generateAndShareExcel(getContext(), report);
                } else {
                    com.example.save.utils.ReportUtils.generateAndShareReport(getContext(), report);
                }
            } else {
                android.widget.Toast.makeText(getContext(),
                        message != null ? message : "Could not generate report",
                        android.widget.Toast.LENGTH_LONG).show();
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

            // Yearly per-member target from live summary (contribution amount × 12)
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
