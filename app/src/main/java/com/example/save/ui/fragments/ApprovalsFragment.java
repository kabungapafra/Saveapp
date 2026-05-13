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

import com.example.save.R;
import com.example.save.databinding.FragmentApprovalsBinding;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;

import java.util.ArrayList;

public class ApprovalsFragment extends Fragment {

    private FragmentApprovalsBinding binding;
    private MembersViewModel viewModel;
    private ApprovalsAdapter adapter;

    public static ApprovalsFragment newInstance() {
        return new ApprovalsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentApprovalsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeApprovals();
    }

    private void setupRecyclerView() {
        String adminEmail = SessionManager.getInstance(requireContext()).getUserEmail();
        adapter = new ApprovalsAdapter(new ApprovalsAdapter.OnApprovalClickListener() {
            @Override
            public void onApproveClick(ApprovalsAdapter.ApprovalItem item) {
                if ("LOAN".equals(item.getType())) {
                    viewModel.approveLoan(item.getId(), adminEmail, (success, message) -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
                        }
                    });
                } else {
                    viewModel.approveTransaction(item.getId(), adminEmail, (success, message) -> {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            }

            @Override
            public void onItemClick(ApprovalsAdapter.ApprovalItem item) {
                if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                    String amountStr = "UGX " + String.format(java.util.Locale.US, "%,.0f", item.getAmount());
                    if ("LOAN".equalsIgnoreCase(item.getType())) {
                        ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                                .loadFragment(ConfirmLoanApprovalFragment.newInstance(item.getId(), item.getTitle(), amountStr), true);
                    } else {
                        ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                                .loadFragment(PayoutConfirmationFragment.newInstance(item.getId(), amountStr), true);
                    }
                }
            }
        });

        binding.rvApprovals.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvApprovals.setAdapter(adapter);
    }

    private void observeApprovals() {
        String adminEmail = SessionManager.getInstance(requireContext()).getUserEmail();
        viewModel.getCombinedApprovals(adminEmail).observe(getViewLifecycleOwner(), items -> {
            if (items != null && !items.isEmpty()) {
                binding.rvApprovals.setVisibility(View.VISIBLE);
                if (binding.emptyState != null) binding.emptyState.setVisibility(View.GONE);
                adapter.updateList(items);
            } else {
                binding.rvApprovals.setVisibility(View.GONE);
                if (binding.emptyState != null) binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
