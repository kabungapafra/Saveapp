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

        binding.tvMaxLoanAmount.setText("UGX " + NumberFormat.getNumberInstance(Locale.US).format(maxLoan));
        binding.tvInterestRate.setText("Interest Rate: " + interest + "%");

        binding.layoutGuarantor.setVisibility(needsGuarantor ? View.VISIBLE : View.GONE);
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
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                double interestRate = viewModel.getLoanInterestRate();
                double total = amount + (amount * interestRate / 100);

                binding.tvRepaymentSummary.setText("Total Repayment: UGX " +
                        NumberFormat.getNumberInstance(Locale.US).format(total));
            } else {
                binding.tvRepaymentSummary.setText("Total Repayment: UGX 0");
            }
        } catch (NumberFormatException e) {
            binding.tvRepaymentSummary.setText("Total Repayment: UGX 0");
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
            com.example.save.data.models.LoanRequest loanRequest = new com.example.save.data.models.LoanRequest(
                    "Current User", // TODO: Get actual logged-in user name
                    amount,
                    duration,
                    guarantor,
                    guarantorPhone,
                    reason);

            viewModel.submitLoanRequest(loanRequest);

            Toast.makeText(getContext(), "Loan request submitted for admin approval!", Toast.LENGTH_LONG).show();
            requireActivity().getSupportFragmentManager().popBackStack();

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
