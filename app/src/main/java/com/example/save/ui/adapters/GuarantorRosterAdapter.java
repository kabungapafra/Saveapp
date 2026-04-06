package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuarantorRosterAdapter extends RecyclerView.Adapter<GuarantorRosterAdapter.ViewHolder> {

    private List<Member> members = new ArrayList<>();
    private Set<String> selectedMemberIds = new HashSet<>();
    private final OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(Member member);
    }

    public GuarantorRosterAdapter(OnMemberClickListener listener) {
        this.listener = listener;
    }

    public void setMembers(List<Member> members) {
        this.members = new ArrayList<>(members);
        notifyDataSetChanged();
    }

    public void setSelectedMemberIds(Set<String> selectedMemberIds) {
        this.selectedMemberIds = new HashSet<>(selectedMemberIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guarantor_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = members.get(position);
        boolean isSelected = selectedMemberIds.contains(member.getId());
        
        // Mock state logic based on specs. Usually this would come from the backend model.
        // We simulate some users being locked or low score based on name or ID to match the UI mockup,
        // or just depend on their credit score.
        String state = "ELIGIBLE";
        if (member.getCreditScore() < 60) {
            state = "LOW_SCORE";
        } else if (member.getName().contains("David")) {
            // Mock David being locked as per mockup
            state = "LOCKED";
        }
        
        holder.tvName.setText(member.getName());
        
        String initials = getInitials(member.getName());
        holder.tvAvatar.setText(initials);
        holder.tvAvatar.setBackgroundTintList(ColorStateList.valueOf(getAvatarColor(member.getName())));

        if (state.equals("LOCKED")) {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FAFAFA"));
            holder.tvName.setTextColor(Color.parseColor("#9CA3AF"));
            
            holder.tvBadge.setText("ALREADY A GUARANTOR");
            holder.tvBadge.setTextColor(Color.parseColor("#9CA3AF"));
            holder.tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F3F6")));
            
            holder.viewUnselected.setVisibility(View.GONE);
            holder.imgSelected.setVisibility(View.GONE);
            holder.imgLocked.setVisibility(View.VISIBLE);
            
            holder.itemView.setOnClickListener(null); // Disabled
        } else {
            holder.cardRoot.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.tvName.setTextColor(Color.parseColor("#1A1D2E"));
            
            if (state.equals("LOW_SCORE")) {
                holder.tvBadge.setText("LOW SAVINGS SCORE");
                holder.tvBadge.setTextColor(Color.parseColor("#D97706"));
                holder.tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
            } else {
                holder.tvBadge.setText("ELIGIBLE");
                holder.tvBadge.setTextColor(Color.parseColor("#16A34A"));
                holder.tvBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
            }
            
            holder.imgLocked.setVisibility(View.GONE);
            
            if (isSelected) {
                holder.viewUnselected.setVisibility(View.GONE);
                holder.imgSelected.setVisibility(View.VISIBLE);
            } else {
                holder.viewUnselected.setVisibility(View.VISIBLE);
                holder.imgSelected.setVisibility(View.GONE);
            }
            
            holder.itemView.setOnClickListener(v -> listener.onMemberClick(member));
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardRoot;
        TextView tvAvatar;
        TextView tvName;
        TextView tvBadge;
        View viewUnselected;
        ImageView imgSelected;
        ImageView imgLocked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot = itemView.findViewById(R.id.cardRoot);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            viewUnselected = itemView.findViewById(R.id.viewUnselected);
            imgSelected = itemView.findViewById(R.id.imgSelected);
            imgLocked = itemView.findViewById(R.id.imgLocked);
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
    
    // Consistent colors
    private int getAvatarColor(String name) {
        String[] colors = {"#2D2D2D", "#C4A882", "#E8C4B0", "#D4B896", "#8B9CB0", "#D1D5DB"};
        int index = Math.abs(name.hashCode()) % colors.length;
        return Color.parseColor(colors[index]);
    }
}
