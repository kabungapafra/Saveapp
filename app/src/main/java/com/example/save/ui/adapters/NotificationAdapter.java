package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        ImageView icon;
        View readIndicator;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            message = itemView.findViewById(R.id.tvMessage);
            time = itemView.findViewById(R.id.tvTime);
            icon = itemView.findViewById(R.id.ivIcon);
            readIndicator = itemView.findViewById(R.id.readIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        void bind(Notification n) {
            title.setText(n.getTitle());
            message.setText(n.getMessage());

            // Format time relative
            long diff = System.currentTimeMillis() - n.getTimestamp();
            String timeStr;
            if (diff < 60000)
                timeStr = "Just now";
            else if (diff < 3600000)
                timeStr = (diff / 60000) + "m ago";
            else if (diff < 86400000)
                timeStr = (diff / 3600000) + "h ago";
            else
                timeStr = (diff / 86400000) + "d ago";

            time.setText(timeStr);

            if (n.isRead()) {
                readIndicator.setVisibility(View.GONE);
                title.setTypeface(null, android.graphics.Typeface.NORMAL);
            } else {
                readIndicator.setVisibility(View.VISIBLE);
                title.setTypeface(null, android.graphics.Typeface.BOLD);
            }

            // Icon based on type
            if ("ALERT".equals(n.getType())) {
                icon.setImageResource(R.drawable.ic_warning); // Assuming exist or use similar
                icon.setColorFilter(android.graphics.Color.parseColor("#F44336")); // Red
            } else if ("PAYOUT".equals(n.getType())) {
                icon.setImageResource(R.drawable.ic_send_money);
                icon.setColorFilter(android.graphics.Color.parseColor("#4CAF50")); // Green
            } else {
                icon.setImageResource(R.drawable.ic_notifications);
                icon.setColorFilter(android.graphics.Color.parseColor("#2196F3")); // Blue
            }
        }
    }
}
