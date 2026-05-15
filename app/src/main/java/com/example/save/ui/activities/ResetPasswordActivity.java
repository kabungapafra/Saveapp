package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityResetpasswordBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * Activity for resetting PIN after OTP verification.
 *
 * Launch modes:
 *  A) With "verificationId" extra  → skip OTP send, show OTP fragment immediately.
 *  B) With "sendOtpOnLaunch=true"  → send Firebase OTP on launch, then show OTP fragment.
 *  C) Neither                      → show PIN-change step directly (legacy / auto-verified).
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetpasswordBinding binding;

    private String userPhone      = "";
    private String mVerificationId = "";
    private String groupName       = "";
    private boolean isOnboarding   = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetpasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userPhone       = getIntent().getStringExtra("phone");
        mVerificationId = getIntent().getStringExtra("verificationId");
        groupName       = getIntent().getStringExtra("groupName");
        isOnboarding    = getIntent().getBooleanExtra("isOnboarding", false);
        boolean sendOnLaunch = getIntent().getBooleanExtra("sendOtpOnLaunch", false);

        // Legacy email field (some callers still pass it)
        String userEmail = getIntent().getStringExtra("email");
        if (userEmail != null) userEmail = userEmail.toLowerCase().trim();

        if ((userPhone == null || userPhone.isEmpty())
                && (userEmail == null || userEmail.isEmpty())) {
            Toast.makeText(this, "Invalid session. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (userEmail != null && !userEmail.isEmpty() && (userPhone == null || userPhone.isEmpty())) {
            // Derive phone from email if caller didn't supply one (legacy)
            userPhone = "";
        }

        mAuth = FirebaseAuth.getInstance();

        setupClickListeners();
        setupBackPressHandler();

        if (mVerificationId != null && !mVerificationId.isEmpty()) {
            // Mode A: verificationId already provided — show OTP screen
            showOtpFragment(mVerificationId);
        } else if (sendOnLaunch && userPhone != null && !userPhone.isEmpty()) {
            // Mode B: send OTP ourselves, then show OTP screen
            sendOtpAndShowFragment(userPhone);
        } else {
            // Mode C: straight to PIN change (auto-verified or legacy)
            showPasswordStep();
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  OTP sending (Mode B)
    // ────────────────────────────────────────────────────────────────────────

    private void sendOtpAndShowFragment(String phone) {
        // Show a loading state while we wait for Firebase
        binding.resetCard.setVisibility(View.GONE);
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.fragmentContainer.setVisibility(View.GONE);

        // Toast for accessibility but UI now handles the visual state
        Toast.makeText(this, "Sending verification code...", Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        // Firebase auto-verified — skip OTP entry, go to PIN change
                        binding.loadingOverlay.setVisibility(View.GONE);
                        binding.fragmentContainer.setVisibility(View.GONE);
                        binding.resetCard.setVisibility(View.VISIBLE);
                        showPasswordStep();
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        binding.resetCard.setVisibility(View.VISIBLE);
                        Toast.makeText(ResetPasswordActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken token) {
                        binding.loadingOverlay.setVisibility(View.GONE);
                        binding.fragmentContainer.setVisibility(View.VISIBLE);
                        mVerificationId = verificationId;
                        // Now show OTP fragment (container is already visible)
                        showOtpFragment(verificationId);
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  OTP fragment (Mode A & B)
    // ────────────────────────────────────────────────────────────────────────

    private void showOtpFragment(String verificationId) {
        if (isFinishing() || isDestroyed()) return;
        
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.resetCard.setVisibility(View.GONE);

        OtpFragment otpFragment = new OtpFragment();
        otpFragment.setOtpListener(new OtpFragment.OtpListener() {
            @Override
            public void onOtpEntered(String code) {
                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(verificationId, code);
                verifyAndShowPinChange(credential);
            }

            @Override
            public void onResendOtp() {
                if (userPhone != null && !userPhone.isEmpty()) {
                    sendOtpAndShowFragment(userPhone);
                } else {
                    Toast.makeText(ResetPasswordActivity.this,
                            "Cannot resend — phone not available.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, otpFragment)
                .commitAllowingStateLoss();
    }

    private void verifyAndShowPinChange(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        binding.fragmentContainer.setVisibility(View.GONE);
                        binding.resetCard.setVisibility(View.VISIBLE);
                        showPasswordStep();
                    } else {
                        Toast.makeText(this, "Invalid code. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ────────────────────────────────────────────────────────────────────────
    //  PIN change step (Mode C / after OTP verified)
    // ────────────────────────────────────────────────────────────────────────

    private void showPasswordStep() {
        binding.actionButton.setText(R.string.reset_pin_button);
        binding.errorMessage.setVisibility(View.GONE);
        binding.newPasswordInput.setText("");
        binding.confirmPasswordInput.setText("");
    }

    private void setupClickListeners() {
        binding.actionButton.setOnClickListener(v -> changePin());
        binding.backToLoginContainer.setOnClickListener(v -> finish());
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void changePin() {
        String newPin     = binding.newPasswordInput.getText().toString();
        String confirmPin = binding.confirmPasswordInput.getText().toString();

        if (!com.example.save.utils.ValidationUtils.isValidPin(newPin)) {
            binding.newPasswordInput.setError("PIN must be exactly 4 digits");
            binding.newPasswordInput.requestFocus();
            return;
        }

        if (!newPin.equals(confirmPin)) {
            binding.confirmPasswordInput.setError("PINs do not match");
            binding.confirmPasswordInput.requestFocus();
            return;
        }

        binding.actionButton.setEnabled(false);
        binding.actionButton.setText("Processing…");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        com.example.save.data.network.ApiService api =
                com.example.save.data.network.RetrofitClient
                        .getClient(this)
                        .create(com.example.save.data.network.ApiService.class);

        if (isOnboarding) {
            com.example.save.data.network.MemberCompleteOnboardingRequest req =
                    new com.example.save.data.network.MemberCompleteOnboardingRequest(userPhone, groupName, newPin);
            
            api.completeMemberOnboarding(req).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call, retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                    binding.loadingIndicator.setVisibility(View.GONE);
                    binding.actionButton.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(ResetPasswordActivity.this, "Onboarding complete!", Toast.LENGTH_SHORT).show();
                        
                        com.example.save.data.network.LoginResponse body = response.body();
                        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(ResetPasswordActivity.this);
                        session.createLoginSession(
                                body.getName(),
                                body.getEmail(),
                                userPhone,
                                body.getRole(),
                                false,
                                body.isCreator());
                        session.saveJwtToken(body.getToken());
                        session.saveLastGroup(groupName);
                        com.example.save.data.network.RetrofitClient.getInstance(getApplicationContext()).updateToken(body.getToken());

                        Intent intent = new Intent(ResetPasswordActivity.this, MemberMainActivity.class);
                        intent.putExtra("member_name", body.getName());
                        intent.putExtra("member_email", body.getEmail());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        String error = "Failed to complete onboarding";
                        if (response.errorBody() != null) {
                            try { error = response.errorBody().string(); }
                            catch (Exception ignored) {}
                        }
                        Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                        binding.actionButton.setText(R.string.reset_pin_button);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                    binding.loadingIndicator.setVisibility(View.GONE);
                    binding.actionButton.setEnabled(true);
                    binding.actionButton.setText(R.string.reset_pin_button);
                    Toast.makeText(ResetPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        com.example.save.data.network.ResetPasswordRequest request =
                new com.example.save.data.network.ResetPasswordRequest();
        request.setPhone(userPhone);
        request.setNewPassword(newPin);

        api.resetPassword(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.actionButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(ResetPasswordActivity.this,
                                    "PIN reset successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPasswordActivity.this,
                                    PasswordResetSuccessActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String error = "Failed to reset PIN";
                            if (response.errorBody() != null) {
                                try { error = response.errorBody().string(); }
                                catch (Exception ignored) {}
                            }
                            Toast.makeText(ResetPasswordActivity.this,
                                    error, Toast.LENGTH_SHORT).show();
                            binding.actionButton.setText(R.string.reset_pin_button);
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.actionButton.setEnabled(true);
                        binding.actionButton.setText(R.string.reset_pin_button);
                        Toast.makeText(ResetPasswordActivity.this,
                                "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}