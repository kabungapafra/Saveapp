package Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity for resetting password after OTP verification
 * This is the final step: New Password → Confirm Password → Back to Login
 */
public class ResetPasswordActivity extends AppCompatActivity {

    // Password change views
    private TextView passwordTitle, passwordSubtitle, errorMessage;
    private TextInputLayout newPasswordInputLayout, confirmPasswordInputLayout;
    private TextInputEditText newPasswordInput, confirmPasswordInput;

    // Common
    private Button actionButton;
    private ProgressBar loadingIndicator;
    private ImageView backButton;

    // State
    private String userEmail = "";
    private String sourceActivity = ""; // Track where user came from

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetpassword);

        // Get email and source activity from intent
        userEmail = getIntent().getStringExtra("email");
        sourceActivity = getIntent().getStringExtra("sourceActivity");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Invalid session. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize all views
        initializeViews();

        // Setup back press handling
        setupBackPressHandler();

        // Setup click listeners
        setupClickListeners();

        // Show password reset step
        showPasswordStep();
    }

    private void initializeViews() {
        // Password views
        passwordTitle = findViewById(R.id.passwordTitle);
        passwordSubtitle = findViewById(R.id.passwordSubtitle);
        newPasswordInputLayout = findViewById(R.id.newPasswordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        // Common views
        errorMessage = findViewById(R.id.errorMessage);
        actionButton = findViewById(R.id.actionButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        backButton = findViewById(R.id.backButton);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        actionButton.setOnClickListener(v -> changePassword());
    }

    private void showPasswordStep() {
        // Show password views
        passwordTitle.setVisibility(View.VISIBLE);
        passwordSubtitle.setVisibility(View.VISIBLE);
        newPasswordInputLayout.setVisibility(View.VISIBLE);
        confirmPasswordInputLayout.setVisibility(View.VISIBLE);

        // Set button text
        actionButton.setText("Change Password");
        errorMessage.setVisibility(View.GONE);

        // Clear password fields
        newPasswordInput.setText("");
        confirmPasswordInput.setText("");
    }

    private void changePassword() {
        String newPassword = newPasswordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (newPassword.isEmpty() || newPassword.length() < 8) {
            newPasswordInput.setError("Password must be at least 8 characters");
            newPasswordInput.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        actionButton.setEnabled(false);
        actionButton.setText("Changing...");
        loadingIndicator.setVisibility(View.VISIBLE);

        // TODO: Call backend API
        // POST /auth/reset-password
        // Body: { "email": userEmail, "newPassword": newPassword }

        // Simulate password change
        new Handler().postDelayed(() -> {
            loadingIndicator.setVisibility(View.GONE);
            actionButton.setEnabled(true);

            Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show();

            // Navigate back to the login page where user clicked forgot password
            Class<?> targetActivity;
            if ("MemberregActivity".equals(sourceActivity)) {
                targetActivity = MemberregActivity.class;
            } else {
                targetActivity = AdminregActivity.class; // Default to admin
            }

            Intent intent = new Intent(this, targetActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }
}