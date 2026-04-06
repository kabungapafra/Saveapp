package com.example.save.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminSetupWizardBinding;
import com.example.save.ui.fragments.WizardGroupInfoFragment;
import com.example.save.ui.fragments.WizardContributionFragment;
import com.example.save.ui.fragments.WizardRulesFragment;
import com.example.save.ui.fragments.WizardPayoutFragment;
import com.example.save.ui.fragments.WizardCompleteFragment;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.data.network.SystemConfigUpdate;

public class AdminSetupWizardActivity extends AppCompatActivity {

    private ActivityAdminSetupWizardBinding binding;
    private int currentStep = 0;
    private static final int TOTAL_STEPS = 5;
    private MembersViewModel viewModel;

    // Signup data from Intent
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminPassword;

    // Store wizard data
    private String groupName;
    private String groupDescription;
    private String currency = "UGX - Uganda Shillings"; // Default Uganda Shillings
    private double contributionAmount;
    private String contributionFrequency;
    private double latePenalty;
    private double payoutAmount;
    private double retentionPercentage;
    private double maxLoanAmount;
    private double interest_rate;
    private int repaymentPeriod;
    private boolean requireGuarantor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSetupWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        // Receive signup data
        Intent intent = getIntent();
        adminName = intent.getStringExtra("ADMIN_NAME");
        adminEmail = intent.getStringExtra("EMAIL");
        adminPhone = intent.getStringExtra("PHONE");
        adminPassword = intent.getStringExtra("PASSWORD");

        setupNavigation();
        loadStepFragment(false);
        updateProgress();
    }

    public void setToolbarVisibility(int visibility) {
        if (binding.toolbar != null) {
            binding.toolbar.setVisibility(visibility);
        }
    }

    private void setupNavigation() {
        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                loadStepFragment(true);
                updateProgress();
            } else {
                finish();
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                nextStep();
            }
        });
    }

    public void nextStep() {
        if (currentStep < TOTAL_STEPS - 1) {
            currentStep++;
            loadStepFragment(false);
            updateProgress();
        } else {
            completeWizard();
        }
    }

    private void loadStepFragment(boolean isBack) {
        Fragment fragment;
        String sectionTitle = "GROUP SETUP";
        
        switch (currentStep) {
            case 0:
                fragment = new WizardGroupInfoFragment();
                sectionTitle = "GROUP SETUP";
                break;
            case 1:
                fragment = new com.example.save.ui.fragments.WizardAddMembersInfoFragment();
                sectionTitle = "MEMBER ADDITION";
                break;
            case 2:
                fragment = new com.example.save.ui.fragments.WizardFinalizeRulesFragment();
                sectionTitle = "FINALIZE RULES";
                break;
            case 3:
                // Pass signup data to OTP fragment
                fragment = com.example.save.ui.fragments.OtpFragment.newInstanceForRegistration(
                    adminName, groupName, adminPhone, adminEmail, adminPassword
                );
                sectionTitle = "VERIFICATION";
                break;
            case 4:
                fragment = new com.example.save.ui.fragments.LegalAgreementFragment();
                sectionTitle = "LEGAL AGREEMENT";
                break;
            default:
                return;
        }
        
        binding.tvSectionTitle.setText(sectionTitle);
        loadFragment(fragment, isBack);
    }

    private void loadFragment(Fragment fragment, boolean isBack) {
        // Hide global toolbar as fragments now have their own headers and back buttons
        if (binding.toolbar != null) {
            binding.toolbar.setVisibility(View.GONE);
        }
        
        int enter = isBack ? R.anim.slide_in_left : R.anim.slide_in_right;
        int exit = isBack ? R.anim.slide_out_right : R.anim.slide_out_left;

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(enter, exit)
                .replace(R.id.wizardFragmentContainer, fragment)
                .commit();
    }

    private void updateProgress() {
        int progress = (int) (((float) (currentStep + 1) / TOTAL_STEPS) * 100);
        binding.wizardProgressBar.setProgress(progress);
        
        // We use 3 setup steps for the "Step X of 3" indicator, then OTP and Legal are final
        if (currentStep < 3) {
            binding.tvStepCount.setText(String.format("Step %d of 3", currentStep + 1));
            binding.tvStepCount.setVisibility(View.VISIBLE);
        } else {
            binding.tvStepCount.setVisibility(View.GONE);
        }

        // Dynamic button text matching mockup exactly
        switch (currentStep) {
            case 0:
                binding.btnNext.setText("Continue");
                binding.btnNext.setIconResource(android.R.color.transparent);
                break;
            case 1:
                binding.btnNext.setText("Go to Add Member Section >");
                binding.btnNext.setIconResource(android.R.color.transparent);
                break;
            case 2:
                binding.btnNext.setText("Get OTP");
                binding.btnNext.setIconResource(android.R.color.transparent);
                break;
            case 3:
                // OTP Fragment has its own verify button, we hide the footer
                break;
            case 4:
                // Legal Fragment has its own Accept button, we hide the footer
                break;
        }

        // Hide footer on Finalize Rules (step 2), OTP (step 3) and Legal (step 4)
        // as those steps have their own integrated buttons
        if (currentStep >= 2) {
            binding.footer.setVisibility(View.GONE);
        } else {
            binding.footer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep > 0) {
            currentStep--;
            loadStepFragment(true);
            updateProgress();
        } else {
            super.onBackPressed();
        }
    }

    private boolean validateCurrentStep() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.wizardFragmentContainer);

        if (currentFragment instanceof WizardGroupInfoFragment) {
            return ((WizardGroupInfoFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof com.example.save.ui.fragments.WizardAddMembersInfoFragment) {
            return ((com.example.save.ui.fragments.WizardAddMembersInfoFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof com.example.save.ui.fragments.WizardFinalizeRulesFragment) {
            return ((com.example.save.ui.fragments.WizardFinalizeRulesFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof com.example.save.ui.fragments.OtpFragment) {
            // OTP verification is handled internally by the fragment, but we can trigger it
            // if we want the "Next" button click to mean "Verify"
             ((com.example.save.ui.fragments.OtpFragment) currentFragment).verifyOtp();
             return false; // Don't advance automatically; fragment will call nextStep() on success
        }

        return true;
    }

    private void completeWizard() {
        // Prepare configuration update for backend
        SystemConfigUpdate update = new SystemConfigUpdate()
                .setCurrency(currency)
                .setContributionAmount(contributionAmount)
                .setPayoutAmount(payoutAmount)
                .setRetentionPercentage(retentionPercentage)
                .setLoanInterestRate(interest_rate)
                .setMaxLoanLimit(maxLoanAmount)
                .setMaxLoanDuration(repaymentPeriod)
                .setLatePenaltyRate(latePenalty);

        // Call backend to update configuration
        viewModel.updateSystemConfig(update, (success, config, message) -> {
            // Local save as fallback/redundancy
            SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString("group_name", groupName);
            editor.putString("group_description", groupDescription);
            editor.putString("currency", currency);
            editor.putFloat("contribution_amount", (float) contributionAmount);
            editor.putString("contribution_frequency", contributionFrequency);
            editor.putFloat("late_penalty", (float) latePenalty);
            editor.putFloat("payout_amount", (float) payoutAmount);
            editor.putFloat("retention_percentage", (float) retentionPercentage);
            editor.putFloat("max_loan_amount", (float) maxLoanAmount);
            editor.putFloat("interest_rate", (float) interest_rate);
            editor.putInt("repayment_period", repaymentPeriod);
            editor.putBoolean("require_guarantor", requireGuarantor);
            editor.putBoolean("wizard_completed", true);
            editor.apply();

            if (success) {
                Toast.makeText(this, "Group successfully configured!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Local setup complete. Cloud sync pending.", Toast.LENGTH_LONG).show();
            }

            // Navigate to main activity
            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
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
}
