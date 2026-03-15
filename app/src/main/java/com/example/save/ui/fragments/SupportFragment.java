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
import com.example.save.databinding.FragmentSupportBinding;

public class SupportFragment extends Fragment {

    private FragmentSupportBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSupportBinding.inflate(inflater, container, false);

        setupListeners();
        applyEntranceAnimations();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnSearch.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Search functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        View.OnClickListener cardListener = v -> {
            applyClickAnimation(v);
            String message = "";
            if (v.getId() == R.id.cardHelpCenter) message = "Help Center";
            else if (v.getId() == R.id.cardContactUs) message = "Contact Us";
            else if (v.getId() == R.id.cardReportProblem) message = "Report a Problem";
            else if (v.getId() == R.id.cardFAQ) message = "Frequently Asked Questions";
            
            Toast.makeText(getContext(), message + " details coming soon", Toast.LENGTH_SHORT).show();
        };

        binding.cardHelpCenter.setOnClickListener(cardListener);
        binding.cardContactUs.setOnClickListener(cardListener);
        binding.cardReportProblem.setOnClickListener(cardListener);
        binding.cardFAQ.setOnClickListener(cardListener);

        binding.btnWatch.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Guided tour video coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyEntranceAnimations() {
        binding.getRoot().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
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
