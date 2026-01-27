package com.example.save.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityContactAdminBinding;
import com.example.save.utils.ValidationUtils;

public class ContactAdminActivity extends AppCompatActivity {

    private ActivityContactAdminBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        loadAdminInfo();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.btnSendMessage.setOnClickListener(v -> sendMessage());
        binding.btnCallAdmin.setOnClickListener(v -> callAdmin());
        binding.btnEmailAdmin.setOnClickListener(v -> emailAdmin());
    }

    private void loadAdminInfo() {
        // Get admin info from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        String adminName = prefs.getString("admin_name", "Group Administrator");
        String adminEmail = prefs.getString("admin_email", "admin@save.com");
        String groupName = prefs.getString("group_name", "Your Group");

        binding.tvAdminName.setText(adminName);
        binding.tvGroupName.setText(groupName);
        binding.tvAdminEmail.setText(adminEmail);
    }

    private void sendMessage() {
        String subject = binding.etSubject.getText().toString().trim();
        String message = binding.etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(subject)) {
            binding.etSubject.setError("Subject is required");
            binding.etSubject.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            binding.etMessage.setError("Message is required");
            binding.etMessage.requestFocus();
            return;
        }

        // Get admin email
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        String adminEmail = prefs.getString("admin_email", "admin@save.com");

        // Create email intent
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{adminEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via..."));
            Toast.makeText(this, "Opening email client...", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No email app found. Please install an email client.", Toast.LENGTH_LONG).show();
        }
    }

    private void callAdmin() {
        // Get admin phone from preferences (stored during login)
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        String adminPhone = prefs.getString("admin_phone", null);

        if (adminPhone == null || adminPhone.isEmpty()) {
            Toast.makeText(this, "Admin phone number not available. Please contact via email.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + adminPhone));
        startActivity(callIntent);
    }

    private void emailAdmin() {
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        String adminEmail = prefs.getString("admin_email", "admin@save.com");

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{adminEmail});

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via..."));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No email app found. Please install an email client.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
