package com.example.save.ui.activities;
import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.widget.EditText;
import androidx.appcompat.widget.SwitchCompat;
import com.example.save.R;
import com.example.save.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupListeners();
    }

    private void initViews() {
        // All views now accessed via binding - no initialization needed
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.btnLogout.setOnClickListener(v -> {
            // TODO: Implement actual logout logic (clear prefs, intent to LoginActivity)
            Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show();
        });

        // Listeners for clickable items (Toasts for now)
        View.OnClickListener notImplementedListener = v -> Toast
                .makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();

        binding.btnProfileInfo.setOnClickListener(notImplementedListener);
        binding.btnChangePassword.setOnClickListener(notImplementedListener);
        binding.btnLanguage.setOnClickListener(notImplementedListener);
        binding.btnNotificationMethod.setOnClickListener(notImplementedListener);
        binding.btnAutoLock.setOnClickListener(notImplementedListener);
        binding.btnDefaultPayment.setOnClickListener(notImplementedListener);
        binding.btnGroupDetails.setOnClickListener(notImplementedListener);
        binding.btnViewMembers.setOnClickListener(notImplementedListener);
        binding.btnContactAdmin.setOnClickListener(notImplementedListener);
        binding.btnDownloadData.setOnClickListener(notImplementedListener);
        binding.btnClearCache.setOnClickListener(v -> Toast.makeText(this, "Cache Cleared", Toast.LENGTH_SHORT).show());
        binding.btnHelpSupport.setOnClickListener(notImplementedListener);
        binding.btnTOS.setOnClickListener(notImplementedListener);
        binding.btnPrivacyPolicy.setOnClickListener(notImplementedListener);
        binding.btnRate
                .setOnClickListener(v -> Toast.makeText(this, "Thank you for rating!", Toast.LENGTH_SHORT).show());
        binding.btnShare.setOnClickListener(v -> Toast.makeText(this, "Sharing...", Toast.LENGTH_SHORT).show());
    }
}
