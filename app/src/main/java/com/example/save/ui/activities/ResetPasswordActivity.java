package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityResetpasswordBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity for resetting password after OTP verification
 * This is the final step: New Password → Confirm Password → Back to Login
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetpasswordBinding binding;

    // State
    private String userEmail = "";
    private String sourceActivity = ""; // Track where user came from

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get email and source activity from intent
        userEmail = getIntent().getStringExtra("email");
        sourceActivity = getIntent().getStringExtra("sourceActivity");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Invalid session. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize all views
        initializeViews();

        // Setup back press handling
        setupBackPressHandler();

        // Setup click listeners
        setupClickListeners();

        // Show password reset step
        showPasswordStep();
    }

    private void initializeViews() {
        // All views now accessed via binding
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        binding.actionButton.setOnClickListener(v -> changePassword());
    }

    private void showPasswordStep() {
        // Show password views
        binding.passwordTitle.setVisibility(View.VISIBLE);
        binding.passwordSubtitle.setVisibility(View.VISIBLE);
        binding.newPasswordInputLayout.setVisibility(View.VISIBLE);
        binding.confirmPasswordInputLayout.setVisibility(View.VISIBLE);

        // Set button text
        binding.actionButton.setText("Change Password");
        binding.errorMessage.setVisibility(View.GONE);

        // Clear password fields
        binding.newPasswordInput.setText("");
        binding.confirmPasswordInput.setText("");
    }

    private void changePassword() {
        String newPassword = binding.newPasswordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        if (newPassword.isEmpty() || newPassword.length() < 8) {
            binding.newPasswordInput.setError("Password must be at least 8 characters");
            binding.newPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.confirmPasswordInput.setError("Passwords do not match");
            binding.confirmPasswordInput.requestFocus();
            return;
        }

        binding.actionButton.setEnabled(false);
        binding.actionButton.setText("Changing...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API
        // POST /auth/reset-password
        // Body: { "email": userEmail, "newPassword": newPassword }

        // Call backend API (ViewModel) to update password
        com.example.save.ui.viewmodels.MembersViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.example.save.ui.viewmodels.MembersViewModel.class);

        // We need to fetch the member object first to pass to changePassword
        // This should ideally be done on a background thread via the ViewModel
        new Thread(() -> {
            Member member = viewModel.getMemberByEmail(userEmail);
            if (member != null) {
                runOnUiThread(() -> {
                    viewModel.changePassword(member.getEmail(), "RECOVERY", newPassword,
                            new MemberRepository.PasswordChangeCallback() {
                                @Override
                                public void onResult(boolean success, String message) {
                                    binding.loadingIndicator.setVisibility(View.GONE);
                                    binding.actionButton.setEnabled(true);

                                    if (success) {
                                        Toast.makeText(ResetPasswordActivity.this, "Password changed successfully!",
                                                Toast.LENGTH_SHORT).show();

                                        // Navigate back to the login page where user clicked forgot password
                                        Class<?> targetActivity;
                                        if ("MemberRegistrationActivity".equals(sourceActivity)) {
                                            targetActivity = MemberRegistrationActivity.class;
                                        } else {
                                            targetActivity = AdminLoginActivity.class; // Default to admin
                                        }

                                        Intent intent = new Intent(ResetPasswordActivity.this, targetActivity);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(ResetPasswordActivity.this, "Error: " + message,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });
            } else {
                runOnUiThread(() -> {
                    binding.loadingIndicator.setVisibility(View.GONE);
                    binding.actionButton.setEnabled(true);
                    Toast.makeText(this, "Error finding user account", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}