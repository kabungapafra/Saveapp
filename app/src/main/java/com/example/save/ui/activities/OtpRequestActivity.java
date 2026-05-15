package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityOtpRequestBinding;

public class OtpRequestActivity extends AppCompatActivity {

    private ActivityOtpRequestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Standard theme setup
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setBackgroundDrawableResource(R.color.dashboard_bg);

        // Animate Logo Image (Heartbeat)
        Animation heartbeat = AnimationUtils.loadAnimation(this, R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        startCascadingAnimations();
        setupListeners();
    }

    private void startCascadingAnimations() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);

        // Logo and Title
        binding.logoContainer.startAnimation(slideUp);
        
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(150);
        binding.titleText.startAnimation(slideUp);
        binding.subtitleText.startAnimation(slideUp);

        // Form Card
        slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(300);
        binding.loginCard.startAnimation(slideUp);
    }

    private void setupListeners() {
        binding.getOtpButton.setOnClickListener(v -> {
            // Button press animation
            Animation press = AnimationUtils.loadAnimation(this, R.anim.login_btn_press);
            press.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override public void onAnimationEnd(Animation a) {
                    Animation release = AnimationUtils.loadAnimation(OtpRequestActivity.this, R.anim.login_btn_release);
                    v.startAnimation(release);
                    
                    handleOtpRequest();
                }
            });
            v.startAnimation(press);
        });

        binding.loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(OtpRequestActivity.this, MemberLoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void handleOtpRequest() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Check with Backend if user is pending
        com.example.save.data.network.ApiService api = com.example.save.data.network.RetrofitClient.getClient(this).create(com.example.save.data.network.ApiService.class);
        api.checkOnboardingPhone(new com.example.save.data.network.OnboardingCheckRequest(phone, groupName))
                .enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call, retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // 2. Trigger Firebase Phone Auth
                    sendFirebaseOtp(phone, groupName);
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Validation failed";
                    Toast.makeText(OtpRequestActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                Toast.makeText(OtpRequestActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFirebaseOtp(String phone, String groupName) {
        com.google.firebase.auth.PhoneAuthOptions options =
                com.google.firebase.auth.PhoneAuthOptions.newBuilder(com.google.firebase.auth.FirebaseAuth.getInstance())
                        .setPhoneNumber(phone)
                        .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(com.google.firebase.auth.PhoneAuthCredential credential) {
                                // Auto-verification (rarely happens on first try)
                            }

                            @Override
                            public void onVerificationFailed(com.google.firebase.FirebaseException e) {
                                Toast.makeText(OtpRequestActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onCodeSent(String verificationId, com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken token) {
                                Intent intent = new Intent(OtpRequestActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phone", phone);
                                intent.putExtra("groupName", groupName);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                        })
                        .build();
        com.google.firebase.auth.PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
