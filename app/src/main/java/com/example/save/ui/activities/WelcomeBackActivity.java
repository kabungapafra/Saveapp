package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.save.R;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.GoogleLoginRequest;
import com.example.save.data.network.LoginRequest;
import com.example.save.data.network.LoginResponse;
import com.example.save.data.network.RetrofitClient;
import com.example.save.databinding.ActivityWelcomeBackBinding;
import com.example.save.utils.SessionManager;
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

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WelcomeBackActivity extends AppCompatActivity {

    private ActivityWelcomeBackBinding binding;
    private SessionManager session;
    private String lastPhone;
    private String lastRole;
    private String lastGroup;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 4;
    private static final long LOCKOUT_DURATION_MS = 2 * 60 * 60 * 1000; // 2 Hours

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        session = SessionManager.getInstance(this);
        // Load server-side configuration to ensure persisted settings are applied
        com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                .fetchSystemConfig(null);
        mAuth = FirebaseAuth.getInstance();

        // Load persisted user data first
        lastPhone = session.getLastPhone();
        lastRole  = session.getLastRole();
        lastGroup = session.getLastGroup();

        // Set personalised greeting
        String name = session.getLastName();
        if (name != null && !name.isEmpty()) {
            binding.titleText.setText("Welcome back,\n" + name);
        } else {
            binding.titleText.setText("Welcome Back");
        }

        // Show group name in subtitle
        String group = lastGroup;
        if (group != null && !group.isEmpty()) {
            binding.subtitleText.setText("Log in to " + group);
        } else {
            binding.subtitleText.setText("Log in to your Save account");
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleGoogleSignInResult(result.getData()));

        startAnimations();
        setupListeners();
        checkLockout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLockout();
    }

    private void checkLockout() {
        // Lockout disabled as per user request
        binding.passwordInput.setEnabled(true);
        binding.loginButton.setEnabled(true);
        binding.passwordInput.setHint("● ● ● ●");
        
        String group = lastGroup;
        if (group != null && !group.isEmpty()) {
            binding.subtitleText.setText("Log in to " + group);
        } else {
            binding.subtitleText.setText("Log in to your Save account");
        }
        binding.subtitleText.setTextColor(getResources().getColor(R.color.v_text_mid));
    }

    private void startAnimations() {
        Animation heartbeat = AnimationUtils.loadAnimation(this, R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        Animation slide = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        binding.logoCard.startAnimation(slide);

        slide = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slide.setStartOffset(150);
        binding.titleText.startAnimation(slide);
        binding.subtitleText.startAnimation(slide);

        slide = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slide.setStartOffset(300);
        binding.loginCard.startAnimation(slide);
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> handleLogin());

        binding.forgotPassword.setOnClickListener(v -> startForgotPinFlow());

        binding.createAccountLink.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        binding.googleButton.setOnClickListener(v ->
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task ->
                        googleSignInLauncher.launch(mGoogleSignInClient.getSignInIntent())));
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

    private void startForgotPinFlow() {
        if (lastPhone == null || lastPhone.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        // Pass only the phone — ResetPasswordActivity will send the OTP
        // and guarantee the OTP screen appears before PIN change.
        Intent i = new Intent(this, ResetPasswordActivity.class);
        i.putExtra("phone", lastPhone);
        i.putExtra("sendOtpOnLaunch", true);
        startActivity(i);
    }

    private void handleLogin() {
        String password = binding.passwordInput.getText().toString().trim();

        if (password.isEmpty()) {
            binding.passwordInput.setError("Please enter your PIN");
            binding.passwordInput.requestFocus();
            return;
        }

        // Disable button and show loading state
        binding.loginButton.setEnabled(false);
        binding.loginButtonText.setText("Logging in...");
        binding.loginProgress.setVisibility(View.VISIBLE);
        binding.loginArrow.setVisibility(View.GONE);

        // Resolve the phone. For users whose first login was Google, lastPhone may be
        // empty. Try JWT extraction first, then fall back to sending the saved Google
        // email — the backend /login now accepts email as an identifier for Google users.
        String phone = (lastPhone != null && !lastPhone.isEmpty())
                ? lastPhone : session.getPhoneFromToken();

        String emailFallback = "";
        if (phone == null || phone.isEmpty()) {
            emailFallback = session.getLastEmail();
        }

        // Build login request using remembered credentials
        LoginRequest req = new LoginRequest(phone, password);
        if (!emailFallback.isEmpty()) {
            req.setEmail(emailFallback);
        }
        req.setLoginType((lastRole == null || lastRole.isEmpty()) ? "member" : lastRole.toLowerCase());
        req.setGroupName(lastGroup);

        ApiService api = RetrofitClient.getClient(this).create(ApiService.class);
        api.login(req).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Restore button state
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                binding.loginProgress.setVisibility(View.GONE);
                binding.loginArrow.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    session.saveLockoutTime(0); // Clear lockout on success
                    session.saveBackgroundTime(0); // Reset auto-lock timer
                    LoginResponse body = response.body();
                    session.createLoginSession(
                            body.getName(),
                            phone,
                            body.getRole(),
                            false,
                            body.isCreator());
                    session.saveJwtToken(body.getToken());
                    
                    // Update Retrofit singleton token
                    com.example.save.data.network.RetrofitClient.getInstance(getApplicationContext()).updateToken(body.getToken());

                    // Restore all financial rule settings from server after login
                    com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                            .fetchSystemConfig((success, config, message) -> {
                                // Config is saved to SharedPreferences inside the repository
                                // Proceed to main activity regardless of fetch success
                                session.saveLastGroup(lastGroup);
                                goToMain(body);
                            });
                } else {
                    String msg = "Invalid PIN. Please try again.";
                    try {
                        okhttp3.ResponseBody errBody = response.errorBody();
                        if (errBody != null) {
                            byte[] bytes = errBody.bytes();
                            if (bytes != null && bytes.length > 0) {
                                String raw = new String(bytes, "UTF-8");
                                String[] parts = raw.split("\"");
                                for (int i = 0; i + 2 < parts.length; i++) {
                                    String key = parts[i].trim();
                                    if ("detail".equals(key) || "error".equals(key) || "message".equals(key)) {
                                        String val = parts[i + 2].trim();
                                        if (!val.isEmpty() && !val.startsWith("{") && !val.startsWith("[")) {
                                            msg = val; break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(WelcomeBackActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Restore button state
                binding.loginButton.setEnabled(true);
                binding.loginButtonText.setText("Login");
                binding.loginProgress.setVisibility(View.GONE);
                binding.loginArrow.setVisibility(View.VISIBLE);
                Toast.makeText(WelcomeBackActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGoogleSignInResult(Intent data) {
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
                        session.saveLastEmail(googleEmail);
                    }
                    fbUser.getIdToken(true).addOnCompleteListener(tokenTask -> {
                        if (tokenTask.isSuccessful()) {
                            performGoogleBackendLogin(tokenTask.getResult().getToken());
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
        GoogleLoginRequest req = new GoogleLoginRequest(firebaseToken);
        ApiService api = RetrofitClient.getClient(this).create(ApiService.class);
        api.googleLogin(req).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.loginButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    String groupName = body.getGroupName() != null ? body.getGroupName() : lastGroup;
                    // Prefer phone from the server response so PIN login works afterward.
                    // Falls back to lastPhone only if the server doesn't return one.
                    String phoneToSave = (body.getPhone() != null && !body.getPhone().isEmpty())
                            ? body.getPhone() : lastPhone;
                    session.createLoginSession(body.getName(), phoneToSave, body.getRole(), false, body.isCreator());
                    session.saveLastGroup(groupName);
                    session.saveJwtToken(body.getToken());
                    com.example.save.data.repository.MemberRepository.getInstance(getApplicationContext())
                            .fetchSystemConfig(null);
                    com.example.save.services.SaveFirebaseMessagingService
                            .registerTokenWithServer(getApplicationContext());
                    goToMain(body);
                } else {
                    String msg = "Google login failed";
                    try {
                        okhttp3.ResponseBody errBody = response.errorBody();
                        if (errBody != null) {
                            String raw = errBody.string();
                            String[] parts = raw.split("\"");
                            for (int i = 0; i + 2 < parts.length; i++) {
                                if ("detail".equals(parts[i].trim()) || "error".equals(parts[i].trim())) {
                                    msg = parts[i + 2].trim();
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(WelcomeBackActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.loginButton.setEnabled(true);
                Toast.makeText(WelcomeBackActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain(LoginResponse body) {
        Intent intent;
        if ("admin".equalsIgnoreCase(body.getRole())) {
            intent = new Intent(this, AdminMainActivity.class);
            intent.putExtra("admin_name",  body.getName());
            intent.putExtra("group_name",  lastGroup);
        } else {
            intent = new Intent(this, MemberMainActivity.class);
            intent.putExtra("member_name",  body.getName());
        }
        // Forward notification deep-link extras from SplashActivity
        String navigateTo = getIntent() != null ? getIntent().getStringExtra("NAVIGATE_TO") : null;
        if (navigateTo != null) intent.putExtra("NAVIGATE_TO", navigateTo);
        String convId = getIntent() != null ? getIntent().getStringExtra("conv_id") : null;
        if (convId != null) intent.putExtra("conv_id", convId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
        finish();
    }
}
