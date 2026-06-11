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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AdminLoginActivity extends AppCompatActivity {

    private ActivityAdminregBinding binding;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        setupPhoneAuthCallbacks();

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
        binding.forgotPassword.setOnClickListener(v -> {
            String phone = com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString().trim());
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number first", Toast.LENGTH_SHORT).show();
                binding.phoneInput.requestFocus();
                return;
            }
            startForgotPinVerification(phone);
        });
        binding.sideSignUpTab.setOnClickListener(this::onsingupClick);
        binding.memberPortalLink.setOnClickListener(v -> navigateToMemberPortal());
        binding.passwordToggle.setOnClickListener(v -> togglePassword(binding.passwordInput, binding.passwordToggle));

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
        Intent intent = new Intent(this, OtpRequestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void startForgotPinVerification(String phone) {
        // Pass only the phone — ResetPasswordActivity will own the entire OTP flow.
        // This guarantees the OTP screen always shows before the PIN change screen.
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("sendOtpOnLaunch", true);
        startActivity(intent);
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
        String rawPhone = binding.phoneInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        // Validation
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show();
            binding.groupNameInput.requestFocus();
            return;
        }
        if (rawPhone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            binding.phoneInput.requestFocus();
            return;
        }
        
        String phone = com.example.save.utils.ValidationUtils.normalizePhone(rawPhone);
        if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            binding.phoneInput.requestFocus();
            return;
        }

        // Validation for 4‑digit numeric PIN
        if (!password.matches("\\d{4}")) {
            Toast.makeText(this, "PIN must be exactly 4 numeric digits", Toast.LENGTH_SHORT).show();
            binding.passwordInput.requestFocus();
            return;
        }
        // Trim any whitespace just in case
        password = password.trim();
        // Enforce 4‑digit PIN length
        if (password.length() != 4) {
            Toast.makeText(this, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show();
            binding.passwordInput.requestFocus();
            return;
        }

        // Disable button and show OTP sending status
        binding.loginButton.setEnabled(false);
        binding.loginButtonText.setText("Sending OTP...");

        // Initiate Firebase phone verification
        startPhoneVerification(phone);
    }

    private void startPhoneVerification(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void setupPhoneAuthCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                Toast.makeText(AdminLoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                showOtpFragment();
            }
        };
    }

    private void showOtpFragment() {
        binding.loginCard.setVisibility(View.GONE);
        binding.sideTabContainer.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        OtpFragment fragment = OtpFragment.newInstanceForRegistration(
                "", // name not needed for login
                binding.groupNameInput.getText().toString(),
                com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString()),
                "", // email empty
                binding.passwordInput.getText().toString()
        );
        
        fragment.setOtpListener(new OtpFragment.OtpListener() {
            @Override
            public void onOtpEntered(String code) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onResendOtp() {
                String phone = com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString().trim());
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(AdminLoginActivity.this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(mResendToken)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        performBackendLogin();
                    } else {
                        binding.loginButton.setEnabled(true);
                        binding.loginButtonText.setText("Login");
                        Toast.makeText(AdminLoginActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performBackendLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String phone = com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString().trim());
        String password = binding.passwordInput.getText().toString().trim();

        com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest(phone, password);
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
                    session.createLoginSession(loginResponse.getName(), phone, loginResponse.getRole(), false, loginResponse.isCreator());
                    session.saveLastGroup(groupName);
                    session.saveJwtToken(loginResponse.getToken());

                    // Update Retrofit singleton token
                    com.example.save.data.network.RetrofitClient.getInstance(getApplicationContext()).updateToken(loginResponse.getToken());

                    // Restore all financial rule settings from server after login
                    com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                            .fetchSystemConfig(null);

                    android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("admin_name", loginResponse.getName())
                            .putString("group_name", groupName)
                            .apply();

                    Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                    intent.putExtra("admin_name", loginResponse.getName());
                    intent.putExtra("group_name", groupName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                    finish();
                } else {
                    String errorMessage = "Login failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            // Simple extraction if it's a FastAPI detail JSON
                            if (errorJson.contains("\"detail\":\"")) {
                                errorMessage = errorJson.split("\"detail\":\"")[1].split("\"")[0];
                            } else {
                                errorMessage = "Error: " + response.code();
                            }
                        }
                    } catch (Exception e) {
                        errorMessage = "Login failed: " + response.message();
                    }
                    Toast.makeText(AdminLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
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



    private void togglePassword(android.widget.EditText editText, android.widget.ImageView toggleIcon) {
        boolean isVisible = editText.getTransformationMethod() == null;
        if (isVisible) {
            editText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
            toggleIcon.setAlpha(0.5f);
        } else {
            editText.setTransformationMethod(null);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
            toggleIcon.setAlpha(0.9f);
        }
        editText.setSelection(editText.getText().length());
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}