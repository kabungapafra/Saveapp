package com.example.save.ui.fragments;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu; // Added import
import android.widget.ImageView; // Added import

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.DialogAddMemberBinding;
import com.example.save.databinding.DialogMemberProfileBinding;
import com.example.save.databinding.FragmentMembersBinding;
import com.example.save.databinding.ItemMemberAdminBinding;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private FragmentMembersBinding binding;
    private com.example.save.ui.adapters.MemberAdapter adapter;
    private com.example.save.ui.adapters.MemberAdapter adminsAdapter; // Adapter for admin list
    private MembersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);

        // Initialize ViewModel
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeViewModel();

        binding.fabAddMember.setOnClickListener(v -> showAddMemberDialog());
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Check for auto-open argument
        if (getArguments() != null && getArguments().getBoolean("SHOW_ADD_DIALOG", false)) {
            // Post to ensure view is ready
            binding.getRoot().post(this::showAddMemberDialog);
        }

        return binding.getRoot();
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

            if (members != null && adapter != null) {
                adapter.updateList(members);
                updateMemberCount(members.size());

                // Update Admins List
                List<Member> admins = viewModel.getAdmins();
                if (adminsAdapter != null) {
                    adminsAdapter.updateList(admins);
                }

                // Toggle Admins Card Visibility
                if (binding.cvAdmins != null) {
                    binding.cvAdmins.setVisibility(admins.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
        });
    }

    private void setupRecyclerView() {
        // Set layout manager and adapter
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create adapter with click listeners
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

        // Setup Admins RecyclerView (vertical list)
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

        // Use Builder Buttons because XML layout (dialog_add_member.xml) does not have
        // buttons
        builder.setPositiveButton("Create Member", (dialog, which) -> {
            String name = dialogBinding.etMemberName.getText().toString().trim();
            String phone = dialogBinding.etMemberPhone.getText().toString().trim();
            String email = dialogBinding.etMemberEmail.getText().toString().trim();
            // Default role to "Member" since spinner is missing in this binding/xml
            // currently, or assume intended design
            String role = "Member";

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(getContext(), "Name required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Using ViewModel to add member
            viewModel.addMember(new Member(name, role, true));
            Toast.makeText(getContext(), "Member Added", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void showProfileDialog(Member member) {
        if (getContext() == null)
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        DialogMemberProfileBinding dialogBinding = DialogMemberProfileBinding
                .inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogBinding.getRoot());

        // Populate data using correct field IDs
        dialogBinding.dialogProfileName.setText(member.getName());
        dialogBinding.dialogProfileRole.setText(member.getRole());

        // Status indicator color
        int color = member.isActive()
                ? getContext().getResources().getColor(android.R.color.holo_green_dark)
                : getContext().getResources().getColor(android.R.color.darker_gray);
        dialogBinding.dialogProfileStatus.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color));

        // Set contact info if available
        if (member.getEmail() != null && !member.getEmail().isEmpty()) {
            dialogBinding.dialogProfileEmail.setText(member.getEmail());
        }

        // Set savings/contribution info
        dialogBinding.dialogProfileSavings.setText(
                String.format("UGX %,.0f", member.getContributionPaid()));

        dialogBinding.btnCloseProfile.setOnClickListener(v -> builder.create().dismiss());

        builder.show();
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
                // Trigger repository update to refresh LiveData
                viewModel.updateMember(position, member);
                return true;
            } else if (item.getItemId() == 2) {
                Toast.makeText(getContext(), "Reset Password feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == 3) {
                Toast.makeText(getContext(), "Remove Member feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
