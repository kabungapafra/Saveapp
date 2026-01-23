package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberregBinding;
import com.google.android.material.textfield.TextInputEditText;

public class MemberRegistrationActivity extends AppCompatActivity {

    private ActivityMemberregBinding binding;
    private com.example.save.ui.viewmodels.MembersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup click listeners
        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.forgotPassword.setOnClickListener(v -> showForgotPasswordFragment());

        viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.example.save.ui.viewmodels.MembersViewModel.class);

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If fragment is visible, show login form again
                if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
                    binding.loginCard.setVisibility(View.VISIBLE);
                    binding.sideTabContainer.setVisibility(View.VISIBLE);
                    binding.fragmentContainer.setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Show forgot password fragment
     */
    private void showForgotPasswordFragment() {
        // Hide login card and show fragment container
        binding.loginCard.setVisibility(View.GONE);
        binding.sideTabContainer.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        // Show forgot password fragment
        NewPasswordFragment fragment = NewPasswordFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void handleLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String phoneInput = binding.phoneInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim(); // Trimmed

        // Normalize phone number (Remove leading zero if any)
        String phone = com.example.save.utils.ValidationUtils.normalizePhone(phoneInput);

        // Validate inputs using ValidationUtils
        if (!com.example.save.utils.ValidationUtils.isNotEmpty(groupName)) {
            com.example.save.utils.ValidationUtils.showError(binding.groupNameInput, "Group name is required");
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
            com.example.save.utils.ValidationUtils.showError(binding.phoneInput, "Invalid phone number format");
            return;
        }

        // Allow both OTP (6 digits) and regular passwords (8+ characters)
        if (!com.example.save.utils.ValidationUtils.isNotEmpty(password)) {
            com.example.save.utils.ValidationUtils.showError(binding.passwordInput,
                    "Password is required");
            return;
        }

        binding.loginButton.setEnabled(false);
        binding.loginButton.setText("Signing in...");

        // Background thread for database query
        new Thread(() -> {
            Member member = viewModel.getMemberByPhone(phone); // Auth by Phone

            runOnUiThread(() -> {
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("Login");

                // DEBUG: Log member details
                if (member != null) {
                    android.util.Log.d("MemberLogin", "Found member: " + member.getName() +
                            ", Phone: " + member.getPhone() +
                            ", Email: " + member.getEmail() +
                            ", Password in DB: " + member.getPassword() +
                            ", Entered password: " + password +
                            ", isFirstLogin: " + member.isFirstLogin());
                } else {
                    android.util.Log.d("MemberLogin", "No member found for phone: " + phone);
                }

                if (member != null && member.getPassword().equals(password)) {
                    // Check if this is first login with OTP
                    if (member.isFirstLogin()) {
                        // First login - must change password
                        Toast.makeText(MemberRegistrationActivity.this,
                                "Welcome! Please create a new password", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(MemberRegistrationActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("member_email", member.getEmail());
                        intent.putExtra("member_name", member.getName());
                        // First login - clear stack since this is mandatory password change
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        // Normal login - proceed to dashboard
                        Toast.makeText(MemberRegistrationActivity.this, "Welcome " + member.getName(),
                                Toast.LENGTH_SHORT)
                                .show();

                        // SAVE SESSION using SessionManager
                        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                                getApplicationContext());
                        session.createLoginSession(member.getName(), member.getEmail(), member.getRole());

                        Intent intent = new Intent(MemberRegistrationActivity.this, MemberMainActivity.class);
                        intent.putExtra("member_email", member.getEmail()); // Pass email to Dashboard
                        // Clear back stack - user should not go back to login after successful login
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }
                } else {
                    Toast.makeText(MemberRegistrationActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}