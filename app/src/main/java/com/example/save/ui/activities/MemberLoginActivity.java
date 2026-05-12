package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.example.save.R;
import com.example.save.databinding.ActivityMemberLoginBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.example.save.ui.fragments.OtpFragment;

import java.util.concurrent.TimeUnit;

public class MemberLoginActivity extends AppCompatActivity {

    private ActivityMemberLoginBinding binding;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setBackgroundDrawableResource(R.color.dashboard_bg);

        setupPhoneAuthCallbacks();

        // Animate Logo Image (Heartbeat)
        android.view.animation.Animation heartbeat = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        startCascadingAnimations();
        setupListeners();
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
                    String groupName = binding.groupNameInput.getText().toString().trim();
                    String phone = binding.phoneInput.getText().toString().trim();
                    String password = binding.passwordInput.getText().toString().trim();

                    // Validation
                    if (groupName.isEmpty()) {
                        Toast.makeText(MemberLoginActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                        binding.groupNameInput.requestFocus();
                        return;
                    }
                    if (phone.isEmpty()) {
                        Toast.makeText(MemberLoginActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                        binding.phoneInput.requestFocus();
                        return;
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(MemberLoginActivity.this, "Please enter your PIN", Toast.LENGTH_SHORT).show();
                        binding.passwordInput.requestFocus();
                        return;
                    }

                    binding.loginButton.setEnabled(false);
                    binding.loginButtonText.setText("Verifying...");

                    startPhoneVerification(phone);
                }
            });
            v.startAnimation(press);
        });

        binding.googleButton.setOnClickListener(v -> {
            // Future Google Login
        });

        binding.forgotPasswordText.setOnClickListener(v -> {
            String phone = binding.phoneInput.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number first", Toast.LENGTH_SHORT).show();
                binding.phoneInput.requestFocus();
                return;
            }
            startForgotPinVerification(phone);
        });

        binding.adminPortalLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private void startForgotPinVerification(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Toast.makeText(MemberLoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        mVerificationId = verificationId;
                        Intent intent = new Intent(MemberLoginActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("phone", phone);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
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
                Toast.makeText(MemberLoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        // Need to make sure layout has a fragment container or hide main UI
        // Checking layout for activity_member_login.xml
        // If it doesn't have one, I might need to add it.
        // For now, I'll assume it has a fragmentContainer if it follows the Admin layout pattern.
        
        // Actually, let's check the layout first.
        
        // Assuming it's there for now based on AdminLoginActivity
        binding.loginCard.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        OtpFragment fragment = OtpFragment.newInstanceForRegistration(
                "", 
                binding.groupNameInput.getText().toString(),
                binding.phoneInput.getText().toString(),
                "", 
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
                String phone = binding.phoneInput.getText().toString().trim();
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(MemberLoginActivity.this)
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
                        Toast.makeText(MemberLoginActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performBackendLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest("", password);
        loginRequest.setPhone(phone);
        loginRequest.setLoginType("member");
        loginRequest.setGroupName(groupName);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(MemberLoginActivity.this).create(com.example.save.data.network.ApiService.class);

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

                    Intent intent = new Intent(MemberLoginActivity.this, MemberMainActivity.class);
                    intent.putExtra("member_name", loginResponse.getName());
                    intent.putExtra("member_email", loginResponse.getEmail());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                    finish();
                } else {
                    Toast.makeText(MemberLoginActivity.this, "Login failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                Toast.makeText(MemberLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    @Override
    public void onBackPressed() {
        if (binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            binding.loginCard.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
