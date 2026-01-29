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
        setupPasswordToggle();

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

        // Call backend API for authentication
        com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest(
                phone, password, groupName, "member");

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.login(loginRequest).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                    retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse loginResponse = response.body();

                    // Verify user is not admin
                    if ("Administrator".equalsIgnoreCase(loginResponse.getRole()) ||
                            "Admin".equalsIgnoreCase(loginResponse.getRole())) {
                        Toast.makeText(MemberRegistrationActivity.this,
                                "Access Denied: Please use the Admin login portal.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Save session with JWT token
                    com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                            getApplicationContext());
                    session.createLoginSession(loginResponse.getName(), loginResponse.getEmail(),
                            loginResponse.getRole(), loginResponse.isFirstLogin());

                    if (loginResponse.getToken() != null) {
                        session.saveJwtToken(loginResponse.getToken());
                    }

                    Toast.makeText(MemberRegistrationActivity.this, "Welcome " + loginResponse.getName(),
                            Toast.LENGTH_SHORT).show();

                    // Check if this is first login - redirect to change password
                    if (loginResponse.isFirstLogin()) {
                        Intent intent = new Intent(MemberRegistrationActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("member_email", loginResponse.getEmail());
                        intent.putExtra("member_name", loginResponse.getName());
                        intent.putExtra("current_password", password); // Pass the OTP used for login
                        intent.putExtra("is_first_login", true);

                        // Clear back stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    } else {
                        // Normal login - go to main activity
                        Intent intent = new Intent(MemberRegistrationActivity.this, MemberMainActivity.class);
                        intent.putExtra("member_email", loginResponse.getEmail());

                        // Clear back stack
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }
                } else {
                    com.example.save.utils.ApiErrorHandler.handleResponse(MemberRegistrationActivity.this, response);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("Login");
                com.example.save.utils.ApiErrorHandler.handleError(MemberRegistrationActivity.this, t);
            }
        });
    }

    private void setupPasswordToggle() {
        binding.passwordToggle.setOnClickListener(v -> {
            boolean isVisible = binding.passwordInput.getTransformationMethod() == null;
            if (isVisible) {
                binding.passwordInput.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off);
            } else {
                binding.passwordInput.setTransformationMethod(null);
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility);
            }
            binding.passwordInput.setSelection(binding.passwordInput.getText().length());
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}