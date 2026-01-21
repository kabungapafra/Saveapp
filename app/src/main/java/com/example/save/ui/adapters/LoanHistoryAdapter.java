package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Loan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoanHistoryAdapter extends RecyclerView.Adapter<LoanHistoryAdapter.ViewHolder> {

    private List<Loan> loans = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public void setLoans(List<Loan> loans) {
        this.loans = loans != null ? loans : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loan_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Loan loan = loans.get(position);

        String amountText = String.format(Locale.getDefault(), "UGX %,.0f", loan.getAmount());
        holder.tvAmount.setText(amountText);

        if (loan.getDateRequested() != null) {
            holder.tvDate.setText(dateFormat.format(loan.getDateRequested()));
        } else {
            holder.tvDate.setText("Unknown Date");
        }

        // Ensure badge is always "PAID OFF" for this specific list as requested,
        // but we can also check status if we want to reuse adapter.
        // The layout has it hardcoded, but we can manage visibility or text here.
        holder.tvPaidBadge.setText("PAID OFF");
    }

    @Override
    public int getItemCount() {
        return loans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvDate, tvPaidBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvLoanAmount);
            tvDate = itemView.findViewById(R.id.tvLoanDate);
            tvPaidBadge = itemView.findViewById(R.id.tvPaidBadge);
        }
    }
}
