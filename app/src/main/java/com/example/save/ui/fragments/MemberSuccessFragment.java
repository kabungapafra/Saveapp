package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentMemberSuccessBinding;
import com.example.save.ui.activities.AdminMainActivity;

public class MemberSuccessFragment extends Fragment {

    private static final String ARG_MEMBER_NAME = "member_name";
    private FragmentMemberSuccessBinding binding;

    public static MemberSuccessFragment newInstance(String memberName) {
        MemberSuccessFragment fragment = new MemberSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEMBER_NAME, memberName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberSuccessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String memberName = getArguments() != null ? getArguments().getString(ARG_MEMBER_NAME, "Member") : "Member";
        binding.tvMemberNameDisplay.setText(memberName);
        
        String description = getString(R.string.member_success_description_format, memberName);
        binding.tvSuccessDesc.setText(description);

        setupInitialState();
        startAnimations();

        binding.btnViewMembers.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                // Navigate back to members tab
                // In AdminMainActivity, we can use updateNav or similar
                // However, the Fragment is usually loaded into fragment_container.
                // We want to clear the backstack and show MembersFragment.
                getParentFragmentManager().popBackStack();
                // AdminMainActivity handles bottom nav sync if we navigate correctly
            }
        });

        binding.btnReturnDashboard.setOnClickListener(v -> {
            if (getActivity() instanceof AdminMainActivity) {
                // This will close the fragment container and show main content
                requireActivity().onBackPressed();
            }
        });
    }

    private void setupInitialState() {
        // Hide all elements initially
        binding.successCircle.setScaleX(0f);
        binding.successCircle.setScaleY(0f);
        binding.successCircle.setAlpha(0f);

        binding.checkmarkCircle.setScaleX(0f);
        binding.checkmarkCircle.setScaleY(0f);
        binding.checkmarkCircle.setAlpha(0f);

        binding.dotBlue.setScaleX(0f);
        binding.dotBlue.setScaleY(0f);
        binding.dotBlue.setAlpha(0f);

        binding.dotYellow.setScaleX(0f);
        binding.dotYellow.setScaleY(0f);
        binding.dotYellow.setAlpha(0f);

        binding.tvSuccessTitle.setAlpha(0f);
        binding.tvSuccessTitle.setTranslationY(40f);

        binding.tvSuccessDesc.setAlpha(0f);
        binding.tvSuccessDesc.setTranslationY(40f);

        binding.memberProfileCard.setAlpha(0f);
        binding.memberProfileCard.setTranslationY(60f);

        binding.btnViewMembers.setAlpha(0f);
        binding.btnViewMembers.setTranslationY(60f);

        binding.btnReturnDashboard.setAlpha(0f);
        binding.btnReturnDashboard.setTranslationY(60f);
    }

    private void startAnimations() {
        long delay = 300;

        // 1. Success Circle Bloom
        binding.successCircle.animate()
                .scaleX(1.2f).scaleY(1.2f).alpha(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    binding.successCircle.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(300)
                            .start();
                }).start();

        // 2. Checkmark Pop & Draw
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.checkmarkCircle.animate()
                    .scaleX(1f).scaleY(1f).alpha(1f)
                    .setDuration(600)
                    .setInterpolator(new OvershootInterpolator(1.4f))
                    .withEndAction(() -> {
                        android.graphics.drawable.Drawable drawable = binding.ivCheckmark.getDrawable();
                        if (drawable instanceof android.graphics.drawable.AnimatedVectorDrawable) {
                            ((android.graphics.drawable.AnimatedVectorDrawable) drawable).start();
                        }
                    })
                    .start();
        }, delay + 200);

        // 3. Deco Dots Pop
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.dotBlue.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(400).start();
            binding.dotYellow.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(400).start();
        }, delay + 400);

        // 4. Text and Card Slide
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.tvSuccessTitle.animate().alpha(1f).translationY(0f).setDuration(500).start();
        }, delay + 500);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.tvSuccessDesc.animate().alpha(1f).translationY(0f).setDuration(500).start();
        }, delay + 650);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.memberProfileCard.animate().alpha(1f).translationY(0f).setDuration(600).start();
        }, delay + 800);

        // 5. Buttons Slide
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.btnViewMembers.animate().alpha(1f).translationY(0f).setDuration(600).start();
        }, delay + 950);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.btnReturnDashboard.animate().alpha(1f).translationY(0f).setDuration(600).start();
        }, delay + 1050);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
