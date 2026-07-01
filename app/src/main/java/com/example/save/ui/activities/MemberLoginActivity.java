package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.example.save.R;
import com.example.save.databinding.ActivityMemberLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.android.material.snackbar.Snackbar;
import com.example.save.ui.fragments.OtpFragment;

import java.util.concurrent.TimeUnit;

public class MemberLoginActivity extends AppCompatActivity {

    private ActivityMemberLoginBinding binding;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        getWindow().setBackgroundDrawableResource(R.color.dashboard_bg);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGoogleSignInResult(result.getData()));

        setupPhoneAuthCallbacks();

        // Animate Logo Image (Heartbeat)
        android.view.animation.Animation heartbeat = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        startCascadingAnimations();
        setupListeners();
    }

    private void startCascadingAnimations() {
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.slide_up_fade);

        // Logo and Title
        binding.logoContainer.startAnimation(slideUp);
        
        slideUp.setStartOffset(150);
        binding.titleText.startAnimation(slideUp);
        binding.subtitleText.startAnimation(slideUp);

        // The entire form container (wrapped in ScrollView's LinearLayout)
        // Since the layout uses a nested LinearLayout as the first child of ScrollView, 
        // I'll animate the major visible child.
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(300);
        
        // Find the CardView which is the form card (line 66 in layout)
        // I'll animate the parent of the buttons as well.
        // For simplicity, I'll just animate the two main visual blocks after header.
        binding.loginButton.startAnimation(slideUp);
        binding.googleButton.startAnimation(slideUp);
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> {
            // Smooth press dip
            android.view.animation.Animation press = android.view.animation.AnimationUtils
                    .loadAnimation(this, R.anim.login_btn_press);
            press.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override public void onAnimationStart(android.view.animation.Animation a) {}
                @Override public void onAnimationRepeat(android.view.animation.Animation a) {}
                @Override public void onAnimationEnd(android.view.animation.Animation a) {
                    android.view.animation.Animation release = android.view.animation.AnimationUtils
                            .loadAnimation(MemberLoginActivity.this, R.anim.login_btn_release);
                    v.startAnimation(release);
                    String groupName = binding.groupNameInput.getText().toString().trim();
                    String rawPhone = binding.phoneInput.getText().toString().trim();
                    String password = binding.passwordInput.getText().toString().trim();

                    // Validation
                    if (groupName.isEmpty()) {
                        Toast.makeText(MemberLoginActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                        binding.groupNameInput.requestFocus();
                        return;
                    }
                    if (rawPhone.isEmpty()) {
                        Toast.makeText(MemberLoginActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                        binding.phoneInput.requestFocus();
                        return;
                    }
                    
                    String phone = com.example.save.utils.ValidationUtils.normalizePhone(rawPhone);
                    if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
                        Toast.makeText(MemberLoginActivity.this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
                        binding.phoneInput.requestFocus();
                        return;
                    }

                    if (password.isEmpty()) {
                        Toast.makeText(MemberLoginActivity.this, "Please enter your PIN", Toast.LENGTH_SHORT).show();
                        binding.passwordInput.requestFocus();
                        return;
                    }

                    binding.loginButton.setEnabled(false);
                    binding.loginButtonText.setText("Logging in...");

                    performBackendLogin();
                }
            });
            v.startAnimation(press);
        });

        binding.googleButton.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task ->
                    googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent()));
        });

        binding.forgotPasswordText.setOnClickListener(v -> {
            String phone = com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString().trim());
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number first", Toast.LENGTH_SHORT).show();
                binding.phoneInput.requestFocus();
                return;
            }
            startForgotPinVerification(phone);
        });

        binding.adminPortalLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        binding.joinGroupLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, OtpRequestActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        binding.passwordToggle.setOnClickListener(v -> togglePassword(binding.passwordInput, binding.passwordToggle));
    }

    private void togglePassword(android.widget.EditText editText, android.widget.ImageView toggleIcon) {
        boolean isVisible = editText.getTransformationMethod() == null;
        if (isVisible) {
            editText.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
            toggleIcon.setImageResource(R.drawable.ic_visibility_off);
            toggleIcon.setAlpha(0.5f);
        } else {
            editText.setTransformationMethod(null);
            toggleIcon.setImageResource(R.drawable.ic_visibility);
            toggleIcon.setAlpha(0.9f);
        }
        editText.setSelection(editText.getText().length());
    }

    private void startForgotPinVerification(String phone) {
        // Pass only the phone — ResetPasswordActivity will own the entire OTP flow.
        // This guarantees the OTP screen always shows before the PIN change screen.
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("sendOtpOnLaunch", true);
        startActivity(intent);
    }

    private void startPhoneVerification(String phone) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void setupPhoneAuthCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override

public void onVerificationFailed(FirebaseException e) {
    binding.loginButton.setEnabled(true);
    binding.loginButtonText.setText("Login");
    String msg;
    if (e.getMessage() != null && e.getMessage().toLowerCase().contains("too many requests")) {
        msg = "Too many attempts, please wait a few minutes";
    } else {
        msg = "Verification failed: " + e.getMessage();
    }
    Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
}

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                showOtpFragment();
            }
        };
    }

    private void showOtpFragment() {
        // Need to make sure layout has a fragment container or hide main UI
        // Checking layout for activity_member_login.xml
        // If it doesn't have one, I might need to add it.
        // For now, I'll assume it has a fragmentContainer if it follows the Admin layout pattern.
        
        // Actually, let's check the layout first.
        
        // Assuming it's there for now based on AdminLoginActivity
        binding.loginCard.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        OtpFragment fragment = OtpFragment.newInstanceForRegistration(
                "", 
                binding.groupNameInput.getText().toString(),
                com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString()),
                "", 
                binding.passwordInput.getText().toString()
        );
        
        fragment.setOtpListener(new OtpFragment.OtpListener() {
            @Override
            public void onOtpEntered(String code) {
                                        if (mVerificationId == null) {
                            Snackbar.make(binding.getRoot(), "Verification failed, please try again", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                        signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onResendOtp() {
                String phone = com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString().trim());
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(MemberLoginActivity.this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(mResendToken)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);
            }
        });
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        performBackendLogin();
                    } else {
                        binding.loginButton.setEnabled(true);
                        binding.loginButtonText.setText("Login");
                        Snackbar.make(binding.getRoot(), "Verification failed: Invalid code", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void performBackendLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String phone = com.example.save.utils.ValidationUtils.normalizePhone(binding.phoneInput.getText().toString().trim());
        String password = binding.passwordInput.getText().toString().trim();

        com.example.save.data.network.LoginRequest loginRequest = new com.example.save.data.network.LoginRequest(phone, password);
        loginRequest.setLoginType("member");
        loginRequest.setGroupName(groupName);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(MemberLoginActivity.this).create(com.example.save.data.network.ApiService.class);

        apiService.login(loginRequest).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                                   retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse loginResponse = response.body();

                    com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(getApplicationContext());
                    session.createLoginSession(loginResponse.getName(), phone, loginResponse.getRole(), false, loginResponse.isCreator());
                    session.saveLastGroup(groupName);
                    session.saveJwtToken(loginResponse.getToken());

                    // Restore all financial rule settings from server after login
                    com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                            .fetchSystemConfig(null);

                    // Register FCM token with server for push notifications
                    com.example.save.services.SaveFirebaseMessagingService
                            .registerTokenWithServer(getApplicationContext());

                    Intent intent = new Intent(MemberLoginActivity.this, MemberMainActivity.class);
                    intent.putExtra("member_name", loginResponse.getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                    finish();
                } else {
                    String errorMessage;
                    try {
                        okhttp3.ResponseBody errBody = response.errorBody();
                        if (errBody == null) {
                            errorMessage = "Login error (code " + response.code() + ")";
                        } else {
                            String raw = errBody.string().trim();
                            if (raw.isEmpty()) {
                                errorMessage = "Login error (code " + response.code() + ")";
                            } else {
                                errorMessage = raw;
                                String[] parts = raw.split("\"");
                                for (int i = 0; i + 2 < parts.length; i++) {
                                    String key = parts[i].trim();
                                    if ("detail".equals(key) || "error".equals(key) || "message".equals(key)) {
                                        String val = parts[i + 2].trim();
                                        if (!val.isEmpty() && !val.startsWith("{") && !val.startsWith("[")) {
                                            errorMessage = val;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        errorMessage = "Login error: " + e.getMessage();
                    }
                    Toast.makeText(MemberLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                Toast.makeText(MemberLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleGoogleSignInResult(android.content.Intent data) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken == null) {
                Toast.makeText(this, "Google sign-in failed: no token", Toast.LENGTH_SHORT).show();
                return;
            }
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            binding.loginButton.setEnabled(false);
            mAuth.signInWithCredential(credential).addOnCompleteListener(this, authTask -> {
                if (authTask.isSuccessful() && authTask.getResult().getUser() != null) {
                    com.google.firebase.auth.FirebaseUser fbUser = authTask.getResult().getUser();
                    String googleEmail = fbUser.getEmail();
                    if (googleEmail != null && !googleEmail.isEmpty()) {
                        com.example.save.utils.SessionManager.getInstance(getApplicationContext()).saveLastEmail(googleEmail);
                    }
                    fbUser.getIdToken(true).addOnCompleteListener(tokenTask -> {
                        if (tokenTask.isSuccessful()) {
                            String firebaseToken = tokenTask.getResult().getToken();
                            performGoogleBackendLogin(firebaseToken);
                        } else {
                            binding.loginButton.setEnabled(true);
                            Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    binding.loginButton.setEnabled(true);
                    Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (ApiException e) {
            if (e.getStatusCode() != com.google.android.gms.common.api.CommonStatusCodes.CANCELED) {
                Toast.makeText(this, "Google sign-in error: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performGoogleBackendLogin(String firebaseToken) {
        com.example.save.data.network.GoogleLoginRequest req =
                new com.example.save.data.network.GoogleLoginRequest(firebaseToken);
        req.setLoginType("member");

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.googleLogin(req).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call,
                                   retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse loginResponse = response.body();
                    String groupName = loginResponse.getGroupName() != null ? loginResponse.getGroupName() : "";
                    com.example.save.utils.SessionManager session =
                            com.example.save.utils.SessionManager.getInstance(getApplicationContext());
                    String phone = (loginResponse.getPhone() != null && !loginResponse.getPhone().isEmpty()) ? loginResponse.getPhone() : "";
                    session.createLoginSession(loginResponse.getName(), phone, loginResponse.getRole(), false, loginResponse.isCreator());
                    session.saveLastGroup(groupName);
                    session.saveJwtToken(loginResponse.getToken());
                    com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                            .fetchSystemConfig(null);
                    com.example.save.services.SaveFirebaseMessagingService
                            .registerTokenWithServer(getApplicationContext());
                    Intent intent = new Intent(MemberLoginActivity.this, MemberMainActivity.class);
                    intent.putExtra("member_name", loginResponse.getName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                    finish();
                } else {
                    String msg = "Login failed";
                    try {
                        okhttp3.ResponseBody errBody = response.errorBody();
                        if (errBody != null) {
                            String raw = errBody.string().trim();
                            String[] parts = raw.split("\"");
                            for (int i = 0; i + 2 < parts.length; i++) {
                                String key = parts[i].trim();
                                if ("detail".equals(key) || "error".equals(key) || "message".equals(key)) {
                                    msg = parts[i + 2].trim();
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                Toast.makeText(MemberLoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            binding.loginCard.setVisibility(View.VISIBLE);
            binding.fragmentContainer.setVisibility(View.GONE);
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
