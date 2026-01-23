package com.example.save.ui.adapters;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.databinding.ItemActivityBinding;
import java.util.List;
import java.util.Locale;
import com.example.save.data.models.ActivityModel;

public class AnalyticsActivityAdapter extends RecyclerView.Adapter<AnalyticsActivityAdapter.ViewHolder> {

    private List<ActivityModel> activities;

    public AnalyticsActivityAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemActivityBinding binding = ItemActivityBinding.inflate(LayoutInflater.from(parent.getContext()), parent,
                false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);
        holder.tvTitle.setText(activity.getTitle());
        holder.tvDate.setText(activity.getDate());

        String prefix = activity.isPositive() ? "+ " : "- ";
        int color = activity.isPositive()
                ? androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                        android.R.color.holo_green_dark)
                : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                        android.R.color.holo_red_dark);

        holder.tvAmount.setText(prefix + String.format(Locale.getDefault(), "UGX %,.0f", activity.getAmount()));
        holder.tvAmount.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemActivityBinding binding;
        TextView tvTitle, tvDate, tvAmount;

        ViewHolder(ItemActivityBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            tvTitle = binding.activityTitle;
            tvDate = binding.activityDate;
            tvAmount = binding.activityAmount;
        }
    }
}
