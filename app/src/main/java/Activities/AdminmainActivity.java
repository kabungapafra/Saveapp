package Activities;

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
import Data.MemberRepository;

public class AdminmainActivity extends AppCompatActivity implements MemberRepository.MemberChangeListener {

    // Views
    private TextView adminName, groupName, currentBalance, savingsBalance, activeMembers;
    private ImageView profileIcon, menuIcon;
    private CardView addMemberCard, executePayoutCard, viewLoansCard, navContainer;
    private RecyclerView activityRecyclerView;
    private NestedScrollView mainScrollView;
    private FrameLayout fragmentContainer;

    // Custom Navigation Views
    private LinearLayout navHome, navMembers, navPayouts, navLoans, navAnalytics;
    private TextView txtHome, txtMembers, txtPayouts, txtLoans, txtAnalytics;
    private ImageView imgHome, imgMembers, imgPayouts, imgLoans, imgAnalytics;

    // Data
    private String adminNameStr;
    private String groupNameStr;
    private MemberRepository memberRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Correct layout name
        setContentView(R.layout.activity_adminmain);

        memberRepository = MemberRepository.getInstance();
        memberRepository.addListener(this);

        initializeViews();
        loadAdminData();
        setupListeners();
        setupBottomNavigation();
        loadDashboardData();
        setupDatePicker();
        setupRecentActivity();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        android.widget.FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            fragmentContainer.setVisibility(View.GONE);
            findViewById(R.id.main_content_scroll_view).setVisibility(View.VISIBLE);
            findViewById(R.id.navContainer).setVisibility(View.VISIBLE); // Restore nav

            // Clear fragment back stack? Or simple hide/show
            getSupportFragmentManager().beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container))
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    private void initializeViews() {
        adminName = findViewById(R.id.adminName);
        groupName = findViewById(R.id.groupName);
        currentBalance = findViewById(R.id.currentBalance);
        savingsBalance = findViewById(R.id.savingsBalance);
        activeMembers = findViewById(R.id.activeMembers);
        profileIcon = findViewById(R.id.profileIcon);
        menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> {
            loadFragment(new Fragments.SettingsFragment());
            // Optionally hide bottom nav or other elements if needed,
            // but loadFragment handles container visibility.
            navContainer.setVisibility(View.GONE); // Hide nav for full screen settings
        });
        addMemberCard = findViewById(R.id.addMemberCard);
        executePayoutCard = findViewById(R.id.executePayoutCard);
        viewLoansCard = findViewById(R.id.viewLoansCard);
        viewLoansCard = findViewById(R.id.viewLoansCard);
        activityRecyclerView = findViewById(R.id.activityRecyclerView);

        mainScrollView = findViewById(R.id.main_content_scroll_view);
        fragmentContainer = findViewById(R.id.fragment_container);
        navContainer = findViewById(R.id.navContainer);

        // Navigation Views
        navHome = findViewById(R.id.nav_home);
        navMembers = findViewById(R.id.nav_members);
        navPayouts = findViewById(R.id.nav_payouts);
        navLoans = findViewById(R.id.nav_loans);
        navAnalytics = findViewById(R.id.nav_analytics);

        txtHome = findViewById(R.id.txt_home);
        txtMembers = findViewById(R.id.txt_members);
        txtPayouts = findViewById(R.id.txt_payouts);
        txtLoans = findViewById(R.id.txt_loans);
        txtAnalytics = findViewById(R.id.txt_analytics);

        imgHome = findViewById(R.id.img_home);
        imgMembers = findViewById(R.id.img_members);
        imgPayouts = findViewById(R.id.img_payouts);
        imgLoans = findViewById(R.id.img_loans);
        imgAnalytics = findViewById(R.id.img_analytics);
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
        adminName.setText(adminNameStr + "!");
        groupName.setText(groupNameStr);
    }

    private void setupListeners() {
        // Profile icon click
        profileIcon.setOnClickListener(v -> {
            // Navigate to profile screen
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // Add Member card
        addMemberCard.setOnClickListener(v -> {
            // Navigate to Members fragment and show add dialog
            updateNav(navMembers, txtMembers, imgMembers);

            // Post a delayed action to ensure fragment is loaded before showing dialog
            navMembers.postDelayed(() -> {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment instanceof Fragments.MembersFragment) {
                    ((Fragments.MembersFragment) fragment).showAddMemberDialog();
                }
            }, 100);
        });

        // Execute Payout card
        executePayoutCard.setOnClickListener(v -> {
            // Navigate to Execute Payout screen
            Toast.makeText(this, "Execute Payout", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, ExecutePayoutActivity.class);
            // startActivity(intent);
        });

        // View Loans card
        viewLoansCard.setOnClickListener(v -> {
            // Navigate to Loans screen
            Toast.makeText(this, "View Loans", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, LoansActivity.class);
            // startActivity(intent);
        });

        // View Details button
        TextView viewDetailsBtn = findViewById(R.id.viewAllActivity);
        if (viewDetailsBtn != null) {
            viewDetailsBtn.setOnClickListener(v -> {
                // Navigate to full report
                Toast.makeText(this, "View Full Report", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> updateNav(navHome, txtHome, imgHome));

        navMembers.setOnClickListener(v -> {
            updateNav(navMembers, txtMembers, imgMembers);
            Toast.makeText(this, "Members Management", Toast.LENGTH_SHORT).show();
        });

        navPayouts.setOnClickListener(v -> {
            updateNav(navPayouts, txtPayouts, imgPayouts);
            Toast.makeText(this, "Payout Management", Toast.LENGTH_SHORT).show();
        });

        navLoans.setOnClickListener(v -> {
            updateNav(navLoans, txtLoans, imgLoans);
            Toast.makeText(this, "Loans Management", Toast.LENGTH_SHORT).show();
        });

        navAnalytics.setOnClickListener(v -> {
            updateNav(navAnalytics, txtAnalytics, imgAnalytics);
            Toast.makeText(this, "Analytics", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateNav(LinearLayout selectedLayout, TextView selectedText, ImageView selectedImage) {
        // 1. Reset all items to unselected state first
        resetNavItem(navHome, txtHome, imgHome);
        resetNavItem(navMembers, txtMembers, imgMembers);
        resetNavItem(navPayouts, txtPayouts, imgPayouts);
        resetNavItem(navLoans, txtLoans, imgLoans);
        resetNavItem(navAnalytics, txtAnalytics, imgAnalytics);

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
        if (selectedLayout == navHome) {
            mainScrollView.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
        } else {
            mainScrollView.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);

            Fragment fragment = null;
            if (selectedLayout == navMembers)
                fragment = new Fragments.MembersFragment();
            else if (selectedLayout == navPayouts)
                fragment = new Fragments.PayoutsFragment();
            else if (selectedLayout == navLoans)
                fragment = new Fragments.LoansFragment();
            else if (selectedLayout == navAnalytics)
                fragment = new Fragments.AnalyticsFragment();

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        mainScrollView.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
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
        // Load data from repository
        double balance = memberRepository.getGroupBalance();
        double savings = balance * 0.2; // Dummy logic for savings portion

        if (currentBalance != null)
            currentBalance.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(balance));
        if (savingsBalance != null)
            savingsBalance.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(savings));

        updateMemberCount();
    }

    private void updateMemberCount() {
        int activeMembersCount = memberRepository.getActiveMemberCount();
        int totalMembers = memberRepository.getTotalMemberCount();
        activeMembers.setText(activeMembersCount + "/" + totalMembers);
    }

    @Override
    public void onMembersChanged() {
        // Update the member count and balance on the dashboard when members change
        runOnUiThread(() -> loadDashboardData());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (memberRepository != null) {
            memberRepository.removeListener(this);
        }
    }

    private void setupDatePicker() {
        RecyclerView dateRecyclerView = findViewById(R.id.dateRecyclerView);
        if (dateRecyclerView != null) {
            dateRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this,
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

            dateRecyclerView.setAdapter(new DateAdapter(dates));
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_date, parent, false);
            return new DateViewHolder(view);
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
            android.widget.LinearLayout container;
            TextView dayText, dateText;

            DateViewHolder(View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.dateContent);
                dayText = itemView.findViewById(R.id.dayText);
                dateText = itemView.findViewById(R.id.dateText);
            }
        }
    }

    private void setupRecentActivity() {
        if (activityRecyclerView != null) {
            activityRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

            List<RecentActivityModel> activities = new ArrayList<>();
            activities.add(new RecentActivityModel("Payment Received", "James K - Monthly Contribution", "+ UGX 50,000",
                    true));
            activities.add(new RecentActivityModel("New Member", "Sarah M joined the group", "Active", true));
            activities
                    .add(new RecentActivityModel("Loan Approved", "Peter L - Emergency Loan", "- UGX 200,000", false));
            activities.add(new RecentActivityModel("Penalty Paid", "John D - Late Meeting", "+ UGX 5,000", true));

            activityRecyclerView.setAdapter(new RecentActivityAdapter(activities));
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
            return new ActivityViewHolder(view);
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

            ActivityViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.activityTitle);
                date = itemView.findViewById(R.id.activityDate);
                amount = itemView.findViewById(R.id.activityAmount);
            }
        }
    }
}