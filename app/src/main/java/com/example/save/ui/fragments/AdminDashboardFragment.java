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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.save.data.models.LoanEntity;
import com.example.save.data.models.Member;
import com.example.save.data.models.MemberEntity;
import com.example.save.data.models.PaginatedResponse;
import com.example.save.data.models.PayoutEntity;
import com.example.save.data.models.TransactionEntity;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.LinkedHashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private MembersViewModel viewModel;
    private NotificationsViewModel notificationsViewModel;
    private String adminNameStr;
    private double totalBalanceValue = 0.0;
    private boolean isAdmin = true;
    private com.example.save.ui.adapters.DashboardMemberAdapter memberAdapter;
    private com.example.save.ui.adapters.PayoutQueueAdapter payoutAdapter;

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
        loadActiveLoans();
        loadPayoutSections();
        updateScheduleUI();
        setupEntranceAnimations();
        observeViewModel();
        viewModel.syncMembers();

        // Initialize adapters
        memberAdapter = new com.example.save.ui.adapters.DashboardMemberAdapter(requireContext());
        binding.rvDashboardMembers.setAdapter(memberAdapter);

        payoutAdapter = new com.example.save.ui.adapters.PayoutQueueAdapter(new ArrayList<>());
        binding.rvDashboardPayouts.setAdapter(payoutAdapter);

        // Hide admin-only actions if member
        if (!isAdmin) {
            binding.btnEditSchedule.setVisibility(View.GONE);
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

        binding.btnViewAllMembers.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new MembersFragment(), true);
            }
        });

        binding.btnViewQueue.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new QueueFragment(), true);
            }
        });
        binding.btnThemeToggle.setOnClickListener(v -> {
            applyClickAnimation(v);
            com.example.save.utils.ThemeUtils.toggleTheme(requireContext(), "admin");
        });
        binding.sectionSavingsTargets.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(SavingsTargetsFragment.newInstance(), true);
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(SavingsTargetsFragment.newInstance());
            }
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

        // EDIT SCHEDULE
        binding.btnEditSchedule.setOnClickListener(v -> showEditScheduleDialog());
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
        
        // Show sections but ensure they use 'Empty State' views instead of GONE
        binding.sectionSavingsTargets.setVisibility(View.VISIBLE);
        binding.sectionActiveLoans.setVisibility(View.VISIBLE);
        binding.sectionRecentPayouts.setVisibility(View.VISIBLE);
        binding.sectionUpcomingPayouts.setVisibility(View.VISIBLE);

        binding.pbTarget1.setProgress(0);
        binding.pbTarget2.setProgress(0);
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                loadDashboardData();
                String currentUserPhone = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserPhone();
                if (memberAdapter != null) memberAdapter.setMembers(members, currentUserPhone);
            }
        });

        notificationsViewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            binding.notificationBadgeDot.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void loadPayoutSections() {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));

        // Fetch config + total contributors to compute real payout amount
        // Uses getDashboardSummary for total_contributors (admin + members) and
        // getSystemConfig for retention_percentage (not in summary)
        api.getSystemConfig().enqueue(new Callback<com.example.save.data.models.SystemConfig>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.SystemConfig> call,
                    Response<com.example.save.data.models.SystemConfig> response) {
                if (!isAdded() || binding == null || !response.isSuccessful() || response.body() == null) return;
                com.example.save.data.models.SystemConfig cfg = response.body();
                api.getDashboardSummary().enqueue(new Callback<com.example.save.data.models.DashboardSummaryResponse>() {
                    @Override
                    public void onResponse(Call<com.example.save.data.models.DashboardSummaryResponse> call2,
                            Response<com.example.save.data.models.DashboardSummaryResponse> r2) {
                        int contributors = (r2.isSuccessful() && r2.body() != null)
                                ? r2.body().getTotalContributors() : 1;
                        double monthly = cfg.getContributionAmount() * contributors;
                        double payoutAmt = monthly * (1.0 - cfg.getRetentionPercentage() / 100.0);
                        if (payoutAdapter != null) payoutAdapter.setPayoutAmount(payoutAmt);
                    }
                    @Override public void onFailure(Call<com.example.save.data.models.DashboardSummaryResponse> call2, Throwable t) {}
                });
            }
            @Override public void onFailure(Call<com.example.save.data.models.SystemConfig> call, Throwable t) {}
        });

        // ---- Upcoming payouts: payout queue (members who haven't received payout yet) ----
        api.getPayoutQueue().enqueue(new Callback<List<MemberEntity>>() {
            @Override
            public void onResponse(Call<List<MemberEntity>> call, Response<List<MemberEntity>> response) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        List<MemberEntity> queue = response.body();
                        // Convert MemberEntity list to Member list for the adapter
                        List<Member> memberList = new ArrayList<>();
                        for (MemberEntity e : queue) {
                            Member m = new Member(e.getName(), "member", e.isActive(), e.getPhone());
                            m.setHasReceivedPayout(e.isHasReceivedPayout());
                            memberList.add(m);
                        }
                        SharedPreferences qPrefs = requireActivity()
                                .getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE);
                        String payoutDate = qPrefs.getString("sched_payout_date", "");
                        if (payoutAdapter != null) payoutAdapter.updateList(memberList, payoutDate);
                        binding.tvNoUpcomingPayouts.setVisibility(memberList.isEmpty() ? View.VISIBLE : View.GONE);
                        binding.rvDashboardPayouts.setVisibility(memberList.isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        binding.tvNoUpcomingPayouts.setVisibility(View.VISIBLE);
                        binding.rvDashboardPayouts.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(Call<List<MemberEntity>> call, Throwable t) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    binding.tvNoUpcomingPayouts.setVisibility(View.VISIBLE);
                    binding.rvDashboardPayouts.setVisibility(View.GONE);
                });
            }
        });

        // ---- Recent payouts ----
        api.getPayouts(5, 0).enqueue(new Callback<PaginatedResponse<PayoutEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<PayoutEntity>> call,
                    Response<PaginatedResponse<PayoutEntity>> response) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getData() != null
                            && !response.body().getData().isEmpty()) {
                        List<PayoutEntity> payouts = response.body().getData();
                        StringBuilder sb = new StringBuilder();
                        for (PayoutEntity p : payouts) {
                            String name = p.getMemberName() != null ? p.getMemberName() : "—";
                            String amount = fmt.format(p.getNetAmount()).replace("UGX", "UGX ");
                            String date = p.getExecutedAt() != null && p.getExecutedAt().length() >= 10
                                    ? p.getExecutedAt().substring(0, 10) : "—";
                            sb.append("• ").append(name).append("  ").append(amount)
                              .append("  ").append(date).append("\n");
                        }
                        binding.tvNoRecentPayouts.setText(sb.toString().trim());
                        binding.tvNoRecentPayouts.setTextColor(
                                androidx.core.content.ContextCompat.getColor(requireContext(),
                                        com.example.save.R.color.v_text_dark));
                    } else {
                        binding.tvNoRecentPayouts.setText("No recent payouts");
                        binding.tvNoRecentPayouts.setTextColor(
                                androidx.core.content.ContextCompat.getColor(requireContext(),
                                        com.example.save.R.color.v_text_muted));
                    }
                });
            }

            @Override
            public void onFailure(Call<PaginatedResponse<PayoutEntity>> call, Throwable t) {}
        });
    }

    private void loadAdminData() {
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
        String nameFromSession = session.getUserName();
        
        if (nameFromSession != null && !nameFromSession.isEmpty()) {
            adminNameStr = nameFromSession.split(" ")[0];
        } else {
            adminNameStr = isAdmin ? "Admin" : "Member";
        }

        // Always try to get the latest real name from ViewModel
        String phone = session.getUserPhone();
        if (phone != null) {
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null && member.getName() != null && !member.getName().isEmpty()) {
                    adminNameStr = member.getName().split(" ")[0];
                    updateGreeting();
                }
            });
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
                        // Monthly contribution = per-member amount set by admin (same for everyone)
                        binding.monthlyContrib.setText(ugFormat.format(summary.getContributionAmount()).replace("UGX", "UGX "));
                        // Paid so far = admin's own contribution_paid for this cycle
                        binding.interestEarned.setText(ugFormat.format(summary.getPersonalSavings()).replace("UGX", "UGX "));

                        double expectedMonthly = summary.getContributionAmount() * summary.getTotalContributors();
                        double expectedYearly = expectedMonthly * 12;

                        int prog1 = expectedMonthly > 0
                                ? (int) Math.min(100, (summary.getMonthlyContributions() / expectedMonthly) * 100)
                                : 0;
                        int prog2 = expectedYearly > 0
                                ? (int) Math.min(100, (summary.getYearlyContributions() / expectedYearly) * 100)
                                : 0;
                        binding.pbTarget1.setProgress(prog1);
                        binding.pbTarget2.setProgress(prog2);

                        String t1 = ugFormat.format(summary.getMonthlyContributions()).replace("UGX", "UGX ")
                                + " / " + ugFormat.format(expectedMonthly).replace("UGX", "UGX ");
                        String t2 = ugFormat.format(summary.getYearlyContributions()).replace("UGX", "UGX ")
                                + " / " + ugFormat.format(expectedYearly).replace("UGX", "UGX ");
                        binding.tvTarget1Amount.setText(t1);
                        binding.tvTarget2Amount.setText(t2);
                    });
                }
            });
        } else {
            // LOAD MEMBER SPECIFIC DATA
            binding.tvBalanceLabel.setText("My Total Savings");
            String phone = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserPhone();
            if (phone != null) {
                viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
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
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F1F5F9"));
        leftAxis.setTextColor(Color.parseColor("#94A3B8"));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);
        chart.getAxisRight().setEnabled(false);

        // Build last-7-months buckets (key = "yyyy-MM")
        LinkedHashMap<String, Float> monthMap = new LinkedHashMap<>();
        ArrayList<String> monthLabels = new ArrayList<>();
        SimpleDateFormat keyFmt = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat labelFmt = new SimpleDateFormat("MMM", Locale.getDefault());
        for (int i = 6; i >= 0; i--) {
            Calendar bucket = Calendar.getInstance();
            bucket.add(Calendar.MONTH, -i);
            monthMap.put(keyFmt.format(bucket.getTime()), 0f);
            monthLabels.add(labelFmt.format(bucket.getTime()));
        }

        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
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
                    if (monthMap.containsKey(key)) {
                        monthMap.put(key, monthMap.get(key) + (float) tx.getAmount());
                    }
                }
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                int idx = 0;
                for (float val : monthMap.values()) barEntries.add(new BarEntry(idx++, val));
                requireActivity().runOnUiThread(() -> {
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                    updateChartData(chart, barEntries);
                });
            }
            @Override
            public void onFailure(Call<PaginatedResponse<TransactionEntity>> call, Throwable t) {
                if (!isAdded() || binding == null) return;
                ArrayList<BarEntry> empty = new ArrayList<>();
                for (int i = 0; i < 7; i++) empty.add(new BarEntry(i, 0f));
                requireActivity().runOnUiThread(() -> {
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(monthLabels));
                    updateChartData(chart, empty);
                });
            }
        });
    }

    private void loadActiveLoans() {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.getLoans(100, 0).enqueue(new Callback<PaginatedResponse<LoanEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<LoanEntity>> call,
                    Response<PaginatedResponse<LoanEntity>> response) {
                if (!isAdded() || binding == null || !response.isSuccessful() || response.body() == null) return;
                List<LoanEntity> allLoans = response.body().getData();
                List<LoanEntity> activeLoans = new ArrayList<>();
                if (allLoans != null) {
                    for (LoanEntity loan : allLoans) {
                        String st = loan.getStatus();
                        if ("ACTIVE".equalsIgnoreCase(st) || "APPROVED".equalsIgnoreCase(st)) {
                            activeLoans.add(loan);
                        }
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    binding.viewAllLoans.setText(activeLoans.size() + " Total");
                    if (activeLoans.isEmpty()) {
                        binding.containerActiveLoans.setVisibility(View.GONE);
                        binding.tvNoLoans.setVisibility(View.VISIBLE);
                        return;
                    }
                    binding.tvNoLoans.setVisibility(View.GONE);
                    binding.containerActiveLoans.setVisibility(View.VISIBLE);
                    binding.containerActiveLoans.removeAllViews();
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
                    SimpleDateFormat dateFmt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                    int limit = Math.min(3, activeLoans.size());
                    for (int i = 0; i < limit; i++) {
                        LoanEntity loan = activeLoans.get(i);
                        View row = LayoutInflater.from(requireContext()).inflate(
                                R.layout.item_loan_active, binding.containerActiveLoans, false);
                        String name = loan.getMemberName() != null ? loan.getMemberName() : "—";
                        ((android.widget.TextView) row.findViewById(R.id.tvMemberName)).setText(name);
                        double totalDue = loan.getAmount() + loan.getInterest();
                        ((android.widget.TextView) row.findViewById(R.id.tvTotalDue))
                                .setText(fmt.format(totalDue).replace("USh", "UGX"));
                        String dueStr = loan.getDueDate() != null ? dateFmt.format(loan.getDueDate()) : "—";
                        ((android.widget.TextView) row.findViewById(R.id.tvDueDate)).setText("Due: " + dueStr);
                        double repaid = loan.getRepaidAmount();
                        int progress = totalDue > 0 ? (int) Math.min(100, (repaid / totalDue) * 100) : 0;
                        ((com.google.android.material.progressindicator.LinearProgressIndicator)
                                row.findViewById(R.id.progressBarRepayment)).setProgress(progress);
                        String paidLabel = fmt.format(repaid).replace("USh", "UGX") + " (" + progress + "%)";
                        ((android.widget.TextView) row.findViewById(R.id.tvRepaidAmount)).setText("Paid: " + paidLabel);
                        binding.containerActiveLoans.addView(row);
                    }
                });
            }
            @Override public void onFailure(Call<PaginatedResponse<LoanEntity>> call, Throwable t) {}
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
