package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.TaskModel; // Reusing TaskModel as a DTO for payments temporarily

import java.util.ArrayList;
import java.util.List;

public class UpcomingPaymentAdapter extends RecyclerView.Adapter<UpcomingPaymentAdapter.ViewHolder> {

    private List<TaskModel> payments = new ArrayList<>();

    public UpcomingPaymentAdapter(List<TaskModel> payments) {
        this.payments = payments;
    }

    public void updateList(List<TaskModel> newPayments) {
        this.payments = newPayments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TaskModel payment = payments.get(position);

        holder.tvTitle.setText(payment.title);
        holder.tvAmount.setText(payment.subtitle); // Storing Amount in subtitle field for reuse
        holder.tvStatus.setText(payment.status);

        // Icon logic
        if (payment.title.contains("Loan")) {
            holder.ivIcon.setImageResource(R.drawable.ic_loan);
            holder.iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFEBEE)); // Light
                                                                                                                // Red
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFF44336)); // Red
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_calendar_month);
            holder.iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8EAF6)); // Light
                                                                                                                // Blue
            holder.ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF3F51B5)); // Blue
        }
        // Urgency Logic
        String status = payment.status.toLowerCase();
        int pillColor = 0xFFE0F7FA; // Default Cyan-ish
        int textColor = 0xFF006064;

        if (status.contains("due now") || status.contains("today") || status.contains("tomorrow")
                || status.contains("1 day")) {
            pillColor = 0xFFFFEBEE; // Red Light
            textColor = 0xFFD32F2F; // Red
        } else if (status.contains("2 days") || status.contains("3 days")) {
            pillColor = 0xFFFFF3E0; // Orange Light
            textColor = 0xFFEF6C00; // Orange
        } else if (status.contains("great") || status.contains("relax")) {
            pillColor = 0xFFE8F5E9; // Green Light
            textColor = 0xFF2E7D32; // Green
        }

        holder.tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pillColor));
        holder.tvStatus.setTextColor(textColor);

        // Ensure icon tint matches text for consistency if using drawables
        for (android.graphics.drawable.Drawable d : holder.tvStatus.getCompoundDrawablesRelative()) {
            if (d != null) {
                d.setTint(textColor);
            }
        }
    }

    @Override
    public int getItemCount() {
        return payments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAmount, tvStatus;
        ImageView ivIcon;
        View iconContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            iconContainer = itemView.findViewById(R.id.iconContainer);
        }
    }
}
