package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;

public class LoanApprovedSuccessFragment extends Fragment {

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
