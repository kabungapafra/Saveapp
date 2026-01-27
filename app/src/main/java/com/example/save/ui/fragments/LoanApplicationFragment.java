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
        // Load config from ViewModel/Repo
        double maxLoan = viewModel.getMaxLoanAmount();
        double interest = viewModel.getLoanInterestRate();
        boolean needsGuarantor = viewModel.isGuarantorRequired();

        binding.tvMaxLoanAmount.setText("Max Limit: UGX " + NumberFormat.getNumberInstance(Locale.US).format(maxLoan));
        binding.tvInterestRate.setText("Interest Rate: " + interest + "%");

        // NOTE: Loan eligibility should be fetched from backend API
        // Backend will calculate eligibility based on: savings, existing loans, payment
        // history, etc.
        // For now, showing max loan limit - actual eligibility will be validated by
        // backend on submission
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(requireContext());
        String userName = session.getUserName();
        if (userName != null) {
            // TODO: Call backend API to get loan eligibility
            // GET /loans/eligibility?memberEmail={email}
            // Backend will return: { "maxEligibleAmount": 1500000, "reason": "3x savings
            // rule" }

            // Placeholder: Show max loan limit (backend will validate actual eligibility)
            binding.tvMaxLoanAmount.setText(
                    "Max Limit: UGX " + NumberFormat.getNumberInstance(Locale.US).format(maxLoan));
        }

        binding.layoutGuarantor.setVisibility(needsGuarantor ? View.VISIBLE : View.GONE);

        // Add View Schedule Button Logic if button exists, or just append a view
        // programmatically if ID not found
        // For this task, assuming we might need to add a button to the layout or just
        // show it via a new dialog triggered by a textview link
    }

    private void showRepaymentSchedule(double amount, int duration, double interestRate) {
        // Simple Dialog to show schedule
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Repayment Schedule");

        StringBuilder schedule = new StringBuilder();
        double totalInterest = amount * interestRate / 100;
        double totalAmount = amount + totalInterest;
        double monthlyInstallment = totalAmount / duration;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        java.util.Calendar cal = java.util.Calendar.getInstance();

        for (int i = 1; i <= duration; i++) {
            cal.add(java.util.Calendar.MONTH, 1);
            schedule.append("Month ").append(i).append(": ")
                    .append(sdf.format(cal.getTime())).append(" - ")
                    .append("UGX ").append(NumberFormat.getNumberInstance(Locale.US).format(monthlyInstallment))
                    .append("\n");
        }

        builder.setMessage(schedule.toString());
        builder.setPositiveButton("OK", null);
        builder.show();
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
                double interestRate = viewModel.getLoanInterestRate();
                double total = amount + (amount * interestRate / 100);

                binding.tvRepaymentSummary.setText("Total Repayment: UGX " +
                        NumberFormat.getNumberInstance(Locale.US).format(total));

                // If duration is valid, enable clicking summary to see schedule
                if (!durationStr.isEmpty()) {
                    int duration = Integer.parseInt(durationStr);
                    // binding.tvRepaymentSummary.setOnClickListener(v ->
                    // showRepaymentSchedule(amount, duration, interestRate));
                    // binding.tvRepaymentSummary.setTextColor(getResources().getColor(R.color.deep_blue));

                    binding.btnViewSchedule.setVisibility(View.VISIBLE);
                    binding.btnViewSchedule
                            .setOnClickListener(v -> showRepaymentSchedule(amount, duration, interestRate));
                } else {
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

            double maxLoan = viewModel.getMaxLoanAmount();
            int maxDuration = viewModel.getMaxLoanDuration();

            if (amount > maxLoan) {
                binding.etLoanAmount
                        .setError("Exceeds limit of " + NumberFormat.getNumberInstance(Locale.US).format(maxLoan));
                return;
            }

            if (duration > maxDuration) {
                binding.etDuration.setError("Max duration is " + maxDuration + " months");
                return;
            }

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
