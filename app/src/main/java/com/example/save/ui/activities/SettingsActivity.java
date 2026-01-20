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

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private MembersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(MembersViewModel.class);

        initViews();
        setupListeners();
    }

    private void initViews() {
        // Load current contribution target
        double currentTarget = viewModel.getContributionTarget();
        binding.etContributionAmount.setText(String.format(java.util.Locale.US, "%.0f", currentTarget));

        // Save when user finishes editing
        binding.etContributionAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveContributionTarget();
            }
        });

        // Initialize new Admin Configs
        setupSlotsSpinner();
        setupDatePicker();
        setupReservePercentage();
    }

    private void setupSlotsSpinner() {
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
        int savedSlots = prefs.getInt("slots_per_round", 1);

        Integer[] items = new Integer[] { 1, 2, 3, 4, 5, 10, 15, 20 };
        android.widget.ArrayAdapter<Integer> adapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSlotsPerRound.setAdapter(adapter);

        // Set selection
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
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
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
        android.content.SharedPreferences prefs = getSharedPreferences("ChamaPrefs", MODE_PRIVATE);
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

                        binding.etReservePercentage.setText(String.valueOf(val)); // Normalize display
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
            // Clear any stored session data (if added in future)
            // For now, just navigate back to Login/Entry point cleanly
            android.content.Intent intent = new android.content.Intent(this, MemberRegistrationActivity.class);
            intent.setFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
}
