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

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private com.example.save.ui.adapters.MemberAdapter adapter;
    private com.example.save.ui.adapters.MemberAdapter adminsAdapter;
    private MembersViewModel viewModel;
    private List<Member> currentMembersList = new ArrayList<>();
    private boolean isSearching = false;

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

        binding.fabAddMember.setOnClickListener(v -> showAddMemberDialog());
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Check for auto-open argument
        if (getArguments() != null && getArguments().getBoolean("SHOW_ADD_DIALOG", false)) {
            binding.getRoot().post(this::showAddMemberDialog);
        }

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
                    // Show all members
                    viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
                        if (members != null && adapter != null) {
                            adapter.updateList(members);
                            updateMemberCount(members.size());
                        }
                    });
                } else {
                    isSearching = true;
                    // Search members
                    viewModel.searchMembers(query).observe(getViewLifecycleOwner(), members -> {
                        if (members != null && adapter != null) {
                            adapter.updateList(members);
                            updateMemberCount(members.size());
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void observeViewModel() {
        // Show loading initially
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            // Hide loading when data arrives
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }

            if (members != null && adapter != null && !isSearching) {
                currentMembersList = members;
                adapter.updateList(members);
                updateMemberCount(members.size());

                // Update Admins List on background thread
                new Thread(() -> {
                    try {
                        List<Member> admins = viewModel.getAdmins();
                        requireActivity().runOnUiThread(() -> {
                            if (binding != null && adminsAdapter != null) {
                                adminsAdapter.updateList(admins);

                                if (binding.tvAdminCount != null) {
                                    binding.tvAdminCount.setText(String.valueOf(admins.size()));
                                }

                                // Toggle Admins Card Visibility
                                if (binding.cvAdmins != null) {
                                    binding.cvAdmins.setVisibility(admins.isEmpty() ? View.GONE : View.VISIBLE);
                                }
                            }
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            if (binding != null && binding.cvAdmins != null) {
                                binding.cvAdmins.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();
            }
        });
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

        AlertDialog dialog = builder.create();

        builder.setPositiveButton("Create Member", null); // Set to null initially
        builder.setNegativeButton("Cancel", (d, which) -> d.dismiss());

        dialog = builder.create();
        dialog.show();

        // Override positive button to prevent auto-dismiss on validation failure
        AlertDialog finalDialog = dialog;
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = dialogBinding.etMemberName.getText().toString().trim();
            String phone = dialogBinding.etMemberPhone.getText().toString().trim();
            String email = dialogBinding.etMemberEmail.getText().toString().trim();

            // Get Role from spinner
            String role = "Member";
            if (dialogBinding.spinnerMemberRole != null && dialogBinding.spinnerMemberRole.getSelectedItem() != null) {
                role = dialogBinding.spinnerMemberRole.getSelectedItem().toString();
            }

            // Get OTP from input or generate
            String manualOtp = dialogBinding.etMemberOTP.getText().toString().trim();

            // Validate inputs
            if (!ValidationUtils.isNotEmpty(name)) {
                ValidationUtils.showError(dialogBinding.etMemberName, "Name is required");
                return;
            }

            if (!ValidationUtils.isValidEmail(email)) {
                ValidationUtils.showError(dialogBinding.etMemberEmail, "Invalid email format");
                return;
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                ValidationUtils.showError(dialogBinding.etMemberPhone, "Invalid phone number format");
                return;
            }

            // Validate OTP if manually entered
            if (!manualOtp.isEmpty() && manualOtp.length() < 4) {
                ValidationUtils.showError(dialogBinding.etMemberOTP, "OTP must be at least 4 digits");
                return;
            }

            // Generate OTP for new member if not provided
            String otp = manualOtp.isEmpty() ? ValidationUtils.generateOTP() : manualOtp;

            // Create member with all fields
            Member newMember = new Member(name, role, true, phone, email);
            newMember.setPassword(otp); // Set the generated/manual OTP as password
            viewModel.addMember(newMember);

            finalDialog.dismiss();

            // Show OTP to admin
            showOTPDialog(name, otp);
        });
    }

    private void showOTPDialog(String memberName, String otp) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Member Created Successfully")
                .setMessage("Member: " + memberName + "\nInitial OTP: " + otp +
                        "\n\nPlease share this OTP with the new member for their first login.")
                .setPositiveButton("Copy OTP", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) requireContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("OTP", otp);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "OTP copied to clipboard", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showProfileDialog(Member member) {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogMemberProfileBinding dialogBinding = DialogMemberProfileBinding
                .inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());

        // Populate data
        dialogBinding.dialogProfileName.setText(member.getName());
        dialogBinding.dialogProfileRole.setText(member.getRole());

        // Status indicator color
        int color = member.isActive()
                ? getContext().getResources().getColor(android.R.color.holo_green_dark)
                : getContext().getResources().getColor(android.R.color.darker_gray);
        dialogBinding.dialogProfileStatus.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color));

        // Set contact info
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            dialogBinding.dialogProfileEmail.setText(member.getEmail());
        }

        // Set savings/contribution info
        dialogBinding.dialogProfileSavings.setText(
                String.format("UGX %,.0f", member.getContributionPaid()));

        AlertDialog dialog = builder.create();
        dialogBinding.btnCloseProfile.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
