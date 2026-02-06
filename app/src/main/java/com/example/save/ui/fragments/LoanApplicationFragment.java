package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.databinding.FragmentLoanApplicationBinding;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.text.NumberFormat;
import java.util.Locale;

import com.example.save.R;
import com.example.save.ui.utils.SimpleTextWatcher;

public class LoanApplicationFragment extends Fragment {

    private FragmentLoanApplicationBinding binding;
    private MembersViewModel viewModel;

    public static LoanApplicationFragment newInstance() {
        return new LoanApplicationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoanApplicationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupConfig();
        setupListeners();
    }

    private void setupConfig() {
        // Fetch config from backend
        viewModel.fetchSystemConfig((success, config, message) -> {
            if (success && config != null) {
                if (getContext() == null)
                    return;

                binding.tvInterestRate.setText("Interest Rate: " + config.getLoanInterestRate() + "%");
                // We initially set max loan to the absolute limit, but will update with user
                // eligibility
                binding.tvMaxLoanAmount.setText("Max Limit: UGX " +
                        NumberFormat.getNumberInstance(Locale.US).format(config.getMaxLoanLimit()));

                // Fetch user specific eligibility
                checkEligibility();
            } else {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Failed to load config: " + message, Toast.LENGTH_SHORT).show();
            }
        });

        // Initial visibility
        binding.layoutGuarantor.setVisibility(viewModel.isGuarantorRequired() ? View.VISIBLE : View.GONE);
    }

    private void checkEligibility() {
        // Call with dummy amount to get max eligibility
        viewModel.checkLoanEligibility(0, 1, (success, response, message) -> {
            if (success && response != null) {
                if (getContext() == null)
                    return;

                double maxEligible = response.getMaxEligibleAmount();
                binding.tvMaxLoanAmount.setText("Max Eligible: UGX " +
                        NumberFormat.getNumberInstance(Locale.US).format(maxEligible));
            }
        });
    }

    private void showRepaymentSchedule(double amount, int duration, double interestRate) {
        // Call backend for schedule
        android.app.ProgressDialog progress = new android.app.ProgressDialog(getContext());
        progress.setMessage("Loading schedule...");
        progress.show();

        viewModel.getRepaymentSchedule(amount, duration, (success, response, message) -> {
            progress.dismiss();
            if (success && response != null && getContext() != null) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
                builder.setTitle("Repayment Schedule");

                StringBuilder scheduleText = new StringBuilder();
                scheduleText.append("Total Repayment: UGX ")
                        .append(NumberFormat.getNumberInstance(Locale.US).format(response.getTotalRepayment()))
                        .append("\n");
                scheduleText.append("Monthly: UGX ")
                        .append(NumberFormat.getNumberInstance(Locale.US).format(response.getMonthlyInstallment()))
                        .append("\n\n");

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

                for (com.example.save.data.models.RepaymentScheduleItem item : response.getSchedule()) {
                    // Parse date string (ISO)
                    String dateStr = item.getDueDate();
                    String displayDate = dateStr;
                    try {
                        // Backend likely sends ISO format
                        // Simple ISO parser if needed or just display string if backend sends readable
                        // Assuming backend sends ISO, we might want to parse.
                        // For now, let's just display what we get or parse if simpler.
                        // Actually backend sends datetime object which Pydantic/FastAPI serializes to
                        // ISO string.
                        // Let's try to parse or just print.
                        // Quick parse:
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            java.time.format.DateTimeFormatter isoFormatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME;
                            java.time.LocalDateTime date = java.time.LocalDateTime.parse(dateStr, isoFormatter);
                            displayDate = date.format(
                                    java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()));
                        } else {
                            // Fallback
                            displayDate = dateStr.substring(0, 10);
                        }
                    } catch (Exception e) {
                        displayDate = dateStr;
                    }

                    scheduleText.append("Month ").append(item.getMonth()).append(": ")
                            .append(displayDate).append(" - ")
                            .append("UGX ").append(NumberFormat.getNumberInstance(Locale.US).format(item.getAmount()))
                            .append("\n");
                }

                builder.setMessage(scheduleText.toString());
                builder.setPositiveButton("OK", null);
                builder.show();
            } else {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Failed to load schedule: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private java.util.List<com.example.save.data.models.Member> availableGuarantors = new java.util.ArrayList<>();

    private void setupListeners() {
        binding.btnBack.setOnClickListener(
                v -> {
                    if (getActivity() != null) {
                        getActivity().getOnBackPressedDispatcher().onBackPressed();
                    }
                });

        binding.etLoanAmount.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateSummary();
            }
        });

        // Also listen to duration changes
        binding.etDuration.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateSummary();
            }
        });

        binding.btnSubmitLoan.setOnClickListener(v -> submitApplication());

        // Setup Guarantor Spinner
        if (viewModel.isGuarantorRequired()) {
            viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
                availableGuarantors.clear();
                // Filter out admin? Or just show all. Ideally exclude current user if we knew
                // who they were.
                // For now, including all.
                if (members != null) {
                    availableGuarantors.addAll(members);
                }

                java.util.List<String> names = new java.util.ArrayList<>();
                names.add("Select a Guarantor"); // Default hint
                for (com.example.save.data.models.Member m : availableGuarantors) {
                    names.add(m.getName());
                }

                android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerGuarantor.setAdapter(adapter);
            });

            binding.spinnerGuarantor.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position > 0) { // 0 is hint
                        com.example.save.data.models.Member selected = availableGuarantors.get(position - 1);
                        binding.etGuarantorPhone.setText(selected.getPhone());
                    } else {
                        binding.etGuarantorPhone.setText("");
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    binding.etGuarantorPhone.setText("");
                }
            });
        }
    }

    private void calculateSummary() {
        try {
            String amountStr = binding.etLoanAmount.getText().toString().replace(",", "");
            String durationStr = binding.etDuration.getText().toString();

            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                // Use cached interest rate or fetch? Use cached for instant feedback,
                // but strictly we should fetch or use what we got from config.
                // We updated repo's interest rate in setupConfig, so getLoanInterestRate should
                // be current
                double interestRate = viewModel.getLoanInterestRate();

                // Simple estimation for UI until they click "View Schedule"
                // Using backend formula locally for estimation: Amount + (Amount * Rate *
                // (Duration/12))
                // But wait, backend formula is Annual.
                // Let's just show "Calculating..." or update via API call? API call on every
                // keystroke is bad.
                // We'll use the local estimation matching backend logic.

                double total = 0;
                if (!durationStr.isEmpty()) {
                    int duration = Integer.parseInt(durationStr);
                    double interest = amount * (interestRate / 100.0) * (duration / 12.0);
                    // Wait, if duration < 12, this is small.
                    // Let's stick to what we see in `business_logic.py`:
                    // return amount * interest_rate * (duration_months / 12)

                    total = amount + interest;

                    binding.tvRepaymentSummary.setText("Est. Total: UGX " +
                            NumberFormat.getNumberInstance(Locale.US).format(total));

                    binding.btnViewSchedule.setVisibility(View.VISIBLE);
                    binding.btnViewSchedule
                            .setOnClickListener(v -> showRepaymentSchedule(amount, duration, interestRate));
                } else {
                    binding.tvRepaymentSummary.setText("Enter Duration");
                    binding.btnViewSchedule.setVisibility(View.GONE);
                }

            } else {
                binding.tvRepaymentSummary.setText("Total Repayment: UGX 0");
                binding.btnViewSchedule.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            binding.tvRepaymentSummary.setText("Total Repayment: UGX 0");
            binding.btnViewSchedule.setVisibility(View.GONE);
        }
    }

    private void submitApplication() {
        String amountStr = binding.etLoanAmount.getText().toString().trim();
        String durationStr = binding.etDuration.getText().toString().trim();

        String reason = binding.etReason.getText().toString().trim();

        if (TextUtils.isEmpty(amountStr)) {
            binding.etLoanAmount.setError("Required");
            return;
        }

        if (TextUtils.isEmpty(durationStr)) {
            binding.etDuration.setError("Required");
            return;
        }

        String guarantor = "";
        String guarantorPhone = "";

        if (viewModel.isGuarantorRequired()) {
            if (binding.spinnerGuarantor.getSelectedItemPosition() <= 0) {
                Toast.makeText(getContext(), "Please select a guarantor", Toast.LENGTH_SHORT).show();
                return;
            }
            // Get from spinner
            guarantor = binding.spinnerGuarantor.getSelectedItem().toString();
            guarantorPhone = binding.etGuarantorPhone.getText().toString().trim();

            if (TextUtils.isEmpty(guarantorPhone)) {
                Toast.makeText(getContext(), "Guarantor phone missing", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            double amount = Double.parseDouble(amountStr.replace(",", ""));
            int duration = Integer.parseInt(durationStr);

            // Validate eligibility via API before submitting?
            // Or just submit and let backend reject?
            // User experience is better if we validate first.
            // But we can also just rely on submit returning error.
            // Let's just submit. Backend `submitLoanRequest` already checks eligibility.

            // Create and submit loan request
            com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(requireContext());
            String userName = session.getUserName();
            if (userName == null)
                userName = "Active Member"; // Fallback

            com.example.save.data.models.LoanRequest loanRequest = new com.example.save.data.models.LoanRequest(
                    userName,
                    amount,
                    duration,
                    guarantor,
                    guarantorPhone,
                    reason);

            // Submit loan request via API - backend will calculate interest and validate
            viewModel.submitLoanRequest(loanRequest,
                    new com.example.save.data.repository.MemberRepository.LoanSubmissionCallback() {
                        @Override
                        public void onResult(boolean success, String message) {
                            if (success) {
                                Toast.makeText(getContext(),
                                        message != null ? message : "Loan request submitted for admin approval!",
                                        Toast.LENGTH_LONG).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            } else {
                                Toast.makeText(getContext(),
                                        message != null ? message : "Failed to submit loan request",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
