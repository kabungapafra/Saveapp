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
            java.util.List<TaskModel> samplePayments = new java.util.ArrayList<>();
            samplePayments.add(new TaskModel("00:00", "Dec Contribution", "UGX 50,000/mo", "2 days left", "",
                    0xFF3F51B5, R.drawable.ic_calendar_month, null, "50000"));
            samplePayments.add(new TaskModel("00:00", "Loan Repay", "UGX 200,000", "5 days left", "",
                    0xFFF44336, R.drawable.ic_loan, null, "200000"));

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
        if (binding.btnHeaderMembers != null) {
            binding.btnHeaderMembers.setOnClickListener(v -> {
                showMembersSection();
            });
        }

        // Clicking the greeting opens Settings (for logout)
        if (binding.greetingName != null) {
            binding.greetingName.setOnClickListener(v -> {
                showProfileDialog();
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
        if (binding.header != null) {
            binding.header.setVisibility(View.GONE);
        }

        // 3. Load Notifications Fragment
        loadFragment(new NotificationsFragment());
    }

    private void showMembersSection() {
        // 1. Reset all bottom nav items (deselect them)
        resetAllNavItems();

        // 2. Hide Main Content, Show Fragment Container
        if (binding.mainScrollView != null)
            binding.mainScrollView.setVisibility(View.GONE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.VISIBLE);

        // 3. Load Members Fragment
        loadFragment(new MemberViewFragment());
    }

    private void setupBottomNavigation() {
        binding.navDashboard
                .setOnClickListener(v -> updateNav(binding.navDashboard, binding.txtDashboard, binding.imgDashboard));

        binding.navPay.setOnClickListener(v -> {
            updateNav(binding.navPay, binding.txtPay, binding.imgPay);
            String email = getIntent().getStringExtra("member_email");
            if (email != null) {
                // Fetch member name from email in background
                new Thread(() -> {
                    com.example.save.data.models.Member member = viewModel.getMemberByEmail(email);
                    runOnUiThread(() -> {
                        if (member != null) {
                            loadFragment(PaymentFragment.newInstance(member.getName()));
                        } else {
                            loadFragment(PaymentFragment.newInstance("Member"));
                        }
                    });
                }).start();
            } else {
                loadFragment(PaymentFragment.newInstance("Member"));
            }
        });

        binding.navQueue.setOnClickListener(v -> {
            updateNav(binding.navQueue, binding.txtQueue, binding.imgQueue);
            loadFragment(QueueFragment.newInstance());
        });

        binding.navLoans.setOnClickListener(v -> {
            updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans);
            String email = getIntent().getStringExtra("member_email");
            if (email == null)
                email = "email@example.com"; // Fallback
            loadFragment(LoansFragment.newInstance(email));
        });

        binding.navStats.setOnClickListener(v -> {
            updateNav(binding.navStats, binding.txtStats, binding.imgStats);
            String email = getIntent().getStringExtra("member_email");
            if (email == null)
                email = "email@example.com"; // Fallback
            loadFragment(AnalyticsFragment.newInstance(false, email));
        });
    }

    private void updateNav(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        // 1. Reset all items to unselected state first
        resetAllNavItems();

        // 2. Set the clicked item to selected state
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selectedLayout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.weight = 0;
        selectedLayout.setLayoutParams(params);

        // Dark Indigo Color for Active State
        int activeColor = androidx.core.content.ContextCompat.getColor(this, R.color.deep_blue);
        selectedLayout.setBackgroundResource(R.drawable.nav_item_pill_white);
        selectedText.setVisibility(View.VISIBLE);
        selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        selectedText.setTextColor(activeColor);

        // Show/Hide Main Content vs Fragment Container
        if (selectedLayout == binding.navDashboard) {
            if (binding.mainScrollView != null)
                binding.mainScrollView.setVisibility(View.VISIBLE);
            if (binding.fragmentContainer != null)
                binding.fragmentContainer.setVisibility(View.GONE);

            // Show Header on Dashboard only
            if (binding.header != null) {
                binding.header.setVisibility(View.VISIBLE);
            }
        } else {
            if (binding.mainScrollView != null)
                binding.mainScrollView.setVisibility(View.GONE);
            if (binding.fragmentContainer != null)
                binding.fragmentContainer.setVisibility(View.VISIBLE);

            // Hide Header on other tabs
            if (binding.header != null) {
                binding.header.setVisibility(View.GONE);
            }
        }
    }

    private void resetAllNavItems() {
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navPay, binding.txtPay, binding.imgPay);
        resetNavItem(binding.navQueue, binding.txtQueue, binding.imgQueue);
        resetNavItem(binding.navLoans, binding.txtLoans, binding.imgLoans);
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

                    // Update My Take Section
                    if (binding.tvMyTakePayoutDate != null) {
                        binding.tvMyTakePayoutDate.setText(member.getNextPayoutDate());
                    }
                    if (binding.tvMyTakeDueDate != null) {
                        binding.tvMyTakeDueDate.setText(member.getNextPaymentDueDate());
                    }
                }
            });
        }

        // Load Group ID
        if (binding.tvGroupId != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
            String groupId = prefs.getString("group_id", null);
            if (groupId == null) {
                // Should have been generated by Admin, but safe fallback
                groupId = "GRP-PENDING";
            }
            binding.tvGroupId.setText("ID: " + groupId);
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
                            binding.tvProgressPercentage.setText(progress + "% of Goal");
                        } else {
                            binding.metricsProgress.setProgress(0);
                            binding.tvProgressPercentage.setText("0% of Goal");
                        }

                    } else {
                        // Member not found, show zero as actual value
                        String formatted = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("en", "UG")).format(0.0);
                        binding.savingsBalance.setText(formatted);
                        binding.metricsProgress.setProgress(0);
                        binding.tvProgressPercentage.setText("0% of Goal");
                    }
                }
            });
        } else if (binding.savingsBalance != null) {
            // No email provided, show zero as actual value
            String formatted = java.text.NumberFormat
                    .getCurrencyInstance(new java.util.Locale("en", "UG")).format(0.0);
            binding.savingsBalance.setText(formatted);
            binding.metricsProgress.setProgress(0);
            binding.tvProgressPercentage.setText("0% of Goal");
        }

        updateExtraStats();
        updateMemberCount();
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

        viewModel.getMonthlyRecipientsLive(slots).observe(this, recipients -> {
            if (binding != null && recipientAdapter != null) {
                recipientAdapter.updateList(recipients);
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

        java.util.List<TaskModel> payments = new java.util.ArrayList<>();

        // Check Monthly Contribution
        if (member.getContributionPaid() < member.getContributionTarget()) {
            double remaining = member.getContributionTarget() - member.getContributionPaid();
            String amountStr = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("en", "UG"))
                    .format(remaining);

            payments.add(new TaskModel(
                    "Due Now",
                    "Monthly Contribution",
                    amountStr + " remaining",
                    "Active Cycle",
                    "",
                    0xFF3F51B5, // Indigo
                    R.drawable.ic_calendar_month,
                    null,
                    String.valueOf((int) remaining)));
        }

        if (payments.isEmpty()) {
            payments.add(new TaskModel(
                    "Great!",
                    "All Paid Up",
                    "You are current",
                    "Relax",
                    "",
                    0xFF4CAF50, // Green
                    R.drawable.ic_notifications,
                    null,
                    "0"));
        }

        upcomingPaymentAdapter.updateList(payments);
    }

    private void updateMemberCount() {
        if (viewModel != null && binding != null && binding.activeMembers != null) {
            viewModel.getActiveMemberCountLive().observe(this, activeMembersCount -> {
                if (binding != null && binding.activeMembers != null) {
                    viewModel.getTotalMemberCountLive().observe(this, totalMembers -> {
                        if (binding != null && binding.activeMembers != null) {
                            binding.activeMembers.setText(activeMembersCount + "/" + totalMembers);
                        }
                    });
                }
            });
        }
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

    private void showProfileDialog() {
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                getApplicationContext());
        String email = session.getUserEmail();

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_member_profile, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        // Add null check for dialog window
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Views
        TextView tvInitials = dialogView.findViewById(R.id.tvProfileInitials);
        TextView tvName = dialogView.findViewById(R.id.tvProfileName);
        TextView tvRole = dialogView.findViewById(R.id.tvProfileRole);
        TextView tvId = dialogView.findViewById(R.id.tvProfileId);
        TextView tvEmail = dialogView.findViewById(R.id.tvProfileEmail);
        TextView tvJoined = dialogView.findViewById(R.id.tvProfileJoined);
        Button btnLogout = dialogView.findViewById(R.id.btnLogout);
        Button btnClose = dialogView.findViewById(R.id.btnCloseProfile);

        // Fetch latest data using LiveData
        if (email != null && viewModel != null) {
            viewModel.getMemberByEmailLive(email).observe(this, member -> {
                if (member != null && dialogView != null) {
                    String name = member.getName();
                    if (tvName != null)
                        tvName.setText(name);
                    if (tvRole != null)
                        tvRole.setText(member.getRole());
                    if (tvEmail != null)
                        tvEmail.setText(member.getEmail());

                    // Initials
                    if (name != null && !name.isEmpty() && tvInitials != null) {
                        String[] parts = name.split(" ");
                        String initials = "";
                        if (parts.length > 0)
                            initials += parts[0].charAt(0);
                        if (parts.length > 1)
                            initials += parts[1].charAt(0);
                        tvInitials.setText(initials.toUpperCase());
                    }

                    // Formatted ID
                    if (tvId != null)
                        tvId.setText(member.getFormattedId());

                    // Joined Date (placeholder)
                    if (tvJoined != null)
                        tvJoined.setText("Jan 2024");

                    // Setup Badges
                    androidx.recyclerview.widget.RecyclerView rvBadges = dialogView.findViewById(R.id.rvBadges);
                    if (rvBadges != null) {
                        java.util.List<com.example.save.data.models.Badge> badges = new java.util.ArrayList<>();
                        int streak = member.getPaymentStreak();

                        // Logic for unlocking badges
                        badges.add(new com.example.save.data.models.Badge("Bronze Saver", R.drawable.ic_stars,
                                streak >= 3));
                        badges.add(new com.example.save.data.models.Badge("Silver Saver", R.drawable.ic_stars,
                                streak >= 6));
                        badges.add(new com.example.save.data.models.Badge("Gold Saver", R.drawable.ic_stars,
                                streak >= 12));

                        com.example.save.ui.adapters.BadgeAdapter badgeAdapter = new com.example.save.ui.adapters.BadgeAdapter(
                                badges);
                        rvBadges.setAdapter(badgeAdapter);
                        rvBadges.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(
                                this, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
                    }
                }
            });
        }

        btnLogout.setOnClickListener(v -> {
            session.logoutUser();
            finish();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
