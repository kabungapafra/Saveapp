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
        // No Back or Save buttons as per immersive redesign
        
        binding.btnInviteOthers.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Invite link copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        binding.btnLeaveGroup.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Confirm leave group in the next step", Toast.LENGTH_LONG).show();
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

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean("group_dark_mode", isChecked).apply();
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Dark Mode " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean("group_push_notifications", isChecked).apply();
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Push Notifications " + state, Toast.LENGTH_SHORT).show();
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
