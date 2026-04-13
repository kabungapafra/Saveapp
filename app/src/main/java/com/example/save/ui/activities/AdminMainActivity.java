package com.example.save.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
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
    private long lastBackPressTime = 0;
    private boolean isMenuOpen = false;

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
        setupQuickActions();
        
        // Initial setup - default to Dashboard
        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment(), false);
            updateNavUI(binding.bgHome, binding.txtHome, binding.imgHome);
        }

        // Sync Nav UI on back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(this::syncNavUI);

        // Premium Back Handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
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
        binding.bgHome.setOnClickListener(v -> switchToDashboard());
        binding.bgMembers.setOnClickListener(v -> loadFragment(new MembersFragment(), true));
        binding.bgLoans.setOnClickListener(v -> loadFragment(new AdminLoansFragment(), true));
        binding.bgSettings.setOnClickListener(v -> loadFragment(new SettingsFragment(), true));
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
            updateNavUI(binding.bgHome, binding.txtHome, binding.imgHome);
            setBottomNavVisible(true);
        } else if (frag instanceof MembersFragment) {
            updateNavUI(binding.bgMembers, binding.txtMembers, binding.imgMembers);
            setBottomNavVisible(true);
        } else if (frag instanceof AdminLoansFragment) {
            updateNavUI(binding.bgLoans, binding.txtLoans, binding.imgLoans);
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavUI(binding.bgSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof com.example.save.ui.fragments.SupportFragment) {
            // Support Hub is a normal screen — show nav, highlight Settings
            updateNavUI(binding.bgSettings, binding.txtSettings, binding.imgSettings);
            setBottomNavVisible(true);
        } else {
            // Immersive chat flow only: ConnectingAgent, LiveChat, ChatFeedback, TicketSuccess
            setBottomNavVisible(false);
        }
    }

    private void updateNavUI(View container, TextView text, ImageView image) {
        resetAllNavItems();
        int activeColor = Color.parseColor("#2563EB");
        if (text != null) {
            text.setVisibility(View.VISIBLE);
            text.setTextColor(activeColor);
        }
        if (image != null) image.setImageTintList(ColorStateList.valueOf(activeColor));
        if (container != null) container.setBackgroundResource(R.drawable.bg_icon_circle_blue);
    }

    private void resetAllNavItems() {
        resetNavItem(binding.bgHome, binding.txtHome, binding.imgHome);
        resetNavItem(binding.bgMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.bgLoans, binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.bgSettings, binding.txtSettings, binding.imgSettings);
    }

    private void resetNavItem(View container, TextView text, ImageView image) {
        if (text != null) {
            text.setVisibility(View.GONE);
            text.setTextColor(Color.parseColor("#94A3B8"));
        }
        if (image != null) image.setImageTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
        if (container != null) container.setBackground(null);
    }

    private void setupQuickActions() {
        binding.fabAction.setOnClickListener(v -> toggleQuickActions());
        binding.quickActionsDim.setOnClickListener(v -> toggleQuickActions());
        
        View overlay = binding.quickActionsLayout.getRoot();
        overlay.findViewById(R.id.btnClose).setOnClickListener(v -> toggleQuickActions());
        overlay.findViewById(R.id.cardAddMember).setOnClickListener(v -> {
            toggleQuickActions();
            Bundle args = new Bundle();
            args.putBoolean("SHOW_ADD_DIALOG", true);
            MembersFragment fragment = new MembersFragment();
            fragment.setArguments(args);
            loadFragment(fragment, true);
        });
        overlay.findViewById(R.id.cardAnalysis).setOnClickListener(v -> {
            toggleQuickActions();
            loadFragment(AnalyticsFragment.newInstance(true), true);
        });
        overlay.findViewById(R.id.cardMakePolls).setOnClickListener(v -> {
            toggleQuickActions();
            loadFragment(PollsFragment.newInstance(), true);
        });
        overlay.findViewById(R.id.cardPaymentQueue).setOnClickListener(v -> {
            toggleQuickActions();
            loadFragment(new QueueFragment(), true);
        });
        overlay.findViewById(R.id.cardMyStash).setOnClickListener(v -> {
            toggleQuickActions();
            loadFragment(StashFragment.newInstance(), true);
        });
        overlay.findViewById(R.id.cardApprovals).setOnClickListener(v -> {
            toggleQuickActions();
            loadFragment(new ApprovalsFragment(), true);
        });
    }

    private void toggleQuickActions() {
        isMenuOpen = !isMenuOpen;
        View fabIcon = ((ViewGroup) binding.fabAction).getChildAt(0);
        if (isMenuOpen) {
            if (fabIcon != null) fabIcon.animate().rotation(45).setDuration(200).start();
            binding.quickActionsDim.setVisibility(View.VISIBLE);
            binding.quickActionsDim.setAlpha(0f);
            binding.quickActionsDim.animate().alpha(1f).setDuration(200).start();
            binding.quickActionsLayout.getRoot().setVisibility(View.VISIBLE);
            binding.quickActionsLayout.getRoot().setAlpha(0f);
            binding.quickActionsLayout.getRoot().setScaleX(0.9f);
            binding.quickActionsLayout.getRoot().setScaleY(0.9f);
            binding.quickActionsLayout.getRoot().animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300).start();
        } else {
            if (fabIcon != null) fabIcon.animate().rotation(0).setDuration(200).start();
            binding.quickActionsDim.animate().alpha(0f).setDuration(200).withEndAction(() -> binding.quickActionsDim.setVisibility(View.GONE)).start();
            binding.quickActionsLayout.getRoot().animate().alpha(0f).scaleX(0.9f).scaleY(0.9f).setDuration(300).withEndAction(() -> binding.quickActionsLayout.getRoot().setVisibility(View.GONE)).start();
        }
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
            if (binding.bottomNavWrapper != null) {
                binding.bottomNavWrapper.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (binding.fabAction != null) {
                binding.fabAction.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void setHeaderVisible(boolean visible) {
        // Admin header is currently integrated into fragment layouts or not explicitly shared
    }
}