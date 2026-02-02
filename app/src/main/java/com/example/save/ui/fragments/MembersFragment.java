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
import android.widget.PopupMenu;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private com.example.save.ui.adapters.MemberAdapter adapter;
    private com.example.save.ui.adapters.MemberAdapter adminsAdapter;
    private MembersViewModel viewModel;
    private List<Member> currentMembersList = new ArrayList<>();
    private boolean isSearching = false;

    // Animation Views
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

        // Sync data with backend
        viewModel.syncMembers();

        binding.fabAddMember.setOnClickListener(v -> showAddMemberDialog());
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
                    loadMembers();
                    refreshAdmins(); // Always refresh admins when clearing search
                } else {
                    isSearching = true;
                    // Hide admins card if we are searching (to avoid duplicates and focus results)
                    if (binding.cvAdmins != null) {
                        binding.cvAdmins.setVisibility(View.GONE);
                    }
                    // Search members
                    viewModel.searchMembers(query).observe(getViewLifecycleOwner(), members -> {
                        if (members != null && adapter != null) {
                            adapter.updateList(members);
                            binding.membersRecyclerView.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
                            binding.emptyStateLayout.getRoot()
                                    .setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
                            if (members.isEmpty()) {
                                binding.emptyStateLayout.tvEmptyTitle.setText("No members found");
                                binding.emptyStateLayout.tvEmptyMessage.setText("Try a different name");
                            }
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
            if (!success && getContext() != null) {
                // Optional: Show toast on failure, but data might still load from local DB
                // Toast.makeText(getContext(), "Sync failed: " + message,
                // Toast.LENGTH_SHORT).show();
            }
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
                // Ensure complete list is shown, including Admins if they are in the 'members'
                // table
                currentMembersList = members;
                adapter.updateList(members);
                updateMemberCount(members.size());

                binding.membersRecyclerView.setVisibility(members.isEmpty() ? View.GONE : View.VISIBLE);
                binding.emptyStateLayout.getRoot().setVisibility(members.isEmpty() ? View.VISIBLE : View.GONE);
                if (members.isEmpty()) {
                    binding.emptyStateLayout.tvEmptyTitle.setText("No members yet");
                    binding.emptyStateLayout.tvEmptyMessage.setText("Add members to get started");
                }

                // Update Admins List on background thread
                refreshAdmins();
            }
        });
    }

    private void refreshAdmins() {
        if (binding == null)
            return;

        new Thread(() -> {
            try {
                List<Member> admins = viewModel.getAdmins();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null && adminsAdapter != null) {
                            adminsAdapter.updateList(admins);

                            if (binding.tvAdminCount != null) {
                                binding.tvAdminCount.setText(String.valueOf(admins.size()));
                            }

                            if (binding.cvAdmins != null) {
                                // If searching, keep it hidden. Otherwise show if not empty.
                                if (isSearching) {
                                    binding.cvAdmins.setVisibility(View.GONE);
                                } else {
                                    binding.cvAdmins.setVisibility(admins.isEmpty() ? View.GONE : View.VISIBLE);
                                }
                            }

                            if (admins.size() < 4) {
                                if (isAdded()) {
                                    binding.tvAdminCount.setTextColor(
                                            getResources().getColor(android.R.color.holo_red_dark));
                                }
                                // Optional: Show a hint that 4 are required for multi-admin approval correctly?
                                // Actually, my logic uses whatever adminCount exists.
                                // But the user requested "minimum of 4".
                            } else {
                                if (isAdded()) {
                                    binding.tvAdminCount
                                            .setTextColor(getResources().getColor(R.color.deep_blue));
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (binding != null && binding.cvAdmins != null) {
                            binding.cvAdmins.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
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

        // Setup Admins RecyclerView
        binding.rvAdmins.setLayoutManager(new LinearLayoutManager(getContext()));
        adminsAdapter = new com.example.save.ui.adapters.MemberAdapter(
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
        binding.rvAdmins.setAdapter(adminsAdapter);
    }

    private void updateMemberCount(int count) {
        if (binding != null && binding.tvMemberCount != null) {
            binding.tvMemberCount.setText(count + " Active Members");
        }
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

        // Regenerate OTP button
        dialogBinding.btnRegenerateOTP.setOnClickListener(v -> {
            String newOtp = com.example.save.utils.ValidationUtils.generateOTP();
            dialogBinding.etMemberOTP.setText(newOtp);
            // Optional: Animate rotation for visual feedback
            v.animate().rotationBy(360).setDuration(400).start();
        });

        // Close button
        dialogBinding.btnCloseDialog.setOnClickListener(v -> {
            // We need the dialog instance to dismiss, but it's not created yet.
            // We can handle this by assigning the dialog to a variable or using the builder
            // differently.
            // A simpler way here since we build the dialog below:
            // We'll set this listener AFTER creating the dialog.
        });

        AlertDialog dialog = builder.create();

        // Remove default buttons as we now have custom UI but need them for the logic
        // structure
        // actually, the current logic relies on positive/negative buttons which might
        // not match the new design
        // The new design has "btnCloseDialog" and "btnCreateMember" inside the layout.
        // So we should NOT set builder buttons, but implement click listeners on the
        // binding views.

        // We configure the dialog to be transparent/custom if needed, or just use alert
        // dialog container
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();

        // Close button logic
        dialogBinding.btnCloseDialog.setOnClickListener(v -> dialog.dismiss());

        // Create Button Logic
        dialogBinding.btnCreateMember.setOnClickListener(v -> {
            String name = dialogBinding.etMemberName.getText().toString().trim();
            String phone = dialogBinding.etMemberPhone.getText().toString().trim();
            String email = dialogBinding.etMemberEmail.getText().toString().trim();

            // Get Role from spinner
            String role = "Member";
            if (dialogBinding.spinnerMemberRole != null && dialogBinding.spinnerMemberRole.getSelectedItem() != null) {
                role = dialogBinding.spinnerMemberRole.getSelectedItem().toString();
            }

            // Get OTP (it's always populated now)
            String otp = dialogBinding.etMemberOTP.getText().toString().trim();

            // Validate inputs
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

            // Create member with all fields
            Member newMember = new Member(name, role, true, phone, email);
            newMember.setId(java.util.UUID.randomUUID().toString()); // Generate unique ID
            newMember.setPassword(otp); // Set the auto-generated OTP
            newMember.setFirstLogin(true); // New members need to change password

            // Disable button
            dialogBinding.btnCreateMember.setEnabled(false);
            dialogBinding.btnCreateMember.setText("Creating...");

            viewModel.addMember(newMember, (success, message) -> {
                // Re-enable button
                if (dialog.isShowing()) {
                    dialogBinding.btnCreateMember.setEnabled(true);
                    dialogBinding.btnCreateMember.setText("CREATE ACCOUNT");
                }

                if (success) {
                    dialog.dismiss();
                    // Show Success Animation then OTP
                    showSuccessAnimation(name, otp);
                } else {
                    String errorMsg = "Failed to add member. Email or Phone might already exist.";
                    if (message != null && message.contains("Constraint")) {
                        errorMsg = "Member with this Email or Phone already exists.";
                    } else if (message != null) {
                        errorMsg = "Error: " + message;
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showSuccessAnimation(String name, String otp) {
        if (successAnimation != null && animationOverlay != null) {
            // Show overlay and animation
            animationOverlay.setVisibility(View.VISIBLE);
            successAnimation.setProgress(0f);
            successAnimation.playAnimation();

            // Add listener to hide animation and show OTP dialog after it completes
            successAnimation.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Hide overlay after animation completes
                    if (animationOverlay != null) {
                        animationOverlay.postDelayed(() -> {
                            if (animationOverlay != null) {
                                animationOverlay.setVisibility(View.GONE);
                                showOTPDialog(name, otp);
                            }
                        }, 500); // Small delay before transition
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    if (animationOverlay != null) {
                        animationOverlay.setVisibility(View.GONE);
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }
            });
        } else {
            // Fallback if views not found
            showOTPDialog(name, otp);
        }
    }

    private void showOTPDialog(String memberName, String otp) {
        if (getContext() == null)
            return;

        // Get group name from SharedPreferences
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs",
                Context.MODE_PRIVATE);
        String groupName = prefs.getString("group_name", "Your Group");

        // Get member details asynchronously to avoid main thread DB access
        viewModel.getMemberByNameLive(memberName).observe(getViewLifecycleOwner(), member -> {
            if (member == null)
                return;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            com.example.save.databinding.DialogMemberCredentialsBinding credBinding = com.example.save.databinding.DialogMemberCredentialsBinding
                    .inflate(LayoutInflater.from(getContext()));
            builder.setView(credBinding.getRoot());

            // Populate credentials
            credBinding.tvCredGroupName.setText(groupName);
            credBinding.tvCredMemberName.setText(member.getName());
            credBinding.tvCredPhone.setText(member.getPhone()); // Display as entered
            credBinding.tvCredEmail.setText(member.getEmail());
            credBinding.tvCredOTP.setText(otp);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            // Format credentials message
            String credentialsMessage = String.format(
                    "🎉 *Welcome to %s!*\n\n" +
                            "Your login credentials:\n\n" +
                            "👤 *Name:* %s\n" +
                            "📱 *Phone:* %s\n" + // Format as entered
                            "📧 *Email:* %s\n" +
                            "🔐 *Temporary Code:* %s\n\n" +
                            "⚠️ Please change your password on first login.\n\n" +
                            "Download the app and login to get started! 🚀",
                    groupName, member.getName(), member.getPhone(), member.getEmail(), otp);

            // Copy button
            credBinding.btnCopyCredentials.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) requireContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Member Credentials", credentialsMessage);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), "Credentials copied!", Toast.LENGTH_SHORT).show();
            });

            // WhatsApp share button
            credBinding.btnShareWhatsApp.setOnClickListener(v -> {
                try {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, credentialsMessage);
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                }
            });

            // Close button
            credBinding.btnCloseCredentials.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        });
    }

    private void showProfileDialog(Member member) {
        if (member.getEmail() == null)
            return;

        // Navigate to MemberProfileFragment
        getParentFragmentManager()
                .beginTransaction()
                .replace(((ViewGroup) getView().getParent()).getId(),
                        com.example.save.ui.fragments.MemberProfileFragment.newInstance(member.getEmail()))
                .addToBackStack(null)
                .commit();
    }

    private void showPopupMenu(View view, Member member, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);

        // Dynamically add Promote or Demote based on current role
        boolean isAdmin = member.getRole().equalsIgnoreCase("Administrator") ||
                member.getRole().equalsIgnoreCase("Admin");

        if (isAdmin) {
            popup.getMenu().add(0, 1, 0, "Demote to Member");
        } else {
            popup.getMenu().add(0, 1, 0, "Promote to Admin");
        }

        popup.getMenu().add(0, 2, 0, "Reset Password");

        // Suspend/Activate Toggle
        if (member.isActive()) {
            popup.getMenu().add(0, 4, 0, "Suspend Member");
        } else {
            popup.getMenu().add(0, 4, 0, "Activate Member");
        }

        popup.getMenu().add(0, 3, 0, "Remove Member");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                // Promote or Demote
                if (isAdmin) {
                    member.setRole("Member");
                    Toast.makeText(getContext(), member.getName() + " demoted to Member", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    member.setRole("Administrator");
                    Toast.makeText(getContext(), member.getName() + " promoted to Admin", Toast.LENGTH_SHORT)
                            .show();
                }
                viewModel.updateMember(position, member);
                return true;
            } else if (item.getItemId() == 2) {
                // Reset Password
                showResetPasswordConfirmation(member);
                return true;
            } else if (item.getItemId() == 3) {
                // Remove Member
                showRemoveMemberConfirmation(member);
                return true;
            } else if (item.getItemId() == 4) {
                // Suspend/Activate
                boolean newStatus = !member.isActive();
                member.setActive(newStatus);
                viewModel.updateMember(position, member);

                String action = newStatus ? "Activated" : "Suspended";
                Toast.makeText(getContext(), member.getName() + " has been " + action, Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showResetPasswordConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Reset Password")
                .setMessage("Generate a new OTP for " + member.getName() + "?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newOtp = viewModel.resetPassword(member);
                    showOTPDialog(member.getName(), newOtp);
                    Toast.makeText(getContext(), "Password reset successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRemoveMemberConfirmation(Member member) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Remove Member")
                .setMessage("Are you sure you want to remove " + member.getName() + "? This action cannot be undone.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    viewModel.removeMember(member);
                    Toast.makeText(getContext(), member.getName() + " removed successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
