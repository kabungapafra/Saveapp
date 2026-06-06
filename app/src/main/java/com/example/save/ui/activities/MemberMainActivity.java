package com.example.save.ui.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberMainBinding;
import com.example.save.ui.fragments.DashboardFragment;
import com.example.save.ui.fragments.NotificationsFragment;
import com.example.save.ui.fragments.SettingsFragment;
import com.example.save.ui.fragments.MembersFragment;
import com.example.save.ui.fragments.AnalyticsFragment;
import com.example.save.ui.fragments.PollsFragment;
import com.example.save.ui.fragments.QueueFragment;
import com.example.save.ui.fragments.StashFragment;
import com.example.save.ui.fragments.LoanApplicationFragment;
import com.example.save.ui.fragments.LoansFragment;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;
import com.example.save.utils.SessionManager;

public class MemberMainActivity extends BaseActivity {

    private ActivityMemberMainBinding binding;
    private boolean isQuickActionsOpen = false;
    private android.animation.ObjectAnimator ringAnimator;
    private long lastBackPressTime = 0;
    private boolean shouldReopenQuickActions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.example.save.utils.ThemeUtils.applyTheme(this, "member");
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.dashboard_bg)));

        MembersViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);
        viewModel.syncMembers();

        // Initialize Notifications
        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);

        // Security Check: If it's the first login, force password change redirect
        SessionManager session = SessionManager.getInstance(this);
        if (session.isFirstLogin()) {
            android.content.Intent intent = new android.content.Intent(this, ResetPasswordActivity.class);
            intent.putExtra("email", session.getUserPhone()); // Keep 'email' extra key for compatibility or fix if used as phone
            intent.putExtra("phone", session.getUserPhone());
            intent.putExtra("is_first_login", true);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupBottomNavigation();
        
        // Initial setup - default back to original Member Dashboard
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment(), false);
            updateNavHighlight(binding.txtDashboard, binding.imgDashboard);
            
            // Premium Entry Animation for Floating Nav Bar
            binding.navContainer.setTranslationY(300f);
            binding.navContainer.animate()
                .translationY(0f)
                .setDuration(1000)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.0f))
                .start();
        }

        // Synchronize Bottom Nav highlighting with Back Stack
        getSupportFragmentManager().addOnBackStackChangedListener(this::syncNavUI);

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isQuickActionsOpen) {
                    hideQuickActions();
                } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                    if (shouldReopenQuickActions) {
                        shouldReopenQuickActions = false;
                        showQuickActions();
                    }
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

        setupRefreshLayout();
    }

    private void setupRefreshLayout() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.brand_blue);
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            startRingSpinning();

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
            View ring = findViewById(R.id.navActionDashedRing);
            if (ring != null) {
                ring.animate()
                    .rotation(0f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
            }
        }
    }

    private void setupBottomNavigation() {
        binding.navDashboard.setOnClickListener(v -> switchToDashboard());

        // Make Payment navigation replaces Loans
        binding.navLoans.setOnClickListener(v -> {
            updateNavHighlight(binding.txtLoans, binding.imgLoans);
            // Open Stash (Make Payments) instead of Loans
            loadFragment(StashFragment.newInstance(), true);
        });

        binding.navAction.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (isQuickActionsOpen) hideQuickActions();
            else showQuickActions();
        });

        binding.navPayouts.setOnClickListener(v -> {
            updateNavHighlight(binding.txtPayouts, binding.imgPayouts);
            loadFragment(new QueueFragment(), true);
        });

        binding.navSettings.setOnClickListener(v -> {
            updateNavHighlight(binding.txtSettings, binding.imgSettings);
            loadFragment(new SettingsFragment(), true);
        });

        // Setup Quick Actions
        View overlay = findViewById(R.id.quickActionsOverlay);
        if (overlay != null) {
            overlay.findViewById(R.id.btnCloseOverlay).setOnClickListener(v -> hideQuickActions());
            overlay.findViewById(R.id.quickActionsDim).setOnClickListener(v -> hideQuickActions());

            // Members action
            View actionMembers = overlay.findViewById(R.id.cardMembersOverlay);
            if (actionMembers != null) {
                actionMembers.setOnClickListener(v -> {
                    shouldReopenQuickActions = true;
                    hideQuickActions();
                    loadFragment(new MembersFragment(), true);
                });
            }

            // Make Polls action (new)
            View actionPolls = overlay.findViewById(R.id.cardMakePollsOverlay);
            if (actionPolls != null) {
                actionPolls.setOnClickListener(v -> {
                    shouldReopenQuickActions = true;
                    hideQuickActions();
                    loadFragment(new PollsFragment(), true);
                });
            }

            // Hide Make Payment (Stash) action as it's removed
            View actionPay = overlay.findViewById(R.id.cardMyStashOverlay);
            if (actionPay != null) {
                actionPay.setVisibility(View.GONE);
            }

            // Hide other actions not intended for Members
            View actionAnalysis = overlay.findViewById(R.id.cardAnalysisOverlay);
            View actionApprovals = overlay.findViewById(R.id.cardApprovalsOverlay);
            if (actionAnalysis != null) actionAnalysis.setVisibility(View.GONE);
            if (actionApprovals != null) actionApprovals.setVisibility(View.GONE);
        }
    }

    public void showQuickActions() {
        isQuickActionsOpen = true;
        View overlayRoot = binding.quickActionsOverlay.getRoot();
        overlayRoot.setVisibility(View.VISIBLE);
        overlayRoot.setAlpha(0f);
        overlayRoot.animate().alpha(1f).setDuration(400).start();

        // Rotate Plus icon (45° to turn into X)
        findViewById(R.id.navActionPlusIcon).animate()
                .rotation(45f).setDuration(300).start();

        // Spin the same icon (the Save logo inside the nav circle) a full 360°
        ImageView navLogo = findViewById(R.id.navActionPlusIcon);
        if (navLogo != null) {
            navLogo.animate()
                    .rotationBy(360f)   // full spin on open
                    .setDuration(500)
                    .start();
        }

        // Animate FAB ring rotation (Dashed circle)
        View ring = findViewById(R.id.navActionDashedRing);
        ringAnimator = android.animation.ObjectAnimator.ofFloat(ring, "rotation", 0f, 360f);
        ringAnimator.setDuration(4000);
        ringAnimator.setRepeatCount(android.animation.ObjectAnimator.INFINITE);
        ringAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        ringAnimator.start();

        // Staggered Cascade Animation for action cards (only visible ones)
        android.view.ViewGroup cardContainer = overlayRoot.findViewById(R.id.cardContainer);
        if (cardContainer != null) {
            int visibleIndex = 0;
            for (int i = 0; i < cardContainer.getChildCount(); i++) {
                View child = cardContainer.getChildAt(i);
                if (child.getVisibility() == View.VISIBLE) {
                    child.setAlpha(0f);
                    child.setTranslationY(60f);
                    child.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setStartDelay(150 + (visibleIndex * 80L))
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
                    visibleIndex++;
                }
            }
        }
    }

    public void hideQuickActions() {
        isQuickActionsOpen = false;

        // Stop ring animation
        if (ringAnimator != null) {
            ringAnimator.cancel();
            findViewById(R.id.navActionDashedRing).animate().rotation(0f).setDuration(300).start();
        }

        // Rotate plus back
        findViewById(R.id.navActionPlusIcon).animate().rotation(0f).setDuration(300).start();

        // Spin the logo (the same icon inside the nav circle) 360° when closing quick actions (returns to original orientation)
        ImageView navLogo = findViewById(R.id.navActionPlusIcon);
        if (navLogo != null) {
            navLogo.animate()
                    .rotationBy(360f)   // full spin back
                    .setDuration(500)
                    .start();
        }

        View overlayRoot = binding.quickActionsOverlay.getRoot();
        overlayRoot.animate().alpha(0f).setDuration(300).withEndAction(() -> overlayRoot.setVisibility(View.GONE)).start();
    }

    public void switchToDashboard() {
        // Clear back stack to return to root
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(new DashboardFragment(), false);
        updateNavHighlight(binding.txtDashboard, binding.imgDashboard);
    }

    public void showNotifications() {
        loadFragment(new NotificationsFragment(), true);
    }

    public void showMembersSection() {
        loadFragment(new MembersFragment(), true);
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
        getSupportFragmentManager().executePendingTransactions();
    }

    private void syncNavUI() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (frag instanceof DashboardFragment) {
            updateNavHighlight(binding.txtDashboard, binding.imgDashboard);
            setBottomNavVisible(true);
        } else if (frag instanceof StashFragment) {
            // Highlight Make Payment (previously Loans) when Stash is displayed
            updateNavHighlight(binding.txtLoans, binding.imgLoans);
            setBottomNavVisible(true);
        } else if (frag instanceof LoanApplicationFragment) {
            updateNavHighlight(binding.txtLoans, binding.imgLoans);
            setBottomNavVisible(true);
        } else if (frag instanceof QueueFragment) {
            updateNavHighlight(binding.txtPayouts, binding.imgPayouts);
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavHighlight(binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof StashFragment || frag instanceof PollsFragment || frag instanceof AnalyticsFragment) {
            setBottomNavVisible(false);
        } else {
            setBottomNavVisible(false); // Hidden for truly immersive screens (e.g. Success screens)
        }
    }

    private void updateNavHighlight(TextView selectedText, ImageView selectedImage) {
        resetAllNavItems();

        // White highlighting for the Blue Admin-style bar
        selectedImage.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        selectedText.setTextColor(Color.WHITE);
        selectedText.setAlpha(1.0f);
        selectedImage.setAlpha(1.0f);

        // Premium Spring-Loaded "Floating Pop" Animation (Admin Parity)
        selectedImage.animate()
            .translationY(-12f)
            .scaleX(1.3f)
            .scaleY(1.3f)
            .rotation(15f)
            .setDuration(400)
            .setInterpolator(new android.view.animation.OvershootInterpolator(2.0f))
            .withEndAction(() -> selectedImage.animate()
                .rotation(0f)
                .setDuration(200)
                .start()).start();
    }

    private void resetAllNavItems() {
        resetNavItem(binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.txtPayouts, binding.imgPayouts);
        resetNavItem(binding.txtSettings, binding.imgSettings);
    }

    private void resetNavItem(TextView text, ImageView image) {
        // Semi-transparent white for inactive state on Blue bar
        int inactiveColor = Color.parseColor("#B3FFFFFF"); 
        image.setImageTintList(ColorStateList.valueOf(inactiveColor));
        text.setTextColor(inactiveColor);
        text.setAlpha(0.7f);
        image.setAlpha(0.7f);
        
        // Smooth snap back
        image.animate()
            .translationY(0f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .rotation(0f)
            .setDuration(300)
            .setInterpolator(new android.view.animation.DecelerateInterpolator())
            .start();
    }

    private void applyClickAnimation(View v) {
        if (v != null) {
            v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim_press));
        }
    }

    public void setBottomNavVisible(boolean visible) {
        if (binding != null) {
            binding.navContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
            binding.navAction.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderVisible() {
        // Compatibility stub: Each fragment now manages its own header.
        // This method is kept to fix compilation in existing fragments.
    }
}
