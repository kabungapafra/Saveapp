package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentGroupSettingsBinding;

public class GroupSettingsFragment extends Fragment {

    private FragmentGroupSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupSettingsBinding.inflate(inflater, container, false);

        setupButtons();
        setupToggles();
        setupFinancialRules();
        loadFinancialRules();
        fetchConfigFromServer();

        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
        String role = session.getUserRole();
        // Show Delete Group button to all admins (not just the creator flag which may not be in session yet)
        boolean isAdmin = role != null && role.equalsIgnoreCase("admin");
        if (!isAdmin) {
            binding.btnDeleteGroup.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    private void setupFinancialRules() {
        binding.rowContribution.setOnClickListener(v -> {
            applyClickAnimation(v);
            showEditNumberDialog("Edit Contribution Amount", binding.valContribution.getText().toString(), binding.valContribution, "UGX ");
        });

        binding.rowFrequency.setOnClickListener(v -> {
            applyClickAnimation(v);
            showFrequencyDialog();
        });

        binding.rowPayoutAmount.setOnClickListener(v -> {
            applyClickAnimation(v);
            showEditNumberDialog("Edit Payout Amount", binding.valPayoutAmount.getText().toString(), binding.valPayoutAmount, "UGX ");
        });

        binding.rowRecipients.setOnClickListener(v -> {
            applyClickAnimation(v);
            showEditNumberDialog("Edit Recipients", binding.valRecipients.getText().toString(), binding.valRecipients, " Member(s)");
        });

        binding.rowLateFee.setOnClickListener(v -> {
            applyClickAnimation(v);
            showEditNumberDialog("Edit Late Fee", binding.valLateFee.getText().toString(), binding.valLateFee, "UGX ");
        });

        binding.rowLoanInterest.setOnClickListener(v -> {
            applyClickAnimation(v);
            showEditNumberDialog("Edit Loan Interest", binding.valLoanInterest.getText().toString(), binding.valLoanInterest, "%");
        });

        binding.rowLoanLateFee.setOnClickListener(v -> {
            applyClickAnimation(v);
            showEditNumberDialog("Edit Loan Late Fee", binding.valLoanLateFee.getText().toString(), binding.valLoanLateFee, "UGX ");
        });

        binding.rowStartDate.setOnClickListener(v -> {
            applyClickAnimation(v);
            showDatePickerDialog();
        });
    }

    private void showEditNumberDialog(String title, String currentValue, android.widget.TextView targetTextView, String formatType) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_premium_input, null);
        dialog.setContentView(view);
        
        android.widget.TextView tvTitle = view.findViewById(R.id.tvSheetTitle);
        android.widget.TextView tvSubtitle = view.findViewById(R.id.tvSheetSubtitle);
        com.google.android.material.textfield.TextInputEditText etInput = view.findViewById(R.id.etSheetInput);
        com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btnSheetSave);
        
        tvTitle.setText(title);
        tvSubtitle.setText("Enter the new " + title.replace("Edit ", "").toLowerCase());
        
        String numericVal = currentValue.replaceAll("[^0-9.]", "");
        etInput.setText(numericVal);
        // Request focus
        etInput.requestFocus();
        
        btnSave.setOnClickListener(v -> {
            String newVal = etInput.getText().toString().trim();
            if (!newVal.isEmpty()) {
                String formattedVal = "";
                if (formatType.equals("UGX ")) {
                    formattedVal = "UGX " + newVal;
                } else if (formatType.equals("%")) {
                    formattedVal = newVal + "%";
                } else if (formatType.equals(" Member(s)")) {
                    formattedVal = newVal + " Member" + (newVal.equals("1") ? "" : "s");
                }
                targetTextView.setText(formattedVal);
                Toast.makeText(getContext(), title + " updated", Toast.LENGTH_SHORT).show();
                
                android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
                prefs.edit().putString("rule_" + title.replaceAll(" ", "_").toLowerCase(), formattedVal).apply();
                
                syncConfigToServer();
                
                dialog.dismiss();
            } else {
                etInput.setError("Required");
            }
        });
        
        // Show keyboard when dialog opens
        dialog.setOnShowListener(d -> {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });
        
        dialog.show();
    }

    private void showFrequencyDialog() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_premium_frequency, null);
        dialog.setContentView(view);
        
        View.OnClickListener clickListener = v -> {
            String selection = "Monthly";
            if (v.getId() == R.id.optDaily) selection = "Daily";
            else if (v.getId() == R.id.optWeekly) selection = "Weekly";
            else if (v.getId() == R.id.optBiWeekly) selection = "Bi-weekly";
            else if (v.getId() == R.id.opt2Months) selection = "Every 2 Months";
            else if (v.getId() == R.id.opt3Months) selection = "Every 3 Months";
            else if (v.getId() == R.id.opt4Months) selection = "Every 4 Months";
            else if (v.getId() == R.id.opt5Months) selection = "Every 5 Months";
            else if (v.getId() == R.id.opt6Months) selection = "Every 6 Months";
            
            binding.valFrequency.setText(selection);
            Toast.makeText(getContext(), "Frequency updated", Toast.LENGTH_SHORT).show();
            
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putString("rule_frequency", selection).apply();
            
            updateNextPayoutDate();
            syncConfigToServer();
            
            dialog.dismiss();
        };
        
        view.findViewById(R.id.optDaily).setOnClickListener(clickListener);
        view.findViewById(R.id.optWeekly).setOnClickListener(clickListener);
        view.findViewById(R.id.optBiWeekly).setOnClickListener(clickListener);
        view.findViewById(R.id.optMonthly).setOnClickListener(clickListener);
        view.findViewById(R.id.opt2Months).setOnClickListener(clickListener);
        view.findViewById(R.id.opt3Months).setOnClickListener(clickListener);
        view.findViewById(R.id.opt4Months).setOnClickListener(clickListener);
        view.findViewById(R.id.opt5Months).setOnClickListener(clickListener);
        view.findViewById(R.id.opt6Months).setOnClickListener(clickListener);
        
        dialog.show();
    }

    private void showDatePickerDialog() {
        com.google.android.material.datepicker.MaterialDatePicker<Long> datePicker = 
            com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECT START DATE")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .build();
                
        datePicker.addOnPositiveButtonClickListener(selection -> {
            java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
            String formattedDate = sdf.format(calendar.getTime());
            binding.valStartDate.setText(formattedDate);
            Toast.makeText(getContext(), "Start Date updated", Toast.LENGTH_SHORT).show();
            
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putString("rule_start_date", formattedDate).apply();
            
            updateNextPayoutDate();
            syncConfigToServer();
        });
        
        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void updateNextPayoutDate() {
        String startDateStr = binding.valStartDate.getText().toString();
        String frequency = binding.valFrequency.getText().toString();
        
        if (startDateStr.isEmpty() || frequency.isEmpty()) return;
        
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
            java.util.Date startDate = sdf.parse(startDateStr);
            if (startDate == null) return;
            
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(startDate);
            
            switch (frequency) {
                case "Daily":
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, 1);
                    break;
                case "Weekly":
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                    break;
                case "Bi-weekly":
                    calendar.add(java.util.Calendar.WEEK_OF_YEAR, 2);
                    break;
                case "Monthly":
                    calendar.add(java.util.Calendar.MONTH, 1);
                    break;
                case "Every 2 Months":
                    calendar.add(java.util.Calendar.MONTH, 2);
                    break;
                case "Every 3 Months":
                    calendar.add(java.util.Calendar.MONTH, 3);
                    break;
                case "Every 4 Months":
                    calendar.add(java.util.Calendar.MONTH, 4);
                    break;
                case "Every 5 Months":
                    calendar.add(java.util.Calendar.MONTH, 5);
                    break;
                case "Every 6 Months":
                    calendar.add(java.util.Calendar.MONTH, 6);
                    break;
            }
            
            String nextDateStr = sdf.format(calendar.getTime());
            
            requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                .edit().putString("rule_next_payout_date", nextDateStr).apply();
            
            if (binding.tvNextAutomaticPayoutDate != null) {
                binding.tvNextAutomaticPayoutDate.setText("Next Payout: " + nextDateStr);
            }
            if (binding.tvNextContributionDate != null) {
                binding.tvNextContributionDate.setText("Next: " + nextDateStr);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtons() {
        // No Back or Save buttons as per immersive redesign
        
        binding.btnInviteOthers.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Invite link copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        binding.btnDeleteGroup.setOnClickListener(v -> {
            applyClickAnimation(v);
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group? This will permanently remove all members, transactions, and group data. This action cannot be undone.")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    deleteGroup();
                })
                .setNegativeButton("CANCEL", null)
                .show();
        });
    }

    private void deleteGroup() {
        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient.getClient(requireContext()).create(com.example.save.data.network.ApiService.class);
        apiService.deleteGroup().enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call, retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "Group deleted successfully", Toast.LENGTH_LONG).show();
                        com.example.save.utils.SessionManager.getInstance(requireContext()).logoutUser();
                    } else {
                        String error = "Failed to delete group";
                        if (response.code() == 403) error = "Only the group creator can delete the group";
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupToggles() {
        binding.switchAutomaticPayouts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Automatic Payouts " + state, Toast.LENGTH_SHORT).show();
            requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("switch_automatic_payouts", isChecked).apply();
            syncConfigToServer();
        });

        binding.switchScheduledContributions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Scheduled Contributions " + state, Toast.LENGTH_SHORT).show();
            requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("switch_scheduled_contributions", isChecked).apply();
            syncConfigToServer();
        });

        binding.switchSmartRoundups.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Smart Round-ups " + state, Toast.LENGTH_SHORT).show();
            requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("switch_smart_roundups", isChecked).apply();
            syncConfigToServer();
        });

        binding.switchAutomatedCycle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Automated Cycle " + state, Toast.LENGTH_SHORT).show();
            requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("switch_automated_cycle", isChecked).apply();
            syncConfigToServer();
        });

        binding.switchLoanRequests.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Loan Requests " + state, Toast.LENGTH_SHORT).show();
            requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE).edit().putBoolean("switch_loan_requests", isChecked).apply();
            syncConfigToServer();
        });

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean("group_push_notifications", isChecked).apply();
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Push Notifications " + state, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFinancialRules() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
        
        binding.valContribution.setText(prefs.getString("rule_edit_contribution_amount", "UGX 500"));
        binding.valFrequency.setText(prefs.getString("rule_frequency", "Monthly"));
        binding.valPayoutAmount.setText(prefs.getString("rule_edit_payout_amount", "UGX 50,000"));
        binding.valRecipients.setText(prefs.getString("rule_edit_recipients", "1 Member"));
        binding.valLateFee.setText(prefs.getString("rule_edit_late_fee", "UGX 25"));
        binding.valLoanInterest.setText(prefs.getString("rule_edit_loan_interest", "5%"));
        binding.valLoanLateFee.setText(prefs.getString("rule_edit_loan_late_fee", "UGX 50"));
        binding.valStartDate.setText(prefs.getString("rule_start_date", "Oct 1, 2023"));
        
        binding.switchAutomaticPayouts.setChecked(prefs.getBoolean("switch_automatic_payouts", true));
        binding.switchScheduledContributions.setChecked(prefs.getBoolean("switch_scheduled_contributions", true));
        binding.switchSmartRoundups.setChecked(prefs.getBoolean("switch_smart_roundups", false));
        binding.switchAutomatedCycle.setChecked(prefs.getBoolean("switch_automated_cycle", true));
        binding.switchLoanRequests.setChecked(prefs.getBoolean("switch_loan_requests", true));
        binding.switchNotifications.setChecked(prefs.getBoolean("group_push_notifications", true));
        
        updateNextPayoutDate();
    }

    private void fetchConfigFromServer() {
        com.example.save.data.repository.MemberRepository.getInstance(requireContext())
            .fetchSystemConfig((success, config, message) -> {
                if (success && isAdded() && config != null) {
                    requireActivity().runOnUiThread(() -> loadFinancialRules());
                }
            });
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
        }
    }

    private void syncConfigToServer() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
        com.example.save.data.models.SystemConfig config = new com.example.save.data.models.SystemConfig();
        
        try {
            String contributionStr = prefs.getString("rule_edit_contribution_amount", "UGX 500").replaceAll("[^0-9.]", "");
            config.setContributionAmount(Double.parseDouble(contributionStr.isEmpty() ? "0" : contributionStr));
            
            String payoutStr = prefs.getString("rule_edit_payout_amount", "UGX 50000").replaceAll("[^0-9.]", "");
            config.setPayoutAmount(Double.parseDouble(payoutStr.isEmpty() ? "0" : payoutStr));
            
            String lateFeeStr = prefs.getString("rule_edit_late_fee", "UGX 25").replaceAll("[^0-9.]", "");
            config.setLatePenaltyRate(Double.parseDouble(lateFeeStr.isEmpty() ? "0" : lateFeeStr));
            
            String loanInterestStr = prefs.getString("rule_edit_loan_interest", "5%").replaceAll("[^0-9.]", "");
            config.setLoanInterestRate(Double.parseDouble(loanInterestStr.isEmpty() ? "0" : loanInterestStr));
            
            String loanLateFeeStr = prefs.getString("rule_edit_loan_late_fee", "UGX 50").replaceAll("[^0-9.]", "");
            config.setLoanLateFee(Double.parseDouble(loanLateFeeStr.isEmpty() ? "0" : loanLateFeeStr));
            
            String recipientsStr = prefs.getString("rule_edit_recipients", "1 Member").replaceAll("[^0-9]", "");
            config.setRecipients(Integer.parseInt(recipientsStr.isEmpty() ? "1" : recipientsStr));
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        
        config.setFrequency(prefs.getString("rule_frequency", "Monthly"));
        config.setStartDate(prefs.getString("rule_start_date", "Oct 1, 2023"));
        
        config.setAutomaticPayouts(prefs.getBoolean("switch_automatic_payouts", true));
        config.setScheduledContributions(prefs.getBoolean("switch_scheduled_contributions", true));
        config.setSmartRoundups(prefs.getBoolean("switch_smart_roundups", false));
        config.setAutomatedCycle(prefs.getBoolean("switch_automated_cycle", true));
        config.setLoanRequests(prefs.getBoolean("switch_loan_requests", true));
        
        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient.getClient(requireContext()).create(com.example.save.data.network.ApiService.class);
        apiService.updateSystemConfig(config).enqueue(new retrofit2.Callback<com.example.save.data.models.SystemConfig>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.models.SystemConfig> call, retrofit2.Response<com.example.save.data.models.SystemConfig> response) {
                // Background sync, no toast needed for success to keep it unobtrusive
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.models.SystemConfig> call, Throwable t) {
                // Background sync failure
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
