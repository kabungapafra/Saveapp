package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.utils.SessionManager;

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

    public void setPayoutAmount(double payoutAmount) {
        this.payoutAmount = payoutAmount;
        notifyDataSetChanged();
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payout_row, parent, false);
        return new QueueViewHolder(v);
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
        boolean paid = member.hasReceivedPayout();

        // Avatar: photo if saved for this member's phone, else initials
        String name = member.getName() != null ? member.getName() : "?";
        android.content.Context ctx = holder.itemView.getContext();
        String photoPath = SessionManager.getInstance(ctx).getProfileImage(member.getPhone());

        if (photoPath != null && !photoPath.isEmpty()) {
            holder.avatarInitials.setVisibility(View.GONE);
            holder.avatarPhoto.setVisibility(View.VISIBLE);
            Glide.with(ctx)
                    .load(photoPath)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(holder.avatarPhoto);
        } else {
            holder.avatarPhoto.setVisibility(View.GONE);
            holder.avatarInitials.setVisibility(View.VISIBLE);
            String initial = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();
            holder.avatarInitials.setText(initial);
            if (paid) {
                holder.avatarInitials.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFDCFCE7));
                holder.avatarInitials.setTextColor(0xFF16A34A);
            } else {
                holder.avatarInitials.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFE8F4FD));
                holder.avatarInitials.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), com.example.save.R.color.dashboard_text_primary));
            }
        }

        holder.title.setText(name);

        if (paid) {
            holder.subtitle.setText("Paid on: " + member.getPayoutDate());
            holder.amount.setText("Paid");
            holder.amount.setTextColor(0xFF2E7D32);
        } else {
            holder.subtitle.setText("Receiving: " + calculatePayoutDate(position, holder.itemView.getContext()));
            String amtStr = "UGX " + java.text.NumberFormat.getIntegerInstance().format(payoutAmount);
            holder.amount.setText(amtStr);
            holder.amount.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), com.example.save.R.color.dashboard_text_primary));
        }

        holder.divider.setVisibility(position == members.size() - 1 ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(member);
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
        TextView avatarInitials;
        ImageView avatarPhoto;
        TextView title, subtitle, amount;
        View divider;

        QueueViewHolder(@NonNull View v) {
            super(v);
            avatarInitials = v.findViewById(R.id.tvAvatarInitials);
            avatarPhoto    = v.findViewById(R.id.ivAvatarPhoto);
            title          = v.findViewById(R.id.tvRowTitle);
            subtitle       = v.findViewById(R.id.tvRowSubtitle);
            amount         = v.findViewById(R.id.tvRowAmount);
            divider        = v.findViewById(R.id.viewRowDivider);
        }
    }
}
