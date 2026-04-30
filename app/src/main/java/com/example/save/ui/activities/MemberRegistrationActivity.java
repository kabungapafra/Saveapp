package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.databinding.ActivityAdminregBinding;

/**
 * MemberRegistrationActivity - Fully Static Version
 * No Database, No Network.
 */
public class MemberRegistrationActivity extends AppCompatActivity {
    private ActivityAdminregBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String name = binding.adminNameInput.getText().toString();
        String email = binding.adminEmailInput.getText().toString();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.loginButton.setEnabled(false);
        binding.loginButtonText.setText("Registering...");

        new Handler().postDelayed(() -> {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}