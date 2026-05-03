package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.example.save.R;
import com.example.save.databinding.ActivityMemberLoginBinding;

public class MemberLoginActivity extends AppCompatActivity {

    private ActivityMemberLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setBackgroundDrawableResource(R.color.dashboard_bg);

        // Animate Logo Image (Heartbeat)
        android.view.animation.Animation heartbeat = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        startCascadingAnimations();
        setupListeners();
        setupPasswordToggle();
    }

    private void startCascadingAnimations() {
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.slide_up_fade);

        // Logo and Title
        binding.logoContainer.startAnimation(slideUp);
        
        slideUp.setStartOffset(150);
        binding.titleText.startAnimation(slideUp);
        binding.subtitleText.startAnimation(slideUp);

        // The entire form container (wrapped in ScrollView's LinearLayout)
        // Since the layout uses a nested LinearLayout as the first child of ScrollView, 
        // I'll animate the major visible child.
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(300);
        
        // Find the CardView which is the form card (line 66 in layout)
        // I'll animate the parent of the buttons as well.
        // For simplicity, I'll just animate the two main visual blocks after header.
        binding.loginButton.startAnimation(slideUp);
        binding.googleButton.startAnimation(slideUp);
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> {
            // Smooth press dip
            android.view.animation.Animation press = android.view.animation.AnimationUtils
                    .loadAnimation(this, R.anim.login_btn_press);
            press.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override public void onAnimationStart(android.view.animation.Animation a) {}
                @Override public void onAnimationRepeat(android.view.animation.Animation a) {}
                @Override public void onAnimationEnd(android.view.animation.Animation a) {
                    android.view.animation.Animation release = android.view.animation.AnimationUtils
                            .loadAnimation(MemberLoginActivity.this, R.anim.login_btn_release);
                    v.startAnimation(release);
                    binding.loginButton.setEnabled(false);
                    com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest(
                            binding.emailInput.getText().toString(),
                            binding.passwordInput.getText().toString());
                    loginRequest.setLoginType("member");

                    com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                            .getClient(MemberLoginActivity.this).create(com.example.save.data.network.ApiService.class);

                    apiService.login(loginRequest).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                                               retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                            binding.loginButton.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                com.example.save.data.network.LoginResponse loginResponse = response.body();
                                com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(getApplicationContext());
                                session.createLoginSession(loginResponse.getName(), loginResponse.getEmail(), loginResponse.getRole(), false, loginResponse.isCreator());
                                session.saveJwtToken(loginResponse.getToken());

                                Intent intent = new Intent(MemberLoginActivity.this, MemberMainActivity.class);
                                intent.putExtra("member_name", loginResponse.getName());
                                intent.putExtra("member_email", loginResponse.getEmail());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                                finish();
                            } else {
                                Toast.makeText(MemberLoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                            binding.loginButton.setEnabled(true);
                            Toast.makeText(MemberLoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            v.startAnimation(press);
        });

        binding.googleButton.setOnClickListener(v -> {
            // Future Google Login
        });

        binding.forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            intent.putExtra("email", binding.emailInput.getText().toString().trim());
            intent.putExtra("sourceActivity", "MemberLoginActivity");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.adminPortalLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
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
}
