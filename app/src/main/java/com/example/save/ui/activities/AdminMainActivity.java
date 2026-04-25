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
    private boolean isQuickActionsOpen = false;
    private android.animation.ObjectAnimator ringAnimator;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.example.save.utils.ThemeUtils.applyTheme(this);
        getWindow().setBackgroundDrawableResource(R.color.dashboard_bg);

        MembersViewModel viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        // Initialize Helpers
        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);

        setupBottomNavigation();
        
        // Initial setup - default to Dashboard
        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment(), false);
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
            
            // Premium Entry Animation for Nav Bar
            binding.navContainer.setTranslationY(200f);
            binding.navAction.setTranslationY(200f);
            binding.navContainer.animate().translationY(0f).setDuration(800).setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
            binding.navAction.animate().translationY(0f).setDuration(800).setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
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

        setupRefreshLayout();
    }

    private void setupRefreshLayout() {
        binding.swipeRefreshLayout.setColorSchemeColors(Color.TRANSPARENT);
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT);
        
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            // Start spinning the dotted circle
            startRingSpinning();
            
            // Simulate refresh logic
            binding.swipeRefreshLayout.postDelayed(() -> {
                binding.swipeRefreshLayout.setRefreshing(false);
                stopRingSpinning();
                Toast.makeText(this, "Dashboard updated", Toast.LENGTH_SHORT).show();
            }, 2000);
        });
    }

    private void startRingSpinning() {
        if (ringAnimator != null && ringAnimator.isRunning()) return;
        
        View ring = findViewById(R.id.navActionDashedRing);
        ringAnimator = android.animation.ObjectAnimator.ofFloat(ring, "rotation", 0f, 360f);
        ringAnimator.setDuration(1000);
        ringAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        ringAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        ringAnimator.start();
    }

    private void stopRingSpinning() {
        if (ringAnimator != null && !isQuickActionsOpen) {
            ringAnimator.cancel();
            findViewById(R.id.navActionDashedRing).animate()
                .rotation(0f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }
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
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment);
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
        if (selectedImage != null) {
            selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));

            // Premium Spring-Loaded "Floating Pop" Animation
            selectedImage.animate()
                .translationY(-12f)
                .scaleX(1.3f)
                .scaleY(1.3f)
                .rotation(15f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.OvershootInterpolator(2.0f))
                .withEndAction(() -> {
                    selectedImage.animate()
                        .rotation(0f)
                        .setDuration(200)
                        .start();
                }).start();
        }
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
        if (image != null) {
            image.setImageTintList(ColorStateList.valueOf(inactiveColor));
            
            // Reset translation, scale and rotation with a smooth snap back
            image.animate()
                .translationY(0f)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .rotation(0f)
                .setDuration(300)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }
    }

    public void showQuickActions() {
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
        
        // Update Theme Label
        TextView themeLabel = overlay.findViewById(R.id.tvThemeLabelOverlay);
        if (themeLabel != null) {
            themeLabel.setText(com.example.save.utils.ThemeUtils.isDarkMode(this) ? "Switch to Light Mode" : "Switch to Dark Mode");
        }

        // Animate FAB ring rotation
        View ring = findViewById(R.id.navActionDashedRing);
        ringAnimator = android.animation.ObjectAnimator.ofFloat(ring, "rotation", 0f, 360f);
        ringAnimator.setDuration(3000);
        ringAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        ringAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        ringAnimator.start();

        // Staggered Cascade Animation for cards
        android.view.ViewGroup container = findViewById(R.id.cardContainer);
        for (int i = 0; i < container.getChildCount(); i++) {
            android.view.View child = container.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(50f);
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(100 + (i * 100))
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
        }

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

        // Stop ring animation
        if (ringAnimator != null) {
            ringAnimator.cancel();
            findViewById(R.id.navActionDashedRing).animate().rotation(0f).setDuration(300).start();
        }

        // Rotate X back to Plus
        findViewById(R.id.navActionPlusIcon).animate().rotation(0f).setDuration(300).start();

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