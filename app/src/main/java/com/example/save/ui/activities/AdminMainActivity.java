package com.example.save.ui.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.example.save.ui.fragments.LoanApplicationFragment;
import com.example.save.ui.fragments.LoanSubmittedSuccessFragment;
import com.example.save.ui.fragments.PollsFragment;
import com.example.save.ui.fragments.QueueFragment;
import com.example.save.ui.fragments.SettingsFragment;
import com.example.save.ui.fragments.SavingsFragment;
import com.example.save.ui.fragments.StashFragment;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;

public class AdminMainActivity extends BaseActivity {

    private ActivityAdminmainBinding binding;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.example.save.utils.ThemeUtils.applyTheme(this, "admin");
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.dashboard_bg)));

        MembersViewModel viewModel = new ViewModelProvider(this).get(MembersViewModel.class);
        viewModel.syncMembers();

        // Initialize Helpers
        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);

        // Security Check: If it's the first login, force password change redirect
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(this);
        session.saveBackgroundTime(0);
        if (session.isFirstLogin()) {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            intent.putExtra("phone", session.getUserPhone());
            intent.putExtra("is_first_login", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupBottomNavigation();

        // Re-hydrate group settings from the server-persisted config so admin-set
        // rules survive a reinstall (local ChamaPrefs are wiped on uninstall, but
        // the SystemConfig row is durable server-side). Repopulates ChamaPrefs.
        com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                .fetchSystemConfig(null);

        // Initial setup
        if (savedInstanceState == null) {
            String startFragClass = getIntent().getStringExtra("start_fragment");
            if (startFragClass != null) {
                try {
                    Fragment frag = (Fragment) Class.forName(startFragClass).newInstance();
                    loadFragment(frag, false);
                } catch (Exception e) {
                    loadFragment(new AdminDashboardFragment(), false);
                }
            } else {
                loadFragment(new AdminDashboardFragment(), false);
            }
            
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);

        }

        // Sync Nav UI on back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(this::syncNavUI);

        // Premium Back Handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
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
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.brand_blue);
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        // Only enabled when the dashboard is active; disabled for all other fragments
        // to prevent scroll interception on scrollable screens like GroupSettings.
        binding.swipeRefreshLayout.setEnabled(true);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.postDelayed(() -> {
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Dashboard updated", Toast.LENGTH_SHORT).show();
            }, 2000);
        });
    }

    private void setupBottomNavigation() {
        binding.navDashboard.setOnClickListener(v -> switchToDashboard());
        binding.navSavings.setOnClickListener(v -> switchToTab(SavingsFragment.newInstance()));
        binding.navLoans.setOnClickListener(v -> switchToTab(new AdminLoansFragment()));
        binding.navPayouts.setOnClickListener(v -> switchToTab(new QueueFragment()));
        binding.navSettings.setOnClickListener(v -> switchToTab(new SettingsFragment()));
    }

    private void switchToTab(Fragment fragment) {
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(fragment, false);
        syncNavUI();
    }

    public void switchToDashboard() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(new AdminDashboardFragment(), false);
        updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);
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
        getSupportFragmentManager().executePendingTransactions();
    }

    private void syncNavUI() {
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        boolean isDashboard = frag instanceof AdminDashboardFragment;
        binding.swipeRefreshLayout.setEnabled(isDashboard);
        if (frag instanceof AdminDashboardFragment) {
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);
            setBottomNavVisible(true);
        } else if (frag instanceof SavingsFragment) {
            updateNavUI(binding.navSavings, binding.txtSavings, binding.imgSavings, binding.pillSavings);
            setBottomNavVisible(true);
        } else if (frag instanceof AdminLoansFragment || frag instanceof LoanApplicationFragment) {
            updateNavUI(binding.navLoans, binding.txtLoans, binding.imgLoans, binding.pillLoans);
            setBottomNavVisible(true);
        } else if (frag instanceof QueueFragment) {
            updateNavUI(binding.navPayouts, binding.txtPayouts, binding.imgPayouts, binding.pillPayouts);
            setBottomNavVisible(true);
        } else if (frag instanceof AnalyticsFragment) {
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavUI(binding.navSettings, binding.txtSettings, binding.imgSettings, binding.pillSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof StashFragment || frag instanceof PollsFragment) {
            setBottomNavVisible(false);
        } else if (frag instanceof com.example.save.ui.fragments.SupportFragment) {
            updateNavUI(binding.navSettings, binding.txtSettings, binding.imgSettings, binding.pillSettings);
            setBottomNavVisible(true);
        } else {
            // Immersive chat flow only: ConnectingAgent, LiveChat, ChatFeedback, TicketSuccess
            setBottomNavVisible(false);
        }
    }

    private void updateNavUI(LinearLayout selectedLayout, TextView selectedText,
                             ImageView selectedImage, LinearLayout selectedPill) {
        resetAllNavItems();
        if (selectedPill != null) selectedPill.setBackgroundResource(R.drawable.bg_nav_pill_active);
        if (selectedImage != null) selectedImage.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        if (selectedText != null) {
            selectedText.setVisibility(View.VISIBLE);
            selectedText.setTextColor(Color.WHITE);
        }
    }

    private void resetAllNavItems() {
        resetNavItem(binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);
        resetNavItem(binding.txtSavings,   binding.imgSavings,   binding.pillSavings);
        resetNavItem(binding.txtLoans,     binding.imgLoans,     binding.pillLoans);
        resetNavItem(binding.txtPayouts,   binding.imgPayouts,   binding.pillPayouts);
        resetNavItem(binding.txtSettings,  binding.imgSettings,  binding.pillSettings);
    }

    private void resetNavItem(TextView text, ImageView image, LinearLayout pill) {
        int inactiveColor = Color.parseColor("#9CA3AF");
        if (pill != null) pill.setBackground(null);
        if (image != null) image.setImageTintList(ColorStateList.valueOf(inactiveColor));
        if (text != null) text.setVisibility(View.GONE);
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
        if (binding != null && binding.navContainer != null) {
            binding.navContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderVisible(boolean visible) {
        // Admin header is currently integrated into fragment layouts or not explicitly shared
    }
}