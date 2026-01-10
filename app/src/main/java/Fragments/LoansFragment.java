package Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Adapters.LoansAdapter;
import Data.Loan;
import Data.LoanRepository;

public class LoansFragment extends Fragment implements LoanRepository.LoanChangeListener {

    private RecyclerView recyclerView;
    private TextView tvTotalOutstanding, tvInterestEarned, tvPendingCount;
    private LoansAdapter adapter;
    private LoanRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loans, container, false);

        repository = LoanRepository.getInstance();
        repository.setListener(this);

        initViews(view);
        setupRecyclerView();
        updateUI();

        view.findViewById(R.id.fabAddLoan).setOnClickListener(
                v -> Toast.makeText(getContext(), "Issue New Loan feature coming soon", Toast.LENGTH_SHORT).show());

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerLoans);
        tvTotalOutstanding = view.findViewById(R.id.tvTotalOutstanding);
        tvInterestEarned = view.findViewById(R.id.tvInterestEarned);
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateUI() {
        if (getContext() == null)
            return;

        // Update Summary Cards
        double outstanding = repository.getTotalOutstanding();
        double interest = repository.getTotalInterestEarned();
        List<Loan> pendingLoans = repository.getPendingLoans();

        tvTotalOutstanding.setText(String.format(Locale.getDefault(), "UGX %,.0f", outstanding));
        tvInterestEarned.setText(String.format(Locale.getDefault(), "UGX %,.0f", interest));
        tvPendingCount.setText(String.valueOf(pendingLoans.size()));

        // Build List Items
        List<Object> items = new ArrayList<>();

        if (!pendingLoans.isEmpty()) {
            items.add("Pending Requests");
            items.addAll(pendingLoans);
        }

        List<Loan> activeLoans = repository.getActiveLoans();
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
                    repository.approveLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Approved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReject(Loan loan) {
                    repository.rejectLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Rejected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRemind(Loan loan) {
                    Toast.makeText(getContext(), "Reminder sent to " + loan.getMemberName(), Toast.LENGTH_SHORT).show();
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            // Re-create adapter to refresh list (simplified for now)
            adapter = new LoansAdapter(items, new LoansAdapter.LoanActionListener() {
                @Override
                public void onApprove(Loan loan) {
                    repository.approveLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Approved", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReject(Loan loan) {
                    repository.rejectLoan(loan.getId());
                    Toast.makeText(getContext(), "Loan Rejected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onRemind(Loan loan) {
                    Toast.makeText(getContext(), "Reminder sent to " + loan.getMemberName(), Toast.LENGTH_SHORT).show();
                }
            });
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onLoansChanged() {
        updateUI();
    }
}
