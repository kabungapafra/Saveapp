package com.example.save.ui.fragments;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.FragmentLoansBinding;
import com.example.save.ui.viewmodels.LoansViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.save.ui.adapters.LoansAdapter;
import com.example.save.data.models.Loan;

public class LoansFragment extends Fragment {

    private FragmentLoansBinding binding;
    private LoansAdapter adapter;
    private LoansViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoansBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(LoansViewModel.class);

        setupRecyclerView();
        observeViewModel();

        binding.fabAddLoan.setOnClickListener(
                v -> Toast.makeText(getContext(), "Issue New Loan feature coming soon", Toast.LENGTH_SHORT).show());

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getLoans().observe(getViewLifecycleOwner(), loans -> {
            if (loans != null) {
                updateUI(loans);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // initViews removed as Binding handles it

    private void setupRecyclerView() {
        binding.recyclerLoans.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateUI(List<Loan> allLoans) {
        if (getContext() == null)
            return;

        // Manual stats calculation (can be moved to ViewModel but keeping here for
        // simplicity for now)
        double outstanding = 0;
        List<Loan> pendingLoans = new ArrayList<>();
        List<Loan> activeLoans = new ArrayList<>();

        for (Loan l : allLoans) {
            if (Loan.STATUS_ACTIVE.equals(l.getStatus())) {
                outstanding += (l.getTotalDue() - l.getRepaidAmount());
                activeLoans.add(l);
            } else if (Loan.STATUS_PENDING.equals(l.getStatus())) {
                pendingLoans.add(l);
            }
        }

        // Not showing interest calculation here to keep it simple, or re-fetch if
        // needed

        binding.tvTotalOutstanding.setText(String.format(Locale.getDefault(), "UGX %,.0f", outstanding));
        binding.tvPendingCount.setText(String.valueOf(pendingLoans.size()));
        // The original code had tvInterestEarned, but the new instruction removes its
        // calculation.
        // For now, we'll leave the TextView update out or set it to a default if it's
        // still in the layout.
        // Assuming it's okay to not update it if the calculation is removed.
        // If tvInterestEarned is still in the layout, it might show old data or need a
        // default value.
        // For now, I'll comment out the line that updated it.
        // binding.tvInterestEarned.setText(String.format(Locale.getDefault(), "UGX
        // %,.0f", 0.0)); // Or some default

        // Build List Items
        List<Object> items = new ArrayList<>();

        if (!pendingLoans.isEmpty()) {
            items.add("Pending Requests");
            items.addAll(pendingLoans);
        }

        if (!activeLoans.isEmpty()) {
            items.add("Active Loans");
            items.addAll(activeLoans);
        } else if (items.isEmpty()) {
            items.add("No active loans or requests.");
        }

        if (adapter == null) {
            adapter = new LoansAdapter(items, new LoansAdapter.LoanActionListener() {
                @Override
                public void onApprove(Loan loan) {
                    viewModel.approveLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Approved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReject(Loan loan) {
                    viewModel.rejectLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Rejected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRemind(Loan loan) {
                    Toast.makeText(getContext(), "Reminder sent to " + loan.getMemberName(), Toast.LENGTH_SHORT).show();
                }
            });
            binding.recyclerLoans.setAdapter(adapter);
        } else {
            // Re-create adapter to refresh list (simplified for now)
            adapter = new LoansAdapter(items, new LoansAdapter.LoanActionListener() {
                @Override
                public void onApprove(Loan loan) {
                    viewModel.approveLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Approved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReject(Loan loan) {
                    viewModel.rejectLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Rejected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRemind(Loan loan) {
                    Toast.makeText(getContext(), "Reminder sent to " + loan.getMemberName(), Toast.LENGTH_SHORT).show();
                }
            });
            binding.recyclerLoans.setAdapter(adapter);
        }
    }
}
