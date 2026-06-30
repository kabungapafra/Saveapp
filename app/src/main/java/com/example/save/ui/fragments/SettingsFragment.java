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
import com.example.save.ui.activities.AdminMainActivity;
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
            binding.cardAutomation.setVisibility(View.GONE);
            binding.cardGeneral.setVisibility(View.GONE);
            binding.dividerBeforeAnalysis.setVisibility(View.GONE);
            binding.dividerBeforeGroupSettings.setVisibility(View.GONE);
            binding.badgeAdmin.setVisibility(View.GONE);
        }
    }

    private void loadUserData() {
        SessionManager session = SessionManager.getInstance(requireContext());
        String name = session.getUserName();
        // Fallback to SharedPreferences for admins
        if (name == null || name.isEmpty()) {
            name = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("admin_name", "");
        }
        String group = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                .getString("group_name", "");
        if (group == null || group.isEmpty()) {
            group = session.getLastGroup();
        }

        String phone = session.getUserPhone();
        binding.tvUserName.setText(name != null && !name.isEmpty() ? name : (phone != null && !phone.isEmpty() ? phone : "User"));
        binding.tvGroupName.setText(group != null && !group.isEmpty() ? group : "");

        if (name != null && !name.isEmpty()) {
            String[] parts = name.split(" ");
            if (parts.length >= 2) {
                binding.tvAvatar.setText((parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase());
            } else {
                binding.tvAvatar.setText(name.substring(0, Math.min(2, name.length())).toUpperCase());
            }
        }

        // Avatar: session.getProfileImage() keys off the session phone, which can be empty or
        // stored in a different format than the member's phone. Resolve the member and key the
        // avatar off its phone — the same value the card adapters use.
        showAvatar(session.getProfileImage());          // instant best-effort
        loadAvatarFromMember(name, phone);              // robust, member-keyed
    }

    /** Resolves the current user's member record and loads their avatar by the member's phone. */
    private void loadAvatarFromMember(String name, String sessionPhone) {
        SessionManager session = SessionManager.getInstance(requireContext());
        String sessionKey = sessionPhone != null ? sessionPhone.replaceAll("[^0-9+]", "") : "";
        com.example.save.ui.viewmodels.MembersViewModel vm =
                new androidx.lifecycle.ViewModelProvider(requireActivity())
                        .get(com.example.save.ui.viewmodels.MembersViewModel.class);
        vm.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members == null || binding == null) return;
            for (com.example.save.data.models.Member m : members) {
                String mKey = m.getPhone() != null ? m.getPhone().replaceAll("[^0-9+]", "") : "";
                boolean mine = (!sessionKey.isEmpty() && sessionKey.equals(mKey))
                        || (name != null && name.equalsIgnoreCase(m.getName()));
                if (mine) {
                    showAvatar(session.getProfileImage(m.getPhone()));
                    break;
                }
            }
        });
        vm.syncMembers();
    }

    private void showAvatar(String img) {
        if (binding == null) return;
        if (img != null && !img.isEmpty()) {
            binding.imgAvatar.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(this)
                    .load(img)
                    .circleCrop()
                    .into(binding.imgAvatar);
        } else {
            binding.imgAvatar.setVisibility(View.GONE);
        }
    }

    private void setupHeader() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });



    }

    private void setupStats() {
        // Load real stats from the backend dashboard summary
        androidx.lifecycle.ViewModelProvider provider = new androidx.lifecycle.ViewModelProvider(requireActivity());
        com.example.save.ui.viewmodels.MembersViewModel vm = provider.get(com.example.save.ui.viewmodels.MembersViewModel.class);
        
        // Observe members locally to consistently show active members count
        vm.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                int active = 0;
                for (com.example.save.data.models.Member m : members) {
                    if (m.isActive()) active++;
                }
                binding.tvPosition.setText(active + " / " + members.size());
            }
        });

        vm.getDashboardSummary((success, summaryObj, message) -> {
            if (success && isAdded() && summaryObj instanceof com.example.save.data.models.DashboardSummaryResponse) {
                com.example.save.data.models.DashboardSummaryResponse summary =
                        (com.example.save.data.models.DashboardSummaryResponse) summaryObj;
                requireActivity().runOnUiThread(() -> {
                    double balance = summary.getTotalBalance();
                    if (balance >= 1_000_000) {
                        binding.tvPoolBalance.setText(String.format(java.util.Locale.getDefault(), "UGX %.1fM", balance / 1_000_000.0));
                    } else if (balance >= 1000) {
                        binding.tvPoolBalance.setText(String.format(java.util.Locale.getDefault(), "UGX %.0fK", balance / 1000.0));
                    } else {
                        binding.tvPoolBalance.setText(String.format(java.util.Locale.getDefault(), "UGX %.0f", balance));
                    }
                    
                    // Display next payout if available, else use a reasonable default
                    String nextPayout = requireContext()
                            .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                            .getString("rule_next_payout_date", "TBD");
                    binding.tvNextPayout.setText(nextPayout);
                    
                    // Update Group name if it was missing
                    if (summary.getGroupName() != null && !summary.getGroupName().isEmpty()) {
                        binding.tvGroupName.setText(summary.getGroupName());
                        com.example.save.utils.SessionManager.getInstance(requireContext()).saveLastGroup(summary.getGroupName());
                    }
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
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).loadFragment(AnalyticsFragment.newInstance(true), true);
            }
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
    }

    private void startEntranceAnimations() {
        binding.headerContainer.setAlpha(0f);
        binding.headerContainer.setTranslationY(40f);
        binding.statsStrip.setAlpha(0f);
        binding.statsStrip.setTranslationY(40f);
        binding.bodyContent.setAlpha(0f);
        binding.bodyContent.setTranslationY(40f);

        binding.headerContainer.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
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
        if (getActivity() != null) {
            View navContainer = getActivity().findViewById(R.id.navContainer);
            if (navContainer != null) navContainer.setVisibility(View.VISIBLE);
            View navAction = getActivity().findViewById(R.id.navAction);
            if (navAction != null) navAction.setVisibility(View.VISIBLE);
            // Settings has no pull-to-refresh — disable so scroll-up doesn't trigger it
            androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl =
                    getActivity().findViewById(R.id.swipeRefreshLayout);
            if (srl != null) srl.setEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Re-enable pull-to-refresh for other fragments (e.g. Dashboard)
        if (getActivity() != null) {
            androidx.swiperefreshlayout.widget.SwipeRefreshLayout srl =
                    getActivity().findViewById(R.id.swipeRefreshLayout);
            if (srl != null) srl.setEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
