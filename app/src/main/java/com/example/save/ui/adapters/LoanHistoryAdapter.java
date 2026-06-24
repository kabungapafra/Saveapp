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

    private List<com.example.save.data.models.LoanWithApproval> loans = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private int adminCount = 0;

    public void setLoans(List<com.example.save.data.models.LoanWithApproval> loans) {
        this.loans = loans != null ? loans : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setAdminCount(int count) {
        this.adminCount = count;
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
        com.example.save.data.models.LoanWithApproval item = loans.get(position);
        com.example.save.data.models.LoanEntity loan = item.loan;

        String amountText = String.format(Locale.getDefault(), "UGX %,.0f", loan.getAmount());
        holder.tvAmount.setText(amountText);

        if (loan.getDateRequested() != null) {
            holder.tvDate.setText(dateFormat.format(loan.getDateRequested()));
        } else {
            holder.tvDate.setText("Unknown Date");
        }

        // Status badge
        String status = loan.getStatus() != null ? loan.getStatus().toUpperCase() : "";
        switch (status) {
            case "PENDING":
                holder.tvPaidBadge.setText("Pending (" + item.approvalCount + "/" + adminCount + ")");
                holder.tvPaidBadge.setTextColor(0xFFD97706);
                holder.tvPaidBadge.setBackgroundResource(R.drawable.badge_background_pending);
                break;
            case "APPROVED":
                holder.tvPaidBadge.setText("Approved");
                holder.tvPaidBadge.setTextColor(0xFF2563EB);
                holder.tvPaidBadge.setBackgroundResource(R.drawable.badge_background_pending);
                break;
            case "DISBURSED":
            case "ACTIVE":
                holder.tvPaidBadge.setText("Active");
                holder.tvPaidBadge.setTextColor(0xFF16A34A);
                holder.tvPaidBadge.setBackgroundResource(R.drawable.bg_badge_success);
                break;
            case "LATE":
            case "OVERDUE":
                holder.tvPaidBadge.setText("Overdue");
                holder.tvPaidBadge.setTextColor(0xFFDC2626);
                holder.tvPaidBadge.setBackgroundResource(R.drawable.bg_badge_red);
                break;
            case "REPAID":
            case "PAID":
                holder.tvPaidBadge.setText("Paid Off");
                holder.tvPaidBadge.setTextColor(0xFF16A34A);
                holder.tvPaidBadge.setBackgroundResource(R.drawable.bg_badge_success);
                break;
            default:
                holder.tvPaidBadge.setText(status.isEmpty() ? "Unknown" : status);
                holder.tvPaidBadge.setTextColor(0xFF6B7280);
                holder.tvPaidBadge.setBackgroundResource(R.drawable.badge_background_pending);
                break;
        }
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
