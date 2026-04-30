package com.example.save.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.Transaction;
import com.example.save.databinding.FragmentDashboardBinding;
import com.example.save.ui.activities.MemberMainActivity;
import com.example.save.ui.adapters.RecipientSmallAdapter;
import com.example.save.ui.adapters.TransactionAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private MembersViewModel viewModel;
    private RecipientSmallAdapter recipientAdapter;
    private TransactionAdapter transactionAdapter;

    private final List<Transaction> allCachedTransactions = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        initializeViews();
        setupFilters();
        setupHeaderInteractions();
        loadDashboardData();
    }

    private void initializeViews() {
        // Initialize RecyclerViews
        recipientAdapter = new RecipientSmallAdapter();
        binding.rvMonthRecipients.setAdapter(recipientAdapter);
        binding.rvMonthRecipients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

        com.example.save.ui.adapters.DashboardMemberAdapter memberAdapter = new com.example.save.ui.adapters.DashboardMemberAdapter(requireContext());
        binding.rvDashboardMembers.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvDashboardMembers.setAdapter(memberAdapter);

        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        binding.rvDashboardTransactions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvDashboardTransactions.setAdapter(transactionAdapter);

        binding.btnViewAllMembers.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).showMembersSection();
        });
    }

    private void setupFilters() {
        // Filter UI removed in high-fidelity redesign. 
        // Logic preserved for future optional restoration or deep-link filtering.
    }

    private void applyFilters() {
        List<Transaction> filteredList = new ArrayList<>(allCachedTransactions);

        if (filteredList.isEmpty()) {
            binding.rvDashboardTransactions.setVisibility(View.GONE);
        } else {
            binding.rvDashboardTransactions.setVisibility(View.VISIBLE);
            transactionAdapter.updateTransactions(filteredList);
        }
    }

    private void setupHeaderInteractions() {

        binding.btnNotifications.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).showNotifications();
        });

        binding.btnPayNowBottom.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).loadFragment(new MakeContributionFragment());
        });

        binding.btnThemeToggle.setOnClickListener(v -> {
            applyClickAnimation(v);
            com.example.save.utils.ThemeUtils.toggleTheme(requireContext(), "member");
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadDashboardData() {
        SessionManager session = SessionManager.getInstance(requireContext().getApplicationContext());
        String email = session.getUserEmail();

        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    binding.tvCircleName.setText("Holiday Fund 2024"); // Design specified static text for mockup parity
                }
            });
        }

        viewModel.getGroupBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                String formatted = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG")).format(balance);
                binding.tvBalanceAmount.setText(formatted);
            }
        });

        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    double mySavings = member.getContributionPaid();

                    double target = member.getContributionTarget();
                    int progress = target > 0 ? (int) ((mySavings / target) * 100) : 0;
                    binding.goalProgress.setProgress(progress);
                }
            });
        }

        updateExtraStats();
        loadRecentTransactions();
    }

    private void updateExtraStats() {
        if (getContext() == null) return;
        
        int slots = requireContext().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE).getInt("slots_per_round", 5);
        // Label removed in high-fidelity redesign as it's integrated into the Active Circle card

        android.content.SharedPreferences adminPrefs = requireContext().getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE);
        String schedPayoutDate = adminPrefs.getString("sched_payout_date", "TBD");

        viewModel.getMonthlyRecipientsLive(slots).observe(getViewLifecycleOwner(), recipients -> {
            if (recipients != null) recipientAdapter.updateList(recipients, schedPayoutDate);
        });

        viewModel.getPendingPaymentsCountLive().observe(getViewLifecycleOwner(), pendingCount -> {
            if (pendingCount != null) {
                if (pendingCount > 0) {
                    binding.notificationIndicator.setVisibility(View.VISIBLE);
                } else {
                    binding.notificationIndicator.setVisibility(View.GONE);
                }
            }
        });

        String email = SessionManager.getInstance(requireContext()).getUserEmail();
        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), currentMember -> {
                // Update additional stats if components are added to UI
            });
        }
    }

    private void loadRecentTransactions() {
        String email = SessionManager.getInstance(requireContext()).getUserEmail();
        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    viewModel.getMemberTransactionsWithApproval(member.getName()).observe(getViewLifecycleOwner(), transactionItems -> {
                        if (transactionItems != null) {
                            allCachedTransactions.clear();
                            for (com.example.save.data.models.TransactionWithApproval item : transactionItems) {
                                com.example.save.data.local.entities.TransactionEntity entity = item.transaction;
                                int iconRes = entity.isPositive() ? R.drawable.ic_money : R.drawable.ic_loan;
                                int color = entity.isPositive() ? 0xFF4CAF50 : 0xFFF44336;
                                String description = entity.getDescription();
                                if ("PENDING_APPROVAL".equals(entity.getStatus())) {
                                    description += " (Pending: " + item.approvalCount + " Approvals)";
                                    color = 0xFFFF9800;
                                }
                                allCachedTransactions.add(new Transaction(description, new java.text.SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(entity.getDate()), entity.getAmount(), entity.isPositive(), iconRes, color));
                            }
                            applyFilters();
                        }
                    });
                }
            });
        }
    }

    private void applyClickAnimation(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.anim_press));
    }

    @Override
    public void onResume() {
        super.onResume();
        // syncNavUI in Activity handles bottom nav visibility
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
