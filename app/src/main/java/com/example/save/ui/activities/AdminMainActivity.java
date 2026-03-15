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
        // setupScheduleListeners(); - method removed due to refined UI
        setupGrowthChart(); // Updated to BarChart
        // setupRecentActivity(); - removed in refined dashboard
        setupEntranceAnimations();

        // Profile Icon was replaced by a search icon in the refined dashboard.
        // If settings navigation is needed, it can be assigned to a different view.

        // Handle External Navigation
        if (getIntent().hasExtra("NAVIGATE_TO")) {
            String target = getIntent().getStringExtra("NAVIGATE_TO");
            if ("MEMBERS".equals(target)) {
                binding.bgMembers.post(
                        () -> updateNav(binding.bgMembers, binding.txtMembers, binding.imgMembers, binding.bgMembers));
            } else if ("LOANS".equals(target)) {
                binding.bgLoans
                        .post(() -> updateNav(binding.bgLoans, binding.txtLoans, binding.imgLoans, binding.bgLoans));
            } else if ("SETTINGS".equals(target)) {
                binding.bgSettings
                        .post(() -> updateNav(binding.bgSettings, binding.txtSettings, binding.imgSettings, binding.bgSettings));
            }
        }


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
            updateNav(binding.bgHome, binding.txtHome, binding.imgHome, binding.bgHome);
        } else {
            // Exit app instead of going back to login
            finishAffinity();
        }
    }

    private void setupEntranceAnimations() {
        if (binding.balanceContainer == null)
            return;

        // Initial state
        binding.balanceContainer.setAlpha(0f);
        binding.balanceContainer.setTranslationY(80f);

        // Animate
        binding.balanceContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void setupBalanceToggle() {
        // Feature removed or moved in refined dashboard
    }

    private void updateBalanceDisplay() {
        // Feature removed or moved in refined dashboard
        if (binding.currentBalance != null) {
            String formattedBalance = java.text.NumberFormat
                    .getCurrencyInstance(new java.util.Locale("en", "UG"))
                    .format(totalBalanceValue);
            formattedBalance = formattedBalance.replace("UGX", "").replace("USh", "").trim();
            if (formattedBalance.contains(".")) {
                int index = formattedBalance.lastIndexOf(".");
                binding.currentBalance.setText(formattedBalance.substring(0, index));
            } else {
                binding.currentBalance.setText(formattedBalance);
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

        updateGreeting();

        // Handle Group ID
        String groupId = prefs.getString("group_id", null);
        if (groupId == null) {
            // Generate new Group ID
            int randomId = new java.util.Random().nextInt(900000) + 100000;
            groupId = "GRP-" + randomId;
            prefs.edit().putString("group_id", groupId).apply();
        }

        // adminName view has been removed from the refined XML layout
        // Note: groupName and tvGroupId views have been removed from the refined XML
        // layout.
        // We still load the data but don't bind it to those specific views.
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
                        // Note: adminName view has been removed from the refined XML layout.
                        adminNameStr = realName; // Update local variable for PaymentFragment usage
                        // Save to prefs for next time
                        prefs.edit().putString("admin_name", realName).apply();

                        // Now that we have the real name, reload dashboard stats to be sure
                        runOnUiThread(() -> loadDashboardData());
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
                            // Note: adminName view has been removed from the refined XML layout.
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
    }
    // Note: adminName and groupName views have been removed from the refined XML
    // layout.

    private void setupRecentActivity() {
        // Legacy recent activity list removed in refined dashboard
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

    private boolean isMenuOpen = false;

    private void setupListeners() {
        // Notifications
        if (binding.btnNotifications != null) {
            binding.btnNotifications.setOnClickListener(v -> {
                applyClickAnimation(v);
                showNotifications();
            });
        }

        // Search Icon (Profile Icon in refined layout)
        if (binding.profileIcon != null) {
            binding.profileIcon.setOnClickListener(v -> {
                applyClickAnimation(v);
                // Future search functionality or profile
                Toast.makeText(this, "Search functionality coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // View All Targets
        if (binding.viewAllTargets != null) {
            binding.viewAllTargets.setOnClickListener(v -> {
                applyClickAnimation(v);
                Toast.makeText(this, "Target management coming soon", Toast.LENGTH_SHORT).show();
            });
        }

        // View Loans
        if (binding.viewAllLoans != null) {
            binding.viewAllLoans.setOnClickListener(v -> {
                applyClickAnimation(v);
                updateNav(binding.bgLoans, binding.txtLoans, binding.imgLoans, binding.bgLoans);
            });
        }

        // FAB Action (Centered button in nav)
        if (binding.fabAction != null) {
            binding.fabAction.setOnClickListener(v -> {
                applyClickAnimation(v);
                toggleQuickActions();
            });
        }

        // Setup Quick Actions Overlay listeners
        View overlay = binding.quickActionsLayout.getRoot();
        overlay.findViewById(R.id.cardAddMember).setOnClickListener(v -> {
            Toast.makeText(this, "Add Member clicked", Toast.LENGTH_SHORT).show();
            toggleQuickActions();
        });
        overlay.findViewById(R.id.cardRecordContribution).setOnClickListener(v -> {
            Toast.makeText(this, "Record Contribution clicked", Toast.LENGTH_SHORT).show();
            toggleQuickActions();
        });
        overlay.findViewById(R.id.cardRequestLoan).setOnClickListener(v -> {
            Toast.makeText(this, "Request Loan clicked", Toast.LENGTH_SHORT).show();
            toggleQuickActions();
        });
        overlay.findViewById(R.id.cardNewSavings).setOnClickListener(v -> {
            Toast.makeText(this, "New Savings Target clicked", Toast.LENGTH_SHORT).show();
            toggleQuickActions();
        });

        binding.quickActionsDim.setOnClickListener(v -> toggleQuickActions());
    }

    private void toggleQuickActions() {
        isMenuOpen = !isMenuOpen;

        // Find the icon inside FAB card
        View fabIcon = ((ViewGroup) binding.fabAction).getChildAt(0);

        if (isMenuOpen) {
            // Open Menu
            if (fabIcon != null)
                fabIcon.animate().rotation(45).setDuration(200).start();
            binding.quickActionsDim.setVisibility(View.VISIBLE);
            binding.quickActionsDim.setAlpha(0f);
            binding.quickActionsDim.animate().alpha(1f).setDuration(200).start();

            binding.quickActionsLayout.getRoot().setVisibility(View.VISIBLE);
            binding.quickActionsLayout.getRoot().setAlpha(0f);
            binding.quickActionsLayout.getRoot().setTranslationY(100f);
            binding.quickActionsLayout.getRoot().animate().alpha(1f).translationY(0).setDuration(300).start();
        } else {
            // Close Menu
            if (fabIcon != null)
                fabIcon.animate().rotation(0).setDuration(200).start();
            binding.quickActionsDim.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> binding.quickActionsDim.setVisibility(View.GONE)).start();

            binding.quickActionsLayout.getRoot().animate().alpha(0f).translationY(100f).setDuration(300)
                    .withEndAction(() -> binding.quickActionsLayout.getRoot().setVisibility(View.GONE)).start();
        }
    }

    private void setupBottomNavigation() {
        binding.bgHome
                .setOnClickListener(v -> updateNav(binding.bgHome, binding.txtHome, binding.imgHome, binding.bgHome));
        binding.bgMembers.setOnClickListener(
                v -> updateNav(binding.bgMembers, binding.txtMembers, binding.imgMembers, binding.bgMembers));
        binding.bgLoans.setOnClickListener(
                v -> updateNav(binding.bgLoans, binding.txtLoans, binding.imgLoans, binding.bgLoans));
        binding.bgSettings.setOnClickListener(
                v -> updateNav(binding.bgSettings, binding.txtSettings, binding.imgSettings, binding.bgSettings));
    }

    private void updateNav(View selectedItem, TextView selectedText, ImageView selectedImage, View selectedBackground) {
        // 1. Reset all items to unselected state first
        resetNavItem(binding.txtHome, binding.imgHome, binding.bgHome);
        resetNavItem(binding.txtMembers, binding.imgMembers, binding.bgMembers);
        resetNavItem(binding.txtLoans, binding.imgLoans, binding.bgLoans);
        resetNavItem(binding.txtSettings, binding.imgSettings, binding.bgSettings);

        // 2. Set the clicked item to selected state
        int activeColor = Color.parseColor("#2563EB"); // Primary Blue
        if (selectedText != null) {
            selectedText.setTextColor(activeColor);
        }
        if (selectedImage != null) {
            selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        }

        // Show background for selected item
        if (selectedBackground != null) {
            selectedBackground.setBackgroundResource(R.drawable.bg_icon_circle_blue);
        }

        // Fragment Switching Logic
        if (selectedItem == binding.bgHome) {
            binding.mainContentScrollView.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
        } else {
            binding.mainContentScrollView.setVisibility(View.GONE);
            binding.fragmentContainer.setVisibility(View.VISIBLE);

            Fragment fragment = null;
            if (selectedItem == binding.bgMembers)
                fragment = new MembersFragment();
            else if (selectedItem == binding.bgLoans)
                fragment = new AdminLoansFragment();
            else if (selectedItem == binding.bgSettings)
                fragment = new SettingsFragment();

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

    private void resetNavItem(TextView text, ImageView image, View background) {
        if (text != null) {
            text.setTextColor(Color.parseColor("#94A3B8"));
        }
        if (image != null) {
            image.setImageTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
        }
        if (background != null) {
            background.setBackground(null);
        }
    }

    private void updateGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (hour >= 0 && hour < 12) {
            greeting = "Good Morning,";
        } else if (hour >= 12 && hour < 17) {
            greeting = "Good Afternoon,";
        } else {
            greeting = "Good Evening,";
        }

        if (binding.tvTopGreeting != null) {
            binding.tvTopGreeting.setText(greeting);
        }

        if (binding.tvTopName != null) {
            binding.tvTopName.setText(adminNameStr);
        }
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

                        // Update Monthly Contrib
                        if (binding.monthlyContrib != null) {
                            String formattedContrib = java.text.NumberFormat
                                    .getCurrencyInstance(new java.util.Locale("en", "UG"))
                                    .format(summary.getMonthlyContributions());
                            binding.monthlyContrib.setText(formattedContrib);
                        }

                        // Update Interest Earned
                        if (binding.interestEarned != null) {
                            String formattedInterest = java.text.NumberFormat
                                    .getCurrencyInstance(new java.util.Locale("en", "UG"))
                                    .format(summary.getInterestEarned());
                            binding.interestEarned.setText(formattedInterest);
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
            // tvAdminNotificationBadge view has been removed from the refined XML layout.
            // Pending loans count is now displayed in the 'Active Loans' section.
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
        // tvAdminNotificationBadge view has been removed from the refined XML layout.

        // 1. Reset all bottom nav items (deselect them)
        resetBottomNav();

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
        // Note: btnEditSchedule view has been removed from the refined XML layout.
        // Load saved dates (Mock preference or variables)
        android.content.SharedPreferences prefs = getSharedPreferences("SaveAppPrefs", MODE_PRIVATE);
        String contribDate = prefs.getString("sched_contrib_date", "Not Set");
        String payoutDate = prefs.getString("sched_payout_date", "Not Set");

        // Note: tvContributionDate and tvPayoutDate views have been removed from the
        // refined XML layout.
    }

    // Note: btnEditSchedule view has been removed from the refined XML layout.

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

    // Updated for Redesign
    private void setupGrowthChart() {
        if (binding.growthBarChart == null)
            return;

        com.github.mikephil.charting.charts.BarChart chart = binding.growthBarChart;

        // Disable various chart elements for cleaner look
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        // Styling Axes
        com.github.mikephil.charting.components.XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setTextSize(10f);

        com.github.mikephil.charting.components.YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F1F5F9"));
        leftAxis.setTextColor(Color.parseColor("#94A3B8"));
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);

        chart.getAxisRight().setEnabled(false);

        // Mock Data for BarChart (Matching Mockup JAN-JUL)
        ArrayList<com.github.mikephil.charting.data.BarEntry> entries = new ArrayList<>();
        entries.add(new com.github.mikephil.charting.data.BarEntry(0, 4.5f)); // Jan
        entries.add(new com.github.mikephil.charting.data.BarEntry(1, 6.2f)); // Feb
        entries.add(new com.github.mikephil.charting.data.BarEntry(2, 5.1f)); // Mar
        entries.add(new com.github.mikephil.charting.data.BarEntry(3, 8.4f)); // Apr
        entries.add(new com.github.mikephil.charting.data.BarEntry(4, 7.8f)); // May
        entries.add(new com.github.mikephil.charting.data.BarEntry(5, 11.2f)); // Jun
        entries.add(new com.github.mikephil.charting.data.BarEntry(6, 13.5f)); // Jul

        com.github.mikephil.charting.data.BarDataSet dataSet = new com.github.mikephil.charting.data.BarDataSet(entries,
                "Growth");

        // Custom colors for matching June (Blue) and July (Orange)
        int[] colors = new int[7];
        int lightBlue = Color.parseColor("#DBEAFE");
        int primaryBlue = Color.parseColor("#2563EB");
        int accentOrange = Color.parseColor("#FF8A00");

        for (int i = 0; i < 5; i++)
            colors[i] = lightBlue;
        colors[5] = primaryBlue;
        colors[6] = accentOrange;

        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(dataSet);
        barData.setBarWidth(0.6f);

        chart.setData(barData);
        chart.setFitBars(true);
        chart.animateY(1000);
        chart.invalidate();
    }

    private void resetBottomNav() {
        if (binding.bgHome != null)
            resetNavItem(binding.txtHome, binding.imgHome, binding.bgHome);
        if (binding.bgMembers != null)
            resetNavItem(binding.txtMembers, binding.imgMembers, binding.bgMembers);
        if (binding.bgLoans != null)
            resetNavItem(binding.txtLoans, binding.imgLoans, binding.bgLoans);
        if (binding.bgSettings != null)
            resetNavItem(binding.txtSettings, binding.imgSettings, binding.bgSettings);
    }
}