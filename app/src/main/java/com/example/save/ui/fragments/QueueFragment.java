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

import com.example.save.databinding.FragmentQueueBinding;
import com.example.save.data.models.Member;
import com.example.save.ui.adapters.PayoutQueueAdapter;
import com.example.save.utils.SessionManager;
import com.example.save.data.models.ApprovalRequest;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;

public class QueueFragment extends Fragment {

    private FragmentQueueBinding binding;
    private PayoutQueueAdapter adapter;
    private ApprovalsAdapter approvalsAdapter;
    private MembersViewModel viewModel;
    private double configContributionAmount = 0;
    private double configRetentionPct = 0;

    public static QueueFragment newInstance() {
        return new QueueFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentQueueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeData();
        setupGenericInteractions();

        viewModel.fetchSystemConfig((success, config, message) -> {
            if (success && config != null) {
                configContributionAmount = config.getContributionAmount();
                configRetentionPct = config.getRetentionPercentage();
            }
        });

        // Ensure data is synced
        viewModel.syncMembers();
    }

    private void setupGenericInteractions() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        boolean isAdmin = getActivity() instanceof com.example.save.ui.activities.AdminMainActivity;
        if (!isAdmin) {
            binding.llPendingApprovals.setVisibility(View.GONE);
        }

        binding.btnApprovalsHeader.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                ((com.example.save.ui.activities.AdminMainActivity) getActivity()).loadFragment(new ApprovalsFragment(), true);
            }
        });
    }

    private void setupRecyclerView() {
        String payoutDate = requireContext()
                .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                .getString("rule_next_payout_date", "TBD");
        adapter = new PayoutQueueAdapter(new ArrayList<>(), 0, payoutDate);
        binding.rvQueue.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQueue.setAdapter(adapter);

        // Setup Approvals (Today's Work Restoration)
        approvalsAdapter = new ApprovalsAdapter(new ApprovalsAdapter.OnApprovalClickListener() {
            @Override
            public void onApproveClick(ApprovalsAdapter.ApprovalItem item) {
                viewModel.processApproval(item, true, (success, message) -> {
                    if (isVisible()) Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onItemClick(ApprovalsAdapter.ApprovalItem item) {
                if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                    String amountStr = "UGX " + String.format(java.util.Locale.US, "%,.0f", item.getAmount());
                    ((com.example.save.ui.activities.AdminMainActivity) getActivity())
                            .loadFragment(PayoutConfirmationFragment.newInstance(item.getId(), amountStr), true);
                }
            }
        });
        binding.rvPendingApprovals.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvPendingApprovals.setAdapter(approvalsAdapter);
    }


    private void observeData() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members == null || members.isEmpty()) {
                binding.rvQueue.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
                return;
            }
            binding.rvQueue.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);

            String payoutDate = requireContext()
                    .getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("rule_next_payout_date", "TBD");
            adapter.updateList(members, payoutDate);

            if (configContributionAmount > 0) {
                double totalCollected = configContributionAmount * members.size();
                double payout = totalCollected * (1.0 - configRetentionPct / 100.0);
                adapter.setPayoutAmount(payout);
            }
        });

        // Observe Payout Approvals (Today's Work Restoration)
        viewModel.getPendingApprovals().observe(getViewLifecycleOwner(), approvals -> {
            List<ApprovalsAdapter.ApprovalItem> payoutApprovals = new ArrayList<>();
            if (approvals != null) {
                for (ApprovalRequest a : approvals) {
                    if ("PAYOUT".equalsIgnoreCase(a.getType())) {
                        payoutApprovals.add(a);
                    }
                }
            }

            if (payoutApprovals.isEmpty()) {
                binding.rvPendingApprovals.setVisibility(View.GONE);
                binding.tvNoApprovals.setVisibility(View.VISIBLE);
            } else {
                binding.rvPendingApprovals.setVisibility(View.VISIBLE);
                binding.tvNoApprovals.setVisibility(View.GONE);
                approvalsAdapter.updateList(payoutApprovals);
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
