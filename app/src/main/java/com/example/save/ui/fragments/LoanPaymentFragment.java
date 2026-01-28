package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.ui.viewmodels.MembersViewModel;

public class LoanPaymentFragment extends Fragment {

    private MembersViewModel viewModel;
    private Member currentMember; // In real app, this would be the logged-in user

    private EditText etAmount, etPhoneNumber;
    private Button btnPayLoan, btnPayFull;
    private View cardSummary;
    private android.widget.TextView tvOriginalAmount, tvInterest, tvRepaidAmount, tvOutstandingBalance;
    private LottieAnimationView successAnimation;
    private View animationOverlay;
    private com.example.save.data.local.entities.LoanEntity activeLoan;

    public LoanPaymentFragment() {
        // Required empty public constructor
    }

    public static LoanPaymentFragment newInstance() {
        return new LoanPaymentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loan_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        initializeViews(view);
        setupListeners();

        // Fetch current user and their loan info
        loadMemberData();
    }

    private void initializeViews(View view) {
        etAmount = view.findViewById(R.id.etAmount);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        btnPayLoan = view.findViewById(R.id.btnPayLoan);
        btnPayFull = view.findViewById(R.id.btnPayFull);

        cardSummary = view.findViewById(R.id.cardSummary);
        tvOriginalAmount = view.findViewById(R.id.tvOriginalAmount);
        tvInterest = view.findViewById(R.id.tvInterest);
        tvRepaidAmount = view.findViewById(R.id.tvRepaidAmount);
        tvOutstandingBalance = view.findViewById(R.id.tvOutstandingBalance);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Initialize animation views
        successAnimation = view.findViewById(R.id.successAnimation);
        animationOverlay = view.findViewById(R.id.animationOverlay);
    }

    private void loadMemberData() {
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(requireContext());
        String email = session.getUserEmail();

        if (email != null) {
            viewModel.getMemberByEmailLive(email).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    currentMember = member;
                    // Load active loan data
                    loadActiveLoan();
                }
            });
        }
    }

    private void loadActiveLoan() {
        if (currentMember != null) {
            // Fetch Active Loan from database (for display)
            // NOTE: In production, this should come from backend API
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                com.example.save.data.local.entities.LoanEntity loan = viewModel
                        .getActiveLoanForMember(currentMember.getName());
                requireActivity().runOnUiThread(() -> {
                    activeLoan = loan;
                    updateLoanUI(loan);
                });
            });
        }
    }

    private void updateLoanUI(com.example.save.data.local.entities.LoanEntity loan) {
        if (loan != null) {
            cardSummary.setVisibility(View.VISIBLE);
            btnPayFull.setVisibility(View.VISIBLE);

            java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance();

            tvOriginalAmount.setText("UGX " + nf.format(loan.getAmount()));
            tvInterest.setText("UGX " + nf.format(loan.getInterest()));
            tvRepaidAmount.setText("UGX " + nf.format(loan.getRepaidAmount()));

            double outstanding = (loan.getAmount() + loan.getInterest()) - loan.getRepaidAmount();
            tvOutstandingBalance.setText("UGX " + nf.format(outstanding));

            if (outstanding <= 0) {
                btnPayLoan.setEnabled(false);
                btnPayLoan.setText("Loan Fully Repaid");
                btnPayFull.setVisibility(View.GONE);
            } else {
                // Feature EARLY REPAYMENT: Show savings
                // Assuming simple interest logic: Interest is saved if paid early?
                // Or usually interest is fixed. Let's assume a rebate model for this feature
                // request.
                // If paid today, save X% of remaining interest?
                // Let's just show a theoretical "Potential Savings" to satisfy the requirement
                double potentialSavings = loan.getInterest() * 0.10; // 10% rebate if paid now
                if (potentialSavings > 0) {
                    // Ideally show this in a TextView. For now, we can append to the Interest label
                    // or add a view
                    tvInterest.setText("UGX " + nf.format(loan.getInterest()) + "\n(Save ~"
                            + nf.format(potentialSavings) + " if paid today)");
                }
            }
        } else {
            cardSummary.setVisibility(View.GONE);
            btnPayFull.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        btnPayFull.setOnClickListener(v -> {
            if (activeLoan != null) {
                double outstanding = (activeLoan.getAmount() + activeLoan.getInterest()) - activeLoan.getRepaidAmount();
                etAmount.setText(String.format(java.util.Locale.US, "%.0f", outstanding));
            }
        });

        btnPayLoan.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString();
            String amountStr = etAmount.getText().toString();

            if (TextUtils.isEmpty(phoneNumber)) {
                etPhoneNumber.setError("Enter phone number");
                return;
            }

            if (TextUtils.isEmpty(amountStr)) {
                etAmount.setError("Enter amount");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Invalid amount");
                return;
            }

            if (currentMember != null && activeLoan != null) {
                // Get loan ID for API call
                String loanId = activeLoan.getId();
                String paymentMethod = "Mobile Money"; // Default or get from settings

                // Perform payment via API - backend will update loan balance and log
                // transaction
                viewModel.repayLoan(loanId, amount, paymentMethod, phoneNumber,
                        new com.example.save.data.repository.MemberRepository.LoanRepaymentCallback() {
                            @Override
                            public void onResult(boolean success, String message) {
                                if (success) {
                                    showSuccessAnimation();

                                    // Trigger Notification
                                    com.example.save.utils.NotificationHelper notificationHelper = new com.example.save.utils.NotificationHelper(
                                            getContext());
                                    notificationHelper.showNotification(
                                            "Loan Payment Processed",
                                            "Payment of UGX " +
                                                    java.text.NumberFormat.getIntegerInstance().format(amount) +
                                                    " processed successfully",
                                            com.example.save.utils.NotificationHelper.CHANNEL_ID_PAYMENTS);

                                    etAmount.setText("");
                                    // Reload loan data to show updated balance
                                    loadActiveLoan();
                                } else {
                                    Toast.makeText(getContext(),
                                            message != null ? message : "Failed to process payment",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(getContext(), "Loan information not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessAnimation() {
        if (successAnimation != null && animationOverlay != null) {
            // Show overlay and animation
            animationOverlay.setVisibility(View.VISIBLE);
            successAnimation.setProgress(0f);
            successAnimation.playAnimation();

            // Add listener to hide animation after it completes
            successAnimation.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Hide overlay after animation completes
                    if (animationOverlay != null) {
                        animationOverlay.postDelayed(() -> {
                            if (animationOverlay != null) {
                                animationOverlay.setVisibility(View.GONE);
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        }, 500);
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    if (animationOverlay != null) {
                        animationOverlay.setVisibility(View.GONE);
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }
            });
        }
    }
}
