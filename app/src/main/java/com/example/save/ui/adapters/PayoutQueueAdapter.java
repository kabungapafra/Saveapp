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

    /**
     * @param members      List of members to display
     * @param isFullQueue  If true, show payout status (paid/estimated); if false,
     *                     show relative estimates (This Month/Next Month)
     * @param payoutAmount The amount to be paid out
     */
    public PayoutQueueAdapter(List<Member> members, boolean isFullQueue, double payoutAmount) {
        this.members = members;
        this.isFullQueue = isFullQueue;
        this.payoutAmount = payoutAmount;
    }

    public void updateList(List<Member> newList) {
        this.members = newList;
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

        if (isFullQueue) {
            if (member.hasReceivedPayout()) {
                holder.date.setText("Paid on: " + member.getPayoutDate());
                holder.date.setTextColor(
                        androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                                android.R.color.holo_green_dark));
                holder.amount.setText("Paid");
            } else {
                holder.date.setText("Receiving: " + member.getNextPayoutDate());
                holder.date.setTextColor(
                        androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                                android.R.color.darker_gray));
                holder.amount.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(payoutAmount));
            }
        } else {
            // Upcoming mode (Top 3)
            holder.date.setText("Receiving: " + member.getNextPayoutDate());
            holder.date.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                    android.R.color.darker_gray));
            holder.amount.setText("UGX " + java.text.NumberFormat.getIntegerInstance().format(payoutAmount));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(member);
            }
        });
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
