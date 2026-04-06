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
        // Handle back press
        binding.btnBack.setOnClickListener(v -> finish());
        
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If fragment is visible (OTP), show signup form again
                if (binding.fragmentContainer.getVisibility() == View.VISIBLE) {
                    binding.loginCard.setVisibility(View.VISIBLE);
                    binding.fragmentContainer.setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Signup button click (Next)
        binding.signupButton.setOnClickListener(v -> sendOtp());

        // Already have account - navigate to login
        binding.alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            finish();
        });

        setupPasswordToggles();
    }

    private void sendOtp() {
        // Get input values
        String adminName = binding.adminNameInput.getText().toString().trim();
        String phoneInput = binding.phoneInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim().toLowerCase();
        String password = binding.passwordInput.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

        // Normalize phone number
        String phone = com.example.save.utils.ValidationUtils.normalizePhone(phoneInput);

        // Validate inputs
        if (adminName.isEmpty()) {
            binding.adminNameInput.setError("Full Name is required");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
            binding.phoneInput.setError("Invalid phone number format");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidEmail(email)) {
            binding.emailInput.setError("Invalid email format");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidPassword(password)) {
            binding.passwordInput.setError("Password must be at least 8 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        // Disable button while transitioning
        binding.signupButton.setEnabled(false);
        binding.signupButton.setText("Continuing...");

        // Navigate directly to Group Setup Wizard instead of sending OTP immediately
        Intent intent = new Intent(AdminSignupActivity.this, AdminSetupWizardActivity.class);
        intent.putExtra("ADMIN_NAME", adminName);
        intent.putExtra("PHONE", phone);
        intent.putExtra("EMAIL", email);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void navigateToOtpFragment(String adminName, String groupName, String phone, String email,
            String password) {
        // Hide signup form, show fragment container
        binding.loginCard.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        // Create OTP fragment with admin data
        OtpFragment otp = OtpFragment.newInstanceForRegistration(adminName, groupName, phone, email, password);

        // Replace current view with OTP fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, otp);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void setupPasswordToggles() {
        binding.passwordToggle.setOnClickListener(v -> togglePassword(binding.passwordInput, binding.passwordToggle));
        binding.confirmPasswordToggle
                .setOnClickListener(v -> togglePassword(binding.confirmPasswordInput, binding.confirmPasswordToggle));
    }

    private void togglePassword(android.widget.EditText editText, android.widget.ImageView toggleIcon) {
        boolean isVisible = editText.getTransformationMethod() == null;
        if (isVisible) {
            editText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            editText.setTransformationMethod(null);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
        }
        editText.setSelection(editText.getText().length());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}