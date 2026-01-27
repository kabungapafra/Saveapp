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
import com.example.save.ui.utils.OtpUtils;
import com.example.save.databinding.FragmentOtpBinding;

import com.example.save.ui.activities.AdminMainActivity;

public class OtpFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_GROUP_NAME = "group_name"; // New Argument
    private static final String ARG_PHONE = "phone";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PASSWORD = "password";

    private static String name;
    private static String groupName; // New Field
    private static String phone;
    private static String email;
    private static String password;

    private FragmentOtpBinding binding;
    private CountDownTimer countDownTimer;

    public static OtpFragment newInstance() {
        return new OtpFragment();
    }

    public static OtpFragment newInstanceForRegistration(String name, String groupName, String phone, String email,
            String password) {
        OtpFragment fragment = new OtpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_GROUP_NAME, groupName);
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
            groupName = getArguments().getString(ARG_GROUP_NAME);
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
    private void setupOtpAutoFocus() {
        binding.otpDigit1.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit1, binding.otpDigit2));
        binding.otpDigit2.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit2, binding.otpDigit3));
        binding.otpDigit3.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit3, binding.otpDigit4));
        binding.otpDigit4.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit4, binding.otpDigit5));
        binding.otpDigit5.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit5, binding.otpDigit6));
        binding.otpDigit6.addTextChangedListener(new OtpUtils.OtpTextWatcher(binding.otpDigit6, null));

        // Handle backspace
        OtpUtils.setupBackspaceHandler(binding.otpDigit2, binding.otpDigit1);
        OtpUtils.setupBackspaceHandler(binding.otpDigit3, binding.otpDigit2);
        OtpUtils.setupBackspaceHandler(binding.otpDigit4, binding.otpDigit3);
        OtpUtils.setupBackspaceHandler(binding.otpDigit5, binding.otpDigit4);
        OtpUtils.setupBackspaceHandler(binding.otpDigit6, binding.otpDigit5);
    }

    /**
     * Start countdown timer for resend OTP
     */
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

    /**
     * Verify OTP
     */
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

        // Hide error, show loading
        binding.errorMessage.setVisibility(View.GONE);
        binding.verifyButton.setEnabled(false);
        binding.verifyButton.setText("Verifying...");
        binding.loadingIndicator.setVisibility(View.VISIBLE);

        // Call backend API to verify OTP and register admin
        com.example.save.data.network.OtpVerificationRequest request = new com.example.save.data.network.OtpVerificationRequest(
                name, phone, email, password, otp, groupName);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);

        apiService.verifyAdminOtp(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                            retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);
                        binding.verifyButton.setText("Verify OTP");

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.LoginResponse loginResponse = response.body();

                            // Save session
                            com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                                    requireContext());
                            session.createLoginSession(loginResponse.getName(), loginResponse.getEmail(),
                                    loginResponse.getRole());

                            if (loginResponse.getToken() != null) {
                                session.saveJwtToken(loginResponse.getToken());
                            }

                            // Save group info
                            if (getActivity() != null) {
                                android.content.SharedPreferences prefs = getActivity()
                                        .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
                                android.content.SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("group_name", groupName);
                                editor.putString("admin_name", loginResponse.getName());
                                editor.putString("admin_email", loginResponse.getEmail());
                                editor.apply();

                                Toast.makeText(getContext(), "Account created! Please login.",
                                        Toast.LENGTH_LONG).show();

                                // Navigate to Login
                                Intent intent = new Intent(getActivity(), AdminLoginActivity.class);
                                intent.putExtra("PHONE", phone);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                getActivity().finish();
                            }
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(requireContext(), response);
                            binding.errorMessage.setText("OTP verification failed. Please try again.");
                            binding.errorMessage.setVisibility(View.VISIBLE);
                            clearOtpFields();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                            Throwable t) {
                        binding.loadingIndicator.setVisibility(View.GONE);
                        binding.verifyButton.setEnabled(true);
                        binding.verifyButton.setText("Verify OTP");
                        com.example.save.utils.ApiErrorHandler.handleError(requireContext(), t);
                        binding.errorMessage.setText("Network error. Please try again.");
                        binding.errorMessage.setVisibility(View.VISIBLE);
                    }
                });
    }

    // NOTE: simulateOtpVerification removed - now uses backend API
    // Backend will verify OTP, hash password, create admin account, and return JWT
    // token

    /**
     * Resend OTP - Now uses backend API
     */
    private void resendOtpCode() {
        Toast.makeText(getContext(), "Resending OTP...", Toast.LENGTH_SHORT).show();

        com.example.save.data.network.OtpRequest request = new com.example.save.data.network.OtpRequest(phone, email);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);

        apiService.resendAdminOtp(request).enqueue(
                new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<com.example.save.data.network.ApiResponse> call,
                            retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.ApiResponse apiResponse = response.body();
                            if (apiResponse.isSuccess()) {
                                Toast.makeText(getContext(), "OTP sent to " + email, Toast.LENGTH_SHORT).show();
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

    /**
     * Clear all OTP fields
     */
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