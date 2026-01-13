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

    /**
     * @param members     List of members to display
     * @param isFullQueue If true, show payout status (paid/estimated); if false,
     *                    show relative estimates (This Month/Next Month)
     */
    public PayoutQueueAdapter(List<Member> members, boolean isFullQueue) {
        this.members = members;
        this.isFullQueue = isFullQueue;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPayoutQueueBinding itemBinding = ItemPayoutQueueBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new QueueViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        Member member = members.get(position);
        holder.rank.setText(String.valueOf(position + 1));
        holder.name.setText(member.getName());

        if (isFullQueue) {
            if (member.hasReceivedPayout()) {
                holder.date.setText("Paid on: " + member.getPayoutDate());
                holder.date.setTextColor(
                        holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                holder.amount.setText("Paid");
            } else {
                holder.date.setText("Estimated: Future Date");
                holder.date.setTextColor(
                        holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                holder.amount.setText("500k");
            }
        } else {
            // Upcoming mode (Top 3)
            holder.date.setText("Estimated: " + (position == 0 ? "This Month" : "Next Month"));
            holder.date.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            holder.amount.setText("500k");
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
