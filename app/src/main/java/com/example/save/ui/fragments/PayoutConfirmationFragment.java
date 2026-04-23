package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentPayoutConfirmationBinding;

public class PayoutConfirmationFragment extends Fragment {

    private FragmentPayoutConfirmationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPayoutConfirmationBinding.inflate(inflater, container, false);

        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        binding.btnConfirmPayout.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Payout Confirmed successfully!", Toast.LENGTH_LONG).show();
            
            // Navigate back after slight delay for visual feedback
            v.postDelayed(() -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }, 1000);
        });
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
