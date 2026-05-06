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
    private String adminEmail;
    private String adminPhone;
    private String adminPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSetupWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        // Get Admin data from Intent
        adminName = getIntent().getStringExtra("ADMIN_NAME");
        adminEmail = getIntent().getStringExtra("EMAIL");
        adminPhone = getIntent().getStringExtra("PHONE");
        adminPassword = getIntent().getStringExtra("PASSWORD");

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
        binding.btnBack.setOnClickListener(v -> onBackPressed());
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
            if (currentStep < 7) {
                if (currentStep == 6) {
                    // Transition to step 7 immediately since we pre-sent the OTP
                    currentStep++;
                    loadStep(currentStep);
                } else {
                    currentStep++;
                    loadStep(currentStep);
                }
            } else {
                completeWizard();
            }
        } else {
            Toast.makeText(this, "Please complete all required fields correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOtpAndProceed(boolean shouldProceed) {
        if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
            if (shouldProceed) {
                currentStep++;
                loadStep(currentStep);
            }
            return;
        }

        // Only show progress if we are explicitly proceeding
        android.app.ProgressDialog progressDialog = null;
        if (shouldProceed) {
            progressDialog = new android.app.ProgressDialog(this);
            progressDialog.setMessage("Sending secure verification code...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        com.example.save.data.network.OtpRequest request = new com.example.save.data.network.OtpRequest(adminEmail, adminPhone);
        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        final android.app.ProgressDialog finalDialog = progressDialog;
        apiService.sendAdminOtp(request).enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call, 
                                   retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                
                if (finalDialog != null && finalDialog.isShowing()) finalDialog.dismiss();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    if (shouldProceed) {
                        Toast.makeText(AdminSetupWizardActivity.this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                        currentStep++;
                        loadStep(currentStep);
                    }
                } else if (shouldProceed) {
                    String error = "Failed to send OTP. Please try again.";
                    if (response.code() == 429) {
                        error = "Too many requests. Please wait a few minutes.";
                    } else if (response.errorBody() != null) {
                        try { error = response.errorBody().string(); } catch (Exception e) {}
                    }
                    new androidx.appcompat.app.AlertDialog.Builder(AdminSetupWizardActivity.this)
                        .setTitle("OTP Error")
                        .setMessage(error)
                        .setPositiveButton("Retry", (d, w) -> sendOtpAndProceed(true))
                        .setNegativeButton("Cancel", null)
                        .show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                if (finalDialog != null && finalDialog.isShowing()) finalDialog.dismiss();
                if (shouldProceed) {
                    new androidx.appcompat.app.AlertDialog.Builder(AdminSetupWizardActivity.this)
                        .setTitle("Network Error")
                        .setMessage("Could not connect to server. Please check your internet.")
                        .setPositiveButton("Retry", (d, w) -> sendOtpAndProceed(true))
                        .setNegativeButton("Cancel", null)
                        .show();
                }
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
                fragment = new WizardCompleteFragment();
                binding.btnNext.setText("Go to Legal Center");
                binding.footer.setVisibility(View.VISIBLE);
                break;
            case 6:
                fragment = new LegalAgreementFragment();
                binding.footer.setVisibility(View.GONE);
                // Pre-send OTP so it's ready by the time they reach the next screen
                sendOtpAndProceed(false); // Add a flag to not proceed to next step automatically
                break;
            case 7:
                fragment = new OtpFragment();
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
        int progress = (step * 100) / 7;
        binding.wizardProgressBar.setProgress(progress);
        binding.tvStepCount.setText("Step " + step + " of 7");
        
        switch (step) {
            case 1: binding.tvSectionTitle.setText("GROUP INFO"); break;
            case 2: binding.tvSectionTitle.setText("CONTRIBUTIONS"); break;
            case 3: binding.tvSectionTitle.setText("PAYOUTS"); break;
            case 4: binding.tvSectionTitle.setText("LOAN RULES"); break;
            case 5: binding.tvSectionTitle.setText("REVIEW"); break;
            case 6: binding.tvSectionTitle.setText("LEGAL"); break;
            case 7: binding.tvSectionTitle.setText("VERIFICATION"); break;
        }
        
        // Progress and Navigation are now handled inside each fragment
        binding.progressSection.setVisibility(View.GONE);
        binding.toolbar.setVisibility(View.GONE);
    }

    private void completeWizard() {
        if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
            finishSetup();
            return;
        }

        // Prepare Config Object
        com.example.save.data.models.SystemConfig config = new com.example.save.data.models.SystemConfig();
        config.setContributionAmount(contributionAmount);
        config.setPayoutAmount(payoutAmount);
        config.setRetentionPercentage(retentionPercentage);
        config.setLoanInterestRate(interest_rate);
        config.setMaxLoanLimit(maxLoanAmount);
        config.setMaxLoanMultiplier(3.0); // Default multiplier (3x savings)
        config.setMaxLoanDuration(repaymentPeriod);
        config.setLatePenaltyRate(latePenalty);
        config.setCurrency(currency);

        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient
                .getClient(this).create(com.example.save.data.network.ApiService.class);

        apiService.updateSystemConfig(config).enqueue(new retrofit2.Callback<com.example.save.data.models.SystemConfig>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.models.SystemConfig> call, 
                                   retrofit2.Response<com.example.save.data.models.SystemConfig> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminSetupWizardActivity.this, "Group Configured Successfully!", Toast.LENGTH_SHORT).show();
                    finishSetup();
                } else {
                    Toast.makeText(AdminSetupWizardActivity.this, "Failed to save configuration", Toast.LENGTH_SHORT).show();
                    // Fallback to finishing anyway for now, or stay on screen
                    finishSetup();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.models.SystemConfig> call, Throwable t) {
                Toast.makeText(AdminSetupWizardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finishSetup();
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
    public String getAdminEmail() { return adminEmail; }
    public String getAdminPhone() { return adminPhone; }
    public String getAdminPassword() { return adminPassword; }
    
    public double getLatePenalty() { return latePenalty; }
    public double getMaxLoanAmount() { return maxLoanAmount; }
    public double getInterestRate() { return interest_rate; }
    public int getRepaymentPeriod() { return repaymentPeriod; }
    public boolean isRequireGuarantor() { return requireGuarantor; }
}
