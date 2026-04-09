package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class MemberProfileHistoryAdapter extends RecyclerView.Adapter<MemberProfileHistoryAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();

    public MemberProfileHistoryAdapter(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_profile_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        holder.tvDate.setText(transaction.getDate());
        
        // Map types more cleanly if needed
        String type = transaction.getType();
        holder.tvType.setText(type);

        if (transaction.isCredit()) {
            holder.ivIcon.setImageResource(R.drawable.ic_money); // Keep existing icon
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#1A6EE0")));
            ((com.google.android.material.card.MaterialCardView) holder.ivIcon.getParent()).setCardBackgroundColor(Color.parseColor("#EFF6FF"));
        } else {
            holder.ivIcon.setImageResource(R.drawable.ic_loan);
            holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#D97706")));
            ((com.google.android.material.card.MaterialCardView) holder.ivIcon.getParent()).setCardBackgroundColor(Color.parseColor("#FFF7ED"));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvType;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvType = itemView.findViewById(R.id.tvType);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
