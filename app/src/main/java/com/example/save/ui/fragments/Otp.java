package com.example.save.ui.fragments;
import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentOtpBinding;

import com.example.save.ui.activities.AdminmainActivity;

public class Otp extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PASSWORD = "password";

    private static String name;
    private static String phone;
    private static String email;
    private static String password;

    private FragmentOtpBinding binding;
    private CountDownTimer countDownTimer;

    public static Otp newInstance() {
        Otp fragment = new Otp();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    public static Otp newInstanceForRegistration(String name, String phone, String email, String password) {
        Otp fragment = new Otp();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_PHONE, phone);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_PASSWORD, password);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            phone = getArguments().getString(ARG_PHONE);
            email = getArguments().getString(ARG_EMAIL);
            password = getArguments().getString(ARG_PASSWORD);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpBinding.inflate(inflater, container, false);

        // Set the email/phone display
        binding.sentToText.setText(email);

        // Start countdown timer
        startResendTimer();

        // Setup auto-focus
        setupOtpAutoFocus();

        // Verify button
        binding.verifyButton.setOnClickListener(v -> verifyOtp());

        // Resend button
        binding.resendOtp.setOnClickListener(v -> {
            if (binding.resendOtp.isEnabled()) {
                resendOtpCode();
            }
        });

        // Back button
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Help text
        binding.helpText.setOnClickListener(v -> {
            // TODO: Open support/help screen
            Toast.makeText(getContext(), "Contact: support@chama.com", Toast.LENGTH_SHORT).show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Setup auto-focus between OTP digits
     */
    /**
     * Setup auto-focus between OTP digits
     */
    private void setupOtpAutoFocus() {
        binding.otpDigit1.addTextChangedListener(new OtpTextWatcher(binding.otpDigit1, binding.otpDigit2));
        binding.otpDigit2.addTextChangedListener(new OtpTextWatcher(binding.otpDigit2, binding.otpDigit3));
        binding.otpDigit3.addTextChangedListener(new OtpTextWatcher(binding.otpDigit3, binding.otpDigit4));
        binding.otpDigit4.addTextChangedListener(new OtpTextWatcher(binding.otpDigit4, null));

        // Handle backspace
        setupBackspaceHandler(binding.otpDigit2, binding.otpDigit1);
        setupBackspaceHandler(binding.otpDigit3, binding.otpDigit2);
        setupBackspaceHandler(binding.otpDigit4, binding.otpDigit3);
    }

    /**
     * Handle backspace to go to previous field
     */
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

    /**
     * TextWatcher for OTP auto-focus
     */
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

    /**
     * Start countdown timer for resend OTP
     */
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

    /**
     * Verify OTP
     */
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

        // Hide error, show loading
        binding.errorMessage.setVisibility(View.GONE);
        binding.verifyButton.setEnabled(false);
        binding.verifyButton.setText("Verifying...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API to verify OTP and register admin
        // POST /auth/admin/verify-otp
        // Body: { "name": name, "phone": phone, "email": email, "password": password,
        // "otp": otp }

        // For now, simulate verification
        simulateOtpVerification(otp);
    }

    /**
     * TEMPORARY: Simulate OTP verification (replace with actual API call)
     */
    private void simulateOtpVerification(String otp) {
        new android.os.Handler().postDelayed(() -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.verifyButton.setEnabled(true);
            binding.verifyButton.setText("Verify OTP");

            // Check if OTP is correct (for testing, accept "1234")
            if (otp.equals("1234")) {
                Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                // Navigate to MainActivity
                Intent intent = new Intent(getActivity(), AdminmainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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

    /**
     * Resend OTP
     */
    private void resendOtpCode() {
        Toast.makeText(getContext(), "Resending OTP...", Toast.LENGTH_SHORT).show();

        // TODO: Call backend API
        // POST /auth/admin/resend-otp
        // Body: { "email": email }

        // Restart timer
        startResendTimer();
        clearOtpFields();

        Toast.makeText(getContext(), "OTP sent to " + email, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clear all OTP fields
     */
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