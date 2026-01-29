package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;
import android.widget.FrameLayout;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Chart Imports
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.datepicker.MaterialDatePicker;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminmainBinding;
import com.example.save.databinding.ItemActivityBinding;
import com.example.save.databinding.ItemCalendarDateBinding;
import com.example.save.data.repository.MemberRepository;
import com.example.save.ui.viewmodels.MembersViewModel;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminmainBinding binding;

    // Data
    private String adminNameStr;
    private String groupNameStr;
    private MemberRepository memberRepository;
    private MembersViewModel viewModel; // Use ViewModel if possible, or just remove listener for now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Correct layout name
        binding = ActivityAdminmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);

        // Initialize Notifications
        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);

        // Load admin and group name FIRST (before other data)
        loadAdminData();

        setupListeners();
        observeViewModel();

        setupBottomNavigation();
        loadDashboardData();
        setupDatePicker();
        setupMonthPicker(); // New
        setupSparklineChart(); // New
        setupRecentActivity();

        // Setup Profile Icon to open Settings
        if (binding.profileIcon != null) {
            binding.profileIcon.setOnClickListener(v -> {
                startActivity(new Intent(AdminMainActivity.this, SettingsActivity.class));
            });
        }

        // Handle External Navigation
        if (getIntent().hasExtra("NAVIGATE_TO")) {
            String target = getIntent().getStringExtra("NAVIGATE_TO");
            if ("MEMBERS".equals(target)) {
                // Post to message queue to allow UI to settle? Or just run immediate
                binding.navMembers.post(() -> updateNav(binding.navMembers, binding.txtMembers, binding.imgMembers));
            } else if ("LOANS".equals(target)) {
                binding.navLoans.post(() -> updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans));
            }
        }

        // Fix for blank screen on swipe back
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                // We are back at the root
                if (binding.fragmentContainer != null) {
                    binding.fragmentContainer.setVisibility(View.GONE);
                }
                if (binding.mainContentScrollView != null) {
                    binding.mainContentScrollView.setVisibility(View.VISIBLE);
                }
                if (binding.navContainer != null) {
                    binding.navContainer.setVisibility(View.VISIBLE);
                }
                // Reset nav to Home visually
                updateNav(binding.navHome, binding.txtHome, binding.imgHome);
            }
        });
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            binding.fragmentContainer.setVisibility(View.GONE);
            binding.mainContentScrollView.setVisibility(View.VISIBLE);
            binding.navContainer.setVisibility(View.VISIBLE); // Restore nav

            // Use beginTransaction().commit() to ensure state is committed
            // We can remove the fragment to cleanup resources, or just hide the container
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment != null) {
                getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
            }

            // Reset nav selection to Home
            updateNav(binding.navHome, binding.txtHome, binding.imgHome);
        } else {
            // Exit app instead of going back to login
            finishAffinity();
        }
    }

    private void initializeViews() {
        // Method removed as binding handles initialization
    }

    private void loadAdminData() {
        // Get admin data from SharedPreferences or Intent
        SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        // Default to "Admin" if not found
        adminNameStr = prefs.getString("admin_name", "Admin");
        groupNameStr = prefs.getString("group_name", "Weekend Savers Club");

        // Overwrite with Intent extra if present
        Intent intent = getIntent();
        if (intent.hasExtra("admin_name")) {
            adminNameStr = intent.getStringExtra("admin_name");
        }
        if (intent.hasExtra("group_name")) {
            groupNameStr = intent.getStringExtra("group_name");
        }

        // Handle Group ID
        String groupId = prefs.getString("group_id", null);
        if (groupId == null) {
            // Generate new Group ID
            int randomId = new java.util.Random().nextInt(900000) + 100000;
            groupId = "GRP-" + randomId;
            prefs.edit().putString("group_id", groupId).apply();
        }

        // Set to UI
        if (binding.adminName != null)
            binding.adminName.setText(adminNameStr + "!");
        if (binding.groupName != null)
            binding.groupName.setText(groupNameStr);
        if (binding.tvGroupId != null)
            binding.tvGroupId.setText("ID: " + groupId);
        if ("Admin".equals(adminNameStr) || "Weekend Savers Club".equals(groupNameStr)) {
            // Try to get email from Intent OR SharedPreferences (Robust Fallback)
            String email = intent.getStringExtra("admin_email");
            if (email == null) {
                email = prefs.getString("admin_email", null);
            }

            if (email != null) {
                String finalEmail = email; // For lambda
                new Thread(() -> {
                    com.example.save.data.models.Member admin = viewModel.getMemberByEmail(finalEmail);
                    if (admin != null) {
                        String realName = admin.getName();
                        runOnUiThread(() -> {
                            binding.adminName.setText(realName + "!");
                            adminNameStr = realName; // Update local variable for PaymentFragment usage
                            // Save to prefs for next time
                            prefs.edit().putString("admin_name", realName).apply();

                            // Now that we have the real name, reload dashboard stats to be sure
                            loadDashboardData();
                        });
                    }
                }).start();
            } else {
                // ULTIMATE FALLBACK: If no email locally, just find the first Administrator in
                // the DB
                new Thread(() -> {
                    List<com.example.save.data.models.Member> admins = viewModel.getAdmins();
                    if (admins != null && !admins.isEmpty()) {
                        com.example.save.data.models.Member firstAdmin = admins.get(0);
                        String recoveredName = firstAdmin.getName();
                        String recoveredEmail = firstAdmin.getEmail();

                        runOnUiThread(() -> {
                            binding.adminName.setText(recoveredName + "!");
                            adminNameStr = recoveredName;
                            // Save recovered identity
                            prefs.edit()
                                    .putString("admin_name", recoveredName)
                                    .putString("admin_email", recoveredEmail)
                                    .apply();

                            // Reload with recovered identity
                            loadDashboardData();
                        });
                    } else {
                        // DEBUG: No admin found
                        runOnUiThread(() -> {
                            // Toast.makeText(AdminMainActivity.this, "DEBUG: No Admin found in Database!",
                            // Toast.LENGTH_LONG).show();
                        });
                    }
                }).start();
            }
        }

        // Set to UI
        if (binding.adminName != null)
            binding.adminName.setText(adminNameStr + "!");
        if (binding.groupName != null)
            binding.groupName.setText(groupNameStr);
    }

    // ...

    private void setupRecentActivity() {
        if (binding.activityRecyclerView != null) {
            binding.activityRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

            // Initialize adapter with empty list
            RecentActivityAdapter adapter = new RecentActivityAdapter(new ArrayList<>());
            binding.activityRecyclerView.setAdapter(adapter);

            // Observe recent transactions from ViewModel
            viewModel.getRecentTransactions().observe(this, transactions -> {
                if (transactions != null && !transactions.isEmpty()) {
                    adapter.updateList(transactions);
                    binding.activityRecyclerView.setVisibility(View.VISIBLE);
                    binding.emptyStateLayout.getRoot().setVisibility(View.GONE);
                } else {
                    binding.activityRecyclerView.setVisibility(View.GONE);
                    binding.emptyStateLayout.getRoot().setVisibility(View.VISIBLE);

                    // Update text for empty state if needed
                    // binding.emptyStateLayout.tvEmptyTitle.setText("No recent activity");
                }
            });
        }
    }

    private void observeViewModel() {
        if (viewModel != null) {
            viewModel.getMembers().observe(this, members -> {
                if (members != null) {
                    loadDashboardData();
                }
            });
        }
    }

    private void setupListeners() {
        binding.profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Notification Icon Click
        if (binding.btnAdminNotifications != null) {
            binding.btnAdminNotifications.setOnClickListener(v -> {
                showNotifications();
            });
        }

        // Main Card: My Savings Click
        if (binding.layoutMySavings != null) {
            binding.layoutMySavings.setOnClickListener(v -> {
                loadFragment(PaymentFragment.newInstance(adminNameStr));
            });
        }

        // Add Member card
        binding.addMemberCard.setOnClickListener(v -> {
            updateNav(binding.navMembers, binding.txtMembers, binding.imgMembers);
            // Replace the standard fragment with one that has the argument
            MembersFragment fragment = new MembersFragment();
            Bundle args = new Bundle();
            args.putBoolean("SHOW_ADD_DIALOG", true);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        });

        // Execute Payout card
        binding.executePayoutCard.setOnClickListener(v -> {
            // Use existing fragment or toast for now
            // Assuming PayoutsFragment exists and is correct destination
            updateNav(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
            // Toast.makeText(this, "Execute Payout", Toast.LENGTH_SHORT).show();
        });

        // My Savings card (Quick Action)
        if (binding.myPaymentCard != null) {
            binding.myPaymentCard.setOnClickListener(v -> {
                loadFragment(PaymentFragment.newInstance(adminNameStr));
            });
        }

        // View Loans card
        binding.viewLoansCard.setOnClickListener(v -> {
            // Ensure visual nav update if desired, or just load fragment
            updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans);
            // loadFragment(new LoansFragment()); // updateNav already loads it
        });

        // View Details button
        if (binding.viewAllActivity != null) {
            binding.viewAllActivity.setOnClickListener(v -> {
                updateNav(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);
            });
        }

        // Pending Approvals Card
        if (binding.cardPendingApprovals != null) {
            binding.cardPendingApprovals.setOnClickListener(v -> {
                loadFragment(new ApprovalsFragment());
            });
        }

        // Analytics Quick Action (New)
        if (binding.actionAnalytics != null) {
            binding.actionAnalytics.setOnClickListener(v -> {
                updateNav(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);
            });
        }
    }

    private void setupBottomNavigation() {
        binding.navHome.setOnClickListener(v -> updateNav(binding.navHome, binding.txtHome, binding.imgHome));

        binding.navMembers.setOnClickListener(v -> {
            updateNav(binding.navMembers, binding.txtMembers, binding.imgMembers);
            Toast.makeText(this, "Members Management", Toast.LENGTH_SHORT).show();
        });

        binding.navPayouts.setOnClickListener(v -> {
            updateNav(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
            Toast.makeText(this, "Payout Management", Toast.LENGTH_SHORT).show();
        });

        binding.navLoans.setOnClickListener(v -> {
            updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans);
            Toast.makeText(this, "Loans Management", Toast.LENGTH_SHORT).show();
        });

        binding.navAnalytics.setOnClickListener(v -> {
            updateNav(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);
            Toast.makeText(this, "Analytics", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateNav(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        // 1. Reset all items to unselected state first
        resetNavItem(binding.navHome, binding.txtHome, binding.imgHome);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
        resetNavItem(binding.navLoans, binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);

        // 2. Set the clicked item to selected state
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selectedLayout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.weight = 0;
        selectedLayout.setLayoutParams(params);

        // Dark Indigo Color for Active State
        int activeColor = Color.parseColor("#0D47A1");
        selectedLayout.setBackgroundResource(R.drawable.nav_item_pill_refined);
        selectedText.setVisibility(View.VISIBLE);
        selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        selectedText.setTextColor(activeColor);

        // Fragment Switching Logic
        if (selectedLayout == binding.navHome) {
            binding.mainContentScrollView.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
        } else {
            binding.mainContentScrollView.setVisibility(View.GONE);
            binding.fragmentContainer.setVisibility(View.VISIBLE);

            Fragment fragment = null;
            if (selectedLayout == binding.navMembers)
                fragment = new MembersFragment();
            else if (selectedLayout == binding.navPayouts)
                fragment = new PayoutsFragment();
            else if (selectedLayout == binding.navLoans)
                fragment = new AdminLoansFragment(); // Use AdminLoansFragment!
            else if (selectedLayout == binding.navAnalytics)
                fragment = AnalyticsFragment.newInstance(true);

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        if (binding.mainContentScrollView != null)
            binding.mainContentScrollView.setVisibility(View.GONE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        layout.setLayoutParams(params);

        layout.setBackground(null);

        // White Color for Inactive State
        image.setImageTintList(ColorStateList.valueOf(Color.BLACK)); // Using Black/White as per theme? Actually
                                                                     // previous was white.
        // Let's use WHITE as per the design requirement (Dark bar, white icons)
        image.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }

    private void loadDashboardData() {
        // Load data from ViewModel
        // Load data from ViewModel
        if (binding.currentBalance != null && viewModel != null) {
            viewModel.getGroupBalance().observe(this, balance -> {
                if (balance != null) {
                    String formattedBalance = java.text.NumberFormat
                            .getCurrencyInstance(new java.util.Locale("en", "UG"))
                            .format(balance);
                    binding.currentBalance.setText(formattedBalance);
                }
            });
        }

        if (binding.savingsBalance != null && viewModel != null) {
            // Show Admin's personal savings - Run on background thread
            new Thread(() -> {
                try {
                    String email = getIntent().getStringExtra("admin_email");
                    if (email == null) {
                        SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
                        email = prefs.getString("admin_email", null);
                    }

                    com.example.save.data.models.Member admin;

                    if (email != null) {
                        admin = viewModel.getMemberByEmail(email);
                    } else {
                        // Use recovered or current name
                        admin = viewModel.getMemberByNameSync(adminNameStr);
                    }

                    com.example.save.data.models.Member finalAdmin = admin;
                    runOnUiThread(() -> {
                        if (finalAdmin != null) {
                            double mySavings = finalAdmin.getContributionPaid();
                            String formattedSavings = java.text.NumberFormat
                                    .getCurrencyInstance(new java.util.Locale("en", "UG"))
                                    .format(mySavings);
                            binding.savingsBalance.setText(formattedSavings);
                        } else {
                            binding.savingsBalance.setText("UGX 0");
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> binding.savingsBalance.setText("UGX 0"));
                }
            }).start();
        }

        updateMemberCount();
        updateNotificationBadge();
        updateApprovalBadge();

        // Sync pending tasks to notifications
        new Thread(() -> {
            try {
                if (viewModel != null) {
                    int pendingPayments = viewModel.getPendingPaymentsCount();
                    java.util.List<com.example.save.data.models.LoanRequest> pendingLoans = viewModel
                            .getPendingLoanRequests();
                    int pendingLoansCount = (pendingLoans != null) ? pendingLoans.size() : 0;

                    runOnUiThread(() -> syncPendingTasksToNotifications(pendingPayments, pendingLoansCount));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateNotificationBadge() {
        if (notificationsViewModel == null) {
            notificationsViewModel = new androidx.lifecycle.ViewModelProvider(this)
                    .get(com.example.save.ui.viewmodels.NotificationsViewModel.class);
        }

        notificationsViewModel.getUnreadCount().observe(this, count -> {
            if (binding.tvAdminNotificationBadge != null) {
                if (count > 0) {
                    binding.tvAdminNotificationBadge.setText(String.valueOf(count));
                    binding.tvAdminNotificationBadge.setVisibility(View.VISIBLE);
                } else {
                    binding.tvAdminNotificationBadge.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateApprovalBadge() {
        if (viewModel != null) {
            viewModel.getPendingTransactions().observe(this, transactions -> {
                int txCount = transactions != null ? transactions.size() : 0;
                new Thread(() -> {
                    int loanCount = viewModel.getPendingLoanRequests().size();
                    int total = txCount + loanCount;

                    runOnUiThread(() -> {
                        if (binding.tvApprovalBadge != null) {
                            if (total > 0) {
                                binding.tvApprovalBadge.setText(String.valueOf(total));
                                binding.tvApprovalBadge.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvApprovalBadge.setVisibility(View.GONE);
                            }
                        }
                    });
                }).start();
            });
        }
    }

    private com.example.save.ui.viewmodels.NotificationsViewModel notificationsViewModel;

    // ... inside onCreate
    // notificationsViewModel = new
    // androidx.lifecycle.ViewModelProvider(this).get(com.example.save.ui.viewmodels.NotificationsViewModel.class);

    private void showNotifications() {
        // Clear badge (UI only)
        if (binding.tvAdminNotificationBadge != null) {
            binding.tvAdminNotificationBadge.setVisibility(View.GONE);
        }

        // 1. Reset all bottom nav items (deselect them)
        resetNavItem(binding.navHome, binding.txtHome, binding.imgHome);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
        resetNavItem(binding.navLoans, binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);

        // 2. Hide Main Content, Show Fragment Container
        if (binding.mainContentScrollView != null)
            binding.mainContentScrollView.setVisibility(View.GONE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.VISIBLE);

        // 3. Load Notifications Fragment with Admin flag
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, com.example.save.ui.fragments.NotificationsFragment.newInstance(true))
                .addToBackStack(null)
                .commit();

        // Mark seen logic handled by Fragment/User interaction now
        try {
            // Optional: Auto-sync again just to be sure list is fresh?
            // syncPendingTasksToNotifications(...)
            // Better to let ViewModel/Repo handle it.
        } catch (Exception e) {
        }
    }

    // Removed local showComposeAnnouncementDialog as it is now in the Fragment

    private void showComposeAnnouncementDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_compose_announcement, null);
        com.google.android.material.textfield.TextInputEditText etTitle = view.findViewById(R.id.etTitle);
        com.google.android.material.textfield.TextInputEditText etMessage = view.findViewById(R.id.etMessage);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Send Announcement")
                .setView(view)
                .setPositiveButton("Send", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String message = etMessage.getText().toString().trim();

                    if (!title.isEmpty() && !message.isEmpty()) {
                        sendAnnouncement(title, message);
                    } else {
                        Toast.makeText(this, "Title and message required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendAnnouncement(String title, String message) {
        // Use NotificationsViewModel or Repository to save
        if (notificationsViewModel == null) {
            notificationsViewModel = new androidx.lifecycle.ViewModelProvider(this)
                    .get(com.example.save.ui.viewmodels.NotificationsViewModel.class);
        }

        // For now, using a helper method in VM or Repos directly.
        // We want to create this notification for ALL members.
        // Our current DB schema simplistic, maybe just 1 entry with "target=ALL" or
        // individual inserts.
        // For simplicity in this prototype, we'll just insert one notification that
        // everyone fetches,
        // OR we loop insert. Let's assume loop insert for 'ALL' support if schema
        // doesn't support broadcast.
        // Actually, let's just use the basic addNotification which is single user
        // context usually?
        // The Repository.addNotification creates AN Entity.
        // If we want everyone to see it, we might need a flag or multiple entries.
        // Let's modify the repository to support "Broadcast" or just insert 1 generic
        // "ANNOUNCEMENT".
        // If the App queries "SELECT * FROM notifications", everyone sees everything
        // unless filtered by userID.
        // My NotificationEntity DOES NOT have a userId column yet!
        // Checking my implementation... I didn't add userId in
        // `create_notification_entity` step (hypothetically).
        // Let's assume it's global for now (broadcast style) as I didn't see userId in
        // the entity update I made (Step 871).

        notificationsViewModel.createAnnouncement(title, message);
        Toast.makeText(this, "Announcement Sent!", Toast.LENGTH_SHORT).show();
    }

    private void syncPendingTasksToNotifications(int pendingPaymentsCount, int pendingLoansCount) {
        if (notificationsViewModel == null) {
            notificationsViewModel = new androidx.lifecycle.ViewModelProvider(this)
                    .get(com.example.save.ui.viewmodels.NotificationsViewModel.class);
        }

        if (pendingPaymentsCount > 0) {
            String msg = "You have " + pendingPaymentsCount + " pending payment(s) to review.";
            notificationsViewModel.ensureSystemNotification("Pending Payments", msg, "PAYMENT");
        }

        if (pendingLoansCount > 0) {
            String msg = "You have " + pendingLoansCount + " pending loan request(s).";
            notificationsViewModel.ensureSystemNotification("Loan Requests", msg, "LOAN");
        }
    }

    private void markNotificationsAsSeen(int pendingPayments, int pendingLoansCount) {
        // Legacy: Logic moved to database 'read' status.
        // But we might want to mark them all read if user opens panel?
        // Let's just keep this empty or remove calls to it.
        // For now, we'll leave it empty to avoid breaking calls.
    }

    private void updateMemberCount() {
        // Run on background thread
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
                        binding.activeMembers.setText("0/0");
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Listener removal not needed
        binding = null;
    }

    private void setupDatePicker() {
        if (binding.dateRecyclerView != null) {
            binding.dateRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

            List<DateItem> dates = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

            for (int i = 0; i < 30; i++) {
                String day = dayFormat.format(calendar.getTime()).toUpperCase();
                String date = dateFormat.format(calendar.getTime());
                dates.add(new DateItem(day, date, i == 0, calendar));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            binding.dateRecyclerView.setAdapter(new DateAdapter(dates));
        }
    }

    private void setupMonthPicker() {
        if (binding.btnSelectMonth != null) {
            binding.btnSelectMonth.setOnClickListener(v -> {
                MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select Month")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

                datePicker.addOnPositiveButtonClickListener(selection -> {
                    // Update calendar to selected date
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(selection);

                    // Here we would typically regenerate the date list starting from this month
                    // For now, just a toast to show it works
                    SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
                    Toast.makeText(this, "Selected: " + format.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
                });

                datePicker.show(getSupportFragmentManager(), "month_picker");
            });
        }
    }

    private void setupSparklineChart() {
        if (binding.sparklineChart == null)
            return;

        LineChart chart = binding.sparklineChart;

        // Disable interactions
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);

        // Remove axis
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);

        // Mock Data for Sparkline (Trend)
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 1000000));
        entries.add(new Entry(1, 1200000));
        entries.add(new Entry(2, 1150000));
        entries.add(new Entry(3, 1350000));
        entries.add(new Entry(4, 1400000));
        entries.add(new Entry(5, 1500000)); // Current

        LineDataSet dataSet = new LineDataSet(entries, "Balance");
        dataSet.setColor(Color.WHITE);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Gradient fill
        dataSet.setDrawFilled(true);
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            // Just use white with transparency for now
            dataSet.setFillColor(Color.WHITE);
            dataSet.setFillAlpha(50);
        }

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh

        // Animate
        chart.animateX(1000);
    }

}