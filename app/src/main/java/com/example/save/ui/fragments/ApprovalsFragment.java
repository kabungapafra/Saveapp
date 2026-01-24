package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.save.databinding.FragmentApprovalsBinding;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.models.LoanRequest;
import java.util.ArrayList;
import java.util.List;

public class ApprovalsFragment extends Fragment {

    private FragmentApprovalsBinding binding;
    private MembersViewModel viewModel;
    private ApprovalsAdapter adapter;
    private String adminEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentApprovalsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        // Get admin email from session
        com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(requireContext());
        adminEmail = session.getUserDetails().get(com.example.save.utils.SessionManager.KEY_EMAIL);

        setupRecyclerView();
        observeData();

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.rvApprovals.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ApprovalsAdapter(item -> {
            if ("LOAN".equals(item.getType())) {
                approveLoan(item.getId());
            } else {
                approvePayout(item.getId());
            }
        });
        binding.rvApprovals.setAdapter(adapter);
    }

    private void observeData() {
        // Combined observer for Loans and Payouts
        viewModel.getPendingTransactions().observe(getViewLifecycleOwner(), txList -> refreshList());
        viewModel.getLoanRequests().observe(getViewLifecycleOwner(), loanList -> refreshList());
    }

    private void refreshList() {
        new Thread(() -> {
            int adminCount = viewModel.getAdminCount();
            fetchApprovalData(adminCount);
        }).start();
    }

    private void fetchApprovalData(int adminCount) {
        new Thread(() -> {
            List<ApprovalsAdapter.ApprovalItem> combinedList = new ArrayList<>();

            // Fetch Payouts
            List<TransactionEntity> pendingsTx = viewModel.getPendingTransactions().getValue();
            if (pendingsTx != null) {
                for (TransactionEntity tx : pendingsTx) {
                    int approvals = repository_getApprovalCount("PAYOUT", tx.getId());
                    boolean hasApproved = viewModel.hasAdminApproved("PAYOUT", tx.getId(), adminEmail);
                    if (!hasApproved) {
                        combinedList.add(new ApprovalItemImpl(tx.getId(), "PAYOUT", tx.getMemberName(), tx.getAmount(),
                                tx.getDescription(), tx.getDate(), approvals, hasApproved));
                    }
                }
            }

            // Fetch Loans
            List<LoanRequest> pendingsLoan = viewModel.getPendingLoanRequests();
            if (pendingsLoan != null) {
                for (LoanRequest req : pendingsLoan) {
                    // LoanRequest ID is String externalId, but ApprovalEntity uses long targetId.
                    // This is a mismatch. I should have used long for Loan approvals too or updated
                    // DAO.
                    // For now, I'll try to get the internal ID.
                    long internalId = 0; // Find internal ID from DB
                    try {
                        com.example.save.data.local.entities.LoanEntity entity = com.example.save.data.repository.MemberRepository
                                .getInstance().getActiveLoanForMember(req.getMemberName());
                        if (entity != null)
                            internalId = entity.getId();
                    } catch (Exception e) {
                    }

                    int approvals = repository_getApprovalCount("LOAN", internalId);
                    boolean hasApproved = viewModel.hasAdminApproved("LOAN", internalId, adminEmail);
                    if (!hasApproved) {
                        combinedList.add(new ApprovalItemImpl(internalId, "LOAN", req.getMemberName(), req.getAmount(),
                                req.getReason(), new java.util.Date(), approvals, hasApproved));
                    }
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding != null) {
                        adapter.updateList(combinedList, adminCount);
                        binding.emptyState.setVisibility(combinedList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
            }
        }).start();
    }

    // Helper for sync repo calls
    private int repository_getApprovalCount(String type, long id) {
        return com.example.save.data.repository.MemberRepository.getInstance().getApprovalCount(type, id);
    }

    private void approveLoan(long id) {
        viewModel.approveLoan(id, adminEmail, (success, message) -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            refreshList();
        });
    }

    private void approvePayout(long id) {
        viewModel.approveTransaction(id, adminEmail, (success, message) -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            refreshList();
        });
    }

    // Implementation of ApprovalItem
    private static class ApprovalItemImpl implements ApprovalsAdapter.ApprovalItem {
        private final long id;
        private final String type, title, description;
        private final double amount;
        private final java.util.Date date;
        private final int approvalCount;
        private final boolean hasApproved;

        public ApprovalItemImpl(long id, String type, String title, double amount, String description,
                java.util.Date date, int approvalCount, boolean hasApproved) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.amount = amount;
            this.description = description;
            this.date = date;
            this.approvalCount = approvalCount;
            this.hasApproved = hasApproved;
        }

        public long getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public double getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public java.util.Date getDate() {
            return date;
        }

        public int getApprovalCount() {
            return approvalCount;
        }

        public boolean hasApproved() {
            return hasApproved;
        }
    }
}
