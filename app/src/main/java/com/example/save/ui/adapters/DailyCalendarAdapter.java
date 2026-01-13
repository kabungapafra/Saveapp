package com.example.save.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.DateItem;
import com.example.save.databinding.ItemCalendarDateBinding;

import java.util.List;

/**
 * Adapter for the horizontal calendar in DailyTasksActivity
 */
public class DailyCalendarAdapter extends RecyclerView.Adapter<DailyCalendarAdapter.DateViewHolder> {
    private List<DateItem> dates;
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(DateItem item);
    }

    public DailyCalendarAdapter(List<DateItem> dates, OnDateSelectedListener listener) {
        this.dates = dates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDateBinding itemBinding = ItemCalendarDateBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DateViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem item = dates.get(position);
        holder.dayText.setText(item.day);
        holder.dateText.setText(item.date);

        if (item.isSelected) {
            holder.container.setBackgroundResource(R.drawable.date_selector_bg);
            holder.container.setSelected(true);
            holder.dayText.setTextColor(Color.WHITE);
            holder.dateText.setTextColor(Color.WHITE);
        } else {
            holder.container.setBackgroundResource(R.drawable.date_selector_bg);
            holder.container.setSelected(false);
            holder.dayText.setTextColor(Color.parseColor("#666666")); // Gray
            holder.dateText.setTextColor(Color.parseColor("#1A1A1A")); // Black
        }

        holder.itemView.setOnClickListener(v -> {
            for (DateItem d : dates)
                d.isSelected = false;
            item.isSelected = true;
            notifyDataSetChanged();

            if (listener != null) {
                listener.onDateSelected(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        TextView dayText, dateText;

        DateViewHolder(ItemCalendarDateBinding itemBinding) {
            super(itemBinding.getRoot());
            container = itemBinding.dateContent;
            dayText = itemBinding.dayText;
            dateText = itemBinding.dateText;
        }
    }
}
