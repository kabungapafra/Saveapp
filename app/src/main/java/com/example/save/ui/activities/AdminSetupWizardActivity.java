package com.example.save.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.ActivityAdminSetupWizardBinding;
import com.example.save.ui.fragments.WizardGroupInfoFragment;
import com.example.save.ui.fragments.WizardContributionFragment;
import com.example.save.ui.fragments.WizardRulesFragment;
import com.example.save.ui.fragments.WizardCompleteFragment;

public class AdminSetupWizardActivity extends AppCompatActivity {

    private ActivityAdminSetupWizardBinding binding;
    private int currentStep = 0;
    private static final int TOTAL_STEPS = 4;

    // Store wizard data
    private String groupName;
    private String groupDescription;
    private double contributionAmount;
    private String contributionFrequency;
    private double latePenalty;
    private double maxLoanAmount;
    private double interestRate;
    private int repaymentPeriod;
    private boolean requireGuarantor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminSetupWizardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        loadFragment(new WizardGroupInfoFragment());
        updateProgress();
    }

    private void setupNavigation() {
        binding.btnBack.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                loadStepFragment();
                updateProgress();
            } else {
                finish();
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < TOTAL_STEPS - 1) {
                    currentStep++;
                    loadStepFragment();
                    updateProgress();
                } else {
                    completeWizard();
                }
            }
        });
    }

    private void loadStepFragment() {
        Fragment fragment;
        switch (currentStep) {
            case 0:
                fragment = new WizardGroupInfoFragment();
                break;
            case 1:
                fragment = new WizardContributionFragment();
                break;
            case 2:
                fragment = new WizardRulesFragment();
                break;
            case 3:
                fragment = new WizardCompleteFragment();
                break;
            default:
                return;
        }
        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.wizardFragmentContainer, fragment)
                .commit();
    }

    private void updateProgress() {
        // Update progress bars
        binding.progressStep1.setBackgroundResource(
                currentStep >= 0 ? R.color.deep_blue : R.color.divider_color);
        binding.progressStep2.setBackgroundResource(
                currentStep >= 1 ? R.color.deep_blue : R.color.divider_color);
        binding.progressStep3.setBackgroundResource(
                currentStep >= 2 ? R.color.deep_blue : R.color.divider_color);
        binding.progressStep4.setBackgroundResource(
                currentStep >= 3 ? R.color.deep_blue : R.color.divider_color);

        // Update button text
        if (currentStep == TOTAL_STEPS - 1) {
            binding.btnNext.setText("Finish");
        } else {
            binding.btnNext.setText("Next");
        }

        // Show/hide back button
        binding.btnBack.setVisibility(currentStep == 0 ? View.GONE : View.VISIBLE);
    }

    private boolean validateCurrentStep() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.wizardFragmentContainer);

        if (currentFragment instanceof WizardGroupInfoFragment) {
            return ((WizardGroupInfoFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof WizardContributionFragment) {
            return ((WizardContributionFragment) currentFragment).validateAndSave();
        } else if (currentFragment instanceof WizardRulesFragment) {
            return ((WizardRulesFragment) currentFragment).validateAndSave();
        }

        return true;
    }

    private void completeWizard() {
        // Save all wizard data
        SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("group_name", groupName);
        editor.putString("group_description", groupDescription);
        editor.putFloat("contribution_amount", (float) contributionAmount);
        editor.putString("contribution_frequency", contributionFrequency);
        editor.putFloat("late_penalty", (float) latePenalty);
        editor.putFloat("max_loan_amount", (float) maxLoanAmount);
        editor.putFloat("interest_rate", (float) interestRate);
        editor.putInt("repayment_period", repaymentPeriod);
        editor.putBoolean("require_guarantor", requireGuarantor);
        editor.putBoolean("wizard_completed", true);
        editor.apply();

        Toast.makeText(this, "Group created successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        Intent intent = new Intent(this, AdminMainActivity.class);
        intent.putExtra("admin_name", prefs.getString("admin_name", "Admin"));
        intent.putExtra("group_name", groupName);
        startActivity(intent);
        finish();
    }

    // Getters and setters for wizard data
    public void setGroupInfo(String name, String description) {
        this.groupName = name;
        this.groupDescription = description;
    }

    public void setContributionSettings(double amount, String frequency, double penalty) {
        this.contributionAmount = amount;
        this.contributionFrequency = frequency;
        this.latePenalty = penalty;
    }

    public void setLoanRules(double maxAmount, double rate, int period, boolean guarantor) {
        this.maxLoanAmount = maxAmount;
        this.interestRate = rate;
        this.repaymentPeriod = period;
        this.requireGuarantor = guarantor;
    }

    public String getGroupName() {
        return groupName;
    }

    public double getContributionAmount() {
        return contributionAmount;
    }

    public String getContributionFrequency() {
        return contributionFrequency;
    }
}
