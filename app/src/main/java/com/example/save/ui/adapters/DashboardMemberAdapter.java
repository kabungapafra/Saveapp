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
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class DashboardMemberAdapter extends RecyclerView.Adapter<DashboardMemberAdapter.ViewHolder> {

    private final List<MemberStatus> members;
    private final Context context;

    public DashboardMemberAdapter(Context context) {
        this.context = context;
        this.members = generateMockData();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_status_v2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberStatus member = members.get(position);
        
        holder.tvName.setText(member.name);
        holder.tvInitials.setText(member.initials);
        holder.tvTurn.setText(member.turnInfo);
        holder.tvStatus.setText(member.status);
        holder.tvAmount.setText(member.amount);

        // Styling Avatar
        holder.ivAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(member.avatarBg)));
        holder.tvInitials.setTextColor(Color.parseColor(member.avatarText));

        // Styling Status Pill
        holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_pill); // Reusing existing pill drawable
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(member.statusBg)));
        holder.tvStatus.setTextColor(Color.parseColor(member.statusText));

        // Highlight Self
        if (member.isSelf) {
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

    private List<MemberStatus> generateMockData() {
        List<MemberStatus> list = new ArrayList<>();
        list.add(new MemberStatus("JK", "#FBBF24", "#78350F", "James K.", "Turn #1 · Received Sept 1", "Paid", "#D1FAE5", "#065F46", "UGX 200k", false));
        list.add(new MemberStatus("AM", "#F87171", "#7F1D1D", "Amara M.", "Turn #2 · Received Oct 1", "Paid", "#D1FAE5", "#065F46", "UGX 200k", false));
        list.add(new MemberStatus("PF", "#DBEAFE", "#1D4ED8", "Pafra F. (You)", "Turn #3 · Due Oct 14", "Due Soon", "#EFF6FF", "#1D4ED8", "UGX 200k", true));
        list.add(new MemberStatus("TR", "#A78BFA", "#3B0764", "Tendo R.", "Turn #4 · Pending", "Paid", "#D1FAE5", "#065F46", "UGX 200k", false));
        list.add(new MemberStatus("NK", "#FEE2E2", "#991B1B", "Nakato K.", "Turn #5 · 2 days late", "Late", "#FEE2E2", "#991B1B", "UGX 200k", false));
        list.add(new MemberStatus("BM", "#D1FAE5", "#065F46", "Brian M.", "Turn #6 · Pending", "Pending", "#FEF3C7", "#92400E", "UGX 200k", false));
        list.add(new MemberStatus("SR", "#E0E7FF", "#3730A3", "Sarah R.", "Turn #7 · Pending", "Pending", "#FEF3C7", "#92400E", "UGX 200k", false));
        list.add(new MemberStatus("OW", "#FEF3C7", "#92400E", "Owen W.", "Turn #8 · Pending", "Pending", "#FEF3C7", "#92400E", "UGX 200k", false));
        return list;
    }

    static class MemberStatus {
        String initials, avatarBg, avatarText, name, turnInfo, status, statusBg, statusText, amount;
        boolean isSelf;

        MemberStatus(String initials, String avatarBg, String avatarText, String name, String turnInfo, String status, String statusBg, String statusText, String amount, boolean isSelf) {
            this.initials = initials;
            this.avatarBg = avatarBg;
            this.avatarText = avatarText;
            this.name = name;
            this.turnInfo = turnInfo;
            this.status = status;
            this.statusBg = statusBg;
            this.statusText = statusText;
            this.amount = amount;
            this.isSelf = isSelf;
        }
    }
}
