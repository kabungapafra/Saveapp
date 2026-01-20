package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.ui.viewmodels.MembersViewModel; // Added import

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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

        initializeViews();
        setupBottomNavigation();
        setupHeaderInteractions();
        loadDashboardData();
    }

    @android.annotation.SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // If a fragment is showing, go back to dashboard
        if (binding.fragmentContainer != null
                && binding.fragmentContainer.getVisibility() == android.view.View.VISIBLE) {
            switchToDashboard();
        } else {
            // Exit app instead of going back to login
            finishAffinity();
        }
    }

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
    }

    /*
     * Removed as button no longer exists
     * private void setupDashboardInteractions() {
     * if (btnTopUp != null) {
     * btnTopUp.setOnClickListener(v -> {
     * Toast.makeText(this, "Top Up Balance", Toast.LENGTH_SHORT).show();
     * });
     * }
     * }
     */

    private void setupHeaderInteractions() {
        if (binding.btnHeaderMembers != null) {
            binding.btnHeaderMembers.setOnClickListener(v -> {
                showMembersSection();
            });
        }

        // Clicking the greeting opens Settings (for logout)
        if (binding.greetingName != null) {
            binding.greetingName.setOnClickListener(v -> {
                startActivity(new Intent(MemberMainActivity.this, SettingsActivity.class));
            });
        }
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
            loadFragment(new LoansFragment());
        });

        binding.navStats.setOnClickListener(v -> {
            updateNav(binding.navStats, binding.txtStats, binding.imgStats);
            String email = getIntent().getStringExtra("member_email");
            if (email == null)
                email = "email@example.com"; // Fallback
            loadFragment(AnalyticsFragment.newInstance(true, email));
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
        // Get member email from intent
        String email = getIntent().getStringExtra("member_email");

        // Fetch member data in background thread to get the name
        if (email != null && viewModel != null) {
            new Thread(() -> {
                com.example.save.data.models.Member member = viewModel.getMemberByEmail(email);
                runOnUiThread(() -> {
                    if (member != null && binding.greetingName != null) {
                        // Extract first name for cleaner display
                        String fullName = member.getName();
                        String firstName = fullName.split(" ")[0];
                        binding.greetingName.setText(firstName + "!");
                    }
                });
            }).start();
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
            new Thread(() -> {
                com.example.save.data.models.Member member = viewModel.getMemberByEmail(email);
                runOnUiThread(() -> {
                    if (member != null) {
                        double mySavings = member.getContributionPaid();
                        String formatted = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("en", "UG")).format(mySavings);
                        binding.savingsBalance.setText(formatted);
                    } else {
                        // Member not found, show zero as actual value (not hardcoded fallback)
                        String formatted = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("en", "UG")).format(0.0);
                        binding.savingsBalance.setText(formatted);
                    }
                });
            }).start();
        } else if (binding.savingsBalance != null) {
            // No email provided, show zero as actual value
            String formatted = java.text.NumberFormat
                    .getCurrencyInstance(new java.util.Locale("en", "UG")).format(0.0);
            binding.savingsBalance.setText(formatted);
        }

        updateExtraStats();
        updateMemberCount();
        loadRecentTransactions();
    }

    private void updateExtraStats() {
        String email = getIntent().getStringExtra("member_email");
        if (viewModel == null)
            return;

        // Fetch recipients
        int slots = getSharedPreferences("ChamaPrefs", MODE_PRIVATE).getInt("slots_per_round", 5);
        new Thread(() -> {
            try {
                java.util.List<com.example.save.data.models.Member> recipients = viewModel.getMonthlyRecipients(slots);
                int pendingCount = viewModel.getPendingPaymentsCount();

                com.example.save.data.models.Member currentMember = null;
                if (email != null) {
                    currentMember = viewModel.getMemberByEmail(email);
                }

                com.example.save.data.models.Member finalMember = currentMember;
                runOnUiThread(() -> {
                    if (recipientAdapter != null) {
                        recipientAdapter.updateList(recipients);
                    }
                    if (binding.tvPendingCount != null) {
                        binding.tvPendingCount.setText(String.valueOf(pendingCount));
                    }
                    if (binding.tvPaymentStreak != null && finalMember != null) {
                        binding.tvPaymentStreak.setText(String.valueOf(finalMember.getPaymentStreak()));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateMemberCount() {
        if (viewModel != null && binding.activeMembers != null) {
            new Thread(() -> {
                try {
                    int activeMembersCount = viewModel.getActiveMemberCountSync();
                    int totalMembers = viewModel.getTotalMemberCountSync();
                    runOnUiThread(() -> {
                        if (binding != null && binding.activeMembers != null) {
                            binding.activeMembers.setText(activeMembersCount + "/" + totalMembers);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        if (binding != null && binding.activeMembers != null) {
                            // On error, still try to show meaningful data or leave empty
                            binding.activeMembers.setText("--");
                        }
                    });
                }
            }).start();
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

    private void loadRecentTransactions() {
        String email = getIntent().getStringExtra("member_email");
        if (email == null || viewModel == null || transactionAdapter == null) {
            return;
        }

        // Load transactions in background
        new Thread(() -> {
            try {
                java.util.List<Transaction> transactions = new java.util.ArrayList<>();
                com.example.save.data.models.Member member = viewModel.getMemberByEmail(email);

                if (member != null) {
                    // Add contribution transaction (sample - based on member's contribution)
                    if (member.getContributionPaid() > 0) {
                        transactions.add(new Transaction(
                                "Monthly Savings",
                                new java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
                                        .format(new java.util.Date()),
                                member.getContributionPaid() / 12, // Approximate monthly
                                true, // Credit
                                R.drawable.ic_money,
                                0xFFE8F5E9 // Light green
                        ));
                    }

                    // Get member's loans directly from repository
                    com.example.save.ui.viewmodels.LoansViewModel loansViewModel = new androidx.lifecycle.ViewModelProvider(
                            this).get(com.example.save.ui.viewmodels.LoansViewModel.class);

                    java.util.List<com.example.save.data.models.Loan> allLoans = loansViewModel.getLoansSync();

                    if (allLoans != null) {
                        for (com.example.save.data.models.Loan loan : allLoans) {
                            if (member.getName().equals(loan.getMemberName())) {
                                // Add loan disbursement
                                if ("ACTIVE".equals(loan.getStatus()) || "PAID".equals(loan.getStatus())) {
                                    transactions.add(new Transaction(
                                            "Loan Disbursement",
                                            new java.text.SimpleDateFormat("dd MMM, hh:mm a",
                                                    java.util.Locale.getDefault())
                                                    .format(loan.getDateRequested() != null
                                                            ? loan.getDateRequested()
                                                            : new java.util.Date()),
                                            loan.getAmount(),
                                            true, // Credit (received money)
                                            R.drawable.ic_loan,
                                            0xFFE3F2FD // Light blue
                                    ));
                                }

                                // Add repayment if any
                                if (loan.getRepaidAmount() > 0) {
                                    transactions.add(new Transaction(
                                            "Loan Repayment",
                                            new java.text.SimpleDateFormat("dd MMM, hh:mm a",
                                                    java.util.Locale.getDefault())
                                                    .format(new java.util.Date()),
                                            loan.getRepaidAmount(),
                                            false, // Debit (paid money)
                                            R.drawable.ic_loan,
                                            0xFFFFEBEE // Light red
                                    ));
                                }
                            }
                        }
                    }

                    // Limit to 5 most recent
                    final java.util.List<Transaction> finalTransactions = transactions.size() > 5
                            ? transactions.subList(0, 5)
                            : transactions;

                    runOnUiThread(() -> {
                        if (transactionAdapter != null) {
                            transactionAdapter.updateTransactions(finalTransactions);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
