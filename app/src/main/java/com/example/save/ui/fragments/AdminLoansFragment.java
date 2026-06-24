package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.R;
import com.example.save.databinding.FragmentAdminLoansBinding;
import com.example.save.data.models.LoanEntity;
import com.example.save.data.models.LoanEntity;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.ui.adapters.LoanBorrowerAdapter;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.data.models.ApprovalRequest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminLoansFragment extends Fragment {

    private FragmentAdminLoansBinding binding;
    private MembersViewModel viewModel;
    private LoanBorrowerAdapter adapter;
    private ApprovalsAdapter approvalsAdapter;
    private List<LoanEntity> allRequests = new ArrayList<>();
    private String currentFilter = "ALL";
    private String searchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminLoansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupListeners();
        setupChart();
        observeLoanRequests();
        loadSummaryStats();
        loadLoanRules();
    }

    private void loadLoanRules() {
        viewModel.fetchSystemConfig((success, config, message) -> {
            if (success && config != null && isVisible()) {
                getActivity().runOnUiThread(() -> {
                    binding.tvMaxLoanLimit.setText("UGX " + String.format(Locale.US, "%,.0f", config.getMaxLoanLimit()));
                    binding.tvLoanInterest.setText(String.format(Locale.US, "%.1f%%", config.getLoanInterestRate()));
                    binding.tvLoanDuration.setText(config.getMaxLoanDuration() + " Months");
                });
            }
        });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Decentralized Approvals - Header Click (Today's Work Restoration)
        binding.btnViewAllApprovals.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                ((com.example.save.ui.activities.AdminMainActivity) getActivity()).loadFragment(new ApprovalsFragment(), true);
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvLoans.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LoanBorrowerAdapter(new LoanBorrowerAdapter.OnLoanActionListener() {
            @Override
            public void onApprove(LoanEntity request) {
                com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(getContext());
                String adminPhone = session.getUserPhone();
                viewModel.initiateLoanApproval(request.getId(), adminPhone, (success, message) -> {
                    if (isVisible()) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onDecline(LoanEntity request) {
                showRejectDialog(request);
            }

            @Override
            public void onRemind(LoanEntity request) {
                Toast.makeText(getContext(), "Reminder sent to " + request.getMemberName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onViewDetails(LoanEntity request) {
                Toast.makeText(getContext(), "Details for " + request.getMemberName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendAlert(LoanEntity request) {
                Toast.makeText(getContext(), "Late payment alert sent to " + request.getMemberName(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvLoans.setAdapter(adapter);

        // Setup Approvals (Today's Work Restoration)
        approvalsAdapter = new ApprovalsAdapter(new ApprovalsAdapter.OnApprovalClickListener() {
            @Override
            public void onApproveClick(ApprovalsAdapter.ApprovalItem item) {
                viewModel.processApproval(item, true, (success, message) -> {
                    if (isVisible()) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onItemClick(ApprovalsAdapter.ApprovalItem item) {
                if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                    String amountStr = "UGX " + String.format(java.util.Locale.US, "%,.0f", item.getAmount());
                    ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                            .loadFragment(ConfirmLoanApprovalFragment.newInstance(item.getId(), item.getTitle(), amountStr), true);
                }
            }
        });
        binding.rvPendingApprovals.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPendingApprovals.setAdapter(approvalsAdapter);
    }

    private void showRejectDialog(LoanEntity request) {
        String formattedAmount = "UGX " + String.format(Locale.US, "%,.0f", request.getAmount());
        DeclineLoanRequestFragment fragment = DeclineLoanRequestFragment.newInstance(
                request.getId(),
                request.getMemberName(),
                formattedAmount,
                "Standard Loan" // Or request.getLoanType() if available
        );
        
        if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
            ((com.example.save.ui.activities.AdminMainActivity) getActivity()).loadFragment(fragment, true);
        } else {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setupSearch() {
        binding.etSearchBorrower.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }
        });
    }

    private void setupFilterChips() {
        binding.btnFilterAll.setOnClickListener(v -> updateFilterUI("ALL"));
        binding.btnFilterActive.setOnClickListener(v -> updateFilterUI("ACTIVE"));
        binding.btnFilterPending.setOnClickListener(v -> updateFilterUI("PENDING"));
        binding.btnFilterLate.setOnClickListener(v -> updateFilterUI("LATE"));

        // Initial selection
        updateFilterUI("ALL");
    }

    private void updateFilterUI(String filter) {
        currentFilter = filter;

        // Reset all buttons to text style
        resetFilterButton(binding.btnFilterAll);
        resetFilterButton(binding.btnFilterActive);
        resetFilterButton(binding.btnFilterPending);
        resetFilterButton(binding.btnFilterLate);

        // Highlight selected
        com.google.android.material.button.MaterialButton selected = null;
        switch (filter) {
            case "ALL":
                selected = binding.btnFilterAll;
                break;
            case "ACTIVE":
                selected = binding.btnFilterActive;
                break;
            case "PENDING":
                selected = binding.btnFilterPending;
                break;
            case "LATE":
                selected = binding.btnFilterLate;
                break;
        }

        if (selected != null) {
            selected.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.brand_blue));
            selected.setTextColor(Color.WHITE);
        }

        applyFilters();
    }

    private void resetFilterButton(com.google.android.material.button.MaterialButton btn) {
        btn.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(requireContext(), R.color.v_input_bg));
        btn.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.v_text_muted));
    }

    private void observeLoanRequests() {
        viewModel.getLoanRequests().observe(getViewLifecycleOwner(), requests -> {
            if (requests != null) {
                allRequests = requests;
                calculateStats(requests);
                applyFilters();
            }
        });

        // Observe Loan Approvals (Today's Work Restoration)
        viewModel.getPendingApprovals().observe(getViewLifecycleOwner(), approvals -> {
            List<ApprovalsAdapter.ApprovalItem> loanApprovals = new ArrayList<>();
            if (approvals != null) {
                for (ApprovalRequest a : approvals) {
                    if ("LOAN".equalsIgnoreCase(a.getType())) {
                        loanApprovals.add(a);
                    }
                }
            }

            if (loanApprovals.isEmpty()) {
                binding.rvPendingApprovals.setVisibility(View.GONE);
                binding.tvNoApprovals.setVisibility(View.VISIBLE);
            } else {
                binding.rvPendingApprovals.setVisibility(View.VISIBLE);
                binding.tvNoApprovals.setVisibility(View.GONE);
                approvalsAdapter.updateList(loanApprovals);
            }
        });
    }

    private void applyFilters() {
        List<LoanEntity> filteredList = new ArrayList<>();
        for (LoanEntity req : allRequests) {
            boolean matchesSearch = req.getMemberName().toLowerCase().contains(searchQuery);
            boolean matchesCategory = currentFilter.equals("ALL")
                    || (currentFilter.equals("ACTIVE") && "ACTIVE".equals(req.getStatus()))
                    || (currentFilter.equals("PENDING") && "PENDING".equals(req.getStatus()))
                    || (currentFilter.equals("LATE") && "LATE".equals(req.getStatus()));

            if (matchesSearch && matchesCategory) {
                filteredList.add(req);
            }
        }

        if (!filteredList.isEmpty()) {
            adapter.updateList(filteredList);
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.rvLoans.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.rvLoans.setVisibility(View.GONE);
            binding.tvEmptyState.setText(currentFilter.equals("ALL") ? "No loan records found"
                    : "No " + currentFilter.toLowerCase() + " loans found");
        }
    }

    private void calculateStats(List<LoanEntity> requests) {
        double totalActiveAmount = 0;
        double overdueAmount = 0;
        int activeCount = 0;
        LoanEntity latestLoan = null;

        for (LoanEntity req : requests) {
            String status = req.getStatus();
            if ("ACTIVE".equals(status) || "APPROVED".equals(status)
                    || "PENDING".equals(status) || "DISBURSED".equals(status)) {
                totalActiveAmount += req.getAmount();
                activeCount++;
                if (latestLoan == null) latestLoan = req;
            }
            if ("LATE".equals(status) || "OVERDUE".equals(status)) {
                overdueAmount += req.getAmount();
                if (latestLoan == null) latestLoan = req;
            }
        }

        binding.tvTotalActiveLoans.setText("UGX " + formatAmount(totalActiveAmount));
        binding.tvDisbursedAmount.setText("UGX " + formatAmount(overdueAmount));
        binding.tvPendingCount.setText(String.valueOf(activeCount));

        populateLoanTracker(latestLoan);
    }

    private void populateLoanTracker(LoanEntity loan) {
        if (loan == null) {
            binding.tvTrackerAmount.setText("--");
            binding.tvTrackerStatusPill.setText("--");
            binding.tvTrackerSubtitle.setText("No active loan");
            binding.pbTrackerRepaid.setProgress(0);
            binding.tvTrackerRepaidPct.setText("0% repaid");
            int gray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.divider_color);
            setStep(binding.ivStepPending, false, gray);
            setStep(binding.ivStepApproved, false, gray);
            setStep(binding.ivStepDisbursed, false, gray);
            setStep(binding.ivStepRepaid, false, gray);
            binding.lineStep1.setBackgroundColor(gray);
            binding.lineStep2.setBackgroundColor(gray);
            binding.lineStep3.setBackgroundColor(gray);
            return;
        }

        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
        binding.tvTrackerAmount.setText(ugFormat.format(loan.getAmount()).replace("UGX", "UGX "));

        String status = loan.getStatus() != null ? loan.getStatus() : "PENDING";
        binding.tvTrackerStatusPill.setText(capitalize(status));

        String subtitle = "Outstanding";
        if (loan.getDueDate() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            subtitle = "Outstanding · next repayment " + sdf.format(loan.getDueDate());
        }
        binding.tvTrackerSubtitle.setText(subtitle);

        int pct = 0;
        if (loan.getAmount() > 0 && loan.getRepaidAmount() > 0) {
            pct = (int) Math.min(100, (loan.getRepaidAmount() / loan.getAmount()) * 100);
        }
        binding.pbTrackerRepaid.setProgress(pct);
        binding.tvTrackerRepaidPct.setText(pct + "% repaid");

        boolean isApproved = "APPROVED".equals(status) || "DISBURSED".equals(status)
                || "ACTIVE".equals(status) || "LATE".equals(status) || "OVERDUE".equals(status) || "REPAID".equals(status);
        boolean isDisbursed = "DISBURSED".equals(status) || "ACTIVE".equals(status)
                || "LATE".equals(status) || "OVERDUE".equals(status) || "REPAID".equals(status);
        boolean isRepaid = "REPAID".equals(status);

        int green = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dashboard_accent_green);
        int divider = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.divider_color);
        int textGreen = green;
        int textGray = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dashboard_text_secondary);

        // Pending is always the first completed step
        setStep(binding.ivStepPending, true, green);
        binding.tvLabelPending.setTextColor(textGreen);
        setStep(binding.ivStepApproved, isApproved, green);
        binding.tvStepApproved.setTextColor(isApproved ? textGreen : textGray);
        setStep(binding.ivStepDisbursed, isDisbursed, green);
        binding.tvStepDisbursed.setTextColor(isDisbursed ? textGreen : textGray);
        setStep(binding.ivStepRepaid, isRepaid, green);
        binding.tvStepRepaid.setTextColor(isRepaid ? textGreen : textGray);

        binding.lineStep1.setBackgroundColor(isApproved ? green : divider);
        binding.lineStep2.setBackgroundColor(isDisbursed ? green : divider);
        binding.lineStep3.setBackgroundColor(isRepaid ? green : divider);
    }

    private void setStep(android.widget.ImageView iv, boolean done, int color) {
        if (done) {
            iv.setImageResource(R.drawable.ic_check_circle);
            iv.setColorFilter(color);
        } else {
            iv.setImageResource(R.drawable.bg_circle_outline_gray);
            iv.clearColorFilter();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    private String formatAmount(double amount) {
        if (amount >= 1000000) {
            return String.format(Locale.US, "%.1fM", amount / 1000000.0);
        } else if (amount >= 1000) {
            return String.format(Locale.US, "%.1fK", amount / 1000.0);
        } else {
            return String.valueOf((int) amount);
        }
    }

    private void loadSummaryStats() {
        viewModel.getDashboardSummary((success, summary, message) -> {
            if (success && summary != null && isVisible()) {
                getActivity().runOnUiThread(() -> {
                    // Summary logic already handled by calculation or summary response
                });
            }
        });
    }

    private void setupChart() {
        com.github.mikephil.charting.charts.BarChart chart = binding.loanGrowthChart;
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);

        com.github.mikephil.charting.components.XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setGranularity(1f);

        com.github.mikephil.charting.components.YAxis yAxis = chart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(Color.parseColor("#F1F5F9"));
        yAxis.setTextColor(Color.parseColor("#94A3B8"));
        yAxis.setAxisMinimum(0f);

        // Chart will be populated when loan data is observed
        viewModel.getLoanRequests().observe(getViewLifecycleOwner(), loans -> {
            if (loans == null || loans.isEmpty()) {
                chart.clear();
                return;
            }
            List<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
            int max = Math.min(loans.size(), 6);
            for (int i = 0; i < max; i++) {
                entries.add(new com.github.mikephil.charting.data.BarEntry(i,
                        (float) (loans.get(i).getAmount() / 1000.0))); // show in K
            }
            com.github.mikephil.charting.data.BarDataSet dataSet =
                    new com.github.mikephil.charting.data.BarDataSet(entries, "Loans");
            int[] colors = new int[entries.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = Color.parseColor(i % 2 == 0 ? "#2563EB" : "#FF8A00");
            }
            dataSet.setColors(colors);
            dataSet.setDrawValues(false);
            com.github.mikephil.charting.data.BarData data =
                    new com.github.mikephil.charting.data.BarData(dataSet);
            data.setBarWidth(0.5f);
            chart.setData(data);
            chart.animateY(1000);
            chart.invalidate();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
