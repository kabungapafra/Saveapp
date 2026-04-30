package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;

public class LoanSubmittedSuccessFragment extends Fragment {

    private static final String ARG_BORROWER = "borrower_name";
    private static final String ARG_AMOUNT = "loan_amount";

    public static LoanSubmittedSuccessFragment newInstance(String borrower, String amount) {
        LoanSubmittedSuccessFragment fragment = new LoanSubmittedSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BORROWER, borrower);
        args.putString(ARG_AMOUNT, amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_submitted_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String borrower = getArguments() != null ? getArguments().getString(ARG_BORROWER, "Active Member") : "Active Member";
        String amount = getArguments() != null ? getArguments().getString(ARG_AMOUNT, "UGX 0") : "UGX 0";

        ((TextView) view.findViewById(R.id.tvBorrowerName)).setText(borrower);
        ((TextView) view.findViewById(R.id.tvAmount)).setText(amount);
        ((TextView) view.findViewById(R.id.tvDescription)).setText("Your loan request for " + amount + " has been sent to the group admins for review. You'll be notified once it's processed.");

        // Animate Checkmark Circle
        View checkmarkCircle = view.findViewById(R.id.checkmarkCircle);
        checkmarkCircle.setScaleX(0f);
        checkmarkCircle.setScaleY(0f);
        checkmarkCircle.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Start Vector Drawable Animation
        ImageView ivCheckmark = view.findViewById(R.id.ivCheckmark);
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
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }

        view.findViewById(R.id.btnDone).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Return to dashboard
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }
}
