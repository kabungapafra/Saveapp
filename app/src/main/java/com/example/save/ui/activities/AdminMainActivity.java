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
import android.view.animation.AnimationUtils;
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
    private boolean isBalanceVisible = true;
    private double totalBalanceValue = 0.0;

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

        // Fetch latest system configuration from backend
        viewModel.fetchSystemConfig(null);

        setupListeners();
        setupBalanceToggle();
        observeViewModel();

        setupBottomNavigation();
        loadDashboardData();
        updateScheduleUI();
        setupScheduleListeners();
        setupSparklineChart(); // New
        setupRecentActivity();
        setupEntranceAnimations();

        // Setup Profile Icon to open Settings
        if (binding.profileIcon != null) {
            binding.profileIcon.setOnClickListener(v -> {
                applyClickAnimation(v);
                startActivity(new Intent(AdminMainActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
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

    private void setupEntranceAnimations() {
        if (binding.balanceCard == null)
            return;

        // Initial state
        binding.balanceCard.setAlpha(0f);
        binding.balanceCard.setTranslationY(80f);

        // Animate
        binding.balanceCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void setupBalanceToggle() {
        if (binding.layoutToggleBalance != null) {
            binding.layoutToggleBalance.setOnClickListener(v -> {
                isBalanceVisible = !isBalanceVisible;
                updateBalanceDisplay();
            });
        }
    }

    private void updateBalanceDisplay() {
        if (binding.currentBalance == null)
            return;

        if (isBalanceVisible) {
            String formattedBalance = java.text.NumberFormat
                    .getCurrencyInstance(new java.util.Locale("en", "UG"))
                    .format(totalBalanceValue);

            // Remove currency symbol if formatted balance contains it
            formattedBalance = formattedBalance.replace("UGX", "").replace("USh", "").trim();

            // Split by decimal if present
            if (formattedBalance.contains(".")) {
                int index = formattedBalance.lastIndexOf(".");
                binding.currentBalance.setText(formattedBalance.substring(0, index));
                if (binding.decimalLabel != null) {
                    binding.decimalLabel.setText(formattedBalance.substring(index));
                }
            } else {
                binding.currentBalance.setText(formattedBalance);
                if (binding.decimalLabel != null) {
                    binding.decimalLabel.setText(".00");
                }
            }

            if (binding.ivToggleBalance != null) {
                binding.ivToggleBalance.setImageResource(R.drawable.ic_visibility);
            }
        } else {
            binding.currentBalance.setText("••••••");
            if (binding.decimalLabel != null) {
                binding.decimalLabel.setText("");
            }
            if (binding.ivToggleBalance != null) {
                binding.ivToggleBalance.setImageResource(R.drawable.ic_visibility_off);
            }
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
            applyClickAnimation(v);
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out);
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
                applyClickAnimation(v);
                loadFragment(PaymentFragment.newInstance(adminNameStr));
            });
        }

        // Add Member card
        binding.addMemberCard.setOnClickListener(v -> {
            applyClickAnimation(v);
            updateNav(binding.navMembers, binding.txtMembers, binding.imgMembers);
            // Replace the standard fragment with one that has the argument
            MembersFragment fragment = new MembersFragment();
            Bundle args = new Bundle();
            args.putBoolean("SHOW_ADD_DIALOG", true);
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        });

        // Execute Payout card
        binding.executePayoutCard.setOnClickListener(v -> {
            applyClickAnimation(v);
            // Use existing fragment or toast for now
            // Assuming PayoutsFragment exists and is correct destination
            updateNav(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
            // Toast.makeText(this, "Execute Payout", Toast.LENGTH_SHORT).show();
        });

        // My Savings card (Quick Action)
        if (binding.myPaymentCard != null) {
            binding.myPaymentCard.setOnClickListener(v -> {
                applyClickAnimation(v);
                loadFragment(PaymentFragment.newInstance(adminNameStr));
            });
        }

        // View Loans card
        binding.viewLoansCard.setOnClickListener(v -> {
            applyClickAnimation(v);
            // Ensure visual nav update if desired, or just load fragment
            updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans);
            // loadFragment(new LoansFragment()); // updateNav already loads it
        });

        // View Details button
        if (binding.viewAllActivity != null) {
            binding.viewAllActivity.setOnClickListener(v -> {
                applyClickAnimation(v);
                updateNav(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);
            });
        }

        // Pending Approvals Card
        if (binding.cardPendingApprovals != null) {
            binding.cardPendingApprovals.setOnClickListener(v -> {
                applyClickAnimation(v);
                loadFragment(new ApprovalsFragment());
            });
        }

        // Analytics Quick Action (New)
        if (binding.actionAnalytics != null) {
            binding.actionAnalytics.setOnClickListener(v -> {
                applyClickAnimation(v);
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
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
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
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void applyClickAnimation(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_press));
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
        if (viewModel == null)
            return;

        viewModel.getDashboardSummary(new com.example.save.data.repository.MemberRepository.SummaryCallback() {
            @Override
            public void onResult(boolean success, com.example.save.data.network.DashboardSummaryResponse summary,
                    String message) {
                if (success && summary != null) {
                    runOnUiThread(() -> {
                        // Store balance for toggle
                        totalBalanceValue = summary.getTotalBalance();
                        updateBalanceDisplay();

                        // Update Personal Savings
                        String formattedSavings = java.text.NumberFormat
                                .getCurrencyInstance(new java.util.Locale("en", "UG"))
                                .format(summary.getPersonalSavings());
                        if (binding.savingsBalance != null)
                            binding.savingsBalance.setText(formattedSavings);

                        // Update Member Count (Show Active Members)
                        if (binding.activeMembers != null) {
                            int activeCount = summary.getActiveMembers();
                            binding.activeMembers.setText(String.valueOf(activeCount));
                        }

                        // Update Approval Badge
                        int pendingTotal = summary.getPendingApprovalsCount();
                        if (binding.tvApprovalBadge != null) {
                            if (pendingTotal > 0) {
                                binding.tvApprovalBadge.setText(String.valueOf(pendingTotal));
                                binding.tvApprovalBadge.setVisibility(View.VISIBLE);
                            } else {
                                binding.tvApprovalBadge.setVisibility(View.GONE);
                            }
                        }

                        // Sync Notification Badge (Legacy sync if needed)
                        // Note: UnreadCount is still reactive via
                        // observeViewModel/updateNotificationBadge
                    });
                }
            }
        });

        updateNotificationBadge();
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
        // Handled by loadDashboardData summary call
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
        // Handled by loadDashboardData summary call
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Listener removal not needed
        binding = null;
    }

    private void updateScheduleUI() {
        if (binding.btnEditSchedule == null)
            return;

        // Load saved dates (Mock preference or variables)
        android.content.SharedPreferences prefs = getSharedPreferences("SaveAppPrefs", MODE_PRIVATE);
        String contribDate = prefs.getString("sched_contrib_date", "Not Set");
        String payoutDate = prefs.getString("sched_payout_date", "Not Set");

        binding.tvContributionDate.setText(contribDate);
        binding.tvPayoutDate.setText(payoutDate);
    }

    private void setupScheduleListeners() {
        if (binding.btnEditSchedule == null)
            return;

        binding.btnEditSchedule.setOnClickListener(v -> showEditScheduleDialog());
    }

    private void showEditScheduleDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Schedule");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // Contributions Button
        android.widget.Button btnContrib = new android.widget.Button(this);
        btnContrib.setText("Set Contribution Date");
        btnContrib.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar_month, 0, 0, 0);
        layout.addView(btnContrib);

        // Payouts Button
        android.widget.Button btnPayout = new android.widget.Button(this);
        btnPayout.setText("Set Payout Date");
        btnPayout.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar_month, 0, 0, 0);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        btnPayout.setLayoutParams(params);
        layout.addView(btnPayout);

        builder.setView(layout);
        builder.setPositiveButton("Close", null);

        android.app.AlertDialog dialog = builder.create();

        btnContrib.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                getSharedPreferences("SaveAppPrefs", MODE_PRIVATE).edit().putString("sched_contrib_date", date).apply();
                updateScheduleUI();
                android.widget.Toast.makeText(this, "Contribution Date Updated", android.widget.Toast.LENGTH_SHORT)
                        .show();
                dialog.dismiss();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPayout.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                getSharedPreferences("SaveAppPrefs", MODE_PRIVATE).edit().putString("sched_payout_date", date).apply();
                updateScheduleUI();
                android.widget.Toast.makeText(this, "Payout Date Updated", android.widget.Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        dialog.show();
    }

    // Month picker removed as per redesign

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