package Fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import Data.MemberRepository;
import Models.Member;

import java.util.ArrayList;
import java.util.List;

public class MembersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvMemberCount;
    private View btnAddMember;
    private MemberAdapter adapter;
    private List<Member> memberList;
    private MemberRepository memberRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_members, container, false);

        memberRepository = MemberRepository.getInstance();

        initializeViews(view);
        setupRecyclerView();
        loadMemberData();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.membersRecyclerView);
        tvMemberCount = view.findViewById(R.id.tvMemberCount);
        btnAddMember = view.findViewById(R.id.btnAddMember);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        memberList = new ArrayList<>();
        adapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(adapter);
    }

    private void loadMemberData() {
        memberList.clear();
        memberList.addAll(memberRepository.getAllMembers());
        updateMemberCount();
        adapter.notifyDataSetChanged();
    }

    private void updateMemberCount() {
        if (tvMemberCount != null) {
            tvMemberCount.setText(memberList.size() + " Active Members");
        }
    }

    private void setupListeners() {
        btnAddMember.setOnClickListener(v -> showAddMemberDialog());
    }

    public void showAddMemberDialog() {
        // Use custom layout for adding member with credentials
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_member, null);
        builder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.etMemberName);
        final EditText etPhone = dialogView.findViewById(R.id.etMemberPhone);
        final EditText etEmail = dialogView.findViewById(R.id.etMemberEmail);
        final EditText etOTP = dialogView.findViewById(R.id.etMemberOTP);

        builder.setPositiveButton("Create Member", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String otp = etOTP.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.isEmpty() && email.isEmpty()) {
                Toast.makeText(getContext(), "Phone or Email is required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (otp.isEmpty()) {
                Toast.makeText(getContext(), "One-Time Access Code (OTP) is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add to list via repository
            Member newMember = new Member(name, "Member", true, phone, email);
            memberRepository.addMember(newMember);

            // Reload data to reflect changes
            loadMemberData();
            recyclerView.scrollToPosition(0);

            // Show confirmation
            showCredentialsDialog(name, phone, email, otp);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showCredentialsDialog(String name, String phone, String email, String otp) {
        String logins = "";
        if (!phone.isEmpty())
            logins += "Phone: " + phone + "\n";
        if (!email.isEmpty())
            logins += "Email: " + email + "\n";

        new AlertDialog.Builder(getContext())
                .setTitle("Member Account Created")
                .setMessage("Share these credentials with " + name + ":\n\n" + logins + "One-Time Access Code (OTP): "
                        + otp + "\n\n(Member will be prompted to change password on first login)")
                .setPositiveButton("Done", null)
                .show();
    }

    private void showProfileDialog(Member member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_member_profile, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Bind Views
        TextView tvName = view.findViewById(R.id.dialogProfileName);
        TextView tvRole = view.findViewById(R.id.dialogProfileRole);
        TextView tvPhone = view.findViewById(R.id.dialogProfilePhone);
        TextView tvEmail = view.findViewById(R.id.dialogProfileEmail);
        View statusView = view.findViewById(R.id.dialogProfileStatus);
        View btnClose = view.findViewById(R.id.btnCloseProfile);

        // Set Data
        tvName.setText(member.getName());
        tvRole.setText(member.getRole());
        tvPhone.setText(member.getPhone().isEmpty() ? "N/A" : member.getPhone());
        tvEmail.setText(member.getEmail().isEmpty() ? "N/A" : member.getEmail());

        int color = member.isActive() ? getContext().getColor(android.R.color.holo_green_dark)
                : getContext().getColor(android.R.color.darker_gray);
        statusView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // --- Adapter ---
    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private List<Member> list;

        MemberAdapter(List<Member> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_admin, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            Member member = list.get(position);
            holder.tvName.setText(member.getName());
            holder.tvRole.setText(member.getRole());

            // Status Indicator Color
            int color = member.isActive() ? holder.itemView.getContext().getColor(android.R.color.holo_green_dark)
                    : holder.itemView.getContext().getColor(android.R.color.darker_gray);
            holder.statusView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

            // Item Click -> Profile
            holder.itemView.setOnClickListener(v -> showProfileDialog(member));

            // Menu Click
            holder.btnMore.setOnClickListener(v -> showPopupMenu(v, member, position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        private void showPopupMenu(View view, Member member, int position) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            // Inflate menu resource or add programmatically
            popup.getMenu().add(0, 1, 0, "Promote to Admin");
            popup.getMenu().add(0, 2, 0, "Reset Password");
            popup.getMenu().add(0, 3, 0, "Remove Member");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: // Promote
                        member.setRole("Admin");
                        memberRepository.updateMember(position, member);
                        loadMemberData();
                        Toast.makeText(getContext(), member.getName() + " promoted to Admin", Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    case 2: // Reset Pass
                        Toast.makeText(getContext(), "Password reset link sent to " + member.getName(),
                                Toast.LENGTH_SHORT)
                                .show();
                        return true;
                    case 3: // Remove
                        new AlertDialog.Builder(getContext())
                                .setTitle("Remove Member")
                                .setMessage("Are you sure you want to remove " + member.getName() + "?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    memberRepository.removeMember(position);
                                    loadMemberData();
                                    Toast.makeText(getContext(), member.getName() + " removed", Toast.LENGTH_SHORT)
                                            .show();
                                })
                                .setNegativeButton("No", null)
                                .show();
                        return true;
                    default:
                        return false;
                }
            });
            popup.show();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole;
            View statusView;
            ImageView btnMore;

            MemberViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvMemberName);
                tvRole = itemView.findViewById(R.id.tvMemberRole);
                statusView = itemView.findViewById(R.id.statusIndicator);
                btnMore = itemView.findViewById(R.id.btnMoreActions);
            }
        }
    }
}
