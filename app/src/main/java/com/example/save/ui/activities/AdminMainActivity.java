package com.example.save.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminmainBinding;
import com.example.save.ui.fragments.AdminDashboardFragment;
import com.example.save.ui.fragments.AdminLoansFragment;
import com.example.save.ui.fragments.AnalyticsFragment;
import com.example.save.ui.fragments.ApprovalsFragment;
import com.example.save.ui.fragments.MembersFragment;
import com.example.save.ui.fragments.NotificationsFragment;
import com.example.save.ui.fragments.PollsFragment;
import com.example.save.ui.fragments.QueueFragment;
import com.example.save.ui.fragments.SettingsFragment;
import com.example.save.ui.fragments.StashFragment;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;

public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminmainBinding binding;
    private MembersViewModel viewModel;
    private boolean isQuickActionsOpen = false;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setBackgroundDrawableResource(R.color.dashboard_bg);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        // Initialize Helpers
        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);

        setupBottomNavigation();
        
        // Initial setup - default to Dashboard
        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment(), false);
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        }

        // Sync Nav UI on back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(this::syncNavUI);

        // Premium Back Handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isQuickActionsOpen) {
                    closeQuickActions();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (!(current instanceof AdminDashboardFragment)) {
                        switchToDashboard();
                    } else {
                        // Double tap to exit logic
                        if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                            finishAffinity();
                        } else {
                            Toast.makeText(AdminMainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                            lastBackPressTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        });

        // Handle External Navigation
        handleIntentExtras(getIntent());
    }

    private void setupBottomNavigation() {
        binding.navDashboard.setOnClickListener(v -> switchToDashboard());
        binding.navMembers.setOnClickListener(v -> loadFragment(new MembersFragment(), true));
        binding.navLoans.setOnClickListener(v -> loadFragment(new AdminLoansFragment(), true));
        binding.navSettings.setOnClickListener(v -> loadFragment(new SettingsFragment(), true));
        binding.navAction.setOnClickListener(v -> showQuickActions());
    }

    public void switchToDashboard() {
        // Pop everything to return to clean root
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(new AdminDashboardFragment(), false);
    }

    public void showNotifications() {
        loadFragment(NotificationsFragment.newInstance(true), true);
    }

    public void loadFragment(Fragment fragment) {
        loadFragment(fragment, true);
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    private void syncNavUI() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (frag instanceof AdminDashboardFragment) {
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
            setBottomNavVisible(true);
        } else if (frag instanceof MembersFragment) {
            updateNavUI(binding.navMembers, binding.txtMembers, binding.imgMembers);
            setBottomNavVisible(true);
        } else if (frag instanceof AdminLoansFragment) {
            updateNavUI(binding.navLoans, binding.txtLoans, binding.imgLoans);
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavUI(binding.navSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof com.example.save.ui.fragments.SupportFragment) {
            // Support Hub is a normal screen — show nav, highlight Settings
            updateNavUI(binding.navSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else {
            // Immersive chat flow only: ConnectingAgent, LiveChat, ChatFeedback, TicketSuccess
            setBottomNavVisible(false);
        }
    }

    private void updateNavUI(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        resetAllNavItems();
        int activeColor = Color.parseColor("#FFFFFF"); // Active White
        if (selectedText != null) {
            selectedText.setTextColor(activeColor);
        }
        if (selectedImage != null) selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
    }

    private void resetAllNavItems() {
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navLoans, binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.navSettings, binding.txtSettings, binding.imgSettings);
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        int inactiveColor = Color.parseColor("#B3FFFFFF"); // Muted White
        if (text != null) {
            text.setTextColor(inactiveColor);
        }
        if (image != null) image.setImageTintList(ColorStateList.valueOf(inactiveColor));
    }

    private void showQuickActions() {
        if (isQuickActionsOpen) {
            closeQuickActions();
        } else {
            openQuickActions();
        }
    }

    private void openQuickActions() {
        isQuickActionsOpen = true;
        View overlay = findViewById(R.id.quickActionsOverlay);
        overlay.setVisibility(View.VISIBLE);
        overlay.setAlpha(0f);
        overlay.animate().alpha(1f).setDuration(300).start();

        // Setup listeners if not already done (or just once)
        findViewById(R.id.btnCloseOverlay).setOnClickListener(v -> closeQuickActions());
        findViewById(R.id.quickActionsDim).setOnClickListener(v -> closeQuickActions());

        findViewById(R.id.cardAddMemberOverlay).setOnClickListener(v -> {
            closeQuickActions();
            Bundle args = new Bundle();
            args.putBoolean("SHOW_ADD_DIALOG", true);
            MembersFragment fragment = new MembersFragment();
            fragment.setArguments(args);
            loadFragment(fragment, true);
        });
        findViewById(R.id.cardAnalysisOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(AnalyticsFragment.newInstance(true), true);
        });
        findViewById(R.id.cardMakePollsOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(PollsFragment.newInstance(), true);
        });
        findViewById(R.id.cardPaymentQueueOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new QueueFragment(), true);
        });
        findViewById(R.id.cardMyStashOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(StashFragment.newInstance(), true);
        });
        findViewById(R.id.cardApprovalsOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new ApprovalsFragment(), true);
        });
    }

    private void closeQuickActions() {
        isQuickActionsOpen = false;
        View overlay = findViewById(R.id.quickActionsOverlay);
        overlay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            overlay.setVisibility(View.GONE);
        }).start();
    }

    private void handleIntentExtras(Intent intent) {
        if (intent != null && intent.hasExtra("NAVIGATE_TO")) {
            String target = intent.getStringExtra("NAVIGATE_TO");
            if ("MEMBERS".equals(target)) loadFragment(new MembersFragment(), true);
            else if ("LOANS".equals(target)) loadFragment(new AdminLoansFragment(), true);
            else if ("SETTINGS".equals(target)) loadFragment(new SettingsFragment(), true);
        }
    }

    public void setBottomNavVisible(boolean visible) {
        if (binding != null) {
            if (binding.navContainer != null) {
                binding.navContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (binding.navAction != null) {
                binding.navAction.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void setHeaderVisible(boolean visible) {
        // Admin header is currently integrated into fragment layouts or not explicitly shared
    }
}