package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;

public class LoanApprovedSuccessFragment extends Fragment {

    private String borrower, amount;

    public static LoanApprovedSuccessFragment newInstance(String borrower, String amount) {
        LoanApprovedSuccessFragment fragment = new LoanApprovedSuccessFragment();
        Bundle args = new Bundle();
        args.putString("borrower", borrower);
        args.putString("amount", amount);
        fragment.setArguments(args);
        return fragment;
    }

    public static LoanApprovedSuccessFragment newPayoutInstance(String recipient, String amount) {
        LoanApprovedSuccessFragment fragment = new LoanApprovedSuccessFragment();
        Bundle args = new Bundle();
        args.putString("borrower", recipient);
        args.putString("amount", amount);
        args.putString("title", "Payout Approved\nSuccessfully!");
        args.putString("message", "has been successfully disbursed");
        fragment.setArguments(args);
        return fragment;
    }

    public static LoanApprovedSuccessFragment newInstance() {
        return new LoanApprovedSuccessFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_approved_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            borrower = getArguments().getString("borrower");
            amount = getArguments().getString("amount");
            String title = getArguments().getString("title", "Loan Approved\nSuccessfully!");
            String message = getArguments().getString("message", "has been successfully allocated");
            
            TextView tvTitle = view.findViewById(R.id.tvSuccessTitle);
            TextView tvAmount = view.findViewById(R.id.tvSuccessAmount);
            TextView tvBorrower = view.findViewById(R.id.tvSuccessBorrower);
            TextView tvAllocatedLabel = view.findViewById(R.id.tvAllocatedLabel);
            
            if (tvTitle != null) tvTitle.setText(title);
            if (tvAmount != null && amount != null) tvAmount.setText(amount);
            if (tvBorrower != null && borrower != null) tvBorrower.setText(borrower);
            if (tvAllocatedLabel != null) tvAllocatedLabel.setText(message);
        }

        // Animate Checkmark Circle
        View checkmarkCircle = view.findViewById(R.id.checkmarkCircle);
        checkmarkCircle.setScaleX(0f);
        checkmarkCircle.setScaleY(0f);
        checkmarkCircle.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();

        // Start Vector Drawable Animation
        android.widget.ImageView ivCheckmark = view.findViewById(R.id.ivCheckmark);
        if (ivCheckmark.getDrawable() instanceof android.graphics.drawable.Animatable) {
            ((android.graphics.drawable.Animatable) ivCheckmark.getDrawable()).start();
        }

        // Animate Confetti
        ViewGroup confettiContainer = view.findViewById(R.id.confettiContainer);
        for (int i = 0; i < confettiContainer.getChildCount(); i++) {
            View confetti = confettiContainer.getChildAt(i);
            confetti.setScaleX(0f);
            confetti.setScaleY(0f);
            confetti.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setStartDelay((long) i * 50 + 200)
                    .setInterpolator(new android.view.animation.OvershootInterpolator())
                    .start();
        }

        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Pop all the way back to ApprovalsFragment or AdminDashboard
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });
    }
}
