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
    private Button btnPayLoan;
    private LottieAnimationView successAnimation;
    private View animationOverlay;

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

        // Fetch current user (mocked for now as "Member")
        loadMemberData();

        initializeViews(view);
        setupListeners();
    }

    private void initializeViews(View view) {
        etAmount = view.findViewById(R.id.etAmount);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        btnPayLoan = view.findViewById(R.id.btnPayLoan);

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
        // Mock loading current user
        new Thread(() -> {
            Member member = viewModel.getMemberByNameSync(); // Or get "Member"
            if (member == null)
                member = viewModel.getMemberByName("Member");

            final Member finalMember = member; // Effectively final for lambda

            requireActivity().runOnUiThread(() -> {
                currentMember = finalMember;
            });
        }).start();
    }

    private void setupListeners() {
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

            if (currentMember != null) {
                // Perform payment
                // We use repayLoan to log this specific transaction type
                viewModel.repayLoan(currentMember, amount);

                // We'll simulate success
                showSuccessAnimation();

                etAmount.setText("");
                etPhoneNumber.setText("");
            } else {
                Toast.makeText(getContext(), "User authentication failed", Toast.LENGTH_SHORT).show();
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
