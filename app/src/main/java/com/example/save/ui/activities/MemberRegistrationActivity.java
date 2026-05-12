package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.databinding.ActivityMemberregBinding;

/**
 * MemberRegistrationActivity - Fully Static Version
 * No Database, No Network.
 */
public class MemberRegistrationActivity extends AppCompatActivity {
    private ActivityMemberregBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        binding.loginButton.setOnClickListener(v -> handleRegistration());
        binding.backToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegistration() {
        String name = binding.groupNameInput.getText().toString();
        String phone = binding.phoneInput.getText().toString();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loginButton.setEnabled(false);

        new Handler().postDelayed(() -> {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}