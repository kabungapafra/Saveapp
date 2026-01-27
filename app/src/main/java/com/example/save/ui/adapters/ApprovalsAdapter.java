package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApprovalsAdapter extends RecyclerView.Adapter<ApprovalsAdapter.ApprovalViewHolder> {
    public interface ApprovalItem {
        long getId();

        String getType();

        String getTitle();

        double getAmount();

        String getDescription();

        java.util.Date getDate();

        String getStatus();

        boolean hasApproved();
    }

    private List<ApprovalItem> items = new ArrayList<>();
    private final OnApprovalClickListener listener;
    private int requiredApprovals = 1;

    public interface OnApprovalClickListener {
        void onApproveClick(ApprovalItem item);
    }

    public ApprovalsAdapter(OnApprovalClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<ApprovalItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApprovalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval, parent, false);
        return new ApprovalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApprovalViewHolder holder, int position) {
        ApprovalItem item = items.get(position);

        holder.tvType.setText(item.getType());
        holder.tvTitle.setText(item.getTitle());
        holder.tvAmount.setText("UGX " + NumberFormat.getIntegerInstance().format(item.getAmount()));
        holder.tvDescription.setText(item.getDescription());
        holder.tvDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(item.getDate()));

        String status = item.getStatus();
        holder.tvApprovalStatus.setText(status);

        if (item.hasApproved() || "COMPLETED".equals(status)) {
            holder.tvApprovalStatus.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            holder.btnApprove.setVisibility(View.GONE);
        } else {
            holder.tvApprovalStatus
                    .setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_secondary));
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnApprove.setOnClickListener(v -> listener.onApproveClick(item));
        }

        // Styling based on type
        if ("LOAN".equals(item.getType())) {
            holder.tvType.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.deep_blue));
        } else {
            holder.tvType.setTextColor(
                    holder.itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ApprovalViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTitle, tvAmount, tvDescription, tvDate, tvApprovalStatus;
        View btnApprove;

        public ApprovalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvApprovalStatus = itemView.findViewById(R.id.tvApprovalStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
