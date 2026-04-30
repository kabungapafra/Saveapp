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
    private void completeWizard() {
        // MOCK: No Database, No Network
        // Save configuration to local SharedPreferences only
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

        Toast.makeText(this, "Group successfully configured (Offline)!", Toast.LENGTH_SHORT).show();

        // Navigate to main activity
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
}
