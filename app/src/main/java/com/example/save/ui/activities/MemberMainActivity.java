package com.example.save.ui.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberMainBinding;
import com.example.save.ui.fragments.DashboardFragment;
import com.example.save.ui.fragments.AnalyticsFragment;
import com.example.save.ui.fragments.SettingsFragment;
import com.example.save.ui.fragments.SupportFragment;
import com.example.save.ui.fragments.NotificationsFragment;
import com.example.save.ui.fragments.MemberViewFragment;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;
import com.example.save.utils.SessionManager;

public class MemberMainActivity extends AppCompatActivity {

    private ActivityMemberMainBinding binding;
    private MembersViewModel viewModel;
    private boolean isQuickActionsOpen = false;
    private android.animation.ObjectAnimator ringAnimator;

    private long lastBackPressTime = 0;

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
        SessionManager session = SessionManager.getInstance(this);
        if (session.isFirstLogin()) {
            android.content.Intent intent = new android.content.Intent(this, ChangePasswordActivity.class);
            intent.putExtra("member_email", session.getUserEmail());
            intent.putExtra("is_first_login", true);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupBottomNavigation();
        
        // Initial setup - default to Dashboard
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment(), false); // Don't add first fragment to backstack
            updateNavHighlight(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
            
            // Premium Entry Animation for Nav Bar
            binding.navContainer.setTranslationY(200f);
            binding.navAction.setTranslationY(200f);
            binding.navContainer.animate().translationY(0f).setDuration(800).setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
            binding.navAction.animate().translationY(0f).setDuration(800).setInterpolator(new android.view.animation.OvershootInterpolator(1.2f)).start();
        }

        // Synchronize Bottom Nav highlighting with Back Stack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            syncNavUI();
        });

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isQuickActionsOpen) {
                    closeQuickActions();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Logic to handle exit or returning to dashboard
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (!(current instanceof DashboardFragment)) {
                        switchToDashboard();
                    } else {
                        // Double tap to exit logic
                        if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                            finishAffinity();
                        } else {
                            Toast.makeText(MemberMainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
                            lastBackPressTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        });
    }

    private void setupBottomNavigation() {
        binding.navDashboard.setOnClickListener(v -> {
            switchToDashboard();
        });

        binding.navMembers.setOnClickListener(v -> {
            updateNavHighlight(binding.navMembers, binding.txtMembers, binding.imgMembers);
            showMembersSection();
        });

        binding.navAction.setOnClickListener(v -> {
            showQuickActions();
        });

        binding.navStats.setOnClickListener(v -> {
            updateNavHighlight(binding.navStats, binding.txtStats, binding.imgStats);
            String email = SessionManager.getInstance(this).getUserEmail();
            loadFragment(AnalyticsFragment.newInstance(false, email != null ? email : "email@example.com"), true);
        });

        binding.navSettings.setOnClickListener(v -> {
            updateNavHighlight(binding.navSettings, binding.txtSettings, binding.imgSettings);
            loadFragment(new SettingsFragment(), true);
        });
    }

    public void switchToDashboard() {
        // Clear back stack to return to root
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(new DashboardFragment(), false);
        updateNavHighlight(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
    }

    public void showNotifications() {
        loadFragment(new NotificationsFragment(), true);
    }

    public void showMembersSection() {
        loadFragment(new MemberViewFragment(), true);
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

        findViewById(R.id.cardMyStashOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new com.example.save.ui.fragments.StashFragment(), true);
        });

        findViewById(R.id.cardAddMemberOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new com.example.save.ui.fragments.WizardAddMembersInfoFragment(), true);
        });

        findViewById(R.id.cardMakePollsOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new com.example.save.ui.fragments.CreatePollFragment(), true);
        });

        findViewById(R.id.cardPaymentQueueOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new com.example.save.ui.fragments.QueueFragment(), true);
        });

        findViewById(R.id.cardAnalysisOverlay).setOnClickListener(v -> {
            closeQuickActions();
            String email = SessionManager.getInstance(this).getUserEmail();
            loadFragment(com.example.save.ui.fragments.AnalyticsFragment.newInstance(false, email != null ? email : "email@example.com"), true);
        });

        findViewById(R.id.cardApprovalsOverlay).setOnClickListener(v -> {
            closeQuickActions();
            loadFragment(new com.example.save.ui.fragments.ApprovalsFragment(), true);
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

    public void loadFragment(Fragment fragment) {
        loadFragment(fragment, true);
    }

    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragment_container, fragment);
        
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
    }

    private void syncNavUI() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (frag instanceof DashboardFragment) {
            updateNavHighlight(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
            setBottomNavVisible(true);
        } else if (frag instanceof MemberViewFragment) {
            updateNavHighlight(binding.navMembers, binding.txtMembers, binding.imgMembers);
            setBottomNavVisible(true);
        } else if (frag instanceof NotificationsFragment) {
            // Notifications are now a secondary screen, but we still highlight Dashboard/Home
            updateNavHighlight(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
            setBottomNavVisible(true);
        } else if (frag instanceof AnalyticsFragment) {
            updateNavHighlight(binding.navStats, binding.txtStats, binding.imgStats);
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavHighlight(binding.navSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof SupportFragment) {
            // Support Hub is a normal screen — keep nav visible, highlight Profile tab
            updateNavHighlight(binding.navSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else {
            // Immersive chat flow: ConnectingAgent, LiveChat, ChatFeedback, TicketSuccess
            setBottomNavVisible(false);
        }
    }

    private void updateNavHighlight(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        resetAllNavItems();

        int activeColor = Color.parseColor("#FFFFFF"); // Active White
        selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        selectedText.setTextColor(activeColor);

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

    private void resetAllNavItems() {
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navStats, binding.txtStats, binding.imgStats);
        resetNavItem(binding.navSettings, binding.txtSettings, binding.imgSettings);
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        int inactiveColor = Color.parseColor("#B3FFFFFF"); // Muted White
        image.setImageTintList(ColorStateList.valueOf(inactiveColor));
        text.setTextColor(inactiveColor);
        
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

    public void setBottomNavVisible(boolean visible) {
        if (binding != null && binding.navContainer != null) {
            binding.navContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderVisible(boolean visible) {
        // Compatibility stub: Each fragment now manages its own header.
        // This method is kept to fix compilation in existing fragments.
    }
}
