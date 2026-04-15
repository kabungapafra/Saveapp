package com.example.save.ui.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.example.save.data.models.PaymentItem;
import com.example.save.data.models.Transaction;
import com.example.save.databinding.FragmentDashboardBinding;
import com.example.save.ui.activities.MemberMainActivity;
import com.example.save.ui.adapters.RecipientSmallAdapter;
import com.example.save.ui.adapters.TransactionAdapter;
import com.example.save.ui.adapters.UpcomingPaymentAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private MembersViewModel viewModel;
    private RecipientSmallAdapter recipientAdapter;
    private UpcomingPaymentAdapter upcomingPaymentAdapter;
    private TransactionAdapter transactionAdapter;

    private String currentFilterType = "All";
    private List<Transaction> allCachedTransactions = new ArrayList<>();

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

        upcomingPaymentAdapter = new UpcomingPaymentAdapter(new ArrayList<>(), item -> {
            ((MemberMainActivity) requireActivity()).loadFragment(new MakeContributionFragment());
        });
        binding.rvUpcomingPayments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        binding.rvUpcomingPayments.setAdapter(upcomingPaymentAdapter);

        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        binding.rvRecentTransactions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.rvRecentTransactions.setAdapter(transactionAdapter);

        binding.btnViewAllMembers.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).showMembersSection();
        });
    }

    private void setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipContributions) {
                currentFilterType = "Contributions";
            } else if (checkedId == R.id.chipLoans) {
                currentFilterType = "Loans";
            } else if (checkedId == R.id.chipPayouts) {
                currentFilterType = "Payouts";
            } else {
                currentFilterType = "All";
            }
            applyFilters();
        });

        binding.btnDateFilter.setOnClickListener(v -> {
            com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                    .dateRangePicker()
                    .setTitleText("Select Date Range")
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                binding.btnDateFilter.setText("Range Selected");
                // In a real app, you'd filter by date here
                applyFilters();
            });

            picker.show(getParentFragmentManager(), "date_filter_dashboard");
        });
    }

    private void applyFilters() {
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : allCachedTransactions) {
            boolean matchesType = true;
            if (currentFilterType.equals("Contributions") && !t.getType().toLowerCase().contains("contribution"))
                matchesType = false;
            if (currentFilterType.equals("Loans") && !t.getType().toLowerCase().contains("loan"))
                matchesType = false;
            if (currentFilterType.equals("Payouts") && !t.getType().toLowerCase().contains("payout"))
                matchesType = false;

            if (matchesType) filteredList.add(t);
        }

        if (filteredList.isEmpty()) {
            binding.rvRecentTransactions.setVisibility(View.GONE);
        } else {
            binding.rvRecentTransactions.setVisibility(View.VISIBLE);
            transactionAdapter.updateTransactions(filteredList);
        }
    }

    private void setupHeaderInteractions() {
        binding.greetingName.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).loadFragment(new SettingsFragment());
        });

        binding.notificationIcon.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).showNotifications();
        });

        binding.actionPay.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).loadFragment(new MakeContributionFragment());
        });

        binding.actionLoan.setOnClickListener(v -> {
            applyClickAnimation(v);
            String email = SessionManager.getInstance(requireContext()).getUserEmail();
            ((MemberMainActivity) requireActivity()).loadFragment(LoansFragment.newInstance(email != null ? email : "email@example.com"));
        });

        binding.actionQueue.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).loadFragment(QueueFragment.newInstance());
        });

        binding.actionProfile.setOnClickListener(v -> {
            applyClickAnimation(v);
            ((MemberMainActivity) requireActivity()).loadFragment(new SettingsFragment());
        });
        
        binding.myTakeCard.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Viewing Payout History", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadDashboardData() {
        SessionManager session = SessionManager.getInstance(requireContext().getApplicationContext());
        String email = session.getUserEmail();

        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    String firstName = member.getName().split(" ")[0];
                    binding.greetingName.setText(firstName + "!");

                    Context context = requireContext();
                    android.content.SharedPreferences adminPrefs = context.getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE);
                    String schedContribDate = adminPrefs.getString("sched_contrib_date", member.getNextPaymentDueDate());
                    String schedPayoutDate = adminPrefs.getString("sched_payout_date", member.getNextPayoutDate());

                    binding.tvMyTakePayoutDate.setText(schedPayoutDate);
                    binding.tvMyTakeDueDate.setText(schedContribDate);
                }
            });
        }

        viewModel.getGroupBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                String formatted = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG")).format(balance);
                binding.txtBalance.setText(formatted);
            }
        });

        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    double mySavings = member.getContributionPaid();
                    String formatted = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG")).format(mySavings);
                    binding.savingsBalance.setText(formatted);

                    double target = member.getContributionTarget();
                    int progress = target > 0 ? (int) ((mySavings / target) * 100) : 0;
                    binding.metricsProgress.setProgress(progress);
                }
            });
        }

        updateExtraStats();
        loadRecentTransactions();
    }

    private void updateExtraStats() {
        if (getContext() == null) return;
        
        int slots = requireContext().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE).getInt("slots_per_round", 5);
        binding.tvRecipientsLabel.setText(slots > 1 ? "Current Batch Recipients" : "Next Recipient");

        android.content.SharedPreferences adminPrefs = requireContext().getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE);
        String schedPayoutDate = adminPrefs.getString("sched_payout_date", "TBD");

        viewModel.getMonthlyRecipientsLive(slots).observe(getViewLifecycleOwner(), recipients -> {
            if (recipients != null) recipientAdapter.updateList(recipients, schedPayoutDate);
        });

        viewModel.getPendingPaymentsCountLive().observe(getViewLifecycleOwner(), pendingCount -> {
            if (pendingCount != null) {
                binding.tvPendingCount.setText(String.valueOf(pendingCount));
                if (pendingCount > 0) {
                    binding.notificationBadge.setText(String.valueOf(pendingCount));
                    binding.notificationBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.notificationBadge.setVisibility(View.GONE);
                }
            }
        });

        String email = SessionManager.getInstance(requireContext()).getUserEmail();
        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), currentMember -> {
                if (currentMember != null) {
                    binding.tvPaymentStreak.setText(String.valueOf(currentMember.getPaymentStreak()));
                    binding.tvCreditScore.setText(String.valueOf(currentMember.getCreditScore()));
                    updateUpcomingPayments(currentMember);
                }
            });
        }
    }

    private void updateUpcomingPayments(com.example.save.data.models.Member member) {
        List<PaymentItem> payments = new ArrayList<>();
        if (member.getContributionPaid() < member.getContributionTarget()) {
            double remaining = member.getContributionTarget() - member.getContributionPaid();
            String amountStr = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG")).format(remaining);
            String schedContribDate = requireContext().getSharedPreferences("SaveAppPrefs", Context.MODE_PRIVATE).getString("sched_contrib_date", "Active Cycle");
            payments.add(new PaymentItem("Monthly Contribution", amountStr + " remaining", "Due: " + schedContribDate, "Contribution"));
        }
        if (payments.isEmpty()) payments.add(new PaymentItem("All Paid Up", "You are current", "Relax", "Status"));
        upcomingPaymentAdapter.updateList(payments);
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
        // Restore nav bar directly when returning to dashboard
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
