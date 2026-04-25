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
import com.example.save.databinding.FragmentPrivacySecurityBinding;

public class PrivacySecurityFragment extends Fragment {

    private FragmentPrivacySecurityBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPrivacySecurityBinding.inflate(inflater, container, false);

        setupListeners();
        setupSwitches();

        return binding.getRoot();
    }

    private void setupListeners() {


        binding.btnChangePassword.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Change Password clicked", Toast.LENGTH_SHORT).show();
        });

        binding.btnManageDevices.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Manage Connected Devices clicked", Toast.LENGTH_SHORT).show();
        });

        binding.btnHideBalances.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Hide Balances setting", Toast.LENGTH_SHORT).show();
        });

        binding.btnSignOutAll.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getContext() != null) {
                com.example.save.utils.SessionManager.getInstance(getContext()).logoutUser();
            }
        });
    }

    private void setupSwitches() {
        binding.switch2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Two-Factor Authentication " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchBiometrics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Biometrics " + state, Toast.LENGTH_SHORT).show();
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
