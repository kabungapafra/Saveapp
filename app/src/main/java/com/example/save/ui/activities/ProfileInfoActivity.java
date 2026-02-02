package com.example.save.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityProfileInfoBinding;
import com.example.save.utils.SessionManager;

public class ProfileInfoActivity extends AppCompatActivity {

    private ActivityProfileInfoBinding binding;
    private com.example.save.ui.viewmodels.MembersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.example.save.ui.viewmodels.MembersViewModel.class);

        setupListeners();
        loadProfileInfo();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void loadProfileInfo() {
        SessionManager session = new SessionManager(this);
        String userEmail = session.getUserEmail();

        if (userEmail == null) {
            Toast.makeText(this, "Session error: Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initially load from session
        binding.tvUserName.setText(session.getUserName() != null ? session.getUserName() : "Loading...");
        binding.tvUserEmail.setText(userEmail);
        binding.tvUserRole.setText(session.getUserRole() != null ? session.getUserRole() : "Member");

        // Fetch real data from DB
        new Thread(() -> {
            com.example.save.data.models.Member member = viewModel.getMemberByEmail(userEmail);
            runOnUiThread(() -> {
                if (member != null) {
                    binding.tvUserName.setText(member.getName());
                    binding.tvUserPhone.setText(member.getPhone());
                    binding.tvUserRole.setText(member.getRole());

                    if (member.getJoinedDate() != null) {
                        binding.tvMemberSince.setText("Member since: " + member.getJoinedDate());
                    }
                } else {
                    // Fallback to Prefs if not in DB (e.g. if sync hasn't happened)
                    android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
                    binding.tvUserPhone.setText(prefs.getString("user_phone", "Not Available"));
                    binding.tvGroupName.setText(prefs.getString("group_name", "Weekend Savers Club"));
                }
            });
        }).start();

        // Always get group name from Prefs
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        binding.tvGroupName.setText(prefs.getString("group_name", "Weekend Savers Club"));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
