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
import com.example.save.data.models.Member;
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
    private double configContributionAmount = 0;
    private double configRetentionPct = 0;

    private com.example.save.ui.adapters.DashboardMemberAdapter memberAdapter;

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
        
        // Ensure members are synced
        viewModel.syncMembers();
        viewModel.getDepositEvent().observe(getViewLifecycleOwner(), v -> {
            if (binding != null) {
                viewModel.syncMembers();
                // Refresh balance, progress, and transaction list
                loadDashboardData();
                // Also reload recent transactions
                allCachedTransactions.clear();
                loadRecentTransactions();
            }
        });
    }

    private void initializeViews() {
        // Initialize RecyclerViews
        recipientAdapter = new RecipientSmallAdapter();
        binding.rvMonthRecipients.setAdapter(recipientAdapter);
        binding.rvMonthRecipients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(),
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

        memberAdapter = new com.example.save.ui.adapters.DashboardMemberAdapter(requireContext());
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
        List<Transaction> filteredList = new ArrayList<>();
        for (Transaction t : allCachedTransactions) {
            if (t.isCredit()) filteredList.add(t); // My Contributions = deposits only
        }

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


        binding.btnThemeToggle.setOnClickListener(v -> {
            applyClickAnimation(v);
            com.example.save.utils.ThemeUtils.toggleTheme(requireContext(), "member");
        });
    }

    @SuppressLint("SetTextI18n")
    private void loadDashboardData() {
        SessionManager session = SessionManager.getInstance(requireContext().getApplicationContext());
        String phone = session.getUserPhone();

        viewModel.fetchSystemConfig((success, config, message) -> {
            if (success && config != null) {
                configContributionAmount = config.getContributionAmount();
                configRetentionPct = config.getRetentionPercentage();
            }
        });

        viewModel.getDashboardSummary((success, summaryObj, message) -> {
            if (success && isAdded() && summaryObj instanceof com.example.save.data.models.DashboardSummaryResponse) {
                com.example.save.data.models.DashboardSummaryResponse summary = (com.example.save.data.models.DashboardSummaryResponse) summaryObj;
                requireActivity().runOnUiThread(() -> {
                    String formatted = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG")).format(summary.getTotalBalance());
                    binding.tvGroupBalance.setText(formatted.replace("UGX", "UGX "));
                    binding.tvCircleName.setText(summary.getGroupName());
                    binding.tvPooledFunds.setText(formatted.replace("UGX", "UGX "));

                    android.content.SharedPreferences adminPrefs = requireContext().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
                    java.util.List<com.example.save.data.models.Member> currentMembers = viewModel.getMembers().getValue();
                    int memberCount = currentMembers != null ? currentMembers.size() : 0;
                    double targetPayout = configContributionAmount > 0 && memberCount > 0
                            ? configContributionAmount * memberCount * (1.0 - configRetentionPct / 100.0)
                            : 0;

                    int payoutPercent = targetPayout > 0 ? (int) Math.min((summary.getTotalBalance() / targetPayout) * 100, 100) : 0;
                    binding.payoutProgress.setProgress(payoutPercent);
                    binding.tvPayoutProgressPercent.setText(payoutPercent + "%");

                    java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG"));
                    String payoutFormatted = nf.format(targetPayout).replace("UGX", "UGX ");
                    binding.tvPayoutAmount.setText(payoutFormatted);

                    String nextPayoutDateStr = adminPrefs.getString("rule_next_payout_date", "TBD");
                    binding.tvPayoutDate.setText(nextPayoutDateStr);
                });
            }
        });

        if (phone != null && !phone.isEmpty()) {
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    binding.tvGreetingName.setText("Welcome back, " + member.getName());
                    String joinDate = member.getJoinedDate() != null ? member.getJoinedDate() : "2024";
                    binding.tvMemberStatus.setText("Member since " + joinDate);

                    java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG"));

                    // Personal balance on the balance card
                    double personalBalance = member.getContributionPaid();
                    binding.tvBalanceAmount.setText(nf.format(personalBalance).replace("UGX", "UGX "));

                    // Goal: use yearly target from system config (contribution × 12)
                    double mySavings = member.getContributionPaid();
                    double yearlyTarget = configContributionAmount > 0
                            ? configContributionAmount * 12
                            : member.getContributionTarget();
                    int progress = yearlyTarget > 0 ? (int) Math.min((mySavings / yearlyTarget) * 100, 100) : 0;
                    binding.goalProgress.setProgress(progress);
                    binding.tvGoalProgressPercentTop.setText(progress + "%");
                    binding.tvGoalProgressPercentCenter.setText(progress + "%");

                    String paidStr = nf.format(mySavings).replace("UGX", "UGX ");
                    String targetStr = nf.format(yearlyTarget).replace("UGX", "UGX ");
                    binding.tvGoalProgressTarget.setText(paidStr + " of " + targetStr);
                }
            });
        }

        updateExtraStats();
        loadRecentTransactions();
    }

    private String calculatePayoutDate(int position, String basePayoutDate, android.content.Context context) {
        if (basePayoutDate == null || basePayoutDate.isEmpty() || basePayoutDate.contains("Not")
                || basePayoutDate.equals("TBD")) {
            return "TBD";
        }
        
        android.content.SharedPreferences prefs = context.getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
        String frequency = prefs.getString("rule_frequency", "Monthly");
        String recipientsStr = prefs.getString("rule_edit_recipients", "1 Member").replaceAll("[^0-9]", "");
        int recipients = 1;
        try {
            recipients = Integer.parseInt(recipientsStr);
            if (recipients < 1) recipients = 1;
        } catch (NumberFormatException e) {
            recipients = 1;
        }

        int cyclesToAdd = position / recipients;

        if (cyclesToAdd == 0) return basePayoutDate;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(basePayoutDate);
            if (date == null) return basePayoutDate;

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);

            for (int i = 0; i < cyclesToAdd; i++) {
                switch (frequency) {
                    case "Daily": cal.add(java.util.Calendar.DAY_OF_YEAR, 1); break;
                    case "Weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); break;
                    case "Bi-weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 2); break;
                    case "Monthly": cal.add(java.util.Calendar.MONTH, 1); break;
                    case "Every 2 Months": cal.add(java.util.Calendar.MONTH, 2); break;
                    case "Every 3 Months": cal.add(java.util.Calendar.MONTH, 3); break;
                    case "Every 4 Months": cal.add(java.util.Calendar.MONTH, 4); break;
                    case "Every 5 Months": cal.add(java.util.Calendar.MONTH, 5); break;
                    case "Every 6 Months": cal.add(java.util.Calendar.MONTH, 6); break;
                }
            }

            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return basePayoutDate;
        }
    }

    private String calculateRemainingDays(String targetDateStr) {
        if (targetDateStr == null || targetDateStr.equals("TBD") || targetDateStr.contains("Not")) {
            return "-- Days";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
            java.util.Date targetDate = sdf.parse(targetDateStr);
            if (targetDate != null) {
                long diff = targetDate.getTime() - System.currentTimeMillis();
                long days = (diff + (1000 * 60 * 60 * 24) - 1) / (1000 * 60 * 60 * 24);
                if (days < 0) return "0 Days";
                return days + " Days";
            }
        } catch (Exception e) {
            // ignore
        }
        return "14 Days";
    }

    private void updateExtraStats() {
        if (getContext() == null) return;
        
        int slots = requireContext().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE).getInt("slots_per_round", 5);

        android.content.SharedPreferences adminPrefs = requireContext().getSharedPreferences("ChamaPrefs", Context.MODE_PRIVATE);
        String schedPayoutDate = adminPrefs.getString("rule_next_payout_date", "TBD");

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

        String phone = SessionManager.getInstance(requireContext()).getUserPhone();
        
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberAdapter.setMembers(members, phone);

                // --- BIND QUEUE AND POSITION STATS DYNAMICALLY ---
                List<Member> sortedMembers = new ArrayList<>(members);
                // Sort by credit score descending
                java.util.Collections.sort(sortedMembers, (m1, m2) -> Integer.compare(m2.getCreditScore(), m1.getCreditScore()));

                int myIndex = -1;
                for (int i = 0; i < sortedMembers.size(); i++) {
                    if (sortedMembers.get(i).getPhone().equals(phone)) {
                        myIndex = i;
                        break;
                    }
                }
                
                if (myIndex != -1) {
                    Member me = sortedMembers.get(myIndex);
                    
                    binding.tvUserPosition.setText("You are #" + (myIndex + 1) + " of " + sortedMembers.size());
                    
                    int roundsCompleted = 0;
                    for (Member m : sortedMembers) {
                        if (m.hasReceivedPayout()) {
                            roundsCompleted++;
                        }
                    }
                    int currentRound = Math.min(roundsCompleted + 1, sortedMembers.size());
                    binding.tvRoundPosition.setText("Round " + currentRound + " of " + sortedMembers.size());
                    
                    String freq = adminPrefs.getString("rule_frequency", "Monthly");
                    binding.tvCircleSubName.setText(sortedMembers.size() + " members · " + freq + " · " + sortedMembers.size() + " rounds");
                    
                    // Last Recipient
                    Member lastRecipient = null;
                    for (int i = sortedMembers.size() - 1; i >= 0; i--) {
                        Member m = sortedMembers.get(i);
                        if (m.hasReceivedPayout()) {
                            lastRecipient = m;
                            break;
                        }
                    }
                    if (lastRecipient != null) {
                        binding.tvLastRecipientName.setText(lastRecipient.getName());
                        binding.tvLastRecipientDate.setText(lastRecipient.getPayoutDate() != null ? lastRecipient.getPayoutDate() : "Completed");
                    } else {
                        binding.tvLastRecipientName.setText("None");
                        binding.tvLastRecipientDate.setText("No payouts yet");
                    }
                    
                    // Next Recipient
                    Member nextRecipient = null;
                    for (Member m : sortedMembers) {
                        if (!m.hasReceivedPayout()) {
                            nextRecipient = m;
                            break;
                        }
                    }
                    if (nextRecipient != null) {
                        String name = nextRecipient.getPhone().equals(phone) ? "You (" + nextRecipient.getName() + ")" : nextRecipient.getName();
                        binding.tvNextRecipientName.setText(name);
                        
                        int nextRecipIdx = sortedMembers.indexOf(nextRecipient);
                        String nextDate = calculatePayoutDate(nextRecipIdx, schedPayoutDate, requireContext());
                        binding.tvNextRecipientDate.setText(nextDate);
                    } else {
                        binding.tvNextRecipientName.setText("None");
                        binding.tvNextRecipientDate.setText("All paid");
                    }
                    
                    // YOUR PAYOUT card
                    binding.tvYourPayoutRecipientName.setText(me.getName());
                    
                    if (me.hasReceivedPayout()) {
                        binding.tvYourPayoutRecipientDate.setText("Paid on: " + me.getPayoutDate());
                        binding.tvYourPayoutAmount.setText("Paid");
                    } else {
                        String myDate = calculatePayoutDate(myIndex, schedPayoutDate, requireContext());
                        binding.tvYourPayoutRecipientDate.setText("Receiving: " + myDate);

                        double myPayout = configContributionAmount > 0 && !sortedMembers.isEmpty()
                                ? configContributionAmount * sortedMembers.size() * (1.0 - configRetentionPct / 100.0)
                                : 0;
                        String myPayoutStr = myPayout > 0
                                ? "UGX " + java.text.NumberFormat.getIntegerInstance().format((long) myPayout)
                                : "TBD";
                        binding.tvYourPayoutAmount.setText(myPayoutStr);
                    }
                    
                    if (nextRecipient != null && nextRecipient.getPhone().equals(phone)) {
                        binding.tvYourTurnBadge.setVisibility(View.VISIBLE);
                        binding.tvYourTurnBadge.setText("Your turn next");
                    } else if (me.hasReceivedPayout()) {
                        binding.tvYourTurnBadge.setVisibility(View.VISIBLE);
                        binding.tvYourTurnBadge.setText("Received Payout");
                    } else {
                        binding.tvYourTurnBadge.setVisibility(View.GONE);
                    }
                }
            }
        });

        // Set remaining settings values
        String contributionAmount = adminPrefs.getString("rule_edit_contribution_amount", "UGX 0");
        String latePenalty = adminPrefs.getString("rule_edit_late_fee", "UGX 0") + " / day";
        java.util.List<com.example.save.data.models.Member> settingsMembers = viewModel.getMembers().getValue();
        int settingsMemberCount = settingsMembers != null ? settingsMembers.size() : 0;
        double totalPayout = configContributionAmount > 0 && settingsMemberCount > 0
                ? configContributionAmount * settingsMemberCount * (1.0 - configRetentionPct / 100.0)
                : 0;
        String totalPayoutStr = totalPayout > 0
                ? "UGX " + java.text.NumberFormat.getIntegerInstance().format((long) totalPayout)
                : adminPrefs.getString("rule_edit_contribution_amount", "UGX 0");

        binding.tvContributionPerMember.setText(contributionAmount);
        binding.tvTotalPayoutPerRound.setText(totalPayoutStr);
        binding.tvLatePenalty.setText(latePenalty);
        binding.tvRemainingDays.setText(calculateRemainingDays(schedPayoutDate));
    }

    private void loadRecentTransactions() {
        String phone = SessionManager.getInstance(requireContext()).getUserPhone();
        if (phone != null && !phone.isEmpty()) {
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    viewModel.getMemberTransactionsWithApproval(member.getName()).observe(getViewLifecycleOwner(), transactionItems -> {
                        if (transactionItems != null) {
                            allCachedTransactions.clear();
                            for (com.example.save.data.models.TransactionWithApproval item : transactionItems) {
                                com.example.save.data.models.TransactionEntity entity = item.transaction;
                                int iconRes = entity.isPositive() ? R.drawable.ic_money : R.drawable.ic_loan;
                                int color = entity.isPositive() ? 0xFF4CAF50 : 0xFFF44336;
                                String description = entity.getDescription();
                                if ("PENDING_APPROVAL".equals(entity.getStatus()) || "PENDING".equals(entity.getStatus())) {
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
        // Refresh entire dashboard when returning to the fragment.
        // This updates balance, progress, and transaction list.
        loadDashboardData();
        // Also reload recent transactions to reflect new deposits.
        allCachedTransactions.clear();
        loadRecentTransactions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
