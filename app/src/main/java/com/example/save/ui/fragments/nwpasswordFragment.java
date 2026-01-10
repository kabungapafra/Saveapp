package com.example.save.ui.fragments;
import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentNwpasswordBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for forgot password flow: Email input → OTP verification → Navigate
 * to password reset
 */
public class nwpasswordFragment extends Fragment {

    // Step 1: Email input
    private FragmentNwpasswordBinding binding;

    // State
    private int currentStep = 1; // 1 = Email, 2 = OTP
    private String userEmail = "";
    private CountDownTimer countDownTimer;

    public nwpasswordFragment() {
        // Required empty public constructor
    }

    public static nwpasswordFragment newInstance() {
        return new nwpasswordFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentNwpasswordBinding.inflate(inflater, container, false);

        // Setup click listeners
        setupClickListeners();

        // Show email input step first
        showEmailStep();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // initializeViews removed as binding handles it

    // initializeViews removed as Binding handles it

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> {
            if (currentStep == 2) {
                showEmailStep();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.verifyButton.setOnClickListener(v -> {
            if (currentStep == 1) {
                sendOtpToEmail();
            } else if (currentStep == 2) {
                verifyOtp();
            }
        });

        binding.resendOtp.setOnClickListener(v -> {
            if (binding.resendOtp.isEnabled()) {
                resendOtpCode();
            }
        });
    }

    // ========== STEP 1: EMAIL INPUT ==========

    private void showEmailStep() {
        currentStep = 1;

        // Update title and subtitle
        binding.emailTitle.setText("Forgot Password");
        binding.otpSubtitle.setText("Enter your email address to receive a password reset code");

        // Show email views
        binding.emailInputLayout.setVisibility(View.VISIBLE);

        // Hide OTP views
        binding.sentToText.setVisibility(View.GONE);
        binding.otpInputContainer.setVisibility(View.GONE);
        binding.resendContainer.setVisibility(View.GONE);

        // Set button text
        binding.verifyButton.setText("Send Code");
        binding.errorMessage.setVisibility(View.GONE);
    }

    private void sendOtpToEmail() {
        String email = binding.emailInput.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInput.setError("Valid email is required");
            binding.emailInput.requestFocus();
            return;
        }

        userEmail = email;
        binding.verifyButton.setEnabled(false);
        binding.verifyButton.setText("Sending...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API
        // POST /auth/forgot-password
        // Body: { "email": email }

        // Simulate sending OTP
        new Handler().postDelayed(() -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.verifyButton.setEnabled(true);
            Toast.makeText(getContext(), "OTP sent to " + email, Toast.LENGTH_SHORT).show();
            showOtpStep();
        }, 1500);
    }

    // ========== STEP 2: OTP VERIFICATION ==========

    private void showOtpStep() {
        currentStep = 2;

        // Update title and subtitle
        binding.emailTitle.setText("Verify OTP");
        binding.otpSubtitle.setText("Enter the 4-digit code sent to");

        // Hide email input
        binding.emailInputLayout.setVisibility(View.GONE);

        // Show OTP views
        binding.sentToText.setVisibility(View.VISIBLE);
        binding.sentToText.setText(userEmail);
        binding.otpInputContainer.setVisibility(View.VISIBLE);
        binding.resendContainer.setVisibility(View.VISIBLE);

        // Set button text
        binding.verifyButton.setText("Verify OTP");
        binding.errorMessage.setVisibility(View.GONE);

        // Setup OTP auto-focus
        setupOtpAutoFocus();

        // Start timer
        startResendTimer();

        // Clear OTP fields
        clearOtpFields();
    }

    private void setupOtpAutoFocus() {
        binding.otpDigit1.addTextChangedListener(new OtpTextWatcher(binding.otpDigit1, binding.otpDigit2));
        binding.otpDigit2.addTextChangedListener(new OtpTextWatcher(binding.otpDigit2, binding.otpDigit3));
        binding.otpDigit3.addTextChangedListener(new OtpTextWatcher(binding.otpDigit3, binding.otpDigit4));
        binding.otpDigit4.addTextChangedListener(new OtpTextWatcher(binding.otpDigit4, null));

        setupBackspaceHandler(binding.otpDigit2, binding.otpDigit1);
        setupBackspaceHandler(binding.otpDigit3, binding.otpDigit2);
        setupBackspaceHandler(binding.otpDigit4, binding.otpDigit3);
    }

    private void setupBackspaceHandler(EditText current, EditText previous) {
        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (current.getText().toString().isEmpty() && previous != null) {
                    previous.requestFocus();
                    return true;
                }
            }
            return false;
        });
    }

    private class OtpTextWatcher implements TextWatcher {
        private final EditText currentView;
        private final EditText nextView;

        OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }
    }

    private void startResendTimer() {
        binding.resendOtp.setEnabled(false);
        binding.resendOtp.setTextColor(getResources().getColor(android.R.color.darker_gray));
        binding.timerText.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                binding.timerText.setText(String.format("(0:%02d)", seconds));
            }

            @Override
            public void onFinish() {
                binding.resendOtp.setEnabled(true);
                binding.resendOtp.setTextColor(getResources().getColor(R.color.deep_blue));
                binding.timerText.setVisibility(View.GONE);
            }
        }.start();
    }

    private void verifyOtp() {
        String otp = binding.otpDigit1.getText().toString() +
                binding.otpDigit2.getText().toString() +
                binding.otpDigit3.getText().toString() +
                binding.otpDigit4.getText().toString();

        if (otp.length() != 4) {
            binding.errorMessage.setText("Please enter complete 4-digit OTP");
            binding.errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        binding.errorMessage.setVisibility(View.GONE);
        binding.verifyButton.setEnabled(false);
        binding.verifyButton.setText("Verifying...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API
        // POST /auth/verify-reset-otp
        // Body: { "email": userEmail, "otp": otp }

        // Simulate verification
        new Handler().postDelayed(() -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.verifyButton.setEnabled(true);

            if (otp.equals("1234")) { // For testing
                Toast.makeText(getContext(), "OTP verified! Redirecting to reset password...", Toast.LENGTH_SHORT)
                        .show();

                // Navigate to ResetPasswordActivity to set new password
                Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
                intent.putExtra("email", userEmail);
                intent.putExtra("verified", true);

                // Pass the source activity code so we know where to return
                if (getActivity() != null) {
                    intent.putExtra("sourceActivity", getActivity().getClass().getSimpleName());
                }

                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().finish();
                }
            } else {
                binding.errorMessage.setText("Invalid OTP. Please try again.");
                binding.errorMessage.setVisibility(View.VISIBLE);
                clearOtpFields();
            }
        }, 2000);
    }

    private void resendOtpCode() {
        Toast.makeText(getContext(), "Resending OTP...", Toast.LENGTH_SHORT).show();

        // TODO: Call backend API
        // POST /auth/resend-reset-otp
        // Body: { "email": userEmail }

        startResendTimer();
        clearOtpFields();
        Toast.makeText(getContext(), "OTP sent to " + userEmail, Toast.LENGTH_SHORT).show();
    }

    private void clearOtpFields() {
        binding.otpDigit1.setText("");
        binding.otpDigit2.setText("");
        binding.otpDigit3.setText("");
        binding.otpDigit4.setText("");
        binding.otpDigit1.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}