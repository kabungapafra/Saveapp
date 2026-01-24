package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.widget.EditText;
import android.text.TextUtils;
import androidx.appcompat.widget.SwitchCompat;
import com.example.save.R;
import com.example.save.databinding.ActivitySettingsBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import android.content.SharedPreferences;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private MembersViewModel viewModel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);
        prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);

        loadAllSettings();
        setupListeners();
    }

    private void loadAllSettings() {
        // Admin Configuration
        loadContributionAmount();
        loadLatePenalty();
        loadPayoutSettings();
        loadAdminSwitches();
        setupSlotsSpinner();
        setupDatePicker();
        setupReservePercentage();

        // Loans
        loadLoanSettings();

        // Notifications
        loadNotificationSettings();

        // Security
        loadSecuritySettings();

        // Payment
        loadPaymentSettings();
    }

    private void loadContributionAmount() {
        double currentTarget = viewModel.getContributionTarget();
        binding.etContributionAmount.setText(String.format(java.util.Locale.US, "%.0f", currentTarget));

        binding.etContributionAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveContributionTarget();
            }
        });
    }

    private void loadLatePenalty() {
        double latePenalty = Double.longBitsToDouble(prefs.getLong("late_penalty", Double.doubleToLongBits(5000.0)));
        binding.etLatePenalty.setText(String.format(java.util.Locale.US, "%.0f", latePenalty));

        binding.etLatePenalty.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveLatePenalty();
            }
        });
    }

    private void loadPayoutSettings() {
        // Payout Amount
        double amount = viewModel.getPayoutAmount();
        binding.etPayoutAmount.setText(String.format(java.util.Locale.US, "%.0f", amount));

        binding.etPayoutAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                savePayoutAmount();
            }
        });

        // Retention Percentage
        double retention = viewModel.getRetentionPercentage();
        binding.etRetentionPercentage.setText(String.format(java.util.Locale.US, "%.1f", retention));

        binding.etRetentionPercentage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveRetentionPercentage();
            }
        });
    }

    private void loadAdminSwitches() {
        binding.switchRandomQueue.setChecked(prefs.getBoolean("randomized_queue", false));
        binding.switchEnforceEligibility.setChecked(prefs.getBoolean("enforce_eligibility", true));
        binding.switchRequire2FA.setChecked(prefs.getBoolean("require_2fa", false));

        binding.switchRandomQueue.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("randomized_queue", isChecked).apply();
            Toast.makeText(this, "Randomized Queue " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        binding.switchEnforceEligibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("enforce_eligibility", isChecked).apply();
            Toast.makeText(this, "Eligibility enforcement " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT)
                    .show();
        });

        binding.switchRequire2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("require_2fa", isChecked).apply();
            Toast.makeText(this, "2FA " + (isChecked ? "required" : "optional"), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadLoanSettings() {
        double maxLoan = Double.longBitsToDouble(prefs.getLong("max_loan_amount", Double.doubleToLongBits(500000.0)));
        binding.etMaxLoanAmount.setText(String.format(java.util.Locale.US, "%.0f", maxLoan));

        double interestRate = Double
                .longBitsToDouble(prefs.getLong("loan_interest_rate", Double.doubleToLongBits(5.0)));
        binding.etLoanInterestRate.setText(String.format(java.util.Locale.US, "%.1f", interestRate));

        int maxDuration = prefs.getInt("max_loan_duration", 12);
        binding.etMaxLoanDuration.setText(String.valueOf(maxDuration));

        binding.switchRequireGuarantor.setChecked(prefs.getBoolean("require_guarantor", true));

        // Save listeners
        binding.etMaxLoanAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                saveLoanAmount();
        });

        binding.etLoanInterestRate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                saveLoanInterestRate();
        });

        binding.etMaxLoanDuration.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus)
                saveLoanDuration();
        });

        binding.switchRequireGuarantor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("require_guarantor", isChecked).apply();
            Toast.makeText(this, "Guarantor " + (isChecked ? "required" : "optional"), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNotificationSettings() {
        binding.switchPaymentReminders.setChecked(prefs.getBoolean("payment_reminders", true));
        binding.switchPayoutAlerts.setChecked(prefs.getBoolean("payout_alerts", true));
        binding.switchLoanUpdates.setChecked(prefs.getBoolean("loan_updates", true));
        binding.switchGroupActivity.setChecked(prefs.getBoolean("group_activity", false));

        binding.switchPaymentReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("payment_reminders", isChecked).apply();
        });

        binding.switchPayoutAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("payout_alerts", isChecked).apply();
        });

        binding.switchLoanUpdates.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("loan_updates", isChecked).apply();
        });

        binding.switchGroupActivity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("group_activity", isChecked).apply();
        });
    }

    private void loadSecuritySettings() {
        binding.switchBiometric.setChecked(prefs.getBoolean("biometric_login", true));
        binding.switchPinForPayments.setChecked(prefs.getBoolean("pin_for_payments", false));

        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("biometric_login", isChecked).apply();
            Toast.makeText(this, "Biometric login " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        binding.switchPinForPayments.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("pin_for_payments", isChecked).apply();
            Toast.makeText(this, "PIN for payments " + (isChecked ? "required" : "optional"), Toast.LENGTH_SHORT)
                    .show();
        });
    }

    private void loadPaymentSettings() {
        binding.switchPaymentConfirm.setChecked(prefs.getBoolean("payment_confirmation", true));
        binding.switchAutoPay.setChecked(prefs.getBoolean("auto_pay", false));

        binding.switchPaymentConfirm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("payment_confirmation", isChecked).apply();
        });

        binding.switchAutoPay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_pay", isChecked).apply();
            Toast.makeText(this, "Auto-pay " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSlotsSpinner() {
        int savedSlots = prefs.getInt("slots_per_round", 1);

        Integer[] items = new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20 };
        android.widget.ArrayAdapter<Integer> adapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSlotsPerRound.setAdapter(adapter);

        int position = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == savedSlots) {
                position = i;
                break;
            }
        }
        binding.spinnerSlotsPerRound.setSelection(position);

        binding.spinnerSlotsPerRound.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                int selected = (int) parent.getItemAtPosition(position);
                prefs.edit().putInt("slots_per_round", selected).apply();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void setupDatePicker() {
        long savedDate = prefs.getLong("cycle_start_date", 0);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (savedDate != 0) {
            calendar.setTimeInMillis(savedDate);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy",
                    java.util.Locale.getDefault());
            binding.tvCycleStartDate.setText(sdf.format(calendar.getTime()));
        }

        binding.tvCycleStartDate.setOnClickListener(v -> {
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                long millis = calendar.getTimeInMillis();

                prefs.edit().putLong("cycle_start_date", millis).apply();

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy",
                        java.util.Locale.getDefault());
                binding.tvCycleStartDate.setText(sdf.format(calendar.getTime()));

            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH),
                    calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupReservePercentage() {
        int savedPercent = prefs.getInt("date_reserve_percentage", 0);
        binding.etReservePercentage.setText(String.valueOf(savedPercent));

        binding.etReservePercentage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String valStr = binding.etReservePercentage.getText().toString().trim();
                if (!TextUtils.isEmpty(valStr)) {
                    try {
                        int val = Integer.parseInt(valStr);
                        if (val < 0)
                            val = 0;
                        if (val > 100)
                            val = 100;

                        binding.etReservePercentage.setText(String.valueOf(val));
                        prefs.edit().putInt("date_reserve_percentage", val).apply();
                        Toast.makeText(this, "Reserve percentage saved", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        binding.etReservePercentage.setError("Invalid number");
                    }
                }
            }
        });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());

        binding.btnLogout.setOnClickListener(v -> {
            com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(this);
            session.logoutUser();
            finishAffinity();
        });

        // Listeners for clickable items (Toasts for now)
        View.OnClickListener notImplementedListener = v -> Toast
                .makeText(this, "Feature coming soon", Toast.LENGTH_SHORT).show();

        binding.btnProfileInfo.setOnClickListener(notImplementedListener);
        binding.btnChangePassword.setOnClickListener(notImplementedListener);
        binding.btnLanguage.setOnClickListener(notImplementedListener);
        binding.btnNotificationMethod.setOnClickListener(notImplementedListener);
        binding.btnAutoLock.setOnClickListener(notImplementedListener);
        binding.btnDefaultPayment.setOnClickListener(notImplementedListener);
        binding.btnGroupDetails.setOnClickListener(notImplementedListener);
        binding.btnViewMembers.setOnClickListener(notImplementedListener);
        binding.btnContactAdmin.setOnClickListener(notImplementedListener);
        binding.btnDownloadData.setOnClickListener(notImplementedListener);
        binding.btnClearCache.setOnClickListener(v -> Toast.makeText(this, "Cache Cleared", Toast.LENGTH_SHORT).show());
        binding.btnHelpSupport.setOnClickListener(notImplementedListener);
        binding.btnTOS.setOnClickListener(notImplementedListener);
        binding.btnPrivacyPolicy.setOnClickListener(notImplementedListener);
        binding.btnRate
                .setOnClickListener(v -> Toast.makeText(this, "Thank you for rating!", Toast.LENGTH_SHORT).show());
        binding.btnShare.setOnClickListener(v -> Toast.makeText(this, "Sharing...", Toast.LENGTH_SHORT).show());
    }

    private void saveContributionTarget() {
        String amountStr = binding.etContributionAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            binding.etContributionAmount.setError("Enter amount");
            return;
        }

        try {
            double newTarget = Double.parseDouble(amountStr.replace(",", ""));

            if (newTarget <= 0) {
                binding.etContributionAmount.setError("Amount must be greater than 0");
                return;
            }

            viewModel.setContributionTarget(newTarget);
            Toast.makeText(this, "Contribution target updated successfully", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etContributionAmount.setError("Invalid amount");
        }
    }

    private void saveLatePenalty() {
        String amountStr = binding.etLatePenalty.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            binding.etLatePenalty.setError("Enter amount");
            return;
        }

        try {
            double penalty = Double.parseDouble(amountStr.replace(",", ""));

            if (penalty < 0) {
                binding.etLatePenalty.setError("Amount cannot be negative");
                return;
            }

            prefs.edit().putLong("late_penalty", Double.doubleToRawLongBits(penalty)).apply();
            Toast.makeText(this, "Late penalty saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etLatePenalty.setError("Invalid amount");
        }
    }

    private void savePayoutAmount() {
        String amountStr = binding.etPayoutAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            binding.etPayoutAmount.setError("Enter amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr.replace(",", ""));

            if (amount <= 0) {
                binding.etPayoutAmount.setError("Amount must be greater than 0");
                return;
            }

            viewModel.setPayoutAmount(amount);
            Toast.makeText(this, "Payout amount saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etPayoutAmount.setError("Invalid amount");
        }
    }

    private void saveRetentionPercentage() {
        String rateStr = binding.etRetentionPercentage.getText().toString().trim();

        if (TextUtils.isEmpty(rateStr)) {
            binding.etRetentionPercentage.setError("Enter percentage");
            return;
        }

        try {
            double rate = Double.parseDouble(rateStr);

            if (rate < 0 || rate > 100) {
                binding.etRetentionPercentage.setError("Percentage must be between 0 and 100");
                return;
            }

            viewModel.setRetentionPercentage(rate);
            Toast.makeText(this, "Retention percentage saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etRetentionPercentage.setError("Invalid percentage");
        }
    }

    private void saveLoanAmount() {
        String amountStr = binding.etMaxLoanAmount.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            binding.etMaxLoanAmount.setError("Enter amount");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr.replace(",", ""));

            if (amount <= 0) {
                binding.etMaxLoanAmount.setError("Amount must be greater than 0");
                return;
            }

            prefs.edit().putLong("max_loan_amount", Double.doubleToRawLongBits(amount)).apply();
            Toast.makeText(this, "Maximum loan amount saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etMaxLoanAmount.setError("Invalid amount");
        }
    }

    private void saveLoanInterestRate() {
        String rateStr = binding.etLoanInterestRate.getText().toString().trim();

        if (TextUtils.isEmpty(rateStr)) {
            binding.etLoanInterestRate.setError("Enter rate");
            return;
        }

        try {
            double rate = Double.parseDouble(rateStr);

            if (rate < 0 || rate > 100) {
                binding.etLoanInterestRate.setError("Rate must be between 0 and 100");
                return;
            }

            prefs.edit().putLong("loan_interest_rate", Double.doubleToRawLongBits(rate)).apply();
            Toast.makeText(this, "Interest rate saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etLoanInterestRate.setError("Invalid rate");
        }
    }

    private void saveLoanDuration() {
        String durationStr = binding.etMaxLoanDuration.getText().toString().trim();

        if (TextUtils.isEmpty(durationStr)) {
            binding.etMaxLoanDuration.setError("Enter duration");
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);

            if (duration <= 0) {
                binding.etMaxLoanDuration.setError("Duration must be greater than 0");
                return;
            }

            prefs.edit().putInt("max_loan_duration", duration).apply();
            Toast.makeText(this, "Maximum loan duration saved", Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            binding.etMaxLoanDuration.setError("Invalid duration");
        }
    }
}
