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
        subscribeToApprovals();
    }

    private void subscribeToApprovals() {
        viewModel.getCombinedApprovals(adminEmail).observe(getViewLifecycleOwner(), list -> {
            if (binding != null) {
                adapter.updateList(list);
                binding.emptyState.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void approveLoan(String id) {
        viewModel.approveLoan(id, adminEmail, (success, message) -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            // No need to call refreshList(), LiveData will update automatically!
        });
    }

    private void approvePayout(String id) {
        viewModel.approveTransaction(id, adminEmail, (success, message) -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            // No need to call refreshList(), LiveData will update automatically!
        });
    }
}
