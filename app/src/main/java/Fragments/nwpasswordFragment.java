package Fragments;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for forgot password flow: Email input → OTP verification → Navigate
 * to password reset
 */
public class nwpasswordFragment extends Fragment {

    // Step 1: Email input
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailInput;
    private TextView emailTitle, otpSubtitle;

    // Step 2: OTP verification
    private TextView sentToText, resendOtp, timerText, errorMessage;
    private EditText otpDigit1, otpDigit2, otpDigit3, otpDigit4;
    private LinearLayout otpInputContainer, resendContainer;

    // Common
    private Button verifyButton;
    private ProgressBar loadingIndicator;

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
        View view = inflater.inflate(R.layout.fragment_nwpassword, container, false);

        // Initialize all views
        initializeViews(view);

        // Setup click listeners
        setupClickListeners(view);

        // Show email input step first
        showEmailStep();

        return view;
    }

    private void initializeViews(View view) {
        // Email views
        emailTitle = view.findViewById(R.id.emailTitle);
        otpSubtitle = view.findViewById(R.id.otpSubtitle);
        emailInputLayout = view.findViewById(R.id.emailInputLayout);
        emailInput = view.findViewById(R.id.emailInput);

        // OTP views
        sentToText = view.findViewById(R.id.sentToText);
        otpDigit1 = view.findViewById(R.id.otpDigit1);
        otpDigit2 = view.findViewById(R.id.otpDigit2);
        otpDigit3 = view.findViewById(R.id.otpDigit3);
        otpDigit4 = view.findViewById(R.id.otpDigit4);
        otpInputContainer = view.findViewById(R.id.otpInputContainer);
        resendContainer = view.findViewById(R.id.resendContainer);
        resendOtp = view.findViewById(R.id.resendOtp);
        timerText = view.findViewById(R.id.timerText);

        // Common views
        errorMessage = view.findViewById(R.id.errorMessage);
        verifyButton = view.findViewById(R.id.verifyButton);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            if (currentStep == 2) {
                showEmailStep();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        verifyButton.setOnClickListener(v -> {
            if (currentStep == 1) {
                sendOtpToEmail();
            } else if (currentStep == 2) {
                verifyOtp();
            }
        });

        resendOtp.setOnClickListener(v -> {
            if (resendOtp.isEnabled()) {
                resendOtpCode();
            }
        });
    }

    // ========== STEP 1: EMAIL INPUT ==========

    private void showEmailStep() {
        currentStep = 1;

        // Update title and subtitle
        emailTitle.setText("Forgot Password");
        otpSubtitle.setText("Enter your email address to receive a password reset code");

        // Show email views
        emailInputLayout.setVisibility(View.VISIBLE);

        // Hide OTP views
        sentToText.setVisibility(View.GONE);
        otpInputContainer.setVisibility(View.GONE);
        resendContainer.setVisibility(View.GONE);

        // Set button text
        verifyButton.setText("Send Code");
        errorMessage.setVisibility(View.GONE);
    }

    private void sendOtpToEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email is required");
            emailInput.requestFocus();
            return;
        }

        userEmail = email;
        verifyButton.setEnabled(false);
        verifyButton.setText("Sending...");
        loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API
        // POST /auth/forgot-password
        // Body: { "email": email }

        // Simulate sending OTP
        new Handler().postDelayed(() -> {
            loadingIndicator.setVisibility(View.GONE);
            verifyButton.setEnabled(true);
            Toast.makeText(getContext(), "OTP sent to " + email, Toast.LENGTH_SHORT).show();
            showOtpStep();
        }, 1500);
    }

    // ========== STEP 2: OTP VERIFICATION ==========

    private void showOtpStep() {
        currentStep = 2;

        // Update title and subtitle
        emailTitle.setText("Verify OTP");
        otpSubtitle.setText("Enter the 4-digit code sent to");

        // Hide email input
        emailInputLayout.setVisibility(View.GONE);

        // Show OTP views
        sentToText.setVisibility(View.VISIBLE);
        sentToText.setText(userEmail);
        otpInputContainer.setVisibility(View.VISIBLE);
        resendContainer.setVisibility(View.VISIBLE);

        // Set button text
        verifyButton.setText("Verify OTP");
        errorMessage.setVisibility(View.GONE);

        // Setup OTP auto-focus
        setupOtpAutoFocus();

        // Start timer
        startResendTimer();

        // Clear OTP fields
        clearOtpFields();
    }

    private void setupOtpAutoFocus() {
        otpDigit1.addTextChangedListener(new OtpTextWatcher(otpDigit1, otpDigit2));
        otpDigit2.addTextChangedListener(new OtpTextWatcher(otpDigit2, otpDigit3));
        otpDigit3.addTextChangedListener(new OtpTextWatcher(otpDigit3, otpDigit4));
        otpDigit4.addTextChangedListener(new OtpTextWatcher(otpDigit4, null));

        setupBackspaceHandler(otpDigit2, otpDigit1);
        setupBackspaceHandler(otpDigit3, otpDigit2);
        setupBackspaceHandler(otpDigit4, otpDigit3);
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
        resendOtp.setEnabled(false);
        resendOtp.setTextColor(getResources().getColor(android.R.color.darker_gray));
        timerText.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerText.setText(String.format("(0:%02d)", seconds));
            }

            @Override
            public void onFinish() {
                resendOtp.setEnabled(true);
                resendOtp.setTextColor(getResources().getColor(R.color.deep_blue));
                timerText.setVisibility(View.GONE);
            }
        }.start();
    }

    private void verifyOtp() {
        String otp = otpDigit1.getText().toString() +
                otpDigit2.getText().toString() +
                otpDigit3.getText().toString() +
                otpDigit4.getText().toString();

        if (otp.length() != 4) {
            errorMessage.setText("Please enter complete 4-digit OTP");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        errorMessage.setVisibility(View.GONE);
        verifyButton.setEnabled(false);
        verifyButton.setText("Verifying...");
        loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API
        // POST /auth/verify-reset-otp
        // Body: { "email": userEmail, "otp": otp }

        // Simulate verification
        new Handler().postDelayed(() -> {
            loadingIndicator.setVisibility(View.GONE);
            verifyButton.setEnabled(true);

            if (otp.equals("1234")) { // For testing
                Toast.makeText(getContext(), "OTP verified! Redirecting to reset password...", Toast.LENGTH_SHORT)
                        .show();

                // Navigate to ResetPasswordActivity to set new password
                Intent intent = new Intent(getActivity(), Activities.ResetPasswordActivity.class);
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
                errorMessage.setText("Invalid OTP. Please try again.");
                errorMessage.setVisibility(View.VISIBLE);
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
        otpDigit1.setText("");
        otpDigit2.setText("");
        otpDigit3.setText("");
        otpDigit4.setText("");
        otpDigit1.requestFocus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}