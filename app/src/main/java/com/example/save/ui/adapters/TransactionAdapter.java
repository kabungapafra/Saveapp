package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public TransactionAdapter(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
    }

    public void updateTransactions(List<Transaction> newTransactions) {
        this.transactions = newTransactions != null ? newTransactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.tvType.setText(transaction.getType());
        holder.tvDate.setText(transaction.getDate());

        // Format amount with +/- prefix
        String amountText = String.format(Locale.getDefault(), "%s %,.0f",
                transaction.isCredit() ? "+" : "-",
                Math.abs(transaction.getAmount()));
        holder.tvAmount.setText(amountText);

        // Set amount color (green for credit, red for debit)
        int amountColor = transaction.isCredit() ? 0xFF4CAF50 : 0xFFF44336;
        holder.tvAmount.setTextColor(amountColor);

        // Set icon and background
        holder.ivIcon.setImageResource(transaction.getIconRes());
        holder.ivIcon.setBackgroundColor(transaction.getIconBackgroundColor());

        // Set icon tint based on transaction type
        int iconTint = transaction.isCredit() ? 0xFF4CAF50 : 0xFFF44336;
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(iconTint));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvAmount;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvTransactionType);
            tvDate = itemView.findViewById(R.id.tvTransactionDate);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivIcon = itemView.findViewById(R.id.ivTransactionIcon);
        }
    }
}
