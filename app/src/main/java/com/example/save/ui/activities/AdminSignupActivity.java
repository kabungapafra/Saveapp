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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import com.example.save.ui.fragments.OtpFragment;
import com.example.save.R;
import com.example.save.databinding.ActivitySignupBinding;

public class AdminSignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle back press with OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If fragment is visible, show signup form again
                if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
                    binding.loginCard.setVisibility(View.VISIBLE);
                    binding.fragmentContainer.setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Let the system handle back press (exit activity)
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Signup button click
        binding.signupButton.setOnClickListener(v -> sendOtp());

        // Already have account - navigate to login
        binding.alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Handle login tab click from XML (if you have a login tab in signup screen)
     */
    public void onloginClick(View view) {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    /**
     * Validate inputs and send OTP to admin
     */
    /**
     * Validate inputs and send OTP to admin
     */
    private void sendOtp() {
        // Get input values
        String adminName = binding.adminNameInput.getText().toString().trim();
        String groupName = binding.companyInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        // Validate inputs using ValidationUtils
        if (!com.example.save.utils.ValidationUtils.isNotEmpty(adminName)) {
            com.example.save.utils.ValidationUtils.showError(binding.adminNameInput, "Admin Name is required");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isNotEmpty(groupName)) {
            com.example.save.utils.ValidationUtils.showError(binding.companyInput, "Group name is required");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
            com.example.save.utils.ValidationUtils.showError(binding.phoneInput, "Invalid phone number format");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidEmail(email)) {
            com.example.save.utils.ValidationUtils.showError(binding.emailInput, "Invalid email format");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidPassword(password)) {
            com.example.save.utils.ValidationUtils.showError(binding.passwordInput,
                    "Password must be at least 8 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            com.example.save.utils.ValidationUtils.showError(binding.confirmPasswordInput, "Passwords do not match");
            return;
        }

        // Disable button while sending OTP
        binding.signupButton.setEnabled(false);
        binding.signupButton.setText("Sending OTP...");

        // TODO: Call backend API to send OTP
        // POST /auth/admin/send-otp
        // Body: { "phone": "+256" + phone, "email": email }

        // For now, simulate OTP sent
        simulateOtpSent(adminName, groupName, phone, email, password);
    }

    /**
     * TEMPORARY: Simulate OTP sent (replace with actual API call)
     */
    private void simulateOtpSent(String adminName, String groupName, String phone, String email, String password) {
        new Handler().postDelayed(() -> {
            binding.signupButton.setEnabled(true);
            binding.signupButton.setText(getString(R.string.create_account));

            Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();

            // Navigate to OTP fragment
            navigateToOtpFragment(adminName, groupName, phone, email, password);
        }, 1500);
    }

    /**
     * Navigate to OTP fragment
     */
    private void navigateToOtpFragment(String adminName, String groupName, String phone, String email,
            String password) {
        // Hide signup form, show fragment container
        binding.loginCard.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        // Create OTP fragment with admin data - USE THE CORRECT METHOD
        OtpFragment otp = OtpFragment.newInstanceForRegistration(adminName, groupName, phone, email, password);

        // Replace current view with OTP fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, otp);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}