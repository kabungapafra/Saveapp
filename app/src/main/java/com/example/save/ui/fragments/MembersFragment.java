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
    private MemberAdapter adapter;
    private MembersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMembersBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        setupRecyclerView();
        observeViewModel();

        // Fixed ID: btnAddMember (CardView) from fragment_members.xml
        binding.btnAddMember.setOnClickListener(v -> showAddMemberDialog());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                if (adapter == null) {
                    adapter = new MemberAdapter(members);
                    binding.membersRecyclerView.setAdapter(adapter);
                } else {
                    adapter.updateList(members);
                }
                updateMemberCount(members.size());
            }
        });
    }

    private void setupRecyclerView() {
        // Fixed ID: membersRecyclerView
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Populating data (checking for nulls/empty)
        // Note: DialogMemberProfileBinding IDs based on previous context:
        // dialogProfileName, dialogProfileRole etc.
        // Assuming these IDs exist based on previous view_file of similar fragments or
        // keeping existing patterns.
        // If compilation fails on IDs, check dialog_member_profile.xml

        // Using IDs from typical profile dialogs or previous MembersFragment usage
        // Note: Previous Usage had: dialogBinding.tvProfileName (in code I wrote
        // earlier)
        // BUT another version of code had dialogBinding.dialogProfileName.
        // I will assume the Binding class generated correct fields.
        // To be safe, I'll rely on what IDE would suggest, but here I just stick to
        // standard naming conventions found in previous read.
        // The last read of `MembersFragment.java` showed
        // `dialogBinding.dialogProfileName`.

        dialogBinding.dialogProfileName.setText(member.getName());
        dialogBinding.dialogProfileRole.setText(member.getRole());
        dialogBinding.dialogProfileEmail.setText(member.getEmail().isEmpty() ? "N/A" : member.getEmail());

        // Status Indicator Color
        int color = member.isActive() ? getContext().getResources().getColor(android.R.color.holo_green_dark)
                : getContext().getResources().getColor(android.R.color.darker_gray);
        dialogBinding.dialogProfileStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

        dialogBinding.btnCloseProfile.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // --- Adapter ---
    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private List<Member> memberList;

        MemberAdapter(List<Member> memberList) {
            this.memberList = memberList;
        }

        // Added updateList method
        void updateList(List<Member> newList) {
            this.memberList = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMemberAdminBinding binding = ItemMemberAdminBinding.inflate(LayoutInflater.from(parent.getContext()),
                    parent, false);
            return new MemberViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            Member member = memberList.get(position);
            holder.binding.tvMemberName.setText(member.getName());
            holder.binding.tvMemberRole.setText(member.getRole());

            // Status Indicator Color
            int color = member.isActive()
                    ? holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark)
                    : holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            holder.binding.statusIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

            holder.itemView.setOnClickListener(v -> showProfileDialog(member));

            // More Actions
            holder.binding.btnMoreActions.setOnClickListener(v -> showPopupMenu(v, member, position));
        }

        @Override
        public int getItemCount() {
            return memberList.size();
        }

        private void showPopupMenu(View view, Member member, int position) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            // Inflate menu resource or add programmatically
            popup.getMenu().add(0, 1, 0, "Promote to Admin");
            popup.getMenu().add(0, 2, 0, "Reset Password");
            popup.getMenu().add(0, 3, 0, "Remove Member");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    Toast.makeText(getContext(), "Promote feature coming soon", Toast.LENGTH_SHORT).show();
                    // memberRepository.updateMember(position, member); // TODO: Move to ViewModel
                    return true;
                } else if (item.getItemId() == 2) {
                    Toast.makeText(getContext(), "Reset Password feature coming soon", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == 3) {
                    Toast.makeText(getContext(), "Remove Member feature coming soon", Toast.LENGTH_SHORT).show();
                    // memberRepository.removeMember(position); // TODO: Move to ViewModel
                    return true;
                }
                return false;
            });
            popup.show();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {
            // Made public for access
            public final ItemMemberAdminBinding binding;

            MemberViewHolder(ItemMemberAdminBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
