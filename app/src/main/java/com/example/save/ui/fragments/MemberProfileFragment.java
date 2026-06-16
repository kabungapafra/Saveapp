package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.data.models.Transaction;
import com.example.save.databinding.FragmentMemberProfileBinding;
import com.example.save.ui.adapters.MemberProfileHistoryAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import java.util.Locale;

public class MemberProfileFragment extends Fragment {

    private static final String ARG_PHONE = "member_phone";

    private FragmentMemberProfileBinding binding;
    private MembersViewModel viewModel;
    private String memberPhone;

    public static MemberProfileFragment newInstance(String phone) {
        MemberProfileFragment fragment = new MemberProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHONE, phone);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberPhone = getArguments().getString(ARG_PHONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberProfileBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        setupToolbar();
        loadMemberData();
        return binding.getRoot();
    }

    private void setupToolbar() {
        binding.toolbar.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().onBackPressed();
        });
    }

    private void loadMemberData() {
        if (memberPhone == null) return;
        
        viewModel.getMemberByPhoneLive(memberPhone).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                updateUI(member);
                loadTransactions(member.getName());
            }
        });
    }

    private void updateUI(Member member) {
        binding.tvMemberName.setText(member.getName());
        binding.tvMemberRole.setText("Member since January 2024");

        // Hide delete button as per user request
        binding.btnDeleteMember.setVisibility(View.GONE);

        // Promote button logic (makes member an Administrator)
        binding.btnPromoteMember.setOnClickListener(v -> {
            if (member != null) {
                member.setRole("Administrator");
                // Find position in current list and update
                List<Member> currentList = viewModel.getMembers().getValue();
                int position = -1;
                if (currentList != null) {
                    for (int i = 0; i < currentList.size(); i++) {
                        Member m = currentList.get(i);
                        if (m.getPhone() != null && m.getPhone().equals(member.getPhone())) {
                            position = i;
                            break;
                        }
                    }
                }
                if (position >= 0) {
                    viewModel.updateMember(position, member);
                    Toast.makeText(requireContext(), "Role updated to Administrator", Toast.LENGTH_SHORT).show();
                    binding.tvMemberRole.setText(member.getRole());
                } else {
                    Toast.makeText(requireContext(), "Failed to locate member", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Demote button logic (makes member a regular Member)
        binding.btnDemoteMember.setOnClickListener(v -> {
            if (member != null) {
                member.setRole("Member");
                List<Member> currentList = viewModel.getMembers().getValue();
                int position = -1;
                if (currentList != null) {
                    for (int i = 0; i < currentList.size(); i++) {
                        Member m = currentList.get(i);
                        if (m.getPhone() != null && m.getPhone().equals(member.getPhone())) {
                            position = i;
                            break;
                        }
                    }
                }
                if (position >= 0) {
                    viewModel.updateMember(position, member);
                    Toast.makeText(requireContext(), "Role updated to Member", Toast.LENGTH_SHORT).show();
                    binding.tvMemberRole.setText(member.getRole());
                } else {
                    Toast.makeText(requireContext(), "Failed to locate member", Toast.LENGTH_LONG).show();
                }
            }
        });
        
        // Handle Profile Picture vs Initials
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
        String currentUserPhone = session.getUserPhone();

        // Normalize both phone numbers for robust comparison
        String normalizedMemberPhone = member.getPhone() != null ? member.getPhone().replaceAll("[^0-9+]", "") : "";
        String normalizedCurrentPhone = currentUserPhone != null ? currentUserPhone.replaceAll("[^0-9+]", "") : "";

        boolean isSelf = !normalizedCurrentPhone.isEmpty() && !normalizedMemberPhone.isEmpty() && normalizedCurrentPhone.equals(normalizedMemberPhone);
        
        String savedImage = isSelf ? session.getProfileImage() : null;

        if (savedImage != null) {
            binding.ivProfileAvatar.setVisibility(View.VISIBLE);
            binding.tvProfileInitials.setVisibility(View.GONE);
            com.bumptech.glide.Glide.with(this)
                    .load(savedImage)
                    .circleCrop()
                    .into(binding.ivProfileAvatar);
        } else if (member.getName() != null && !member.getName().isEmpty()) {
            String[] parts = member.getName().split(" ");
            String initials = "";
            if (parts.length > 0 && !parts[0].isEmpty())
                initials += parts[0].charAt(0);
            if (parts.length > 1 && !parts[1].isEmpty())
                initials += parts[1].charAt(0);
            binding.tvProfileInitials.setText(initials.toUpperCase());
            binding.tvProfileInitials.setVisibility(View.VISIBLE);
            binding.ivProfileAvatar.setVisibility(View.GONE);
        }

        double balance = member.getContributionPaid();
        binding.tvSavingsBalance.setText(String.format(Locale.getDefault(), "UGX %,.0f", balance));
        binding.tvTotalContributed.setText(String.format(Locale.getDefault(), "UGX %,.0f", balance));
        
        // Active Loans
        int loans = member.getShortfallAmount() > 0 ? 1 : 0;
        binding.tvStreak.setText(String.valueOf(loans)); // Note: ID is tvStreak mapped to Active Loans count in design
    }


    private void loadTransactions(String memberName) {
        viewModel.getLatestMemberTransactions(memberName).observe(getViewLifecycleOwner(), entities -> {
            if (entities != null) {
                List<Transaction> transactions = new ArrayList<>();
                for (com.example.save.data.models.TransactionEntity entity : entities) {
                    transactions.add(new Transaction(
                            entity.getDescription(),
                            new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                    .format(entity.getDate()),
                            entity.getAmount(),
                            entity.isPositive(),
                            entity.isPositive() ? R.drawable.ic_money : R.drawable.ic_loan,
                            0));
                }

                MemberProfileHistoryAdapter adapter = new MemberProfileHistoryAdapter(transactions);
                binding.rvTransactions.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
