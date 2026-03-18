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
import com.example.save.databinding.FragmentGroupSettingsBinding;

public class GroupSettingsFragment extends Fragment {

    private FragmentGroupSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupSettingsBinding.inflate(inflater, container, false);

        setupButtons();
        setupToggles();

        return binding.getRoot();
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Settings saved successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Change Password screen coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnDeleteGroup.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Delete Group functionality restricted", Toast.LENGTH_LONG).show();
        });
    }

    private void setupToggles() {
        binding.switchAutomaticPayouts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Automatic Payouts " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchScheduledContributions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Scheduled Contributions " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchSmartRoundups.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Smart Round-ups " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchAutomatedCycle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Automated Cycle " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchLoanRequests.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Loan Requests " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchInviteOthers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Invite Others " + state, Toast.LENGTH_SHORT).show();
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
