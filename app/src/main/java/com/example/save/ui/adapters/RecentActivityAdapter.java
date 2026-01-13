package com.example.save.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.RecentActivityModel;
import com.example.save.databinding.ItemActivityBinding;

import java.util.List;

/**
 * Adapter for the recent activity list in AdminMainActivity
 */
public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ActivityViewHolder> {
    private List<RecentActivityModel> list;

    public RecentActivityAdapter(List<RecentActivityModel> list) {
        this.list = list;
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
        RecentActivityModel item = list.get(position);
        holder.title.setText(item.title);
        holder.date.setText(item.description);
        holder.amount.setText(item.amount);

        if (item.isPositive) {
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
