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
import com.example.save.ui.utils.OtpUtils;
import com.example.save.databinding.FragmentNwpasswordBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for forgot password flow: Email input → OTP verification → Navigate
 * to password reset
 */
public class NewPasswordFragment extends Fragment {

    // Step 1: Email input
    private FragmentNwpasswordBinding binding;

    // State
    private int currentStep = 1; // 1 = Email, 2 = OTP
    private String userEmail = "";
    private CountDownTimer countDownTimer;

    public NewPasswordFragment() {
        // Required empty public constructor
    }

    public static NewPasswordFragment newInstance() {
        return new NewPasswordFragment();
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

        // Call backend API to send password reset OTP
        com.example.save.data.network.ForgotPasswordRequest request = new com.example.save.data.network.ForgotPasswordRequest(
                email);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);

        apiService.forgotPassword(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(getContext(), "OTP sent to " + email, Toast.LENGTH_SHORT).show();
                                showOtpStep();
                            } else {
                                Toast.makeText(getContext(),
                                        apiResponse.getMessage() != null ? apiResponse.getMessage()
                                                : "Failed to send OTP",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(requireContext(), response);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);
                        com.example.save.utils.ApiErrorHandler.handleError(requireContext(), t);
                    }
                });
    }

    // ========== STEP 2: OTP VERIFICATION ==========

    private void showOtpStep() {
        currentStep = 2;

        // Update title and subtitle
        binding.emailTitle.setText("Verify OTP");
        binding.otpSubtitle.setText("Enter the 6-digit code sent to");

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
        binding.otpDigit1.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit1, binding.otpDigit2));
        binding.otpDigit2.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit2, binding.otpDigit3));
        binding.otpDigit3.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit3, binding.otpDigit4));
        binding.otpDigit4.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit4, binding.otpDigit5));
        binding.otpDigit5.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit5, binding.otpDigit6));
        binding.otpDigit6.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit6, null));

        OtpUtils.setupBackspaceHandler(binding.otpDigit2, binding.otpDigit1);
        OtpUtils.setupBackspaceHandler(binding.otpDigit3, binding.otpDigit2);
        OtpUtils.setupBackspaceHandler(binding.otpDigit4, binding.otpDigit3);
        OtpUtils.setupBackspaceHandler(binding.otpDigit5, binding.otpDigit4);
        OtpUtils.setupBackspaceHandler(binding.otpDigit6, binding.otpDigit5);
    }

    private void startResendTimer() {
        binding.resendOtp.setEnabled(false);
        binding.resendOtp.setTextColor(
                androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
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
                binding.otpDigit4.getText().toString() +
                binding.otpDigit5.getText().toString() +
                binding.otpDigit6.getText().toString();

        if (otp.length() != 6) {
            binding.errorMessage.setText("Please enter complete 6-digit OTP");
            binding.errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        binding.errorMessage.setVisibility(View.GONE);
        binding.verifyButton.setEnabled(false);
        binding.verifyButton.setText("Verifying...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // Call backend API to verify reset OTP
        com.example.save.data.network.OtpVerificationRequest request = new com.example.save.data.network.OtpVerificationRequest(
                userEmail, otp);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);

        apiService.verifyResetOtp(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(getContext(), "OTP verified! Redirecting to reset password...",
                                        Toast.LENGTH_SHORT).show();

                                // Navigate to ResetPasswordActivity to set new password
                                Intent intent = new Intent(getActivity(), ResetPasswordActivity.class);
                                intent.putExtra("email", userEmail);
                                intent.putExtra("verified", true);
                                intent.putExtra("otp", otp); // Pass OTP for password reset

                                if (getActivity() != null) {
                                    intent.putExtra("sourceActivity", getActivity().getClass().getSimpleName());
                                }

                                startActivity(intent);

                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            } else {
                                binding.errorMessage.setText(
                                        apiResponse.getMessage() != null ? apiResponse.getMessage()
                                                : "Invalid OTP. Please try again.");
                                binding.errorMessage.setVisibility(View.VISIBLE);
                                clearOtpFields();
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(requireContext(), response);
                            binding.errorMessage.setText("OTP verification failed. Please try again.");
                            binding.errorMessage.setVisibility(View.VISIBLE);
                            clearOtpFields();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);
                        com.example.save.utils.ApiErrorHandler.handleError(requireContext(), t);
                        binding.errorMessage.setText("Network error. Please try again.");
                        binding.errorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void resendOtpCode() {
        Toast.makeText(getContext(), "Resending OTP...", Toast.LENGTH_SHORT).show();

        // Call backend API to resend reset OTP
        com.example.save.data.network.ForgotPasswordRequest request = new com.example.save.data.network.ForgotPasswordRequest(
                userEmail);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);

        apiService.resendResetOtp(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(getContext(), "OTP sent to " + userEmail, Toast.LENGTH_SHORT).show();
                                startResendTimer();
                                clearOtpFields();
                            } else {
                                Toast.makeText(getContext(),
                                        apiResponse.getMessage() != null ? apiResponse.getMessage()
                                                : "Failed to resend OTP",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(requireContext(), response);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            Throwable t) {
                        com.example.save.utils.ApiErrorHandler.handleError(requireContext(), t);
                    }
                });
    }

    private void clearOtpFields() {
        binding.otpDigit1.setText("");
        binding.otpDigit2.setText("");
        binding.otpDigit3.setText("");
        binding.otpDigit4.setText("");
        binding.otpDigit5.setText("");
        binding.otpDigit6.setText("");
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