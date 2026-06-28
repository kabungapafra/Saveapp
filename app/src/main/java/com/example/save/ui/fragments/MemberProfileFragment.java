package com.example.save.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.data.models.LoanEntity;
import com.example.save.data.models.Transaction;
import com.example.save.databinding.FragmentMemberProfileBinding;
import com.example.save.ui.adapters.MemberProfileHistoryAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;
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

        binding.toolbar.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        loadMemberData();
        return binding.getRoot();
    }

    private void loadMemberData() {
        if (memberPhone == null) return;
        viewModel.getMemberByPhoneLive(memberPhone).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                updateUI(member);
                loadTransactions(member.getName());
                loadLoanData(member.getName());
            }
        });
    }

    private void updateUI(Member member) {
        // Identity
        binding.tvMemberName.setText(member.getName());
        String joinedDate = member.getJoinedDate();
        binding.tvMemberRole.setText(joinedDate != null && !joinedDate.isEmpty()
                ? "Joined " + formatJoinDate(joinedDate) : "Member");

        // Badges (real)
        String role = member.getRole() != null ? member.getRole().toUpperCase(Locale.getDefault()) : "MEMBER";
        binding.tvBadgeRole.setText(role);
        String reliability = member.getReliabilityLabel();
        binding.tvBadgeReliability.setText(reliability != null ? reliability.toUpperCase(Locale.getDefault()) : "—");

        // Avatar / initials
        com.example.save.utils.SessionManager session =
                com.example.save.utils.SessionManager.getInstance(requireContext());
        String normalizedMemberPhone = member.getPhone() != null ? member.getPhone().replaceAll("[^0-9+]", "") : "";
        String savedImage = session.getProfileImage(normalizedMemberPhone);

        if (savedImage != null && new java.io.File(savedImage).exists()) {
            binding.ivProfileAvatar.setVisibility(View.VISIBLE);
            binding.tvProfileInitials.setVisibility(View.GONE);
            com.bumptech.glide.Glide.with(this).load(savedImage).circleCrop().into(binding.ivProfileAvatar);
        } else if (member.getName() != null && !member.getName().isEmpty()) {
            String[] parts = member.getName().split(" ");
            String initials = "";
            if (parts.length > 0 && !parts[0].isEmpty()) initials += parts[0].charAt(0);
            if (parts.length > 1 && !parts[1].isEmpty()) initials += parts[1].charAt(0);
            binding.tvProfileInitials.setText(initials.toUpperCase(Locale.getDefault()));
            binding.tvProfileInitials.setVisibility(View.VISIBLE);
            binding.ivProfileAvatar.setVisibility(View.GONE);
        }

        // Total contributed (real)
        binding.tvTotalContributed.setText(formatCurrency(member.getContributionPaid()));

        // Admin actions — only an admin can manage other members
        String userRole = com.example.save.utils.SessionManager.getInstance(requireContext()).getUserRole();
        boolean isAdmin = "admin".equalsIgnoreCase(userRole) || "Administrator".equalsIgnoreCase(userRole);
        binding.cvAdminActions.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        binding.btnDeleteMember.setOnClickListener(v -> viewModel.removeMember(member, (success, message) -> {
            if (getActivity() == null) return;
            if (success) {
                Toast.makeText(requireContext(), "Member removed successfully", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(requireContext(), "Failed to remove member: " + message, Toast.LENGTH_LONG).show();
            }
        }));
        binding.btnPromoteMember.setOnClickListener(v -> changeRole(member, "Administrator"));
        binding.btnDemoteMember.setOnClickListener(v -> changeRole(member, "Member"));
    }

    private void changeRole(Member member, String newRole) {
        member.setRole(newRole);
        List<Member> currentList = viewModel.getMembers().getValue();
        int position = -1;
        if (currentList != null) {
            for (int i = 0; i < currentList.size(); i++) {
                Member m = currentList.get(i);
                if (m.getPhone() != null && m.getPhone().equals(member.getPhone())) { position = i; break; }
            }
        }
        if (position >= 0) {
            viewModel.updateMember(position, member);
            Toast.makeText(requireContext(), "Role updated to " + newRole, Toast.LENGTH_SHORT).show();
            binding.tvBadgeRole.setText(newRole.toUpperCase(Locale.getDefault()));
        } else {
            Toast.makeText(requireContext(), "Failed to locate member", Toast.LENGTH_LONG).show();
        }
    }

    /** Populates the Active Loans count and the loan details card from real loan records. */
    private void loadLoanData(String memberName) {
        viewModel.getLoanRequests().observe(getViewLifecycleOwner(), loans -> {
            if (binding == null) return;
            LoanEntity active = null;
            if (loans != null && memberName != null) {
                for (LoanEntity l : loans) {
                    String st = l.getStatus() != null ? l.getStatus() : "";
                    if (memberName.equalsIgnoreCase(l.getMemberName())
                            && ("ACTIVE".equalsIgnoreCase(st) || "APPROVED".equalsIgnoreCase(st))) {
                        active = l;
                        break;
                    }
                }
            }

            binding.tvStreak.setText(active != null ? "1" : "0");

            if (active == null) {
                binding.cvActiveLoanDetails.setVisibility(View.GONE);
                return;
            }

            binding.cvActiveLoanDetails.setVisibility(View.VISIBLE);
            double principal = active.getAmount();
            double interest = active.getInterest();
            double totalDue = principal + interest;
            double repaid = active.getRepaidAmount();
            double remaining = Math.max(0, totalDue - repaid);
            int rate = principal > 0 ? (int) Math.round(interest / principal * 100) : 0;
            int progress = totalDue > 0 ? (int) Math.min(100, Math.max(0, repaid / totalDue * 100)) : 0;

            binding.tvCreditScore.setText(formatCurrency(principal));
            binding.tvReliability.setText(rate + "%");
            binding.loanProgressBar.setProgress(progress);
            binding.tvLoanPaid.setText("Paid: " + formatCurrency(repaid));
            binding.tvLoanRemaining.setText("Remaining: " + formatCurrency(remaining));
            binding.tvLoanDueDate.setText(active.getDueDate() != null
                    ? new java.text.SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(active.getDueDate())
                    : "—");
        });
    }

    private void loadTransactions(String memberName) {
        viewModel.getLatestMemberTransactions(memberName).observe(getViewLifecycleOwner(), entities -> {
            if (binding == null) return;
            List<Transaction> transactions = new ArrayList<>();
            if (entities != null) {
                for (com.example.save.data.models.TransactionEntity entity : entities) {
                    transactions.add(new Transaction(
                            entity.getDescription(),
                            entity.getDate() != null
                                    ? new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(entity.getDate())
                                    : "",
                            entity.getAmount(),
                            entity.isPositive(),
                            entity.isPositive() ? R.drawable.ic_money : R.drawable.ic_loan,
                            0));
                }
            }
            binding.rvTransactions.setAdapter(new MemberProfileHistoryAdapter(transactions));
            binding.tvEmptyHistory.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "UGX %,.0f", amount);
    }

    private String formatJoinDate(String iso) {
        String[] formats = {"yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};
        for (String fmt : formats) {
            try {
                java.util.Date d = new java.text.SimpleDateFormat(fmt, Locale.getDefault()).parse(iso);
                if (d != null) return new java.text.SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(d);
            } catch (java.text.ParseException ignored) { }
        }
        return iso;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
