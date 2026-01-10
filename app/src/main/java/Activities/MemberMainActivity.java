package Activities;

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

public class MemberMainActivity extends AppCompatActivity {

    private NestedScrollView mainScrollView;

    // Dashboard Views
    private TextView greetingName;
    private View btnHeaderMembers;

    // Balance Card Views (same as Admin)
    private TextView currentBalance, savingsBalance, activeMembers;

    // Data
    private Data.MemberRepository memberRepository;

    // Custom Navigation Views
    private LinearLayout navDashboard, navMembers, navPay, navQueue, navLoans, navStats;
    private TextView txtDashboard, txtMembers, txtPay, txtQueue, txtLoans, txtStats;
    private ImageView imgDashboard, imgMembers, imgPay, imgQueue, imgLoans, imgStats;

    // Fragment Container
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_main);

        memberRepository = Data.MemberRepository.getInstance();

        initializeViews();
        setupBottomNavigation();
        setupHeaderInteractions();
        loadDashboardData();
    }

    private void initializeViews() {
        // Main Content
        mainScrollView = findViewById(R.id.mainScrollView);
        fragmentContainer = findViewById(R.id.fragment_container);

        // Dashboard Views
        greetingName = findViewById(R.id.greetingName);
        btnHeaderMembers = findViewById(R.id.btnHeaderMembers);

        // Balance Card Views (same as Admin)
        currentBalance = findViewById(R.id.txtBalance);
        savingsBalance = findViewById(R.id.savingsBalance);
        activeMembers = findViewById(R.id.activeMembers);

        // Navigation Views
        navDashboard = findViewById(R.id.nav_dashboard);
        navPay = findViewById(R.id.nav_pay);
        navQueue = findViewById(R.id.nav_queue);
        navLoans = findViewById(R.id.nav_loans);
        navStats = findViewById(R.id.nav_stats);

        txtDashboard = findViewById(R.id.txt_dashboard);
        txtPay = findViewById(R.id.txt_pay);
        txtQueue = findViewById(R.id.txt_queue);
        txtLoans = findViewById(R.id.txt_loans);
        txtStats = findViewById(R.id.txt_stats);

        imgDashboard = findViewById(R.id.img_dashboard);
        imgPay = findViewById(R.id.img_pay);
        imgQueue = findViewById(R.id.img_queue);
        imgLoans = findViewById(R.id.img_loans);
        imgStats = findViewById(R.id.img_stats);
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
        if (btnHeaderMembers != null) {
            btnHeaderMembers.setOnClickListener(v -> {
                showMembersSection();
            });
        }
    }

    private void showMembersSection() {
        // 1. Reset all bottom nav items (deselect them)
        resetAllNavItems();

        // 2. Hide Main Content, Show Fragment Container
        if (mainScrollView != null)
            mainScrollView.setVisibility(View.GONE);
        if (fragmentContainer != null)
            fragmentContainer.setVisibility(View.VISIBLE);

        // 3. Load Members Fragment
        loadFragment(new Fragments.MemberViewFragment());
    }

    private void setupBottomNavigation() {
        navDashboard.setOnClickListener(v -> updateNav(navDashboard, txtDashboard, imgDashboard));

        navPay.setOnClickListener(v -> {
            updateNav(navPay, txtPay, imgPay);
            Toast.makeText(this, "Make a Payment", Toast.LENGTH_SHORT).show();
        });

        navQueue.setOnClickListener(v -> {
            updateNav(navQueue, txtQueue, imgQueue);
            Toast.makeText(this, "View Queue", Toast.LENGTH_SHORT).show();
        });

        navLoans.setOnClickListener(v -> {
            updateNav(navLoans, txtLoans, imgLoans);
            Toast.makeText(this, "Apply for Loan", Toast.LENGTH_SHORT).show();
        });

        navStats.setOnClickListener(v -> {
            updateNav(navStats, txtStats, imgStats);
            loadFragment(Fragments.AnalyticsFragment.newInstance(false));
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
        if (selectedLayout == navDashboard) {
            if (mainScrollView != null)
                mainScrollView.setVisibility(View.VISIBLE);
            if (fragmentContainer != null)
                fragmentContainer.setVisibility(View.GONE);
        } else {
            if (mainScrollView != null)
                mainScrollView.setVisibility(View.GONE);
            if (fragmentContainer != null)
                fragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void resetAllNavItems() {
        resetNavItem(navDashboard, txtDashboard, imgDashboard);
        resetNavItem(navPay, txtPay, imgPay);
        resetNavItem(navQueue, txtQueue, imgQueue);
        resetNavItem(navLoans, txtLoans, imgLoans);
        resetNavItem(navStats, txtStats, imgStats);
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void loadDashboardData() {
        // Load balance data (placeholder for now)
        if (currentBalance != null)
            currentBalance.setText("UGX 1,500,000");
        if (savingsBalance != null)
            savingsBalance.setText("UGX 350,000");

        updateMemberCount();
    }

    private void updateMemberCount() {
        if (memberRepository != null && activeMembers != null) {
            int activeMembersCount = memberRepository.getActiveMemberCount();
            int totalMembers = memberRepository.getAllMembers().size();
            activeMembers.setText(activeMembersCount + "/" + totalMembers);
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
