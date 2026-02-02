package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.Member;
import com.example.save.databinding.ItemPayoutQueueBinding;

import java.util.List;

/**
 * Consolidated adapter for the payout queue (upcoming and full queue)
 */
public class PayoutQueueAdapter extends RecyclerView.Adapter<PayoutQueueAdapter.QueueViewHolder> {
    private List<Member> members;
    private boolean isFullQueue;
    private double payoutAmount;
    private String basePayoutDate;

    public PayoutQueueAdapter(List<Member> members, boolean isFullQueue, double payoutAmount, String basePayoutDate) {
        this.members = members;
        this.isFullQueue = isFullQueue;
        this.payoutAmount = payoutAmount;
        this.basePayoutDate = basePayoutDate;
    }

    public void updateList(List<Member> newList, String basePayoutDate) {
        this.members = newList;
        this.basePayoutDate = basePayoutDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPayoutQueueBinding itemBinding = ItemPayoutQueueBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QueueViewHolder(itemBinding);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Member member);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Member member = members.get(position);
        holder.rank.setText(String.valueOf(position + 1));
        holder.name.setText(member.getName());

        String displayDate;
        if (member.hasReceivedPayout()) {
            displayDate = "Paid on: " + member.getPayoutDate();
            holder.date.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.holo_green_dark));
            holder.amount.setText("Paid");
        } else {
            displayDate = "Receiving: " + calculatePayoutDate(position);
            holder.date.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.darker_gray));
            holder.amount.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(payoutAmount));
        }
        holder.date.setText(displayDate);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(member);
            }
        });
    }

    private String calculatePayoutDate(int position) {
        if (basePayoutDate == null || basePayoutDate.isEmpty() || basePayoutDate.contains("Not")
                || basePayoutDate.equals("TBD")) {
            return "TBD";
        }
        try {
            String[] parts = basePayoutDate.split("/");
            if (parts.length != 3)
                return basePayoutDate;

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // 0-indexed month
            int year = Integer.parseInt(parts[2]);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, day);
            cal.add(java.util.Calendar.MONTH, position);

            return cal.get(java.util.Calendar.DAY_OF_MONTH) + "/" +
                    (cal.get(java.util.Calendar.MONTH) + 1) + "/" +
                    cal.get(java.util.Calendar.YEAR);
        } catch (Exception e) {
            return basePayoutDate;
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView rank, name, date, amount;

        QueueViewHolder(ItemPayoutQueueBinding itemBinding) {
            super(itemBinding.getRoot());
            rank = itemBinding.tvRank;
            name = itemBinding.tvName;
            date = itemBinding.tvDate;
            amount = itemBinding.tvAmount;
        }
    }
}
