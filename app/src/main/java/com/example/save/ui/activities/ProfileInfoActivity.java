package com.example.save.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityProfileInfoBinding;
import com.example.save.utils.SessionManager;

public class ProfileInfoActivity extends AppCompatActivity {

    private ActivityProfileInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        loadProfileInfo();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void loadProfileInfo() {
        SessionManager session = new SessionManager(this);
        
        String userName = session.getUserName();
        String userEmail = session.getUserEmail();
        
        // Get user role from session details
        java.util.HashMap<String, String> userDetails = session.getUserDetails();
        String userRole = userDetails.get(SessionManager.KEY_ROLE);

        binding.tvUserName.setText(userName != null ? userName : "Not Available");
        binding.tvUserEmail.setText(userEmail != null ? userEmail : "Not Available");
        binding.tvUserRole.setText(userRole != null ? userRole : "Not Available");

        // Get phone and group info from preferences
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "Not Available");
        String groupName = prefs.getString("group_name", "Not Available");
        
        binding.tvUserPhone.setText(userPhone);
        binding.tvGroupName.setText(groupName);

        // Get account creation date (if stored)
        // This would typically come from the database
        binding.tvMemberSince.setText("Member since: " + getAccountCreationDate());
    }

    private String getAccountCreationDate() {
        // This would typically fetch from database
        // For now, return a placeholder
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        long creationDate = prefs.getLong("account_creation_date", 0);
        
        if (creationDate > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(creationDate));
        }
        
        return "Recently";
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
