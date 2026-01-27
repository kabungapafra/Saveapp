package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.databinding.FragmentAdminLoansBinding;
import com.example.save.data.models.LoanRequest;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.ui.adapters.LoanRequestAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminLoansFragment extends Fragment {

    private FragmentAdminLoansBinding binding;
    private MembersViewModel viewModel;
    private LoanRequestAdapter adapter;
    private List<LoanRequest> allRequests = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminLoansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupTabs();
        setupRecyclerView();
        setupListeners();
        observeLoanRequests();
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Pending Requests"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Active Loans"));

        binding.tabLayout
                .addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                        filterList(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    }

                    @Override
                    public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    }
                });
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupRecyclerView() {
        binding.rvLoanRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LoanRequestAdapter(new LoanRequestAdapter.OnLoanActionListener() {
            @Override
            public void onApprove(LoanRequest request) {
                com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(getContext());
                String adminEmail = session.getUserEmail();

                viewModel.initiateLoanApproval(request.getId(), adminEmail, (success, message) -> {
                    if (isVisible()) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onReject(LoanRequest request) {
                // Show rejection reason dialog
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                android.widget.EditText reasonInput = new android.widget.EditText(getContext());
                reasonInput.setHint("Rejection reason (optional)");
                builder.setTitle("Reject Loan Request");
                builder.setMessage("Are you sure you want to reject this loan request?");
                builder.setView(reasonInput);
                builder.setPositiveButton("Reject", (dialog, which) -> {
                    String reason = reasonInput.getText().toString().trim();
                    // Reject via API - backend will update status and notify member
                    viewModel.rejectLoanRequest(request.getId(), reason,
                            new com.example.save.data.repository.MemberRepository.RejectionCallback() {
                                @Override
                                public void onResult(boolean success, String message) {
                                    if (isVisible()) {
                                        Toast.makeText(getContext(),
                                                message != null ? message
                                                        : (success ? "Loan rejected" : "Failed to reject loan"),
                                                Toast.LENGTH_SHORT).show();
                                        if (success) {
                                            // Refresh list
                                            observeLoanRequests();
                                        }
                                    }
                                }
                            });
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });
        binding.rvLoanRequests.setAdapter(adapter);
    }

    private void observeLoanRequests() {
        viewModel.getLoanRequests().observe(getViewLifecycleOwner(), requests -> {
            if (requests != null) {
                allRequests = requests;
                updatePendingCount(requests);
                // Filter based on currently selected tab
                filterList(binding.tabLayout.getSelectedTabPosition());
            }
        });
    }

    private void updatePendingCount(List<LoanRequest> requests) {
        int pendingCount = 0;
        for (LoanRequest req : requests) {
            if ("PENDING".equals(req.getStatus()))
                pendingCount++;
        }
        // If we want to show badges, we can update them here
    }

    private void filterList(int tabIndex) {
        List<LoanRequest> filteredList = new ArrayList<>();
        String targetStatus = (tabIndex == 0) ? "PENDING" : "APPROVED";

        for (LoanRequest req : allRequests) {
            if (tabIndex == 0) {
                if ("PENDING".equals(req.getStatus())) {
                    filteredList.add(req);
                }
            } else {
                if ("APPROVED".equals(req.getStatus()) || "ACTIVE".equals(req.getStatus())) {
                    filteredList.add(req);
                }
            }
        }

        if (!filteredList.isEmpty()) {
            adapter.updateList(filteredList);
            binding.tvEmptyState.setVisibility(View.GONE);
            binding.rvLoanRequests.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.rvLoanRequests.setVisibility(View.GONE);
            binding.tvEmptyState.setText(tabIndex == 0 ? "No pending loan requests" : "No active loans");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
