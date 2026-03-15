package com.example.save.ui.fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.databinding.DialogAddMemberBinding;
import com.example.save.databinding.DialogMemberProfileBinding;
import com.example.save.databinding.FragmentMembersBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.ValidationUtils;
import com.example.save.ui.adapters.MemberAdapter;
import com.example.save.ui.adapters.TechnicalInsightsAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private MembersViewModel viewModel;
    private MemberAdapter adapter;
    private TechnicalInsightsAdapter insightsAdapter;
    private List<Member> currentMembersList = new ArrayList<>();
    private String currentTab = "All";
    private String searchQuery = "";
    private boolean isSearching = false;

    // Animation views
    private LottieAnimationView successAnimation;
    private View animationOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeViewModel();
        setupSearchView();
        setupTabs();

        // Sync data with backend
        viewModel.syncMembers();

        binding.btnInvite.setOnClickListener(v -> showAddMemberDialog());
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Check for auto-open argument
        if (getArguments() != null && getArguments().getBoolean("SHOW_ADD_DIALOG", false)) {
            binding.getRoot().post(this::showAddMemberDialog);
        }

        // Setup Swipe Refresh
        binding.swipeRefreshUtils.setOnRefreshListener(() -> {
            loadMembers();
        });

        // Initialize animation views
        successAnimation = binding.successAnimation;
        animationOverlay = binding.animationOverlay;

        return binding.getRoot();
    }

    private void setupTabs() {
        binding.tabAll.setOnClickListener(v -> selectTab("All"));
        binding.tabActive.setOnClickListener(v -> selectTab("Active"));
        binding.tabInactive.setOnClickListener(v -> selectTab("Inactive"));
    }

    private void selectTab(String tab) {
        currentTab = tab;

        // Reset styles
        binding.tabAll.setBackgroundResource(R.drawable.bg_pill_tab_unselected);
        binding.tabActive.setBackgroundResource(R.drawable.bg_pill_tab_unselected);
        binding.tabInactive.setBackgroundResource(R.drawable.bg_pill_tab_unselected);

        binding.tabAll.setTextColor(android.graphics.Color.parseColor("#64748B"));
        binding.tabActive.setTextColor(android.graphics.Color.parseColor("#64748B"));
        binding.tabInactive.setTextColor(android.graphics.Color.parseColor("#64748B"));

        // Set selected style
        TextView selectedView = null;
        if (tab.equals("All"))
            selectedView = binding.tabAll;
        else if (tab.equals("Active"))
            selectedView = binding.tabActive;
        else if (tab.equals("Inactive"))
            selectedView = binding.tabInactive;

        if (selectedView != null) {
            selectedView.setBackgroundResource(R.drawable.bg_pill_tab_selected);
            selectedView.setTextColor(android.graphics.Color.parseColor("#2563EB"));
            selectedView.setElevation(2f);
        }

        filterAndApply();
    }

    private void filterAndApply() {
        if (currentMembersList == null || adapter == null)
            return;

        List<Member> filtered = new ArrayList<>();
        for (Member m : currentMembersList) {
            if (currentTab.equals("All")) {
                filtered.add(m);
            } else if (currentTab.equals("Active")) {
                if (m.isActive())
                    filtered.add(m);
            } else if (currentTab.equals("Inactive")) {
                if (!m.isActive())
                    filtered.add(m);
            }
        }
        adapter.updateList(filtered);
    }

    private void setupSearchView() {
        binding.etSearchMember.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    isSearching = false;
                    filterAndApply();
                } else {
                    isSearching = true;
                    // Search members
                    viewModel.searchMembers(query).observe(getViewLifecycleOwner(), members -> {
                        if (members != null && adapter != null) {
                            adapter.updateList(members);
                            binding.membersRecyclerView.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadMembers() {
        // Show loading initially if not refreshing
        if (!binding.swipeRefreshUtils.isRefreshing() && binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // Trigger backend sync
        viewModel.refreshMembers((success, message) -> {
        });

        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            // Hide loading when data arrives
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            if (binding.swipeRefreshUtils != null) {
                binding.swipeRefreshUtils.setRefreshing(false);
            }

            if (members != null && adapter != null && !isSearching) {
                currentMembersList = members;
                filterAndApply();

                // Update Technical Insights
                if (insightsAdapter != null) {
                    insightsAdapter.updateList(members);
                }

                binding.tabAll.setText("All Members (" + members.size() + ")");

                binding.membersRecyclerView.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void observeViewModel() {
        loadMembers();
    }

    private void setupRecyclerView() {
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new com.example.save.ui.adapters.MemberAdapter(
                new com.example.save.ui.adapters.MemberAdapter.OnMemberClickListener() {
                    @Override
                    public void onMemberClick(Member member) {
                        showProfileDialog(member);
                    }

                    @Override
                    public void onMoreActionsClick(View view, Member member, int position) {
                        showPopupMenu(view, member, position);
                    }
                });
        binding.membersRecyclerView.setAdapter(adapter);

        // Setup Insights RecyclerView
        binding.rvTechnicalInsights.setLayoutManager(new LinearLayoutManager(getContext()));
        insightsAdapter = new TechnicalInsightsAdapter();
        binding.rvTechnicalInsights.setAdapter(insightsAdapter);
    }

    public void showAddMemberDialog() {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogAddMemberBinding dialogBinding = DialogAddMemberBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());

        // Create role spinner
        String[] roles = { "Member", "Secretary", "Treasurer", "Administrator" };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (dialogBinding.spinnerMemberRole != null) {
            dialogBinding.spinnerMemberRole.setAdapter(roleAdapter);
        }

        // Auto-generate OTP immediately
        String initialOtp = com.example.save.utils.ValidationUtils.generateOTP();
        dialogBinding.etMemberOTP.setText(initialOtp);

        dialogBinding.btnRegenerateOTP.setOnClickListener(v -> {
            String newOtp = com.example.save.utils.ValidationUtils.generateOTP();
            dialogBinding.etMemberOTP.setText(newOtp);
            v.animate().rotationBy(360).setDuration(400).start();
        });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        dialogBinding.btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnCreateMember.setOnClickListener(v -> {
            String name = dialogBinding.etMemberName.getText().toString().trim();
            String phone = dialogBinding.etMemberPhone.getText().toString().trim();
            String email = dialogBinding.etMemberEmail.getText().toString().trim();

            String role = "Member";
            if (dialogBinding.spinnerMemberRole != null && dialogBinding.spinnerMemberRole.getSelectedItem() != null) {
                role = dialogBinding.spinnerMemberRole.getSelectedItem().toString();
            }

            String otp = dialogBinding.etMemberOTP.getText().toString().trim();

            if (!com.example.save.utils.ValidationUtils.isNotEmpty(name)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberName, "Name is required");
                return;
            }

            if (!com.example.save.utils.ValidationUtils.isValidEmail(email)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberEmail, "Invalid email format");
                return;
            }

            if (!com.example.save.utils.ValidationUtils.isValidPhone(phone)) {
                com.example.save.utils.ValidationUtils.showError(dialogBinding.etMemberPhone,
                        "Invalid phone number format");
                return;
            }

            Member newMember = new Member(name, role, true, phone, email);
            newMember.setId(java.util.UUID.randomUUID().toString());
            newMember.setPassword(otp);
            newMember.setFirstLogin(true);

            dialogBinding.btnCreateMember.setEnabled(false);
            dialogBinding.btnCreateMember.setText("Creating...");

            viewModel.addMember(newMember, (success, message) -> {
                if (dialog.isShowing()) {
                    dialogBinding.btnCreateMember.setEnabled(true);
                    dialogBinding.btnCreateMember.setText("CREATE ACCOUNT");
                }

                if (success) {
                    dialog.dismiss();
                    showSuccessAnimation(name, otp);
                } else {
                    Toast.makeText(getContext(), "Failed: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showSuccessAnimation(String name, String otp) {
        if (successAnimation != null && animationOverlay != null) {
            animationOverlay.setVisibility(View.VISIBLE);
            successAnimation.setProgress(0f);
            successAnimation.playAnimation();

            successAnimation.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (animationOverlay != null) {
                        animationOverlay.postDelayed(() -> {
                            if (animationOverlay != null) {
                                animationOverlay.setVisibility(View.GONE);
                                showOTPDialog(name, otp);
                            }
                        }, 500);
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    if (animationOverlay != null)
                        animationOverlay.setVisibility(View.GONE);
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }
            });
        } else {
            showOTPDialog(name, otp);
        }
    }

    private void showOTPDialog(String memberName, String otp) {
        if (getContext() == null)
            return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs",
                Context.MODE_PRIVATE);
        String groupName = prefs.getString("group_name", "Your Group");

        viewModel.getMemberByNameLive(memberName).observe(getViewLifecycleOwner(), member -> {
            if (member == null)
                return;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            com.example.save.databinding.DialogMemberCredentialsBinding credBinding = com.example.save.databinding.DialogMemberCredentialsBinding
                    .inflate(LayoutInflater.from(getContext()));
            builder.setView(credBinding.getRoot());

            credBinding.tvCredGroupName.setText(groupName);
            credBinding.tvCredMemberName.setText(member.getName());
            credBinding.tvCredPhone.setText(member.getPhone());
            credBinding.tvCredEmail.setText(member.getEmail());
            credBinding.tvCredOTP.setText(otp);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            String credentialsMessage = String.format(
                    "🎉 *Welcome to %s!*\n\nYour login credentials:\n\n👤 *Name:* %s\n📱 *Phone:* %s\n📧 *Email:* %s\n🔐 *Code:* %s\n\nDownload the app to get started! 🚀",
                    groupName, member.getName(), member.getPhone(), member.getEmail(), otp);

            credBinding.btnCopyCredentials.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) requireContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Member Credentials", credentialsMessage));
                Toast.makeText(getContext(), "Credentials copied!", Toast.LENGTH_SHORT).show();
            });

            credBinding.btnShareWhatsApp.setOnClickListener(v -> {
                try {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, credentialsMessage);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
            });

            credBinding.btnCloseCredentials.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        });
    }

    private void showProfileDialog(Member member) {
        if (member.getEmail() == null)
            return;
        getParentFragmentManager().beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(),
                        com.example.save.ui.fragments.MemberProfileFragment.newInstance(member.getEmail()))
                .addToBackStack(null).commit();
    }

    private void showPopupMenu(View view, Member member, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        boolean isAdmin = member.getRole().equalsIgnoreCase("Administrator")
                || member.getRole().equalsIgnoreCase("Admin");
        if (isAdmin)
            popup.getMenu().add(0, 1, 0, "Demote to Member");
        else
            popup.getMenu().add(0, 1, 0, "Promote to Admin");
        popup.getMenu().add(0, 2, 0, "Reset Password");
        if (member.isActive())
            popup.getMenu().add(0, 4, 0, "Suspend Member");
        else
            popup.getMenu().add(0, 4, 0, "Activate Member");
        popup.getMenu().add(0, 3, 0, "Remove Member");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                member.setRole(isAdmin ? "Member" : "Administrator");
                viewModel.updateMember(position, member);
                return true;
            } else if (item.getItemId() == 2) {
                showResetPasswordConfirmation(member);
                return true;
            } else if (item.getItemId() == 3) {
                showRemoveMemberConfirmation(member);
                return true;
            } else if (item.getItemId() == 4) {
                member.setActive(!member.isActive());
                viewModel.updateMember(position, member);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showResetPasswordConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext()).setTitle("Reset Password")
                .setMessage("Reset for " + member.getName() + "?")
                .setPositiveButton("Reset", (d, w) -> showOTPDialog(member.getName(), viewModel.resetPassword(member)))
                .setNegativeButton("Cancel", null).show();
    }

    private void showRemoveMemberConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext()).setTitle("Remove Member")
                .setMessage("Remove " + member.getName() + "?")
                .setPositiveButton("Remove", (d, w) -> {
                    viewModel.removeMember(member);
                    Toast.makeText(getContext(), "Removed", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
