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
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.databinding.FragmentLoansBinding;
import com.example.save.databinding.ItemLoanRequestBinding;
import com.example.save.data.models.LoanRequest;
import com.example.save.ui.viewmodels.MembersViewModel;

import com.example.save.ui.adapters.LoanRequestAdapter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoansFragment extends Fragment {

    private FragmentLoansBinding binding;
    private MembersViewModel viewModel;
    private LoanRequestAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeLoanRequests();
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
                viewModel.approveLoanRequest(request.getId());
                Toast.makeText(getContext(), "Loan approved for " + request.getMemberName(), Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onReject(LoanRequest request) {
                viewModel.rejectLoanRequest(request.getId());
                Toast.makeText(getContext(), "Loan rejected", Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvLoanRequests.setAdapter(adapter);
    }

    private void observeLoanRequests() {
        viewModel.getLoanRequests().observe(getViewLifecycleOwner(), requests -> {
            if (requests != null && !requests.isEmpty()) {
                adapter.updateList(requests);
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvLoanRequests.setVisibility(View.VISIBLE);

                // Update pending count
                int pendingCount = 0;
                for (LoanRequest req : requests) {
                    if ("PENDING".equals(req.getStatus()))
                        pendingCount++;
                }
                binding.tvPendingCount.setText(pendingCount + " Pending");
            } else {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvLoanRequests.setVisibility(View.GONE);
                binding.tvPendingCount.setText("0 Pending");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
