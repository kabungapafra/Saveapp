package com.example.save.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.save.data.models.Member;
import com.example.save.data.models.MemberEntity;
import com.example.save.data.models.PaginatedResponse;
import com.example.save.data.models.TransactionEntity;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.adapters.DashboardTxAdapter;
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
    private DashboardTxAdapter transactionAdapter;

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

        transactionAdapter = new DashboardTxAdapter();
        binding.rvRecentTransactions.setAdapter(transactionAdapter);

        // Restore cached data instantly — avoids any blank flash when returning to this screen
        viewModel.getDashboardCache().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null && isAdded() && binding != null) {
                applyDashboardSummary(summary);
            }
        });
        viewModel.getPayoutQueueCache().observe(getViewLifecycleOwner(), members -> {
            if (members != null && isAdded() && binding != null) {
                String payoutDate = requireActivity()
                        .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                        .getString("rule_next_payout_date", "");
                applyUpcomingPayout(members, payoutDate);
            }
        });
        viewModel.getRecentTransactionsCache().observe(getViewLifecycleOwner(), txList -> {
            if (txList != null && isAdded() && binding != null) {
                transactionAdapter.setItems(txList);
                binding.tvNoRecentTransactions.setVisibility(txList.isEmpty() ? View.VISIBLE : View.GONE);
                binding.rvRecentTransactions.setVisibility(txList.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        // Logo: natural colours in light mode, white in dark mode
        boolean isNight = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        if (isNight) {
            binding.imgLogo.setImageTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        } else {
            binding.imgLogo.setImageTintList(null);
        }

        loadAdminData();
        setupListeners();
        loadDashboardData();
        loadPayoutSections();
        loadRecentTransactions();
        updateScheduleUI();
        setupEntranceAnimations();
        observeViewModel();
        viewModel.syncMembers();

        // (no admin-only visibility overrides needed here)
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

        binding.btnViewQueue.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new QueueFragment(), true);
            }
        });

        binding.btnThemeToggle.setOnClickListener(v -> {
            applyClickAnimation(v);
            String role = (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) ? "member" : "admin";
            com.example.save.utils.ThemeUtils.toggleTheme(requireContext(), role);
        });

        binding.sectionSavingsTargets.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(SavingsTargetsFragment.newInstance(), true);
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(SavingsTargetsFragment.newInstance());
            }
        });

        binding.btnQuickContribute.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new MakeContributionFragment(), true);
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(new MakeContributionFragment());
            }
        });

        binding.btnQuickRequestLoan.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new LoanApplicationFragment(), true);
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(new LoanApplicationFragment());
            }
        });

        binding.btnQuickRepayLoan.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new LoanPaymentFragment(), true);
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(new LoanPaymentFragment());
            }
        });

        binding.btnQuickMembers.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(new MembersFragment(), true);
            }
        });

        binding.btnSeeAllTransactions.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).showNotifications();
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).showNotifications();
            }
        });

    }



    private void updateScheduleUI() {
        binding.sectionSavingsTargets.setVisibility(View.VISIBLE);
        binding.sectionUpcomingPayouts.setVisibility(View.VISIBLE);
        binding.pbTarget1.setProgress(0);
        binding.pbTarget2.setProgress(0);
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

    private void loadPayoutSections() {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);

        api.getPayoutQueue().enqueue(new Callback<com.example.save.data.models.PayoutQueueResponse>() {
            @Override
            public void onResponse(Call<com.example.save.data.models.PayoutQueueResponse> call, Response<com.example.save.data.models.PayoutQueueResponse> response) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getQueue() != null) {
                        List<com.example.save.data.models.PayoutQueueEntry> queue = response.body().getQueue();
                        List<Member> memberList = new ArrayList<>();
                        for (com.example.save.data.models.PayoutQueueEntry e : queue) {
                            Member m = new Member(e.getMemberName(), "member", true, e.getMemberPhone());
                            m.setHasReceivedPayout(e.hasReceivedPayout());
                            if (e.getActualPayoutDate() != null) m.setPayoutDate(e.getActualPayoutDate());
                            memberList.add(m);
                        }
                        String payoutDate = requireActivity()
                                .getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE)
                                .getString("rule_next_payout_date", "");
                        applyUpcomingPayout(memberList, payoutDate);
                        viewModel.setPayoutQueueCache(memberList);
                    }
                });
            }
            @Override
            public void onFailure(Call<com.example.save.data.models.PayoutQueueResponse> call, Throwable t) {}
        });
    }

    private void applyUpcomingPayout(List<Member> members, String payoutDate) {
        if (!isAdded() || binding == null) return;
        binding.tvUpcomingPayoutDate.setText(
                (payoutDate != null && !payoutDate.isEmpty()) ? payoutDate : "--");
        Member next = null;
        for (Member m : members) {
            if (!m.hasReceivedPayout()) { next = m; break; }
        }
        binding.tvUpcomingPayoutReceiver.setText(next != null ? next.getName() : "--");
    }

    private void loadAdminData() {
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
        String nameFromSession = session.getUserName();
        
        if (nameFromSession != null && !nameFromSession.isEmpty()) {
            adminNameStr = nameFromSession;
        } else {
            adminNameStr = isAdmin ? "Admin" : "Member";
        }

        // Always try to get the latest real name from ViewModel
        String phone = session.getUserPhone();
        if (phone != null) {
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null && member.getName() != null && !member.getName().isEmpty()) {
                    adminNameStr = member.getName();
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
                    requireActivity().runOnUiThread(() -> applyDashboardSummary(summary));
                }
            });
        } else {
            binding.tvBalanceLabel.setText("My Total Savings");
            String phone = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserPhone();
            if (phone != null) {
                // Instant: apply cached member data right now if available
                com.example.save.data.models.Member cached = viewModel.getMemberByPhone(phone);
                if (cached != null) applyMemberData(cached);
                // Live: update when data arrives / changes
                viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                    if (member != null) applyMemberData(member);
                });
            }
            // Load group-level summary (next contribution date, payout amount, targets)
            viewModel.getDashboardSummary((success, summaryObj, message) -> {
                if (success && isAdded() && summaryObj instanceof com.example.save.data.models.DashboardSummaryResponse) {
                    com.example.save.data.models.DashboardSummaryResponse summary =
                            (com.example.save.data.models.DashboardSummaryResponse) summaryObj;
                    requireActivity().runOnUiThread(() -> applyDashboardSummary(summary));
                }
            });
        }
    }

    private void applyDashboardSummary(com.example.save.data.models.DashboardSummaryResponse summary) {
        if (!isAdded() || binding == null) return;
        // For members keep their personal balance (set by applyMemberData); only admin uses group total
        if (isAdmin) {
            totalBalanceValue = summary.getTotalBalance();
            updateBalanceDisplay();
            NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
            binding.monthlyContrib.setText(ugFormat.format(summary.getAvailableSavings()).replace("UGX", "UGX "));
            binding.interestEarned.setText(ugFormat.format(summary.getLoanBalance()).replace("UGX", "UGX "));
        }

        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));

        binding.pbTarget1.setProgress(summary.getMonthlyTargetProgress());
        binding.pbTarget2.setProgress(summary.getYearlyTargetProgress());
        binding.tvTarget1Amount.setText(ugFormat.format(summary.getMonthlyTarget()).replace("UGX", "UGX ").replace("USh", "UGX "));
        binding.tvTarget2Amount.setText(ugFormat.format(summary.getYearlyTarget()).replace("UGX", "UGX ").replace("USh", "UGX "));

        binding.tvNextContribDate.setText(summary.getNextContributionDate());

        java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "UG"));
        binding.tvUpcomingPayoutAmount.setText(fmt.format(summary.getUpcomingPayoutAmount()).replace("UGX", "UGX "));
    }

    private void applyMemberData(com.example.save.data.models.Member member) {
        if (!isAdded() || binding == null) return;
        totalBalanceValue = member.getContributionPaid();
        updateBalanceDisplay();
        NumberFormat ugFormat = NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
        binding.monthlyContrib.setText(ugFormat.format(member.getContributionTarget()).replace("UGX", "UGX "));
        binding.interestEarned.setText(ugFormat.format(member.getLoanBalance()).replace("UGX", "UGX "));
        binding.tvInterestLabel.setText("Active Loans");
        binding.tvMonthlyLabel.setText("Savings Target");
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

    private void loadRecentTransactions() {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.getTransactions(6, 0).enqueue(new Callback<PaginatedResponse<TransactionEntity>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<TransactionEntity>> call,
                    Response<PaginatedResponse<TransactionEntity>> response) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (!response.isSuccessful() || response.body() == null
                            || response.body().getData() == null
                            || response.body().getData().isEmpty()) {
                        binding.tvNoRecentTransactions.setVisibility(View.VISIBLE);
                        binding.rvRecentTransactions.setVisibility(View.GONE);
                        return;
                    }
                    List<TransactionEntity> txList = response.body().getData();
                    if (transactionAdapter != null) transactionAdapter.setItems(txList);
                    binding.tvNoRecentTransactions.setVisibility(View.GONE);
                    binding.rvRecentTransactions.setVisibility(View.VISIBLE);
                    viewModel.setRecentTransactionsCache(txList);
                });
            }
            @Override
            public void onFailure(Call<PaginatedResponse<TransactionEntity>> call, Throwable t) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    binding.tvNoRecentTransactions.setVisibility(View.VISIBLE);
                    binding.rvRecentTransactions.setVisibility(View.GONE);
                });
            }
        });
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
        if (getActivity() != null) {
            View navContainer = getActivity().findViewById(R.id.navContainer);
            if (navContainer != null) navContainer.setVisibility(View.VISIBLE);
            View navAction = getActivity().findViewById(R.id.navAction);
            if (navAction != null) navAction.setVisibility(View.VISIBLE);
        }
        // Refresh data every time this screen is shown
        viewModel.syncMembers();
        loadDashboardData();
        loadRecentTransactions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
