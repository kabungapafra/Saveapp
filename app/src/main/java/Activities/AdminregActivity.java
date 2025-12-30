package Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.google.android.material.textfield.TextInputEditText;

public class AdminregActivity extends AppCompatActivity {

    private TextInputEditText groupNameInput, phoneInput, passwordInput;
    private Button loginButton, signUpTab;
    private TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminreg);

        // Initialize views
        groupNameInput = findViewById(R.id.groupNameInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpTab = findViewById(R.id.signUpTab);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Setup click listeners
        loginButton.setOnClickListener(v -> handleLogin());

        forgotPassword.setOnClickListener(v -> showForgotPasswordFragment());
    }

    public void onsingupClick(View view) {
        Intent intent = new Intent(AdminregActivity.this, SignupActivity.class);
        startActivity(intent);
    }

    /**
     * Show forgot password fragment
     */
    private void showForgotPasswordFragment() {
        // Hide login card and show fragment container
        findViewById(R.id.loginCard).setVisibility(View.GONE);
        findViewById(R.id.sideTabContainer).setVisibility(View.GONE);
        View fragmentContainer = findViewById(R.id.fragmentContainer);
        fragmentContainer.setVisibility(View.VISIBLE);

        // Show forgot password fragment
        Fragments.nwpasswordFragment fragment = Fragments.nwpasswordFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // If fragment is visible, show login form again
        View fragmentContainer = findViewById(R.id.fragmentContainer);
        if (fragmentContainer != null && fragmentContainer.getVisibility() == View.VISIBLE) {
            findViewById(R.id.loginCard).setVisibility(View.VISIBLE);
            findViewById(R.id.sideTabContainer).setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void handleLogin() {
        String groupName = groupNameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (groupName.isEmpty()) {
            groupNameInput.setError("Group name is required");
            groupNameInput.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() != 9) {
            phoneInput.setError("Valid phone number is required");
            phoneInput.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters");
            passwordInput.requestFocus();
            return;
        }

        // TODO: Implement actual login logic with backend
        // For now, just show a toast and navigate to admin main
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(AdminregActivity.this, AdminmainActivity.class);
        startActivity(intent);
        finish();
    }
}