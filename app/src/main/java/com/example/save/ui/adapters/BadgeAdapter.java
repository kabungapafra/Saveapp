package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Badge;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private List<Badge> badgeList;

    public BadgeAdapter(List<Badge> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_badge_item, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge badge = badgeList.get(position);
        holder.tvName.setText(badge.getName());
        holder.imgIcon.setImageResource(badge.getIconResId());

        if (badge.isUnlocked()) {
            holder.imgIcon.setAlpha(1.0f);
            holder.tvName.setTextColor(0xFF000000); // Black
        } else {
            holder.imgIcon.setAlpha(0.3f); // Dimmed
            holder.tvName.setTextColor(0xFF9E9E9E); // Grey
        }
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgBadgeIcon);
            tvName = itemView.findViewById(R.id.tvBadgeName);
        }
    }
}
