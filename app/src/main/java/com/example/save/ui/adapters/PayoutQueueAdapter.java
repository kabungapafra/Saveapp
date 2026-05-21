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
    private double payoutAmount;
    private String basePayoutDate;

    public PayoutQueueAdapter(List<Member> members) {
        this.members = members;
        this.payoutAmount = 0.0;
        this.basePayoutDate = "TBD";
    }

    public PayoutQueueAdapter(List<Member> members, double payoutAmount, String basePayoutDate) {
        this.members = members;
        this.payoutAmount = payoutAmount;
        this.basePayoutDate = basePayoutDate;
    }

    public PayoutQueueAdapter(List<Member> members, boolean isFullQueue, double payoutAmount, String basePayoutDate) {
        this.members = members;
        this.payoutAmount = payoutAmount;
        this.basePayoutDate = basePayoutDate;
    }

    public void updateList(List<Member> newList) {
        this.members = newList;
        notifyDataSetChanged();
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
            displayDate = "Receiving: " + calculatePayoutDate(position, holder.itemView.getContext());
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

    private String calculatePayoutDate(int position, android.content.Context context) {
        if (basePayoutDate == null || basePayoutDate.isEmpty() || basePayoutDate.contains("Not")
                || basePayoutDate.equals("TBD")) {
            return "TBD";
        }
        
        android.content.SharedPreferences prefs = context.getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
        String frequency = prefs.getString("rule_frequency", "Monthly");
        String recipientsStr = prefs.getString("rule_edit_recipients", "1 Member").replaceAll("[^0-9]", "");
        int recipients = 1;
        try {
            recipients = Integer.parseInt(recipientsStr);
            if (recipients < 1) recipients = 1;
        } catch (NumberFormatException e) {
            recipients = 1;
        }

        int cyclesToAdd = position / recipients;

        if (cyclesToAdd == 0) return basePayoutDate;

        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(basePayoutDate);
            if (date == null) return basePayoutDate;

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);

            for (int i = 0; i < cyclesToAdd; i++) {
                switch (frequency) {
                    case "Daily": cal.add(java.util.Calendar.DAY_OF_YEAR, 1); break;
                    case "Weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); break;
                    case "Bi-weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 2); break;
                    case "Monthly": cal.add(java.util.Calendar.MONTH, 1); break;
                    case "Every 2 Months": cal.add(java.util.Calendar.MONTH, 2); break;
                    case "Every 3 Months": cal.add(java.util.Calendar.MONTH, 3); break;
                    case "Every 4 Months": cal.add(java.util.Calendar.MONTH, 4); break;
                    case "Every 5 Months": cal.add(java.util.Calendar.MONTH, 5); break;
                    case "Every 6 Months": cal.add(java.util.Calendar.MONTH, 6); break;
                }
            }

            return sdf.format(cal.getTime());
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
