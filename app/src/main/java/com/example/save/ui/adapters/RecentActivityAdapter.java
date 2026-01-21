package com.example.save.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.databinding.ItemActivityBinding;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the recent activity list in AdminMainActivity
 */
public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {
    private List<TransactionEntity> list;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public RecentActivityAdapter(List<TransactionEntity> list) {
        this.list = list;
    }

    public void updateList(List<TransactionEntity> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActivityBinding itemBinding = ItemActivityBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ActivityViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        TransactionEntity item = list.get(position);
        holder.title.setText(item.getDescription());
        holder.date.setText(dateFormat.format(item.getDate()));

        String formattedAmount = java.text.NumberFormat.getCurrencyInstance(new Locale("en", "UG"))
                .format(item.getAmount());
        holder.amount.setText(formattedAmount);

        if (item.isPositive()) {
            holder.amount.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.amount.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, amount;

        ActivityViewHolder(ItemActivityBinding itemBinding) {
            super(itemBinding.getRoot());
            title = itemBinding.activityTitle;
            date = itemBinding.activityDate;
            amount = itemBinding.activityAmount;
        }
    }
}
