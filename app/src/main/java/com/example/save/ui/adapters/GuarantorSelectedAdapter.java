package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;

import java.util.ArrayList;
import java.util.List;

public class GuarantorSelectedAdapter extends RecyclerView.Adapter<GuarantorSelectedAdapter.ViewHolder> {

    private List<Member> selectedMembers = new ArrayList<>();
    private final OnRemoveClickListener listener;
    // We add +1 for the "Add" slot
    
    public interface OnRemoveClickListener {
        void onRemoveClick(Member member);
        void onAddSlotClick();
    }

    public GuarantorSelectedAdapter(OnRemoveClickListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<Member> members) {
        this.selectedMembers = new ArrayList<>(members);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guarantor_selected_avatar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < selectedMembers.size()) {
            // Member Avatar
            Member member = selectedMembers.get(position);
            holder.avatarContainer.setVisibility(View.VISIBLE);
            holder.removeBadge.setVisibility(View.VISIBLE);
            holder.addSlotContainer.setVisibility(View.GONE);
            
            holder.tvName.setText(member.getName().split(" ")[0]);
            holder.tvName.setTextColor(Color.parseColor("#1A1D2E"));
            
            String initials = getInitials(member.getName());
            holder.tvAvatarInitials.setText(initials);
            holder.tvAvatarInitials.setBackgroundTintList(ColorStateList.valueOf(getAvatarColor(member.getName())));
            
            holder.itemView.setOnClickListener(v -> listener.onRemoveClick(member));
        } else {
            // Add Slot
            holder.avatarContainer.setVisibility(View.GONE);
            holder.removeBadge.setVisibility(View.GONE);
            holder.addSlotContainer.setVisibility(View.VISIBLE);
            
            holder.tvName.setText("Add");
            holder.tvName.setTextColor(Color.parseColor("#9CA3AF"));
            
            holder.itemView.setOnClickListener(v -> listener.onAddSlotClick());
        }
    }

    @Override
    public int getItemCount() {
        return selectedMembers.size() + 1; // Always show Add slot at the end
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout avatarContainer;
        TextView tvAvatarInitials;
        FrameLayout removeBadge;
        FrameLayout addSlotContainer;
        TextView tvName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarContainer = itemView.findViewById(R.id.avatarContainer);
            tvAvatarInitials = itemView.findViewById(R.id.tvAvatarInitials);
            removeBadge = itemView.findViewById(R.id.removeBadge);
            addSlotContainer = itemView.findViewById(R.id.addSlotContainer);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }
    
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "G";
        String[] parts = name.trim().split("\\s+");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (name.length() > 1) {
            return name.substring(0, 2).toUpperCase();
        }
        return name.substring(0, 1).toUpperCase();
    }
    
    private int getAvatarColor(String name) {
        String[] colors = {"#2D2D2D", "#C4A882", "#E8C4B0", "#D4B896", "#8B9CB0", "#D1D5DB"};
        int index = Math.abs(name.hashCode()) % colors.length;
        return Color.parseColor(colors[index]);
    }
}
