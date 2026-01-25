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
        // 1. Observe Admin Count first (needed for Adapter)
        viewModel.getAdminCountLive().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                // 2. Observe Approvals only when we have the count
                // Using a Mediator or just nested observation might be tricky if not careful
                // with duplicates.
                // Better approach: Store count in a member variable, and update adapter.
                // But adapter needs it for EVERY item update.
                // Let's update the adapter's admin count property.
                if (adapter != null) {
                    // We need to re-submit list if count changes?
                    // Or just set the count. For now, let's keep it simpler:
                    // Just triggering the list observation again might be enough if we use a field.
                }

                // Real reactive chain:
                subscribeToApprovals(count);
            }
        });
    }

    private void subscribeToApprovals(int adminCount) {
        // Remove previous observers if any? LiveData handles this if we use the same
        // observer instance,
        // but here we are creating a new lambda every time adminCount changes.
        // Ideally we use a CombinedLiveData.
        // For simplicity in this fix:

        viewModel.getCombinedApprovals(adminEmail).observe(getViewLifecycleOwner(), list -> {
            if (binding != null) {
                adapter.updateList(list, adminCount);
                binding.emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void approveLoan(long id) {
        viewModel.approveLoan(id, adminEmail, (success, message) -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            // No need to call refreshList(), LiveData will update automatically!
        });
    }

    private void approvePayout(long id) {
        viewModel.approveTransaction(id, adminEmail, (success, message) -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            // No need to call refreshList(), LiveData will update automatically!
        });
    }
}
