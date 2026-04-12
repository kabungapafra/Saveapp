package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public static class DisplayItem {
        public int type;
        public String headerTitle;
        public Notification notification;

        public DisplayItem(String headerTitle) {
            this.type = TYPE_HEADER;
            this.headerTitle = headerTitle;
        }

        public DisplayItem(Notification notification) {
            this.type = TYPE_ITEM;
            this.notification = notification;
        }
    }

    private List<DisplayItem> items = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.items.clear();
        if (notifications != null && !notifications.isEmpty()) {
            // Simple sectioning logic for the demo/app
            long now = System.currentTimeMillis();
            boolean addedToday = false;
            boolean addedYesterday = false;
            boolean addedEarlier = false;

            for (Notification n : notifications) {
                long diff = now - n.getTimestamp();
                if (diff < 86400000 && !addedToday) {
                    items.add(new DisplayItem("TODAY"));
                    addedToday = true;
                } else if (diff >= 86400000 && diff < 172800000 && !addedYesterday) {
                    items.add(new DisplayItem("YESTERDAY"));
                    addedYesterday = true;
                } else if (diff >= 172800000 && !addedEarlier) {
                    items.add(new DisplayItem("EARLIER"));
                    addedEarlier = true;
                }
                items.add(new DisplayItem(n));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeader.setText(item.headerTitle);
        } else if (holder instanceof NotificationViewHolder) {
            ((NotificationViewHolder) holder).bind(item.notification);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = (TextView) itemView;
        }
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        ImageView icon;
        View readIndicator, iconContainer;
        LinearLayout layoutActions;
        View btnViewDetails, btnDismiss;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            message = itemView.findViewById(R.id.tvMessage);
            time = itemView.findViewById(R.id.tvTime);
            icon = itemView.findViewById(R.id.ivIcon);
            readIndicator = itemView.findViewById(R.id.readIndicator);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnDismiss = itemView.findViewById(R.id.btnDismiss);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    Notification n = items.get(position).notification;
                    if (n != null) listener.onNotificationClick(n);
                }
            });
        }

        void bind(Notification n) {
            title.setText(n.getTitle());
            message.setText(n.getMessage());

            // Format time relative
            long diff = System.currentTimeMillis() - n.getTimestamp();
            String timeStr;
            if (diff < 60000) timeStr = "Just now";
            else if (diff < 3600000) timeStr = (diff / 60000) + " minutes ago";
            else if (diff < 86400000) timeStr = (diff / 3600000) + " hours ago";
            else if (diff < 172800000) timeStr = "Yesterday";
            else timeStr = (diff / 86400000) + " days ago";

            time.setText(timeStr);

            if (n.isRead()) {
                readIndicator.setVisibility(View.GONE);
            } else {
                readIndicator.setVisibility(View.VISIBLE);
            }

            // Type-specific styling
            String type = n.getType() != null ? n.getType() : "";
            layoutActions.setVisibility(View.GONE);

            switch (type) {
                case "LOAN_REQUEST":
                    icon.setImageResource(R.drawable.ic_handshake_blue);
                    iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_blue_light);
                    icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#3B82F6")));
                    layoutActions.setVisibility(View.VISIBLE);
                    break;
                case "DEPOSIT":
                    icon.setImageResource(R.drawable.ic_payments);
                    iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_green_light);
                    icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#10B981")));
                    break;
                case "REMINDER":
                    icon.setImageResource(R.drawable.ic_calendar);
                    iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_orange_light);
                    icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F59E0B")));
                    break;
                case "MEMBER_JOINED":
                    icon.setImageResource(R.drawable.ic_person_add_premium);
                    iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_blue_light);
                    icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#6366F1")));
                    break;
                case "SUMMARY":
                    icon.setImageResource(R.drawable.ic_analytics);
                    iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_gray_light);
                    icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#64748B")));
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_notifications);
                    iconContainer.setBackgroundResource(R.drawable.bg_icon_circle_blue_light);
                    icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#3B82F6")));
                    break;
            }
        }
    }
}
