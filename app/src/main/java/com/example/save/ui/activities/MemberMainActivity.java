package com.example.save.ui.activities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberMainBinding;
import com.example.save.ui.fragments.AdminDashboardFragment;
import com.example.save.ui.fragments.DashboardFragment;
import com.example.save.ui.fragments.NotificationsFragment;
import com.example.save.ui.fragments.SettingsFragment;
import com.example.save.ui.fragments.MembersFragment;
import com.example.save.ui.fragments.AnalyticsFragment;
import com.example.save.ui.fragments.PollsFragment;
import com.example.save.ui.fragments.QueueFragment;
import com.example.save.ui.fragments.SavingsFragment;
import com.example.save.ui.fragments.LoansFragment;
import com.example.save.ui.fragments.LoanApplicationFragment;
import com.example.save.ui.fragments.StashFragment;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.NotificationHelper;
import com.example.save.utils.PermissionUtils;
import com.example.save.utils.SessionManager;

public class MemberMainActivity extends BaseActivity {

    private ActivityMemberMainBinding binding;
    private long lastBackPressTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.example.save.utils.ThemeUtils.applyTheme(this, "member");
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(this, R.color.dashboard_bg)));

        MembersViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);
        viewModel.syncMembers();

        new NotificationHelper(this);
        PermissionUtils.requestNotificationPermission(this);
        com.example.save.services.SaveFirebaseMessagingService
                .registerTokenWithServer(getApplicationContext());

        SessionManager session = SessionManager.getInstance(this);
        session.saveBackgroundTime(0);
        if (session.isFirstLogin()) {
            android.content.Intent intent = new android.content.Intent(this, ResetPasswordActivity.class);
            intent.putExtra("email", session.getUserPhone());
            intent.putExtra("phone", session.getUserPhone());
            intent.putExtra("is_first_login", true);
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupBottomNavigation();

        com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                .fetchSystemConfig(null);

        if (savedInstanceState == null) {
            loadFragment(AdminDashboardFragment.newInstance(false), false);
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this::syncNavUI);

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStackImmediate();
                } else {
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (!(current instanceof AdminDashboardFragment)) {
                        switchToDashboard();
                    } else {
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
            binding.swipeRefreshLayout.postDelayed(() -> {
                binding.swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(this, "Dashboard updated", Toast.LENGTH_SHORT).show();
            }, 2000);
        });
    }

    private void setupBottomNavigation() {
        binding.navDashboard.setOnClickListener(v -> switchToDashboard());
        binding.navLoans.setOnClickListener(v -> switchToTab(SavingsFragment.newInstance()));
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
        loadFragment(AdminDashboardFragment.newInstance(false), false);
        updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);
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
        boolean isDashboard = frag instanceof AdminDashboardFragment;
        binding.swipeRefreshLayout.setEnabled(isDashboard);
        if (frag instanceof AdminDashboardFragment) {
            updateNavUI(binding.navDashboard, binding.txtDashboard, binding.imgDashboard, binding.pillDashboard);
            setBottomNavVisible(true);
        } else if (frag instanceof SavingsFragment) {
            updateNavUI(binding.navLoans, binding.txtLoans, binding.imgLoans, binding.pillLoans);
            setBottomNavVisible(true);
        } else if (frag instanceof QueueFragment) {
            updateNavUI(binding.navPayouts, binding.txtPayouts, binding.imgPayouts, binding.pillPayouts);
            setBottomNavVisible(true);
        } else if (frag instanceof SettingsFragment) {
            updateNavUI(binding.navSettings, binding.txtSettings, binding.imgSettings, binding.pillSettings);
            setBottomNavVisible(true);
        } else if (frag instanceof PollsFragment || frag instanceof AnalyticsFragment) {
            setBottomNavVisible(false);
        } else {
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
        resetNavItem(binding.txtLoans,     binding.imgLoans,     binding.pillLoans);
        resetNavItem(binding.txtSavings,   binding.imgSavings,   binding.pillSavings);
        resetNavItem(binding.txtPayouts,   binding.imgPayouts,   binding.pillPayouts);
        resetNavItem(binding.txtSettings,  binding.imgSettings,  binding.pillSettings);
    }

    private void resetNavItem(TextView text, ImageView image, LinearLayout pill) {
        int inactiveColor = Color.parseColor("#9CA3AF");
        if (pill != null) pill.setBackground(null);
        if (image != null) image.setImageTintList(ColorStateList.valueOf(inactiveColor));
        if (text != null) {
            text.setVisibility(View.VISIBLE);
            text.setTextColor(inactiveColor);
        }
    }

    public void setBottomNavVisible(boolean visible) {
        if (binding != null && binding.navContainer != null) {
            binding.navContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setHeaderVisible() {
        // Compatibility stub
    }
}
