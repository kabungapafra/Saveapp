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
        }

        // Synchronize Bottom Nav highlighting with Back Stack
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            syncNavUI();
        });

        // Handle Back Press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
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

        binding.navNotifications.setOnClickListener(v -> {
            updateNavHighlight(binding.navNotifications, binding.txtNotifications, binding.imgNotifications);
            showNotifications();
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
            updateNavHighlight(binding.navNotifications, binding.txtNotifications, binding.imgNotifications);
            setBottomNavVisible(true);
        } else if (frag instanceof AnalyticsFragment) {
            updateNavHighlight(binding.navStats, binding.txtStats, binding.imgStats);
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavHighlight(binding.navSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof SupportFragment) {
            // Support Hub is a normal screen — keep nav visible, highlight Settings tab
            updateNavHighlight(binding.navSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else {
            // Immersive chat flow: ConnectingAgent, LiveChat, ChatFeedback, TicketSuccess
            setBottomNavVisible(false);
        }
    }

    private void updateNavHighlight(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        resetAllNavItems();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selectedLayout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.weight = 0;
        selectedLayout.setLayoutParams(params);

        selectedLayout.setBackgroundResource(R.drawable.nav_item_pill_refined);
        selectedText.setVisibility(View.VISIBLE);

        int activeColor = Color.parseColor("#0D47A1");
        selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        selectedText.setTextColor(activeColor);

        if (selectedLayout == binding.navNotifications && binding.dotNotifications != null) {
            binding.dotNotifications.setVisibility(View.GONE);
        }
    }

    private void resetAllNavItems() {
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navNotifications, binding.txtNotifications, binding.imgNotifications);
        resetNavItem(binding.navStats, binding.txtStats, binding.imgStats);
        resetNavItem(binding.navSettings, binding.txtSettings, binding.imgSettings);
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        layout.setLayoutParams(params);
        layout.setBackground(null);
        text.setVisibility(View.GONE);
        image.setImageTintList(ColorStateList.valueOf(Color.WHITE));
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
