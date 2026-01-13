package com.example.save.ui.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.DateItem;
import com.example.save.databinding.ItemCalendarDateBinding;
import com.example.save.ui.activities.DailyTasksActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the horizontal date picker in AdminMainActivity
 */
public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private List<DateItem> dates;

    public DateAdapter(List<DateItem> dates) {
        this.dates = dates;
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
            holder.container.setBackground(null);
            holder.container.setSelected(false);
            holder.dayText.setTextColor(Color.parseColor("#9E9E9E"));
            holder.dateText.setTextColor(Color.parseColor("#1A1A1A"));
        }

        holder.itemView.setOnClickListener(v -> {
            // Update selection visually
            for (DateItem d : dates)
                d.isSelected = false;
            item.isSelected = true;
            notifyDataSetChanged();

            // Navigate to DailyTasksActivity with selected date
            Intent intent = new Intent(holder.itemView.getContext(), DailyTasksActivity.class);
            SimpleDateFormat fullDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
            String formattedDate = fullDateFormat.format(item.calendar.getTime());
            intent.putExtra("selected_date", formattedDate);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        android.widget.LinearLayout container;
        TextView dayText, dateText;

        DateViewHolder(ItemCalendarDateBinding itemBinding) {
            super(itemBinding.getRoot());
            container = itemBinding.dateContent;
            dayText = itemBinding.dayText;
            dateText = itemBinding.dateText;
        }
    }
}
