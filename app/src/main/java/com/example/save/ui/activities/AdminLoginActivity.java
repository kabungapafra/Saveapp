package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminregBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.save.data.network.FirebaseLoginRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class AdminLoginActivity extends AppCompatActivity {

    private ActivityAdminregBinding binding;
    private com.example.save.ui.viewmodels.MembersViewModel viewModel;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup click listeners
        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.forgotPassword.setOnClickListener(v -> showForgotPasswordFragment());
        setupPasswordToggle();

        viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.example.save.ui.viewmodels.MembersViewModel.class);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.googleSignInButton.setOnClickListener(v -> signIn());
    }

    public void onsingupClick(View view) {
        Intent intent = new Intent(AdminLoginActivity.this, AdminSignupActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Show forgot password fragment
     */
    private void showForgotPasswordFragment() {
        // Hide login card and show fragment container
        binding.loginCard.setVisibility(View.GONE);
        binding.sideTabContainer.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        // Show forgot password fragment
        NewPasswordFragment fragment = NewPasswordFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // If fragment is visible, show login form again
        if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            binding.loginCard.setVisibility(View.VISIBLE);
            binding.sideTabContainer.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void handleLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        // Client-side validation
        if (groupName.isEmpty()) {
            binding.groupNameInput.setError("Group name is required");
            binding.groupNameInput.requestFocus();
            return;
        }

        if (!com.example.save.utils.ValidationUtils.isValidEmail(email)) {
            binding.emailInput.setError("Valid email address is required");
            binding.emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.passwordInput.setError("Password is required");
            binding.passwordInput.requestFocus();
            return;
        }

        binding.loginButton.setEnabled(false);
        binding.loginButton.setText("Signing in...");

        // Directly call Backend Login (Native Email/Password)
        com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest(email,
                password);
        loginRequest.setGroupName(groupName);
        loginRequest.setLoginType("admin");

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.login(loginRequest).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                    retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse loginResponse = response.body();

                    // Verify user is admin
                    if (!"Administrator".equalsIgnoreCase(loginResponse.getRole()) &&
                            !"Admin".equalsIgnoreCase(loginResponse.getRole())) {
                        Toast.makeText(AdminLoginActivity.this,
                                "Access Denied: Please use the Member login portal.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Save session with JWT token
                    com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                            getApplicationContext());
                    session.createLoginSession(loginResponse.getName(), loginResponse.getEmail(),
                            loginResponse.getRole(), false);

                    if (loginResponse.getToken() != null) {
                        session.saveJwtToken(loginResponse.getToken());
                    }

                    // Save to SharedPreferences for backward compatibility
                    android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("admin_name", loginResponse.getName())
                            .putString("group_name", groupName)
                            .putString("admin_email", loginResponse.getEmail())
                            .apply();

                    Toast.makeText(AdminLoginActivity.this, "Welcome " + loginResponse.getName(),
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                    intent.putExtra("admin_email", loginResponse.getEmail());
                    intent.putExtra("admin_name", loginResponse.getName());
                    intent.putExtra("group_name", groupName);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    com.example.save.utils.ApiErrorHandler.handleResponse(AdminLoginActivity.this, response);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("Login");
                com.example.save.utils.ApiErrorHandler.handleError(AdminLoginActivity.this, t);
            }
        });
    }

    private void performBackendLogin(String idToken, String groupName) {
        // Call backend API with Firebase Token
        FirebaseLoginRequest loginRequest = new FirebaseLoginRequest(idToken, groupName);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.firebaseLogin(loginRequest)
                .enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                            retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                        binding.loginButton.setEnabled(true);
                        binding.loginButton.setText("Login");

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.save.data.network.LoginResponse loginResponse = response.body();

                            // Verify user is admin
                            if (!"Administrator".equalsIgnoreCase(loginResponse.getRole()) &&
                                    !"Admin".equalsIgnoreCase(loginResponse.getRole())) {
                                Toast.makeText(AdminLoginActivity.this,
                                        "Access Denied: Please use the Member login portal.", Toast.LENGTH_LONG).show();
                                // Sign out from Firebase if backend says not admin
                                mAuth.signOut();
                                return;
                            }

                            // Save session with JWT token
                            com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                                    getApplicationContext());
                            session.createLoginSession(loginResponse.getName(), loginResponse.getEmail(),
                                    loginResponse.getRole(), false);

                            if (loginResponse.getToken() != null) {
                                session.saveJwtToken(loginResponse.getToken());
                            }

                            // Save to SharedPreferences for backward compatibility
                            android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("admin_name", loginResponse.getName())
                                    .putString("group_name", groupName)
                                    .putString("admin_email", loginResponse.getEmail())
                                    .apply();

                            Toast.makeText(AdminLoginActivity.this, "Welcome " + loginResponse.getName(),
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                            // Pass data if needed, though mostly in session/prefs now
                            intent.putExtra("admin_email", loginResponse.getEmail());
                            intent.putExtra("admin_name", loginResponse.getName());
                            intent.putExtra("group_name", groupName);

                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            com.example.save.utils.ApiErrorHandler.handleResponse(AdminLoginActivity.this, response);
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                            Throwable t) {
                        binding.loginButton.setEnabled(true);
                        binding.loginButton.setText("Login");
                        com.example.save.utils.ApiErrorHandler.handleError(AdminLoginActivity.this, t);
                    }
                });
    }

    private void signIn() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        if (groupName.isEmpty()) {
            binding.groupNameInput.setError("Group name is required");
            binding.groupNameInput.requestFocus();
            return;
        }

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from
        // GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        binding.loginButton.setEnabled(false);
        binding.loginButton.setText("Signing in...");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String firebaseToken = tokenTask.getResult().getToken();
                                String groupName = binding.groupNameInput.getText().toString().trim();
                                performBackendLogin(firebaseToken, groupName);
                            } else {
                                binding.loginButton.setEnabled(true);
                                binding.loginButton.setText("Login");
                                Toast.makeText(AdminLoginActivity.this, "Failed to get token", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
                    } else {
                        // If sign in fails, display a message to the user.
                        binding.loginButton.setEnabled(true);
                        binding.loginButton.setText("Login");
                        Toast.makeText(AdminLoginActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupPasswordToggle() {
        binding.passwordToggle.setOnClickListener(v -> {
            boolean isVisible = binding.passwordInput.getTransformationMethod() == null;
            if (isVisible) {
                binding.passwordInput.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off);
            } else {
                binding.passwordInput.setTransformationMethod(null);
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility);
            }
            binding.passwordInput.setSelection(binding.passwordInput.getText().length());
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}