package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.save.R;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.LoginRequest;
import com.example.save.data.network.LoginResponse;
import com.example.save.data.network.RetrofitClient;
import com.example.save.databinding.ActivityWelcomeBackBinding;
import com.example.save.utils.SessionManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WelcomeBackActivity extends AppCompatActivity {

    private ActivityWelcomeBackBinding binding;
    private SessionManager session;
    private String lastPhone;
    private String lastRole;
    private String lastGroup;
    private FirebaseAuth mAuth;
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 4;
    private static final long LOCKOUT_DURATION_MS = 2 * 60 * 60 * 1000; // 2 Hours

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        session = SessionManager.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        // Load persisted user data first
        lastPhone = session.getLastPhone();
        lastRole  = session.getLastRole();
        lastGroup = session.getLastGroup();

        // Set personalised greeting
        String name = session.getLastName();
        if (name != null && !name.isEmpty()) {
            binding.titleText.setText("Welcome back,\n" + name);
        } else {
            binding.titleText.setText("Welcome Back");
        }

        // Show group name in subtitle
        String group = lastGroup;
        if (group != null && !group.isEmpty()) {
            binding.subtitleText.setText("Log in to " + group);
        } else {
            binding.subtitleText.setText("Log in to your Save account");
        }

        startAnimations();
        setupListeners();
        checkLockout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLockout();
    }

    private void checkLockout() {
        long lockoutTime = session.getLockoutTime();
        long currentTime = System.currentTimeMillis();

        if (lockoutTime > currentTime) {
            long remainingMs = lockoutTime - currentTime;
            long hours = TimeUnit.MILLISECONDS.toHours(remainingMs);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) % 60;

            String timeMsg = String.format("Too many attempts. Try again in %dh %dm", hours, minutes);
            binding.passwordInput.setEnabled(false);
            binding.loginButton.setEnabled(false);
            binding.passwordInput.setHint("Locked");
            binding.subtitleText.setText(timeMsg);
            binding.subtitleText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            binding.passwordInput.setEnabled(true);
            binding.loginButton.setEnabled(true);
            binding.passwordInput.setHint("● ● ● ●");
            
            String group = lastGroup;
            if (group != null && !group.isEmpty()) {
                binding.subtitleText.setText("Log in to " + group);
            } else {
                binding.subtitleText.setText("Log in to your Save account");
            }
            binding.subtitleText.setTextColor(getResources().getColor(R.color.v_text_mid));
        }
    }

    private void startAnimations() {
        Animation heartbeat = AnimationUtils.loadAnimation(this, R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        Animation slide = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        binding.logoCard.startAnimation(slide);

        slide = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slide.setStartOffset(150);
        binding.titleText.startAnimation(slide);
        binding.subtitleText.startAnimation(slide);

        slide = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slide.setStartOffset(300);
        binding.loginCard.startAnimation(slide);
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> handleLogin());

        binding.forgotPassword.setOnClickListener(v -> startForgotPinFlow());

        binding.createAccountLink.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        binding.googleButton.setOnClickListener(v ->
                Toast.makeText(this, "Google Login coming soon", Toast.LENGTH_SHORT).show());
    }

    private void startForgotPinFlow() {
        if (lastPhone == null || lastPhone.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // Pass only the phone — ResetPasswordActivity will send the OTP
        // and guarantee the OTP screen appears before PIN change.
        Intent i = new Intent(this, ResetPasswordActivity.class);
        i.putExtra("phone", lastPhone);
        i.putExtra("sendOtpOnLaunch", true);
        startActivity(i);
    }

    private void handleLogin() {
        String password = binding.passwordInput.getText().toString().trim();

        if (password.isEmpty()) {
            binding.passwordInput.setError("Please enter your PIN");
            binding.passwordInput.requestFocus();
            return;
        }

        // Disable button and show loading state
        binding.loginButton.setEnabled(false);
        binding.loginButtonText.setText("Logging in...");
        binding.loginProgress.setVisibility(View.VISIBLE);
        binding.loginArrow.setVisibility(View.GONE);

        // Build login request using remembered credentials
        LoginRequest req = new LoginRequest(null, password);
        req.setPhone(lastPhone);
        req.setLoginType((lastRole == null || lastRole.isEmpty()) ? "member" : lastRole.toLowerCase());
        req.setGroupName(lastGroup);

        ApiService api = RetrofitClient.getClient(this).create(ApiService.class);
        api.login(req).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Restore button state
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                binding.loginProgress.setVisibility(View.GONE);
                binding.loginArrow.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    failedAttempts = 0; // Reset on success
                    session.saveLockoutTime(0); // Clear lockout on success
                    session.saveBackgroundTime(0); // Reset auto-lock timer
                    LoginResponse body = response.body();
                    // ... existing session creation code ...
                    session.createLoginSession(
                            body.getName(),
                            body.getEmail(),
                            lastPhone,
                            body.getRole(),
                            false,
                            body.isCreator());
                    session.saveJwtToken(body.getToken());
                    
                    // Update Retrofit singleton token
                    com.example.save.data.network.RetrofitClient.getInstance(getApplicationContext()).updateToken(body.getToken());

                    session.saveLastGroup(lastGroup);

                    goToMain(body);
                } else {
                    failedAttempts++;
                    String msg = "Invalid PIN. Please try again.";
                    
                    if (failedAttempts >= MAX_ATTEMPTS) {
                        session.saveLockoutTime(System.currentTimeMillis() + LOCKOUT_DURATION_MS);
                        failedAttempts = 0; // Reset counter since we've applied the lock
                        checkLockout();
                        return;
                    }

                    try {
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            if (err.contains("\"detail\":\"")) {
                                msg = err.split("\"detail\":\"")[1].split("\"")[0];
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(WelcomeBackActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Restore button state
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                binding.loginProgress.setVisibility(View.GONE);
                binding.loginArrow.setVisibility(View.VISIBLE);
                Toast.makeText(WelcomeBackActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain(LoginResponse body) {
        Intent intent;
        if ("admin".equalsIgnoreCase(body.getRole())) {
            intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("admin_email", body.getEmail());
            intent.putExtra("admin_name",  body.getName());
            intent.putExtra("group_name",  lastGroup);
        } else {
            intent = new Intent(this, MemberMainActivity.class);
            intent.putExtra("member_name",  body.getName());
            intent.putExtra("member_email", body.getEmail());
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
        finish();
    }
}
