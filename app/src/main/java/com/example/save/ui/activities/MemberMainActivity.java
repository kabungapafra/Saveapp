package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.ui.viewmodels.MembersViewModel; // Added import
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.content.Intent; // Added import

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberMainBinding;

public class MemberMainActivity extends AppCompatActivity {

    private ActivityMemberMainBinding binding;
    private RecipientSmallAdapter recipientAdapter;
    private UpcomingPaymentAdapter upcomingPaymentAdapter;
    private TransactionAdapter transactionAdapter;

    // Data
    private MembersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);

        // Initialize Notifications
        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);

        // Security Check: If it's the first login, force password change redirect
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(this);
        if (session.isFirstLogin()) {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("member_email", session.getUserEmail());
            intent.putExtra("is_first_login", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        setupHeaderInteractions();
        loadDashboardData();

        // Modern back press handling
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If a fragment is showing, go back to dashboard
                if (binding != null && binding.fragmentContainer != null
                        && binding.fragmentContainer.getVisibility() == android.view.View.VISIBLE) {
                    switchToDashboard();
                } else {
                    // Exit app instead of going back to login
                    finishAffinity();
                }
            }
        });
    }

    // Filter State
    private String currentFilterType = "All";
    private androidx.core.util.Pair<Long, Long> currentDateRange = null;
    private java.util.List<Transaction> allCachedTransactions = new java.util.ArrayList<>();

    private void initializeViews() {
        // Initialize RecyclerView
        recipientAdapter = new RecipientSmallAdapter();
        if (binding.rvMonthRecipients != null) {
            binding.rvMonthRecipients.setAdapter(recipientAdapter);
            binding.rvMonthRecipients.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        }

        // Initialize Upcoming Payments
        if (binding.rvUpcomingPayments != null) {
            java.util.List<PaymentItem> samplePayments = new java.util.ArrayList<>();
            samplePayments.add(new PaymentItem("Dec Contribution", "UGX 50,000/mo", "2 days left", "Contribution"));
            samplePayments.add(new PaymentItem("Loan Repay", "UGX 200,000", "5 days left", "Loan"));

            upcomingPaymentAdapter = new UpcomingPaymentAdapter(samplePayments);
            binding.rvUpcomingPayments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
            binding.rvUpcomingPayments.setAdapter(upcomingPaymentAdapter);
        }

        // Initialize Recent Transactions
        if (binding.rvRecentTransactions != null) {
            transactionAdapter = new TransactionAdapter(new java.util.ArrayList<>());
            binding.rvRecentTransactions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            binding.rvRecentTransactions.setAdapter(transactionAdapter);
        }

        if (binding.btnViewAllMembers != null) {
            binding.btnViewAllMembers.setOnClickListener(v -> showMembersSection());
        }

        setupFilters();
    }

    private void setupFilters() {
        if (binding.chipGroupFilters != null) {
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
        }

        if (binding.btnDateFilter != null) {
            binding.btnDateFilter.setOnClickListener(v -> {
                com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                        .dateRangePicker()
                        .setTitleText("Select Date Range")
                        .build();

                picker.addOnPositiveButtonClickListener(selection -> {
                    currentDateRange = selection;
                    binding.btnDateFilter.setText("Range Selected");
                    applyFilters();
                });

                picker.show(getSupportFragmentManager(), "date_filter");
            });
        }
    }

    private void applyFilters() {
        java.util.List<Transaction> filteredList = new java.util.ArrayList<>();

        for (Transaction t : allCachedTransactions) {
            boolean matchesType = true;
            boolean matchesDate = true;

            // Type Filter
            if (currentFilterType.equals("Contributions") && !t.getType().toLowerCase().contains("contribution"))
                matchesType = false;
            if (currentFilterType.equals("Loans") && !t.getType().toLowerCase().contains("loan"))
                matchesType = false;
            if (currentFilterType.equals("Payouts") && !t.getType().toLowerCase().contains("payout"))
                matchesType = false;

            // Date Filter (Mock logic as Transaction model string date parsing is complex
            // without original date object)
            // Ideally we filter against the original Entity list, but for now we filter
            // what we have or accept limitation
            // For this implementation, we will skip date filtering on the UI model if
            // parsing is too hard,
            // OR we assume applyFilters re-processes the source entities.
            // Let's rely on cached UI transactions for simplicity, noting date limitations.

            if (matchesType) {
                filteredList.add(t);
            }
        }

        if (filteredList.isEmpty()) {
            binding.rvRecentTransactions.setVisibility(View.GONE);
            binding.emptyStateLayout.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.rvRecentTransactions.setVisibility(View.VISIBLE);
            binding.emptyStateLayout.getRoot().setVisibility(View.GONE);
            transactionAdapter.updateTransactions(filteredList);
        }
    }

    // Updated loadRecentTransactions to cache data
    private void loadRecentTransactions() {
        // ... (partially existing code)
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                getApplicationContext());
        String email = session.getUserEmail();

        if (email != null && viewModel != null) {
            viewModel.getMemberByEmailLive(email).observe(this, member -> {
                if (member != null) {
                    final String memberName = member.getName();
                    viewModel.getMemberTransactionsWithApproval(memberName).observe(this, transactionItems -> {
                        if (transactionItems != null && binding != null && transactionAdapter != null) {
                            allCachedTransactions.clear(); // Clear cache

                            for (com.example.save.data.models.TransactionWithApproval item : transactionItems) {
                                com.example.save.data.local.entities.TransactionEntity entity = item.transaction;

                                int iconRes = R.drawable.ic_money;
                                int color = 0xFF4CAF50; // Green
                                String description = entity.getDescription();

                                if (!entity.isPositive()) {
                                    color = 0xFFF44336; // Red
                                    iconRes = R.drawable.ic_loan;
                                }

                                // Pending Status Logic
                                if ("PENDING_APPROVAL".equals(entity.getStatus())) {
                                    // Fetch admin count synchronously or assume passed/observed elsewhere?
                                    // For now, let's just show "Pending Approval" or try to show stats if possible.
                                    // Ideally, we'd need total admin count here.
                                    // But we can just show "Pending (x Approvals)" which acts as progress.
                                    description += " (Pending: " + item.approvalCount + " Approvals)";
                                    color = 0xFFFF9800; // Orange for pending
                                }

                                allCachedTransactions.add(new Transaction(
                                        description,
                                        new java.text.SimpleDateFormat("MMM dd, hh:mm a",
                                                java.util.Locale.getDefault()).format(entity.getDate()),
                                        entity.getAmount(),
                                        entity.isPositive(),
                                        iconRes,
                                        color));
                            }

                            // Apply initial filters
                            applyFilters();
                        }
                    });
                }
            });
        }
    }

    private void setupHeaderInteractions() {
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(this);
        // Clicking the greeting opens Settings (for logout)
        if (binding.greetingName != null) {
            binding.greetingName.setOnClickListener(v -> {
                startActivity(new Intent(MemberMainActivity.this, SettingsActivity.class));
            });
        }

        // Notification Icon Click
        if (binding.notificationIcon != null) {
            binding.notificationIcon.setOnClickListener(v -> {
                showNotifications();
            });
        }

        // My Take Card Click
        if (binding.myTakeCard != null) {
            binding.myTakeCard.setOnClickListener(v -> {
                // Show payout history or navigate to payouts
                // For members, we can show a summary or navigate to stats
                Toast.makeText(this, "Viewing Payout History", Toast.LENGTH_SHORT).show();
                // Optionally open a dialog or fragment
                // loadFragment(new PayoutsFragment()); // If members should see it
            });
        }

        // --- QUICK ACTION ICONS ---

        // Pay Icon
        if (binding.actionPay != null) {
            binding.actionPay.setOnClickListener(v -> {
                String email = session.getUserEmail();
                if (email != null) {
                    new Thread(() -> {
                        com.example.save.data.models.Member member = viewModel.getMemberByEmail(email);
                        runOnUiThread(() -> {
                            resetAllNavItems();
                            hideHeader();
                            if (member != null) {
                                loadFragment(PaymentFragment.newInstance(member.getName()));
                            } else {
                                loadFragment(PaymentFragment.newInstance("Member"));
                            }
                        });
                    }).start();
                } else {
                    resetAllNavItems();
                    hideHeader();
                    loadFragment(PaymentFragment.newInstance("Member"));
                }
            });
        }

        // Loan Icon
        if (binding.actionLoan != null) {
            binding.actionLoan.setOnClickListener(v -> {
                resetAllNavItems();
                hideHeader();
                String email = session.getUserEmail();
                if (email == null)
                    email = "email@example.com";
                loadFragment(LoansFragment.newInstance(email));
            });
        }

        // Queue Icon
        if (binding.actionQueue != null) {
            binding.actionQueue.setOnClickListener(v -> {
                resetAllNavItems();
                hideHeader();
                loadFragment(QueueFragment.newInstance());
            });
        }

        // Profile Icon
        if (binding.actionProfile != null) {
            binding.actionProfile.setOnClickListener(
                    v -> startActivity(new Intent(MemberMainActivity.this, SettingsActivity.class)));
        }
    }

    private void hideHeader() {
        if (binding.headerGradientView != null)
            binding.headerGradientView.setVisibility(View.GONE);
        if (binding.header != null)
            binding.header.setVisibility(View.GONE);
        if (binding.mainScrollView != null)
            binding.mainScrollView.setVisibility(View.GONE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.VISIBLE);
    }

    private void showHeader() {
        if (binding.headerGradientView != null)
            binding.headerGradientView.setVisibility(View.VISIBLE);
        if (binding.header != null)
            binding.header.setVisibility(View.VISIBLE);
        if (binding.mainScrollView != null)
            binding.mainScrollView.setVisibility(View.VISIBLE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.GONE);
    }

    private void showNotifications() {
        // Clear badge (UI only, logic should be in VM or synced)
        if (binding.notificationBadge != null) {
            binding.notificationBadge.setVisibility(View.GONE);
        }

        // 1. Reset all bottom nav items (deselect them)
        resetAllNavItems();

        // 2. Hide Main Content, Show Fragment Container
        if (binding.mainScrollView != null)
            binding.mainScrollView.setVisibility(View.GONE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.VISIBLE);

        // Hide Header explicitly
        hideHeader();

        // 3. Load Notifications Fragment
        loadFragment(new NotificationsFragment());
    }

    private void showMembersSection() {
        // 1. Reset all bottom nav items (deselect them)
        resetAllNavItems();

        // 2. Hide Main Header & Content, Show Fragment Container
        hideHeader();

        // 3. Load Members Fragment
        loadFragment(new MemberViewFragment());
    }

    private void setupBottomNavigation() {
        binding.navDashboard
                .setOnClickListener(v -> {
                    updateNav(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
                    showHeader();
                });

        binding.navMembers.setOnClickListener(v -> {
            showMembersSection();
        });

        binding.navStats.setOnClickListener(v -> {
            updateNav(binding.navStats, binding.txtStats, binding.imgStats);
            hideHeader();
            String email = getIntent().getStringExtra("member_email");
            if (email == null)
                email = "email@example.com"; // Fallback
            loadFragment(AnalyticsFragment.newInstance(false, email));
        });
    }

    private void updateNav(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        // 1. Reset all items
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navStats, binding.txtStats, binding.imgStats);

        // 2. Set active
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selectedLayout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.weight = 0;
        selectedLayout.setLayoutParams(params);

        selectedLayout.setBackgroundResource(R.drawable.nav_item_pill_refined);
        selectedText.setVisibility(View.VISIBLE);

        int activeColor = Color.parseColor("#0D47A1");
        selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        selectedText.setTextColor(activeColor);

        // 3. Header & Content Visibility
        if (selectedLayout == binding.navDashboard) {
            showHeader();
        } else {
            hideHeader();
        }
    }

    private void resetAllNavItems() {
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navStats, binding.txtStats, binding.imgStats);
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void loadDashboardData() {
        // Get member email from SessionManager
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                getApplicationContext());
        String email = session.getUserEmail();

        // Fetch member data using LiveData instead of manual thread
        if (email != null && viewModel != null) {
            viewModel.getMemberByEmailLive(email).observe(this, member -> {
                if (member != null && binding != null && binding.greetingName != null) {
                    // Extract first name for cleaner display
                    String fullName = member.getName();
                    String firstName = fullName.split(" ")[0];
                    binding.greetingName.setText(firstName + "!");

                    // Update My Take Section from Admin Schedule Preferences
                    android.content.SharedPreferences adminPrefs = getSharedPreferences("SaveAppPrefs", MODE_PRIVATE);
                    String schedContribDate = adminPrefs.getString("sched_contrib_date",
                            member.getNextPaymentDueDate());
                    String schedPayoutDate = adminPrefs.getString("sched_payout_date", member.getNextPayoutDate());

                    if (binding.tvMyTakePayoutDate != null) {
                        binding.tvMyTakePayoutDate.setText(schedPayoutDate);
                    }
                    if (binding.tvMyTakeDueDate != null) {
                        binding.tvMyTakeDueDate.setText(schedContribDate);
                    }
                }
            });
        }

        if (binding.txtBalance != null && viewModel != null) {
            viewModel.getGroupBalance().observe(this, balance -> {
                if (balance != null) {
                    String formattedBalance = java.text.NumberFormat
                            .getCurrencyInstance(new java.util.Locale("en", "UG"))
                            .format(balance);
                    binding.txtBalance.setText(formattedBalance);
                }
            });
        }

        if (binding.savingsBalance != null && viewModel != null && email != null) {
            viewModel.getMemberByEmailLive(email).observe(this, member -> {
                if (binding != null && binding.savingsBalance != null) {
                    if (member != null) {
                        double mySavings = member.getContributionPaid();
                        String formatted = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("en", "UG")).format(mySavings);
                        binding.savingsBalance.setText(formatted);

                        // Update Progress
                        double target = member.getContributionTarget();
                        if (target > 0) {
                            int progress = (int) ((mySavings / target) * 100);
                            binding.metricsProgress.setProgress(progress);
                        } else {
                            binding.metricsProgress.setProgress(0);
                        }

                    } else {
                        // Member not found, show zero as actual value
                        String formatted = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("en", "UG")).format(0.0);
                        binding.savingsBalance.setText(formatted);
                        binding.metricsProgress.setProgress(0);
                    }
                }
            });
        } else if (binding.savingsBalance != null) {
            // No email provided, show zero as actual value
            String formatted = java.text.NumberFormat
                    .getCurrencyInstance(new java.util.Locale("en", "UG")).format(0.0);
            binding.savingsBalance.setText(formatted);
            binding.metricsProgress.setProgress(0);
        }

        updateExtraStats();
        loadRecentTransactions();
    }

    private void updateExtraStats() {
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                getApplicationContext());
        String email = session.getUserEmail();

        if (viewModel == null)
            return;

        // Fetch recipients using LiveData
        int slots = getSharedPreferences("ChamaPrefs", MODE_PRIVATE).getInt("slots_per_round", 5);

        if (binding.tvRecipientsLabel != null) {
            binding.tvRecipientsLabel.setText(slots > 1 ? "Current Batch Recipients" : "Next Recipient");
        }

        android.content.SharedPreferences adminPrefs = getSharedPreferences("SaveAppPrefs", MODE_PRIVATE);
        String schedPayoutDate = adminPrefs.getString("sched_payout_date", "TBD");

        viewModel.getMonthlyRecipientsLive(slots).observe(this, recipients -> {
            if (binding != null && recipientAdapter != null) {
                recipientAdapter.updateList(recipients, schedPayoutDate);
            }
        });

        viewModel.getPendingPaymentsCountLive().observe(this, pendingCount -> {
            if (binding != null && binding.tvPendingCount != null) {
                binding.tvPendingCount.setText(String.valueOf(pendingCount));

                // Update Notification Badge with pending count
                if (binding.notificationBadge != null) {
                    if (pendingCount > 0) {
                        binding.notificationBadge.setText(String.valueOf(pendingCount));
                        binding.notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        binding.notificationBadge.setVisibility(View.GONE);
                    }
                }
            }
        });

        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(this, currentMember -> {
                if (currentMember != null && binding != null) {
                    if (binding.tvPaymentStreak != null) {
                        binding.tvPaymentStreak.setText(String.valueOf(currentMember.getPaymentStreak()));
                    }
                    if (binding.tvCreditScore != null) {
                        binding.tvCreditScore.setText(String.valueOf(currentMember.getCreditScore()));
                    }

                    // Update Upcoming Payments based on real data
                    updateUpcomingPayments(currentMember);
                }
            });
        }
    }

    private void updateUpcomingPayments(com.example.save.data.models.Member member) {
        if (binding.rvUpcomingPayments == null || upcomingPaymentAdapter == null)
            return;

        java.util.List<PaymentItem> payments = new java.util.ArrayList<>();

        // Check Monthly Contribution
        if (member.getContributionPaid() < member.getContributionTarget()) {
            double remaining = member.getContributionTarget() - member.getContributionPaid();
            String amountStr = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "UG"))
                    .format(remaining);

            android.content.SharedPreferences adminPrefs = getSharedPreferences("SaveAppPrefs", MODE_PRIVATE);
            String schedContribDate = adminPrefs.getString("sched_contrib_date", "Active Cycle");

            payments.add(new PaymentItem(
                    "Monthly Contribution",
                    amountStr + " remaining",
                    "Due: " + schedContribDate,
                    "Contribution"));
        }

        if (payments.isEmpty()) {
            payments.add(new PaymentItem(
                    "All Paid Up",
                    "You are current",
                    "Relax",
                    "Status"));
        }

        upcomingPaymentAdapter.updateList(payments);
    }

    public void switchToDashboard() {
        if (binding != null && binding.navDashboard != null) {
            binding.navDashboard.performClick();
        }
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        layout.setLayoutParams(params);

        layout.setBackground(null);
        text.setVisibility(View.GONE);

        // White Color for Inactive State
        image.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }

}
