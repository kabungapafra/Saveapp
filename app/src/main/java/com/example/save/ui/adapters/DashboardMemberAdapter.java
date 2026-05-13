package com.example.save.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import com.example.save.data.models.Member;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardMemberAdapter extends RecyclerView.Adapter<DashboardMemberAdapter.ViewHolder> {

    private List<Member> members = new ArrayList<>();
    private String currentUserEmail = "";

    private static final String[] AVATAR_COLORS = {
        "#FBBF24", "#F87171", "#A78BFA", "#34D399", "#60A5FA", "#F472B6", "#FCA5A5", "#6EE7B7"
    };

    public DashboardMemberAdapter(Context context) {
        // Empty — data will be loaded via setMembers()
    }

    public void setMembers(List<Member> members, String currentUserEmail) {
        this.members = members != null ? members : new ArrayList<>();
        this.currentUserEmail = currentUserEmail != null ? currentUserEmail : "";
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_status_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = members.get(position);
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(holder.itemView.getContext());
        String currentUserPhone = session.getUserPhone();
        boolean isSelf = (member.getEmail() != null && !member.getEmail().isEmpty() && member.getEmail().equalsIgnoreCase(currentUserEmail))
                || (member.getPhone() != null && !member.getPhone().isEmpty() && member.getPhone().replaceAll("\\s+", "").equalsIgnoreCase(currentUserPhone.replaceAll("\\s+", "")));

        // Name & Initials
        holder.tvName.setText(isSelf ? member.getName() + " (You)" : member.getName());
        holder.tvInitials.setText(getInitials(member.getName()));

        // Turn Info
        holder.tvTurn.setText(String.format(Locale.getDefault(), "Turn #%d", position + 1));

        // Status from server-authoritative reliability label
        String label = member.getReliabilityLabel();
        String color = member.getReliabilityColor();
        holder.tvStatus.setText(label);

        // Avatar image/initials
        String savedImage = isSelf ? session.getProfileImage() : null;
        if (savedImage != null) {
            holder.tvInitials.setVisibility(View.GONE);
            holder.ivAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(savedImage)
                    .circleCrop()
                    .into(holder.ivAvatar);
            holder.ivAvatar.setBackgroundTintList(null);
        } else {
            holder.tvInitials.setVisibility(View.VISIBLE);
            holder.ivAvatar.setImageDrawable(null);
            holder.ivAvatar.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            // Avatar color based on name hash
            int colorIdx = Math.abs(member.getName().hashCode()) % AVATAR_COLORS.length;
            holder.ivAvatar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(AVATAR_COLORS[colorIdx])));
            holder.tvInitials.setTextColor(android.graphics.Color.parseColor("#1A1D2E"));
        }

        // Status pill color
        int statusColor = Color.parseColor(color);
        holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pill);
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(statusColor & 0x30FFFFFF | 0x20000000));
        holder.tvStatus.setTextColor(statusColor);

        // Amount
        double amount = member.getContributionPaid();
        if (amount >= 1_000_000) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "UGX %.1fM", amount / 1_000_000));
        } else if (amount >= 1000) {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "UGX %.0fK", amount / 1000));
        } else {
            holder.tvAmount.setText(String.format(Locale.getDefault(), "UGX %.0f", amount));
        }

        // Highlight self
        if (isSelf) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F0F7FF"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivAvatar;
        TextView tvInitials, tvName, tvTurn, tvStatus, tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            tvInitials = itemView.findViewById(R.id.tvMemberInitials);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvTurn = itemView.findViewById(R.id.tvMemberTurnInfo);
            tvStatus = itemView.findViewById(R.id.tvMemberStatusPill);
            tvAmount = itemView.findViewById(R.id.tvMemberAmount);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 1) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
