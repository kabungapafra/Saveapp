package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.databinding.ItemMemberSimpleBinding;

import java.text.NumberFormat;
import java.util.List;

/**
 * Adapter for displaying members with contribution shortfalls
 */
public class ShortfallAdapter extends RecyclerView.Adapter<ShortfallAdapter.ShortfallViewHolder> {
    private List<Member> members;

    public ShortfallAdapter(List<Member> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public ShortfallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberSimpleBinding itemBinding = ItemMemberSimpleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ShortfallViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShortfallViewHolder holder, int position) {
        Member member = members.get(position);
        holder.name.setText(member.getName());
        holder.role.setText("Shortfall: UGX "
                + NumberFormat.getIntegerInstance().format(member.getShortfallAmount()));
        holder.role.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
        holder.status.setBackgroundResource(R.drawable.circle_red);
        holder.status.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ShortfallViewHolder extends RecyclerView.ViewHolder {
        TextView name, role;
        View status;

        ShortfallViewHolder(ItemMemberSimpleBinding itemBinding) {
            super(itemBinding.getRoot());
            name = itemBinding.tvMemberName;
            role = itemBinding.tvMemberRole;
            status = itemBinding.statusIndicator;
        }
    }
}
