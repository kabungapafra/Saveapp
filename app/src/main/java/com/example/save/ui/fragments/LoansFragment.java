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
        adapter = new LoanRequestAdapter();
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

    // Adapter
    private class LoanRequestAdapter extends RecyclerView.Adapter<LoanRequestAdapter.ViewHolder> {
        private List<LoanRequest> requests = new ArrayList<>();

        void updateList(List<LoanRequest> newList) {
            this.requests = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLoanRequestBinding itemBinding = ItemLoanRequestBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LoanRequest request = requests.get(position);
            holder.bind(request);
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemLoanRequestBinding binding;

            ViewHolder(ItemLoanRequestBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            void bind(LoanRequest request) {
                binding.tvMemberName.setText(request.getMemberName());
                binding.tvStatus.setText(request.getStatus());
                binding.tvAmount
                        .setText("UGX " + NumberFormat.getNumberInstance(Locale.US).format(request.getAmount()));
                binding.tvDuration.setText(request.getDurationMonths() + " months");
                binding.tvRepayment.setText(
                        "UGX " + NumberFormat.getNumberInstance(Locale.US).format(request.getTotalRepayment()));

                if (request.getGuarantor() != null && !request.getGuarantor().isEmpty()) {
                    binding.tvGuarantor.setText("Guarantor: " + request.getGuarantor());
                    binding.tvGuarantor.setVisibility(View.VISIBLE);
                } else {
                    binding.tvGuarantor.setVisibility(View.GONE);
                }

                if (request.getReason() != null && !request.getReason().isEmpty()) {
                    binding.tvReason.setText("Reason: " + request.getReason());
                    binding.tvReason.setVisibility(View.VISIBLE);
                } else {
                    binding.tvReason.setVisibility(View.GONE);
                }

                binding.tvDate.setText("Requested: " + request.getRequestDate());

                // Show/hide action buttons based on status
                if ("PENDING".equals(request.getStatus())) {
                    binding.layoutActions.setVisibility(View.VISIBLE);

                    binding.btnApprove.setOnClickListener(v -> {
                        viewModel.approveLoanRequest(request.getId());
                        Toast.makeText(getContext(), "Loan approved for " + request.getMemberName(), Toast.LENGTH_SHORT)
                                .show();
                    });

                    binding.btnReject.setOnClickListener(v -> {
                        viewModel.rejectLoanRequest(request.getId());
                        Toast.makeText(getContext(), "Loan rejected", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    binding.layoutActions.setVisibility(View.GONE);
                }

                // Update status badge color
                if ("APPROVED".equals(request.getStatus())) {
                    binding.tvStatus.setBackgroundColor(0xFF4CAF50);
                } else if ("REJECTED".equals(request.getStatus())) {
                    binding.tvStatus.setBackgroundColor(0xFFD32F2F);
                } else {
                    binding.tvStatus.setBackgroundColor(0xFFFF9800);
                }
            }
        }
    }
}
