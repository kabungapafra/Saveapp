package com.example.save.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminSetupWizardBinding;
import com.example.save.ui.fragments.WizardGroupInfoFragment;
import com.example.save.ui.fragments.WizardContributionFragment;
import com.example.save.ui.fragments.WizardRulesFragment;
import com.example.save.ui.fragments.WizardPayoutFragment;
import com.example.save.ui.fragments.WizardCompleteFragment;
import com.example.save.ui.fragments.LegalAgreementFragment;
import com.example.save.ui.fragments.OtpFragment;
import com.example.save.ui.fragments.WizardAddMembersInfoFragment;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AdminSetupWizardActivity extends AppCompatActivity {

    private ActivityAdminSetupWizardBinding binding;
    private int currentStep = 1;

    // Wizard Data
    private String groupName;
    private String groupDescription;
    private String currency;
    private double contributionAmount;
    private String contributionFrequency;
    private double latePenalty;
    private double payoutAmount;
    private double retentionPercentage;
    private double maxLoanAmount;
    private double interest_rate;
    private int repaymentPeriod;
    private boolean requireGuarantor;
    
    // Admin Signup Data
    private String adminName;

    private String adminPhone;
    private String adminPassword;

    // Firebase Auth for OTP
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSetupWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        // Get Admin data from Intent
        adminName = getIntent().getStringExtra("ADMIN_NAME");

        adminPhone = getIntent().getStringExtra("PHONE");
        adminPassword = getIntent().getStringExtra("PASSWORD");

        mAuth = FirebaseAuth.getInstance();
        setupPhoneAuthCallbacks();

        setupListeners();
        loadStep(currentStep);
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent == null || intent.getData() == null) return;
        
        android.net.Uri data = intent.getData();
        if ("saveapp".equals(data.getScheme()) && "otp".equals(data.getHost())) {
            String code = data.getQueryParameter("code");
            if (code != null && !code.isEmpty()) {
                // If we are on the OTP step, we can auto-fill or just store it
                // For now, let's toast and we can improve by passing it to the fragment
                Toast.makeText(this, "OTP Received from link: " + code, Toast.LENGTH_LONG).show();
                
                // Store code for fragment to pick up
                getIntent().putExtra("DEEP_LINK_OTP", code);
                
                // If not yet on OTP step, maybe wait. If already there, update fragment.
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.wizardFragmentContainer);
                if (currentFragment instanceof com.example.save.ui.fragments.OtpFragment) {
                    ((com.example.save.ui.fragments.OtpFragment) currentFragment).autoFillOtp(code);
                }
            }
        }
    }

    private void setupListeners() {
        binding.btnNext.setOnClickListener(v -> nextStep());
    }

    public void nextStep() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.wizardFragmentContainer);
        boolean isValid = true;

        if (currentFragment instanceof WizardGroupInfoFragment) {
            isValid = ((WizardGroupInfoFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof WizardContributionFragment) {
            isValid = ((WizardContributionFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof WizardPayoutFragment) {
            isValid = ((WizardPayoutFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof WizardRulesFragment) {
            isValid = ((WizardRulesFragment) currentFragment).validateAndSave();
        }

        if (isValid) {
            if (currentStep < 8) {
                currentStep++;
                loadStep(currentStep);
            } else {
                completeWizard();
            }
        } else {
            Toast.makeText(this, "Please complete all required fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendOtpExplicitly() {
        sendOtpAndProceed(true);
    }

    private void sendOtpAndProceed(boolean shouldProceed) {
        // Ensure phone is normalized before Firebase call
        adminPhone = com.example.save.utils.ValidationUtils.normalizePhone(adminPhone);
        Toast.makeText(this, "Verifying: " + adminPhone, Toast.LENGTH_SHORT).show();

        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("Sending verification code...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final android.app.ProgressDialog finalDialog = progressDialog;
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(adminPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        if (finalDialog != null && finalDialog.isShowing()) finalDialog.dismiss();
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        if (finalDialog != null && finalDialog.isShowing()) finalDialog.dismiss();
                        Toast.makeText(AdminSetupWizardActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                        if (finalDialog != null && finalDialog.isShowing()) finalDialog.dismiss();
                        mVerificationId = verificationId;
                        mResendToken = token;
                        if (shouldProceed) {
                            currentStep++;
                            loadStep(currentStep);
                        }
                    }
                })
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
                Toast.makeText(AdminSetupWizardActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        if (credential == null) {
            Toast.makeText(this, "Verification failed: Null credential", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        completeWizard();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Invalid code";
                        Toast.makeText(AdminSetupWizardActivity.this, "Sign-in failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 1) {
            currentStep--;
            loadStep(currentStep);
        } else {
            super.onBackPressed();
        }
    }

    private void loadStep(int step) {
        Fragment fragment;
        switch (step) {
            case 1:
                fragment = new WizardGroupInfoFragment();
                binding.btnNext.setText("Continue");
                break;
            case 2:
                fragment = new WizardContributionFragment();
                binding.btnNext.setText("Continue");
                break;
            case 3:
                fragment = new WizardPayoutFragment();
                binding.btnNext.setText("Continue");
                break;
            case 4:
                fragment = new WizardRulesFragment();
                binding.btnNext.setText("Continue");
                binding.footer.setVisibility(View.VISIBLE);
                break;
            case 5:
                fragment = new WizardAddMembersInfoFragment();
                binding.btnNext.setText("Continue to Review");
                binding.footer.setVisibility(View.VISIBLE);
                break;
            case 6:
                fragment = new WizardCompleteFragment();
                binding.btnNext.setText("Go to Legal Center");
                binding.footer.setVisibility(View.VISIBLE);
                break;
            case 7:
                fragment = new LegalAgreementFragment();
                binding.footer.setVisibility(View.GONE); // Legal has its own internal buttons
                break;
            case 8:
                OtpFragment otpFragment = OtpFragment.newInstanceForRegistration(adminName, groupName, adminPhone, "", adminPassword);
                otpFragment.setOtpListener(new OtpFragment.OtpListener() {
                    @Override
                    public void onOtpEntered(String code) {
                        if (mVerificationId == null) {
                            Toast.makeText(AdminSetupWizardActivity.this, "Verification session expired. Please resend code.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onResendOtp() {
                        sendOtpAndProceed(false);
                    }
                });
                fragment = otpFragment;
                binding.footer.setVisibility(View.GONE);
                break;
            default:
                fragment = new WizardGroupInfoFragment();
                binding.btnNext.setText("Continue");
                binding.footer.setVisibility(View.VISIBLE);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.wizardFragmentContainer, fragment);
        transaction.commit();

        updateProgress(step);
    }

    private void updateProgress(int step) {
        // Activity-level progress indicators have been removed.
        // Each fragment now handles its own high-fidelity header and progress.
    }

    private void completeWizard() {
        // Prepare Config Object
        com.example.save.data.models.SystemConfig config = new com.example.save.data.models.SystemConfig();
        config.setLatePenaltyRate(latePenalty);
        config.setCurrency(currency);

        // Prepare Admin User Data
        com.example.save.data.network.OtpVerificationRequest registrationRequest = new com.example.save.data.network.OtpVerificationRequest(
                adminPhone,
                "FIREBASE_VERIFIED", // Placeholder since Firebase verified it
                adminName,
                adminPassword,
                groupName
        );

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        // Step 1: Register Admin First
        apiService.verifyAdminOtp(registrationRequest).enqueue(new retrofit2.Callback<com.example.save.data.network.LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.LoginResponse> call, 
                                   retrofit2.Response<com.example.save.data.network.LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.save.data.network.LoginResponse loginData = response.body();
                    com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(getApplicationContext());
                    session.saveJwtToken(loginData.getToken());
                    
                    // Update Retrofit singleton token
                    com.example.save.data.network.RetrofitClient.getInstance(getApplicationContext()).updateToken(loginData.getToken());

                    session.createLoginSession(loginData.getName(), adminPhone, loginData.getRole(), false, loginData.isCreator());
                    session.saveLastGroup(groupName);

                    // Step 2: Now that we are logged in, Save System Config
                    apiService.updateSystemConfig(config).enqueue(new retrofit2.Callback<com.example.save.data.models.SystemConfig>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.example.save.data.models.SystemConfig> call, 
                                               retrofit2.Response<com.example.save.data.models.SystemConfig> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminSetupWizardActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                                finishSetup();
                            } else {
                                // Even if config fails, the admin is created, so we can proceed
                                Toast.makeText(AdminSetupWizardActivity.this, "Admin created, but failed to save some settings.", Toast.LENGTH_SHORT).show();
                                finishSetup();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.example.save.data.models.SystemConfig> call, Throwable t) {
                            finishSetup();
                        }
                    });
                } else {
                    String error = "Admin registration failed";
                    if (response.errorBody() != null) {
                        try { error = response.errorBody().string(); } catch (Exception e) {}
                    }
                    Toast.makeText(AdminSetupWizardActivity.this, error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.LoginResponse> call, Throwable t) {
                Toast.makeText(AdminSetupWizardActivity.this, "Network error during registration: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishSetup() {
        SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        prefs.edit()
                .putString("group_name", groupName)
                .putBoolean("wizard_completed", true)
                .apply();

        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Getters and setters for wizard data
    public void setGroupInfo(String name, String description, String currency, double amount) {
        this.groupName = name;
        this.groupDescription = description;
        this.currency = currency;
        this.contributionAmount = amount;
    }

    public void setContributionSettings(String frequency, double penalty) {
        this.contributionFrequency = frequency;
        this.latePenalty = penalty;
    }

    public void setPayoutSettings(double amount, double retention) {
        this.payoutAmount = amount;
        this.retentionPercentage = retention;
    }

    public void setLoanRules(double maxAmount, double rate, int period, boolean guarantor) {
        this.maxLoanAmount = maxAmount;
        this.interest_rate = rate;
        this.repaymentPeriod = period;
        this.requireGuarantor = guarantor;
    }

    public String getGroupName() { return groupName; }
    public String getCurrency() { return currency; }
    public double getContributionAmount() { return contributionAmount; }
    public String getContributionFrequency() { return contributionFrequency; }
    public double getPayoutAmount() { return payoutAmount; }
    public double getRetentionPercentage() { return retentionPercentage; }

    public String getAdminName() { return adminName; }

    public String getAdminPhone() { return adminPhone; }
    public String getAdminPassword() { return adminPassword; }
    
    public double getLatePenalty() { return latePenalty; }
    public double getMaxLoanAmount() { return maxLoanAmount; }
    public double getInterestRate() { return interest_rate; }
    public int getRepaymentPeriod() { return repaymentPeriod; }
    public boolean isRequireGuarantor() { return requireGuarantor; }
}
