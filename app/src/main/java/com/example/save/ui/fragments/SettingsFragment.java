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

        loadUserData();
        setupHeader();
        setupStats();
        setupGridListeners();
        setupPreferences();
        startEntranceAnimations();

        return binding.getRoot();
    }

    private void loadUserData() {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
        String name = prefs.getString("admin_name", "John Doe");
        String group = prefs.getString("group_name", "Alpha Savers Group");

        binding.tvUserName.setText(name);
        binding.tvGroupName.setText(group);

        // Simple initials logic
        if (name != null && !name.isEmpty()) {
            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                binding.tvAvatar.setText((parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase());
            } else {
                binding.tvAvatar.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
            }
        }
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.btnNotification.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Notifications", Toast.LENGTH_SHORT).show();
        });

        binding.btnEditProfile.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupStats() {
        // Mock data as per design or load from repo if available
        binding.tvPoolBalance.setText("KES 45K");
        binding.tvNextPayout.setText("Apr 28");
        binding.tvPosition.setText("3 / 12");
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
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, new GroupSettingsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void setupPreferences() {
        binding.rowHelpCenter.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, new SupportFragment())
                        .addToBackStack("SettingsRoot")
                        .commit();
            }
        });

        binding.rowLiveChat.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fragment_container, ConnectingAgentFragment.newInstance())
                        .addToBackStack("SupportRoot")
                        .commit();
            }
        });

        binding.rowCallSupport.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Calling Support: +254 700 000 000", Toast.LENGTH_SHORT).show();
        });

        binding.btnReferFriend.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Referral link copied! Share to earn KES 200", Toast.LENGTH_SHORT).show();
        });

        binding.btnSignOut.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Signing out...", Toast.LENGTH_SHORT).show();
            // Implement actual logout logic here
        });
    }

    private void startEntranceAnimations() {
        binding.userHeroCard.setAlpha(0f);
        binding.userHeroCard.setTranslationY(40f);
        binding.statsStrip.setAlpha(0f);
        binding.statsStrip.setTranslationY(40f);
        binding.bodyContent.setAlpha(0f);
        binding.bodyContent.setTranslationY(40f);

        binding.userHeroCard.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        binding.statsStrip.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(200).start();
        binding.bodyContent.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Directly restore the nav bar — guaranteed to work regardless of syncNavUI timing
        if (getActivity() != null) {
            View navContainer = getActivity().findViewById(R.id.navContainer);
            if (navContainer != null) navContainer.setVisibility(View.VISIBLE);
            View navAction = getActivity().findViewById(R.id.navAction);
            if (navAction != null) navAction.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
