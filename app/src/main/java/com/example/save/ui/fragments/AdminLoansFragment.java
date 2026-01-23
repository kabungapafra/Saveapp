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
                // Execute approval on background thread
                new Thread(() -> {
                    boolean success = viewModel.approveLoanRequest(request.getId());
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(),
                                    "Loan approved for " + request.getMemberName(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Cannot approve: Insufficient group balance",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }).start();
            }

            @Override
            public void onReject(LoanRequest request) {
                // Execute rejection on background thread for consistency
                new Thread(() -> {
                    viewModel.rejectLoanRequest(request.getId());
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                                "Loan rejected for " + request.getMemberName(),
                                Toast.LENGTH_SHORT).show();
                    });
                }).start();
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
