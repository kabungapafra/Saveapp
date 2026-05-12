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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

/**
 * Activity for resetting password after OTP verification
 * This is the final step: New Password → Confirm Password → Back to Login
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetpasswordBinding binding;

    // State
    private String userEmail = "";
    private String userPhone = "";
    private String mVerificationId = "";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get email and source activity from intent
        userEmail = getIntent().getStringExtra("email");
        userPhone = getIntent().getStringExtra("phone");
        // Track where user came from
        String sourceActivity = getIntent().getStringExtra("sourceActivity");
        if (userEmail != null) {
            userEmail = userEmail.toLowerCase().trim();
        }
        if (userPhone != null) {
            userPhone = userPhone.trim();
        }

        if ((userEmail == null || userEmail.isEmpty()) && (userPhone == null || userPhone.isEmpty())) {
            Toast.makeText(this, "Invalid session. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize all views
        initializeViews();

        mAuth = FirebaseAuth.getInstance();
        mVerificationId = getIntent().getStringExtra("verificationId");

        if (mVerificationId != null && !mVerificationId.isEmpty()) {
            showOtpVerification();
        } else {
            // Show password reset step
            showPasswordStep();
        }

        // Setup back press handling
        setupBackPressHandler();

        // Setup click listeners
        setupClickListeners();
    }

    private void showOtpVerification() {
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        
        OtpFragment otpFragment = new OtpFragment();
        otpFragment.setOtpListener(new OtpFragment.OtpListener() {
            @Override
            public void onOtpEntered(String code) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                verifyAndProceed(credential);
            }

            @Override
            public void onResendOtp() {
                // Handle resend if needed
                Toast.makeText(ResetPasswordActivity.this, "Resend not implemented in this view", Toast.LENGTH_SHORT).show();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, otpFragment)
                .commit();
    }

    private void verifyAndProceed(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        binding.fragmentContainer.setVisibility(View.GONE);
                        showPasswordStep();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                    }
                });
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
        binding.actionButton.setOnClickListener(v -> changePassword());
        binding.backToLoginContainer.setOnClickListener(v -> finish());
    }

    private void showPasswordStep() {
        // Update button text from strings
        binding.actionButton.setText(R.string.reset_pin_button);
        binding.errorMessage.setVisibility(View.GONE);

        // Clear password fields
        binding.newPasswordInput.setText("");
        binding.confirmPasswordInput.setText("");
    }

    private void changePassword() {
        String newPassword = binding.newPasswordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        if (!com.example.save.utils.ValidationUtils.isValidPin(newPassword)) {
            binding.newPasswordInput.setError("PIN must be exactly 6 digits");
            binding.newPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.confirmPasswordInput.setError("PINs do not match");
            binding.confirmPasswordInput.requestFocus();
            return;
        }

        binding.actionButton.setEnabled(false);
        binding.actionButton.setText("Resetting...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // Call backend API to update password
        com.example.save.data.network.ResetPasswordRequest request = new com.example.save.data.network.ResetPasswordRequest();
        request.setEmail(userEmail);
        request.setPhone(userPhone);
        request.setNewPassword(newPassword);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.resetPassword(request).enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call, 
                                   retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                binding.loadingIndicator.setVisibility(View.GONE);
                binding.actionButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ResetPasswordActivity.this, "PIN reset successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, PasswordResetSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String error = "Failed to reset PIN";
                    if (response.errorBody() != null) {
                        try { error = response.errorBody().string(); } catch (Exception e) {}
                    }
                    Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                binding.loadingIndicator.setVisibility(View.GONE);
                binding.actionButton.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}