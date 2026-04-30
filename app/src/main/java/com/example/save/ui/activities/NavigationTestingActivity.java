package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.R;
import com.example.save.ui.fragments.*;

public class NavigationTestingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_testing);

        // Activity Links
        findViewById(R.id.btnLanding).setOnClickListener(v -> startActivity(new Intent(this, SplashActivity.class)));
        findViewById(R.id.btnAdminLogin).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.btnAdminMain).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("group_name", "Test Group");
            startActivity(intent);
        });
        findViewById(R.id.btnMemberMain).setOnClickListener(v -> {
            Intent intent = new Intent(this, MemberMainActivity.class);
            intent.putExtra("member_name", "Test Member");
            startActivity(intent);
        });

        // Flow / Fragment Links (These might require launching a host activity first)
        findViewById(R.id.btnLoanRequest).setOnClickListener(v -> launchFragment(new LoanApplicationFragment()));
        findViewById(R.id.btnLoanSuccess).setOnClickListener(v -> launchFragment(new LoanSubmittedSuccessFragment()));
        findViewById(R.id.btnAddMember).setOnClickListener(v -> launchFragment(new MembersFragment())); // Usually opens the list which has add button
        findViewById(R.id.btnMemberSuccess).setOnClickListener(v -> launchFragment(new MemberSuccessFragment()));
        findViewById(R.id.btnMakePayment).setOnClickListener(v -> launchFragment(new MakeContributionFragment()));
        findViewById(R.id.btnSettings).setOnClickListener(v -> launchFragment(new SettingsFragment()));
    }

    private void launchFragment(androidx.fragment.app.Fragment fragment) {
        // We use AdminMainActivity as a host for fragment testing
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.putExtra("start_fragment", fragment.getClass().getName());
        startActivity(intent);
    }
}
