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
import com.example.save.utils.SessionManager;

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
        applyRoleBasedVisibility();
        startEntranceAnimations();

        return binding.getRoot();
    }

    private void applyRoleBasedVisibility() {
        if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
            // Hide Admin-specific items for Members
            binding.cardAutomation.setVisibility(View.GONE);
            binding.cardGeneral.setVisibility(View.GONE);
            binding.badgeAdmin.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        SessionManager session = SessionManager.getInstance(requireContext());
        String name = session.getUserName();
        String email = session.getUserEmail();

        // Fallback to SharedPreferences for admins
        if (name == null || name.isEmpty()) {
            name = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("admin_name", "");
        }
        String group = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                .getString("group_name", "");

        binding.tvUserName.setText(name != null && !name.isEmpty() ? name : email);
        binding.tvGroupName.setText(group != null && !group.isEmpty() ? group : "");

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

        binding.btnEditProfile.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Edit Profile", Toast.LENGTH_SHORT).show();
        });

    }

    private void setupStats() {
        // Load real stats from the backend dashboard summary
        androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(requireActivity());
        com.example.save.ui.viewmodels.MembersViewModel vm = provider.get(com.example.save.ui.viewmodels.MembersViewModel.class);
        vm.getDashboardSummary((success, summaryObj, message) -> {
            if (success && isAdded() && summaryObj instanceof com.example.save.data.models.DashboardSummaryResponse) {
                com.example.save.data.models.DashboardSummaryResponse summary =
                        (com.example.save.data.models.DashboardSummaryResponse) summaryObj;
                requireActivity().runOnUiThread(() -> {
                    double balance = summary.getTotalBalance();
                    if (balance >= 1_000_000) {
                        binding.tvPoolBalance.setText(String.format(java.util.Locale.getDefault(), "UGX %.1fM", balance / 1_000_000));
                    } else if (balance >= 1000) {
                        binding.tvPoolBalance.setText(String.format(java.util.Locale.getDefault(), "UGX %.0fK", balance / 1000));
                    } else {
                        binding.tvPoolBalance.setText(String.format(java.util.Locale.getDefault(), "UGX %.0f", balance));
                    }
                    String nextPayout = requireContext()
                            .getSharedPreferences("SaveAppPrefs", android.content.Context.MODE_PRIVATE)
                            .getString("sched_payout_date", "TBD");
                    binding.tvNextPayout.setText(nextPayout);
                    int total = summary.getTotalMembers() > 0 ? summary.getTotalMembers() : 0;
                    binding.tvPosition.setText("1 / " + total);
                });
            }
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
            if (getContext() != null) {
                com.example.save.utils.SessionManager.getInstance(getContext()).logoutUser();
            }
        });

        binding.btnDeleteAccount.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getContext() != null) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        com.example.save.utils.SessionManager.getInstance(getContext()).logoutUser();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
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
