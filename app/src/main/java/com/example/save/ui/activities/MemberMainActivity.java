package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.ui.viewmodels.MembersViewModel; // Added import

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberMainBinding;

public class MemberMainActivity extends AppCompatActivity {

    private ActivityMemberMainBinding binding;

    // Data
    private MembersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);

        initializeViews();
        setupBottomNavigation();
        setupHeaderInteractions();
        loadDashboardData();
    }

    private void initializeViews() {
        // All views now accessed via binding
    }

    /*
     * Removed as button no longer exists
     * private void setupDashboardInteractions() {
     * if (btnTopUp != null) {
     * btnTopUp.setOnClickListener(v -> {
     * Toast.makeText(this, "Top Up Balance", Toast.LENGTH_SHORT).show();
     * });
     * }
     * }
     */

    private void setupHeaderInteractions() {
        if (binding.btnHeaderMembers != null) {
            binding.btnHeaderMembers.setOnClickListener(v -> {
                showMembersSection();
            });
        }
    }

    private void showMembersSection() {
        // 1. Reset all bottom nav items (deselect them)
        resetAllNavItems();

        // 2. Hide Main Content, Show Fragment Container
        if (binding.mainScrollView != null)
            binding.mainScrollView.setVisibility(View.GONE);
        if (binding.fragmentContainer != null)
            binding.fragmentContainer.setVisibility(View.VISIBLE);

        // 3. Load Members Fragment
        loadFragment(new MemberViewFragment());
    }

    private void setupBottomNavigation() {
        binding.navDashboard
                .setOnClickListener(v -> updateNav(binding.navDashboard, binding.txtDashboard, binding.imgDashboard));

        binding.navPay.setOnClickListener(v -> {
            updateNav(binding.navPay, binding.txtPay, binding.imgPay);
            loadFragment(PaymentFragment.newInstance("Alice Johnson")); // Using default/logged-in user
        });

        binding.navQueue.setOnClickListener(v -> {
            updateNav(binding.navQueue, binding.txtQueue, binding.imgQueue);
            loadFragment(QueueFragment.newInstance());
        });

        binding.navLoans.setOnClickListener(v -> {
            updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans);
            loadFragment(LoanApplicationFragment.newInstance());
        });

        binding.navStats.setOnClickListener(v -> {
            updateNav(binding.navStats, binding.txtStats, binding.imgStats);
            loadFragment(AnalyticsFragment.newInstance(false));
        });
    }

    private void updateNav(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        // 1. Reset all items to unselected state first
        resetAllNavItems();

        // 2. Set the clicked item to selected state
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) selectedLayout.getLayoutParams();
        params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        params.weight = 0;
        selectedLayout.setLayoutParams(params);

        // Dark Indigo Color for Active State
        int activeColor = androidx.core.content.ContextCompat.getColor(this, R.color.deep_blue);
        selectedLayout.setBackgroundResource(R.drawable.nav_item_pill_white);
        selectedText.setVisibility(View.VISIBLE);
        selectedImage.setImageTintList(ColorStateList.valueOf(activeColor));
        selectedText.setTextColor(activeColor);

        // Show/Hide Main Content vs Fragment Container
        if (selectedLayout == binding.navDashboard) {
            if (binding.mainScrollView != null)
                binding.mainScrollView.setVisibility(View.VISIBLE);
            if (binding.fragmentContainer != null)
                binding.fragmentContainer.setVisibility(View.GONE);
        } else {
            if (binding.mainScrollView != null)
                binding.mainScrollView.setVisibility(View.GONE);
            if (binding.fragmentContainer != null)
                binding.fragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void resetAllNavItems() {
        resetNavItem(binding.navDashboard, binding.txtDashboard, binding.imgDashboard);
        resetNavItem(binding.navPay, binding.txtPay, binding.imgPay);
        resetNavItem(binding.navQueue, binding.txtQueue, binding.imgQueue);
        resetNavItem(binding.navLoans, binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.navStats, binding.txtStats, binding.imgStats);
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void loadDashboardData() {
        // Load balance data (placeholder for now, or from VM if available)
        // Note: MembersViewModel has getGroupBalance(), so we can use it if appropriate
        // OR kept as placeholder if it was intended.
        // Previous code accessed memberRepository.getActiveMemberCount() so we should
        // migrate that.

        // Let's use ViewModel for consistency if balance is needed
        // Assuming MemberMainActivity mimics AdminMainActivity logic or similar
        // BUT the original code:
        // binding.txtBalance.setText("UGX 1,500,000"); // Hardcoded string in original?
        // Wait, looking at the view_file output for MemberMainActivity:
        // line 155: binding.txtBalance.setText("UGX 1,500,000");
        // It was hardcoded!

        if (binding.txtBalance != null)
            binding.txtBalance.setText("UGX 1,500,000"); // Kept hardcoded as per original

        if (binding.savingsBalance != null)
            binding.savingsBalance.setText("UGX 350,000");

        updateMemberCount();
    }

    private void updateMemberCount() {
        if (viewModel != null && binding.activeMembers != null) {
            int activeMembersCount = viewModel.getActiveMemberCount();
            int totalMembers = viewModel.getTotalMemberCount();
            binding.activeMembers.setText(activeMembersCount + "/" + totalMembers);
        }
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        layout.setLayoutParams(params);

        layout.setBackground(null);
        text.setVisibility(View.GONE);

        // White Color for Inactive State
        image.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }
}
