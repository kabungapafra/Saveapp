package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

import com.example.save.R;
import com.example.save.databinding.ActivityAdminmainBinding;
import com.example.save.databinding.ItemActivityBinding;
import com.example.save.databinding.ItemCalendarDateBinding;
import com.example.save.data.repository.MemberRepository;
import com.example.save.ui.viewmodels.MembersViewModel;

public class AdminmainActivity extends AppCompatActivity {

    private ActivityAdminmainBinding binding;

    // Data
    private String adminNameStr;
    private String groupNameStr;
    private MemberRepository memberRepository;
    private MembersViewModel viewModel; // Use ViewModel if possible, or just remove listener for now

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Correct layout name
        binding = ActivityAdminmainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);

        // Listener removed

        setupBottomNavigation();
        // Assuming setupQuickActions() and updateDashboardStats() are new methods to be
        // called
        // setupQuickActions(); // This method does not exist in the original code,
        // adding it would require implementation
        // updateDashboardStats(); // This method does not exist in the original code,
        // adding it would require implementation
        loadDashboardData();
        setupDatePicker();
        setupRecentActivity();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            binding.fragmentContainer.setVisibility(View.GONE);
            binding.mainContentScrollView.setVisibility(View.VISIBLE);
            binding.navContainer.setVisibility(View.VISIBLE); // Restore nav

            // Clear fragment back stack? Or simple hide/show
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        // Method removed as binding handles initialization
    }

    private void loadAdminData() {
        // Get admin data from SharedPreferences or Intent
        SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        adminNameStr = prefs.getString("admin_name", "Admin");
        groupNameStr = prefs.getString("group_name", "Weekend Savers Club");

        // Or get from Intent
        Intent intent = getIntent();
        if (intent.hasExtra("admin_name")) {
            adminNameStr = intent.getStringExtra("admin_name");
        }

        // Set to UI
        binding.adminName.setText(adminNameStr + "!");
        binding.groupName.setText(groupNameStr);
    }

    private void setupListeners() {
        // Profile icon click
        binding.profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // Add Member card
        binding.addMemberCard.setOnClickListener(v -> {
            // Navigate to Members fragment and show add dialog
            updateNav(binding.navMembers, binding.txtMembers, binding.imgMembers);

            // Post a delayed action to ensure fragment is loaded before showing dialog
            binding.navMembers.postDelayed(() -> {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment instanceof MembersFragment) {
                    ((MembersFragment) fragment).showAddMemberDialog();
                }
            }, 100);
        });

        // Execute Payout card
        binding.executePayoutCard.setOnClickListener(v -> {
            // Navigate to Execute Payout screen
            Toast.makeText(this, "Execute Payout", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, ExecutePayoutActivity.class);
            // startActivity(intent);
        });

        // View Loans card
        binding.viewLoansCard.setOnClickListener(v -> {
            loadFragment(new LoansFragment());
        });

        // View Details button
        if (binding.viewAllActivity != null) {
            binding.viewAllActivity.setOnClickListener(v -> {
                loadFragment(AnalyticsFragment.newInstance(true));
            });
        }
    }

    private void setupBottomNavigation() {
        binding.navHome.setOnClickListener(v -> updateNav(binding.navHome, binding.txtHome, binding.imgHome));

        binding.navMembers.setOnClickListener(v -> {
            updateNav(binding.navMembers, binding.txtMembers, binding.imgMembers);
            Toast.makeText(this, "Members Management", Toast.LENGTH_SHORT).show();
        });

        binding.navPayouts.setOnClickListener(v -> {
            updateNav(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
            Toast.makeText(this, "Payout Management", Toast.LENGTH_SHORT).show();
        });

        binding.navLoans.setOnClickListener(v -> {
            updateNav(binding.navLoans, binding.txtLoans, binding.imgLoans);
            Toast.makeText(this, "Loans Management", Toast.LENGTH_SHORT).show();
        });

        binding.navAnalytics.setOnClickListener(v -> {
            updateNav(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);
            Toast.makeText(this, "Analytics", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateNav(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        // 1. Reset all items to unselected state first
        resetNavItem(binding.navHome, binding.txtHome, binding.imgHome);
        resetNavItem(binding.navMembers, binding.txtMembers, binding.imgMembers);
        resetNavItem(binding.navPayouts, binding.txtPayouts, binding.imgPayouts);
        resetNavItem(binding.navLoans, binding.txtLoans, binding.imgLoans);
        resetNavItem(binding.navAnalytics, binding.txtAnalytics, binding.imgAnalytics);

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

        // Fragment Switching Logic
        if (selectedLayout == binding.navHome) {
            binding.mainContentScrollView.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
        } else {
            binding.mainContentScrollView.setVisibility(View.GONE);
            binding.fragmentContainer.setVisibility(View.VISIBLE);

            Fragment fragment = null;
            if (selectedLayout == binding.navMembers)
                fragment = new MembersFragment();
            else if (selectedLayout == binding.navPayouts)
                fragment = new PayoutsFragment();
            else if (selectedLayout == binding.navLoans)
                fragment = new LoansFragment();
            else if (selectedLayout == binding.navAnalytics)
                fragment = new AnalyticsFragment();

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
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
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void resetNavItem(LinearLayout layout, TextView text, ImageView image) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        params.width = 0;
        params.weight = 1;
        layout.setLayoutParams(params);

        layout.setBackground(null);

        // White Color for Inactive State
        image.setImageTintList(ColorStateList.valueOf(Color.BLACK)); // Using Black/White as per theme? Actually
                                                                     // previous was white.
        // Let's use WHITE as per the design requirement (Dark bar, white icons)
        image.setImageTintList(ColorStateList.valueOf(Color.WHITE));
    }

    private void loadDashboardData() {
        // Load data from ViewModel
        double balance = viewModel.getGroupBalance();
        double savings = balance * 0.2; // Dummy logic for savings portion

        if (binding.currentBalance != null)
            binding.currentBalance.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(balance));
        if (binding.savingsBalance != null)
            binding.savingsBalance.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(savings));

        updateMemberCount();
    }

    private void updateMemberCount() {
        int activeMembersCount = viewModel.getActiveMemberCount();
        int totalMembers = viewModel.getTotalMemberCount();
        binding.activeMembers.setText(activeMembersCount + "/" + totalMembers);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Listener removal not needed
        binding = null;
    }

    private void setupDatePicker() {
        if (binding.dateRecyclerView != null) {
            binding.dateRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));

            List<DateItem> dates = new ArrayList<>();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);

            for (int i = 0; i < 30; i++) {
                String day = dayFormat.format(calendar.getTime()).toUpperCase();
                String date = dateFormat.format(calendar.getTime());
                dates.add(new DateItem(day, date, i == 0, calendar));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            binding.dateRecyclerView.setAdapter(new DateAdapter(dates));
        }
    }

    // Date Picker Data Model
    private static class DateItem {
        String day;
        String date;
        boolean isSelected;
        Calendar calendar;

        DateItem(String day, String date, boolean isSelected, Calendar calendar) {
            this.day = day;
            this.date = date;
            this.isSelected = isSelected;
            this.calendar = (Calendar) calendar.clone();
        }
    }

    // Date Picker Adapter
    private class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
        private List<DateItem> dates;

        DateAdapter(List<DateItem> dates) {
            this.dates = dates;
        }

        @NonNull
        @Override
        public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCalendarDateBinding itemBinding = ItemCalendarDateBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new DateViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
            DateItem item = dates.get(position);
            holder.dayText.setText(item.day);
            holder.dateText.setText(item.date);

            if (item.isSelected) {
                holder.container.setBackgroundResource(R.drawable.date_selector_bg);
                holder.container.setSelected(true);
                holder.dayText.setTextColor(Color.WHITE);
                holder.dateText.setTextColor(Color.WHITE);
            } else {
                holder.container.setBackground(null);
                holder.container.setSelected(false);
                holder.dayText.setTextColor(Color.parseColor("#9E9E9E"));
                holder.dateText.setTextColor(Color.parseColor("#1A1A1A"));
            }

            // Click listener to navigate to CreateTaskActivity
            holder.itemView.setOnClickListener(v -> {
                // Update selection visually
                for (DateItem d : dates)
                    d.isSelected = false;
                item.isSelected = true;
                notifyDataSetChanged();

                // Navigate to DailyTasksActivity with selected date
                Intent intent = new Intent(AdminmainActivity.this, DailyTasksActivity.class);
                SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
                String formattedDate = fullDateFormat.format(item.calendar.getTime());
                intent.putExtra("selected_date", formattedDate);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return dates.size();
        }

        class DateViewHolder extends RecyclerView.ViewHolder {
            private final ItemCalendarDateBinding itemBinding;
            android.widget.LinearLayout container;
            TextView dayText, dateText;

            DateViewHolder(ItemCalendarDateBinding itemBinding) {
                super(itemBinding.getRoot());
                this.itemBinding = itemBinding;
                container = itemBinding.dateContent;
                dayText = itemBinding.dayText;
                dateText = itemBinding.dateText;
            }
        }
    }

    private void setupRecentActivity() {
        if (binding.activityRecyclerView != null) {
            binding.activityRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

            List<RecentActivityModel> activities = new ArrayList<>();
            activities.add(new RecentActivityModel("Payment Received", "James K - Monthly Contribution", "+ UGX 50,000",
                    true));
            activities.add(new RecentActivityModel("New Member", "Sarah M joined the group", "Active", true));
            activities
                    .add(new RecentActivityModel("Loan Approved", "Peter L - Emergency Loan", "- UGX 200,000", false));
            activities.add(new RecentActivityModel("Penalty Paid", "John D - Late Meeting", "+ UGX 5,000", true));

            binding.activityRecyclerView.setAdapter(new RecentActivityAdapter(activities));
        }
    }

    // Recent Activity Model
    private static class RecentActivityModel {
        String title;
        String description;
        String amount;
        boolean isPositive;

        RecentActivityModel(String title, String description, String amount, boolean isPositive) {
            this.title = title;
            this.description = description;
            this.amount = amount;
            this.isPositive = isPositive;
        }
    }

    // Recent Activity Adapter
    private class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {
        private List<RecentActivityModel> list;

        RecentActivityAdapter(List<RecentActivityModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemActivityBinding itemBinding = ItemActivityBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ActivityViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
            RecentActivityModel item = list.get(position);
            holder.title.setText(item.title);
            holder.date.setText(item.description);
            holder.amount.setText(item.amount);

            if (item.isPositive) {
                holder.amount.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else {
                holder.amount.setTextColor(Color.parseColor("#F44336")); // Red
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ActivityViewHolder extends RecyclerView.ViewHolder {
            TextView title, date, amount;

            ActivityViewHolder(ItemActivityBinding itemBinding) {
                super(itemBinding.getRoot());
                title = itemBinding.activityTitle;
                date = itemBinding.activityDate;
                amount = itemBinding.activityAmount;
            }
        }
    }
}