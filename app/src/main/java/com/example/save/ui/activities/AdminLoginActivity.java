package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminregBinding;
import com.google.android.material.textfield.TextInputEditText;

public class AdminLoginActivity extends AppCompatActivity {

    private ActivityAdminregBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        // Setup click listeners
        binding.loginButton.setOnClickListener(v -> {
            // Subtle press animation then handle login
            android.view.animation.Animation press = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.login_btn_press);
            press.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override public void onAnimationStart(android.view.animation.Animation a) {}
                @Override public void onAnimationRepeat(android.view.animation.Animation a) {}
                @Override public void onAnimationEnd(android.view.animation.Animation a) {
                    android.view.animation.Animation release = android.view.animation.AnimationUtils.loadAnimation(AdminLoginActivity.this, R.anim.login_btn_release);
                    v.startAnimation(release);
                    handleLogin();
                }
            });
            v.startAnimation(press);
        });
        binding.forgotPassword.setOnClickListener(v -> showForgotPasswordFragment());
        binding.sideSignUpTab.setOnClickListener(this::onsingupClick);
        binding.memberPortalLink.setOnClickListener(v -> navigateToMemberPortal());
        setupPasswordToggle();

        // Animate Logo Image (Heartbeat)
        android.view.animation.Animation heartbeat = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        // Secret Debug Feature: Tap logo 5 times to open Screen Navigator
        final int[] tapCount = {0};
        binding.logoImage.setOnClickListener(v -> {
            tapCount[0]++;
            if (tapCount[0] >= 5) {
                tapCount[0] = 0;
                startActivity(new Intent(this, NavigationTestingActivity.class));
                Toast.makeText(this, "Opening Screen Navigator...", Toast.LENGTH_SHORT).show();
            }
        });

        startCascadingAnimations();

        com.example.save.ui.viewmodels.MembersViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.example.save.ui.viewmodels.MembersViewModel.class);
    }

    private void startCascadingAnimations() {
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.slide_up_fade);

        // Logo and Header
        binding.logoContainer.startAnimation(slideUp);
        
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(150);
        binding.titleText.startAnimation(slideUp);
        binding.subtitleText.startAnimation(slideUp);

        // Form Card
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(300);
        binding.loginCard.startAnimation(slideUp);

        // Links
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(450);
        binding.sideSignUpTab.startAnimation(slideUp);
        binding.memberPortalLink.startAnimation(slideUp);
    }

    public void onsingupClick(View view) {
        Intent intent = new Intent(AdminLoginActivity.this, AdminSignupActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToMemberPortal() {
        Intent intent = new Intent(this, MemberLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    /**
     * Show forgot password fragment
     */
    private void showForgotPasswordFragment() {
        // Navigate directly to ResetPasswordActivity as the "Forgot Password" flow
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("email", binding.emailInput.getText().toString().trim());
        intent.putExtra("sourceActivity", "AdminLoginActivity");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // If fragment is visible, show login form again
        if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            binding.loginCard.setVisibility(View.VISIBLE);
            binding.sideTabContainer.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void handleLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim().toLowerCase();
        String password = binding.passwordInput.getText().toString().trim();

        if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
            // Immediately navigate to AdminMainActivity
            Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
            intent.putExtra("admin_email", email.isEmpty() ? "admin@design.com" : email);
            intent.putExtra("admin_name", "Design Admin");
            intent.putExtra("group_name", groupName.isEmpty() ? "Design Group" : groupName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
            finish();
            return;
        }

        binding.loginButton.setEnabled(false);
        binding.loginButtonText.setText("Signing in...");

        com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest(email, password);
        loginRequest.setLoginType("admin");
        loginRequest.setGroupName(groupName);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.login(loginRequest).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                                   retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse loginResponse = response.body();

                    com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(getApplicationContext());
                    session.createLoginSession(loginResponse.getName(), loginResponse.getEmail(), loginResponse.getRole(), false, loginResponse.isCreator());
                    session.saveJwtToken(loginResponse.getToken());

                    android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("admin_name", loginResponse.getName())
                            .putString("group_name", groupName)
                            .putString("admin_email", loginResponse.getEmail())
                            .apply();

                    Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                    intent.putExtra("admin_email", loginResponse.getEmail());
                    intent.putExtra("admin_name", loginResponse.getName());
                    intent.putExtra("group_name", groupName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                    finish();
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Login failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                Toast.makeText(AdminLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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