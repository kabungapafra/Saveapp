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

    private android.widget.EditText etAmount, etPhoneNumber;
    private android.widget.TextView btnMax, tvRemainingAmount, tvRepaidPercentBadge;
    private android.widget.RadioButton radioAirtel, radioMTN;
    private android.view.View cardAirtel, cardMTN;
    private com.google.android.material.progressindicator.CircularProgressIndicator loanProgressRail;
    private com.airbnb.lottie.LottieAnimationView successAnimation;
    private android.view.View animationOverlay;
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
        btnMax = view.findViewById(R.id.btnMax);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        tvRepaidPercentBadge = view.findViewById(R.id.tvRepaidPercentBadge);
        loanProgressRail = view.findViewById(R.id.loanProgressRail);

        cardAirtel = view.findViewById(R.id.cardAirtel);
        cardMTN = view.findViewById(R.id.cardMTN);
        radioAirtel = view.findViewById(R.id.radioAirtel);
        radioMTN = view.findViewById(R.id.radioMTN);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        view.findViewById(R.id.btnHistory).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Transaction History coming soon", Toast.LENGTH_SHORT).show();
        });

        // Initialize animation views
        successAnimation = view.findViewById(R.id.successAnimation);
        animationOverlay = view.findViewById(R.id.animationOverlay);
    }

    private void loadMemberData() {
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
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
            java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance();

            double totalToPay = loan.getAmount() + loan.getInterest();
            double outstanding = totalToPay - loan.getRepaidAmount();
            double progress = (loan.getRepaidAmount() / totalToPay) * 100;

            tvRemainingAmount.setText(nf.format(outstanding));
            loanProgressRail.setProgress((int) progress);
            tvRepaidPercentBadge.setText(String.format(java.util.Locale.US, "%.0f%% Repaid", progress));

            etAmount.setText(nf.format(outstanding));
        }
    }

    private void setupListeners() {
        btnMax.setOnClickListener(v -> {
            if (activeLoan != null) {
                double outstanding = (activeLoan.getAmount() + activeLoan.getInterest()) - activeLoan.getRepaidAmount();
                etAmount.setText(String.format(java.util.Locale.US, "%.0f", outstanding));
            }
        });

        cardAirtel.setOnClickListener(v -> selectPaymentMethod(true));
        cardMTN.setOnClickListener(v -> selectPaymentMethod(false));
        radioAirtel.setOnClickListener(v -> selectPaymentMethod(true));
        radioMTN.setOnClickListener(v -> selectPaymentMethod(false));

        View view = getView();
        if (view != null) {
            view.findViewById(R.id.btnPayLoan).setOnClickListener(v -> {
                String amountStr = etAmount.getText().toString().replace(",", "");
                String phoneNumber = etPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(amountStr)) {
                    etAmount.setError("Enter amount");
                    return;
                }

                if (TextUtils.isEmpty(phoneNumber)) {
                    etPhoneNumber.setError("Enter phone number");
                    return;
                }

                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    etAmount.setError("Invalid amount");
                    return;
                }

                if (currentMember != null && activeLoan != null) {
                    String loanId = activeLoan.getId();
                    String paymentMethod = radioAirtel.isChecked() ? "Airtel Money" : "MTN MoMo";

                    viewModel.repayLoan(loanId, amount, paymentMethod, phoneNumber,
                            new com.example.save.data.repository.MemberRepository.LoanRepaymentCallback() {
                                @Override
                                public void onResult(boolean success, String message) {
                                    if (success) {
                                        showSuccessAnimation();
                                        etAmount.setText("");
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
    }

    private void selectPaymentMethod(boolean isAirtel) {
        radioAirtel.setChecked(isAirtel);
        radioMTN.setChecked(!isAirtel);

        if (isAirtel) {
            cardAirtel.setBackgroundResource(R.drawable.bg_item_premium_white);
            cardAirtel.setElevation(com.example.save.utils.UIUtils.dpToPx(getContext(), 4));
            radioAirtel.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#215DA1")));
            etPhoneNumber.setHint("e.g. 0755123456");

            cardMTN.setBackgroundResource(R.drawable.bg_payment_card_unselected);
            cardMTN.setElevation(0f);
            radioMTN.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CBD5E1")));
        } else {
            cardMTN.setBackgroundResource(R.drawable.bg_item_premium_white);
            cardMTN.setElevation(com.example.save.utils.UIUtils.dpToPx(getContext(), 4));
            radioMTN.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#215DA1")));
            etPhoneNumber.setHint("e.g. 0772123890");

            cardAirtel.setBackgroundResource(R.drawable.bg_item_premium_white);
            cardAirtel.setBackgroundResource(R.drawable.bg_payment_card_unselected);
            cardAirtel.setElevation(0f);
            radioAirtel.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CBD5E1")));
        }
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
