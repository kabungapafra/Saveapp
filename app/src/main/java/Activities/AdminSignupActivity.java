package Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import Fragments.Otp;
import com.example.save.R;

public class AdminSignupActivity extends AppCompatActivity {

    private EditText nameInput, phoneInput, emailInput, passwordInput, confirmPasswordInput;
    private Button signupButton;
    private TextView alreadyHaveAccount;
    private CardView loginCard; // This should be signupCard in your layout
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize views
        nameInput = findViewById(R.id.companyInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupButton = findViewById(R.id.signupButton);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);
        loginCard = findViewById(R.id.loginCard); // Update this ID in your XML to signupCard
        fragmentContainer = findViewById(R.id.fragmentContainer);

        // Handle back press with OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If fragment is visible, show signup form again
                if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
                    loginCard.setVisibility(View.VISIBLE);
                    fragmentContainer.setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Let the system handle back press (exit activity)
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Signup button click
        signupButton.setOnClickListener(v -> sendOtp());

        // Already have account - navigate to login
        alreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Handle login tab click from XML (if you have a login tab in signup screen)
     */
    public void onloginClick(View view) {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    /**
     * Validate inputs and send OTP to admin
     */
    private void sendOtp() {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        // Validate inputs
        if (name.isEmpty()) {
            nameInput.setError("Group name is required");
            nameInput.requestFocus();
            return;
        }

        // Phone validation: expecting 9 digits after +256 prefix
        if (phone.isEmpty() || phone.length() != 9) {
            phoneInput.setError("Phone number must be 9 digits");
            phoneInput.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email is required");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Disable button while sending OTP
        signupButton.setEnabled(false);
        signupButton.setText("Sending OTP...");

        // TODO: Call backend API to send OTP
        // POST /auth/admin/send-otp
        // Body: { "phone": "+256" + phone, "email": email }

        // For now, simulate OTP sent
        simulateOtpSent(name, phone, email, password);
    }

    /**
     * TEMPORARY: Simulate OTP sent (replace with actual API call)
     */
    private void simulateOtpSent(String name, String phone, String email, String password) {
        new Handler().postDelayed(() -> {
            signupButton.setEnabled(true);
            signupButton.setText(getString(R.string.create_account));

            Toast.makeText(this, "OTP sent to " + email, Toast.LENGTH_SHORT).show();

            // Navigate to OTP fragment
            navigateToOtpFragment(name, phone, email, password);
        }, 1500);
    }

    /**
     * Navigate to OTP fragment
     */
    private void navigateToOtpFragment(String name, String phone, String email, String password) {
        // Hide signup form, show fragment container
        loginCard.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Create OTP fragment with admin data - USE THE CORRECT METHOD
        Otp otp = Otp.newInstanceForRegistration(name, phone, email, password);

        // Replace current view with OTP fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, otp);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}