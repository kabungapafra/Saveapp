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
import com.example.save.util.ViewUtils;

public class LoanPaymentFragment extends Fragment {

    private MembersViewModel viewModel;
    private Member currentMember; // In real app, this would be the logged-in user

    private android.widget.EditText etAmount, etPhoneNumber;
    private android.widget.TextView tvRemainingAmount, tvRepaidPercentBadge, tvPrincipal, tvInterest, tvDueDate;
    private com.google.android.material.button.MaterialButton btnQuarter, btnHalf, btnMax;
    private android.widget.RadioButton radioAirtel, radioMTN;
    private android.view.View cardAirtel, cardMTN, emptyState, contentScroll, bottomAction;
    private com.google.android.material.progressindicator.LinearProgressIndicator loanProgressRail;
    private com.airbnb.lottie.LottieAnimationView successAnimation;
    private android.view.View animationOverlay;
    private com.example.save.data.models.LoanEntity activeLoan;
    private double outstandingAmount = 0;

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

        // Default to the "no active loan" state until data arrives, so neither admin nor
        // member ever sees a stray repayment form before the loan has loaded.
        updateLoanUI(null);

        // Fetch current user and their loan info
        loadMemberData();
        // Also fetch independent of the member lookup so the admin (whose member record may
        // not resolve locally) gets the same data-driven behaviour as a member.
        loadActiveLoan();
    }

    private void initializeViews(View view) {
        etAmount = view.findViewById(R.id.etAmount);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        btnQuarter = view.findViewById(R.id.btnQuarter);
        btnHalf = view.findViewById(R.id.btnHalf);
        btnMax = view.findViewById(R.id.btnMax);
        tvRemainingAmount = view.findViewById(R.id.tvRemainingAmount);
        tvRepaidPercentBadge = view.findViewById(R.id.tvRepaidPercentBadge);
        tvPrincipal = view.findViewById(R.id.tvPrincipal);
        tvInterest = view.findViewById(R.id.tvInterest);
        tvDueDate = view.findViewById(R.id.tvDueDate);
        loanProgressRail = view.findViewById(R.id.loanProgressRail);
        emptyState = view.findViewById(R.id.emptyState);
        contentScroll = view.findViewById(R.id.contentScroll);
        bottomAction = view.findViewById(R.id.bottomAction);

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
        String phone = session.getUserPhone();

        if (phone != null && !phone.isEmpty()) {
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    currentMember = member;
                    // Prefill the member's own number (they can change it)
                    if (etPhoneNumber.getText().toString().trim().isEmpty() && member.getPhone() != null) {
                        etPhoneNumber.setText(member.getPhone());
                    }
                    // Load active loan data
                    loadActiveLoan();
                }
            });
        }
    }

    private void loadActiveLoan() {
        if (!isAdded()) return;

        // Identify the current user — prefer the resolved member, fall back to the session.
        final String myId = currentMember != null ? currentMember.getId() : null;
        final String myName = currentMember != null
                ? currentMember.getName()
                : com.example.save.utils.SessionManager.getInstance(requireContext()).getUserName();

        com.example.save.data.network.ApiService api = com.example.save.data.network.RetrofitClient
                .getClient(requireContext()).create(com.example.save.data.network.ApiService.class);

        api.getLoans(100, 0).enqueue(
                new retrofit2.Callback<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>>() {
            @Override
            public void onResponse(
                    retrofit2.Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call,
                    retrofit2.Response<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> response) {
                if (!isAdded()) return;
                com.example.save.data.models.LoanEntity best = findMyActiveLoan(
                        response.isSuccessful() && response.body() != null ? response.body().getData() : null,
                        myId, myName);
                activeLoan = best;
                updateLoanUI(best);
            }

            @Override
            public void onFailure(
                    retrofit2.Call<com.example.save.data.models.PaginatedResponse<com.example.save.data.models.LoanEntity>> call,
                    Throwable t) {
                if (!isAdded()) return;
                activeLoan = null;
                updateLoanUI(null);
            }
        });
    }

    /** Picks the caller's own repayable (active/approved/overdue, still-owing) loan, or null. */
    private static com.example.save.data.models.LoanEntity findMyActiveLoan(
            java.util.List<com.example.save.data.models.LoanEntity> loans, String myId, String myName) {
        if (loans == null) return null;
        for (com.example.save.data.models.LoanEntity e : loans) {
            boolean mine = (myId != null && myId.equals(e.getMemberId()))
                    || (myName != null && myName.equalsIgnoreCase(e.getMemberName()));
            if (!mine) continue;
            String st = e.getStatus();
            boolean repayable = "ACTIVE".equalsIgnoreCase(st)
                    || "APPROVED".equalsIgnoreCase(st)
                    || "OVERDUE".equalsIgnoreCase(st);
            if (!repayable) continue;
            if (e.getAmount() + e.getInterest() - e.getRepaidAmount() <= 0) continue;
            return e;
        }
        return null;
    }

    private void updateLoanUI(com.example.save.data.models.LoanEntity loan) {
        boolean hasLoan = loan != null
                && (loan.getAmount() + loan.getInterest() - loan.getRepaidAmount()) > 0;

        // Toggle between the repayment form and the "all clear" empty state
        emptyState.setVisibility(hasLoan ? View.GONE : View.VISIBLE);
        contentScroll.setVisibility(hasLoan ? View.VISIBLE : View.GONE);
        bottomAction.setVisibility(hasLoan ? View.VISIBLE : View.GONE);

        if (!hasLoan) {
            outstandingAmount = 0;
            return;
        }

        java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance();

        double totalToPay = loan.getAmount() + loan.getInterest();
        double outstanding = totalToPay - loan.getRepaidAmount();
        double progress = totalToPay > 0 ? (loan.getRepaidAmount() / totalToPay) * 100 : 0;
        outstandingAmount = outstanding;

        tvRemainingAmount.setText("UGX " + nf.format(outstanding));
        loanProgressRail.setProgress((int) Math.round(progress));
        tvRepaidPercentBadge.setText(String.format(java.util.Locale.US, "%.0f%% repaid", progress));

        tvPrincipal.setText(nf.format(loan.getAmount()));
        tvInterest.setText(nf.format(loan.getInterest()));
        tvDueDate.setText(loan.getDueDate() != null
                ? new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault()).format(loan.getDueDate())
                : "—");

        etAmount.setText(nf.format(outstanding));
    }

    private void setupListeners() {
        btnQuarter.setOnClickListener(v -> setAmountFraction(0.25));
        btnHalf.setOnClickListener(v -> setAmountFraction(0.50));
        btnMax.setOnClickListener(v -> setAmountFraction(1.0));

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

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                } catch (NumberFormatException e) {
                    etAmount.setError("Invalid amount");
                    return;
                }
                if (amount <= 0) {
                    etAmount.setError("Invalid amount");
                    return;
                }

                // Never let a member overpay the loan
                if (outstandingAmount > 0 && amount > outstandingAmount) {
                    amount = outstandingAmount;
                    etAmount.setText(java.text.NumberFormat.getIntegerInstance().format(outstandingAmount));
                    Toast.makeText(getContext(), "Amount capped to the outstanding balance", Toast.LENGTH_SHORT).show();
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

    /** Sets the amount field to a fraction of the outstanding balance (25% / 50% / full). */
    private void setAmountFraction(double fraction) {
        if (outstandingAmount <= 0) return;
        double value = Math.round(outstandingAmount * fraction);
        etAmount.setText(java.text.NumberFormat.getIntegerInstance().format(value));
        etAmount.setSelection(etAmount.getText().length());
    }

    private void selectPaymentMethod(boolean isAirtel) {
        radioAirtel.setChecked(isAirtel);
        radioMTN.setChecked(!isAirtel);

        if (isAirtel) {
            // Use safe utility to avoid invalid resource ID errors
            ViewUtils.safeSetBackground(R.drawable.bg_item_premium_white, cardAirtel);
            cardAirtel.setElevation(com.example.save.utils.UIUtils.dpToPx(getContext(), 4));
            radioAirtel.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#215DA1")));
            etPhoneNumber.setHint("e.g. 0755123456");

            ViewUtils.safeSetBackground(R.drawable.bg_payment_card_unselected, cardMTN);
            cardMTN.setElevation(0f);
            radioMTN.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#CBD5E1")));
        } else {
            ViewUtils.safeSetBackground(R.drawable.bg_item_premium_white, cardMTN);
            cardMTN.setElevation(com.example.save.utils.UIUtils.dpToPx(getContext(), 4));
            radioMTN.setButtonTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#215DA1")));
            etPhoneNumber.setHint("e.g. 0772123890");

            // First set premium then unselected for Airtel card
            ViewUtils.safeSetBackground(R.drawable.bg_item_premium_white, cardAirtel);
            ViewUtils.safeSetBackground(R.drawable.bg_payment_card_unselected, cardAirtel);
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
