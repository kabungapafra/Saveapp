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
import com.example.save.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        setupHeader();
        setupGridListeners();
        setupPreferences();

        return binding.getRoot();
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.btnUpgradePro.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Upgrade to Pro specialized features coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnReferFriend.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Referral link copied to clipboard", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupGridListeners() {
        binding.cardPersonal.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, new ProfileInfoFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.cardBank.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, new WalletPaymentsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.cardAutomation.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Scheduled Actions settings", Toast.LENGTH_SHORT).show();
        });

        binding.cardGeneral.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "General Account settings", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupPreferences() {
        binding.rowHelpCenter.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, new SupportFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.rowTOS.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Terms of Service coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.btnSignOut.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Signing out...", Toast.LENGTH_SHORT).show();
            // Implement actual logout logic here (clear session, navigate to login)
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
