package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.save.R;
import com.example.save.data.models.PayoutQueueEntry;
import com.example.save.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PayoutQueueAdapter extends RecyclerView.Adapter<PayoutQueueAdapter.QueueViewHolder> {

    private List<PayoutQueueEntry> entries;
    private double payoutAmount;
    private boolean isAdmin;
    private ItemTouchHelper touchHelper;

    public interface OnReorderListener {
        void onReordered(List<PayoutQueueEntry> newOrder);
    }

    private OnReorderListener reorderListener;

    public PayoutQueueAdapter(List<PayoutQueueEntry> entries, double payoutAmount, boolean isAdmin) {
        this.entries = entries != null ? entries : new ArrayList<>();
        this.payoutAmount = payoutAmount;
        this.isAdmin = isAdmin;
    }

    public void setTouchHelper(ItemTouchHelper helper) {
        this.touchHelper = helper;
    }

    public void setOnReorderListener(OnReorderListener listener) {
        this.reorderListener = listener;
    }

    public void setPayoutAmount(double payoutAmount) {
        this.payoutAmount = payoutAmount;
        notifyDataSetChanged();
    }

    public void updateEntries(List<PayoutQueueEntry> newEntries) {
        this.entries = newEntries != null ? newEntries : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<PayoutQueueEntry> getEntries() {
        return entries;
    }

    public void onItemMoved(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) Collections.swap(entries, i, i + 1);
        } else {
            for (int i = from; i > to; i--) Collections.swap(entries, i, i - 1);
        }
        notifyItemMoved(from, to);
    }

    public void onDropFinished() {
        // Reassign positions 1..N
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setPosition(i + 1);
        }
        if (reorderListener != null) reorderListener.onReordered(new ArrayList<>(entries));
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payout_row, parent, false);
        return new QueueViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        PayoutQueueEntry entry = entries.get(position);
        boolean paid = entry.hasReceivedPayout();

        String name = entry.getMemberName() != null ? entry.getMemberName() : "?";
        android.content.Context ctx = holder.itemView.getContext();
        String photoPath = SessionManager.getInstance(ctx).getProfileImage(entry.getMemberPhone());

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
                holder.avatarInitials.setTextColor(
                        androidx.core.content.ContextCompat.getColor(ctx, R.color.dashboard_text_primary));
            }
        }

        holder.paidBadge.setVisibility(paid ? View.VISIBLE : View.GONE);
        holder.title.setText(name);

        if (paid) {
            String when = entry.getActualPayoutDate() != null ? formatShortDate(entry.getActualPayoutDate()) : "Done";
            holder.subtitle.setText("Paid on: " + when);
            holder.amount.setText("Received");
            holder.amount.setTextColor(0xFF10B981);
        } else {
            holder.subtitle.setText("Score: " + (int) entry.getCreditScore());
            String amtStr = "UGX " + java.text.NumberFormat.getIntegerInstance().format(payoutAmount);
            holder.amount.setText(amtStr);
            holder.amount.setTextColor(
                    androidx.core.content.ContextCompat.getColor(ctx, R.color.dashboard_text_primary));
        }

        // Show drag handle only for admins on unpaid items
        if (isAdmin && !paid) {
            holder.dragHandle.setVisibility(View.VISIBLE);
            holder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN && touchHelper != null) {
                    touchHelper.startDrag(holder);
                }
                return false;
            });
        } else {
            holder.dragHandle.setVisibility(View.GONE);
        }

        holder.divider.setVisibility(position == entries.size() - 1 ? View.GONE : View.VISIBLE);
    }

    private String formatShortDate(String iso) {
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            java.util.Date d = in.parse(iso);
            if (d == null) return iso.substring(0, 10);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("MMM d", java.util.Locale.US);
            return out.format(d);
        } catch (Exception e) {
            return iso.length() >= 10 ? iso.substring(0, 10) : iso;
        }
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class QueueViewHolder extends RecyclerView.ViewHolder {
        TextView avatarInitials, title, subtitle, amount;
        ImageView avatarPhoto, paidBadge, dragHandle;
        View divider;

        QueueViewHolder(@NonNull View v) {
            super(v);
            avatarInitials = v.findViewById(R.id.tvAvatarInitials);
            avatarPhoto    = v.findViewById(R.id.ivAvatarPhoto);
            paidBadge      = v.findViewById(R.id.ivPaidBadge);
            title          = v.findViewById(R.id.tvRowTitle);
            subtitle       = v.findViewById(R.id.tvRowSubtitle);
            amount         = v.findViewById(R.id.tvRowAmount);
            dragHandle     = v.findViewById(R.id.ivDragHandle);
            divider        = v.findViewById(R.id.viewRowDivider);
        }
    }
}
