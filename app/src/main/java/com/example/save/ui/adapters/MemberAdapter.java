package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.Member;
import com.example.save.databinding.ItemMemberAdminBinding;
import com.example.save.databinding.ItemMemberSimpleBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Flexible adapter for displaying members in both read-only and admin views.
 */
public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SIMPLE = 0;
    private static final int TYPE_ADMIN = 1;

    private List<Member> list;
    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onMemberClick(Member member);

        void onMoreActionsClick(View view, Member member, int position);
    }

    // Default constructor
    public MemberAdapter() {
        this.list = new ArrayList<>();
    }

    // Simplified constructor for read-only view
    public MemberAdapter(List<Member> list) {
        this.list = list;
    }

    // Constructor for admin view with actions
    public MemberAdapter(OnMemberClickListener listener) {
        this.list = new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Member> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return listener != null ? TYPE_ADMIN : TYPE_SIMPLE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ADMIN) {
            ItemMemberAdminBinding binding = ItemMemberAdminBinding.inflate(inflater, parent, false);
            return new AdminViewHolder(binding);
        } else {
            ItemMemberSimpleBinding binding = ItemMemberSimpleBinding.inflate(inflater, parent, false);
            return new SimpleViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Member member = list.get(position);
        
        // Add entrance animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.getContext(), com.example.save.R.anim.slide_up_fade);
        holder.itemView.startAnimation(animation);

        if (holder instanceof AdminViewHolder) {
            ((AdminViewHolder) holder).bind(member, position, listener);
        } else if (holder instanceof SimpleViewHolder) {
            ((SimpleViewHolder) holder).bind(member);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ViewHolder for ItemMemberSimple
    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberSimpleBinding binding;

        SimpleViewHolder(ItemMemberSimpleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Member member) {
            binding.tvMemberName.setText(member.getName());
            binding.tvMemberRole.setText(member.getRole());
            binding.tvMemberSavings.setText(String.format("UGX %,.0f", member.getContributionPaid()));

            // Subtle status color
            int color = member.isActive()
                    ? android.graphics.Color.parseColor("#10B981")
                    : android.graphics.Color.parseColor("#94A3B8");
            binding.statusIndicator.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    // ViewHolder for ItemMemberAdmin
    static class AdminViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberAdminBinding binding;

        AdminViewHolder(ItemMemberAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Member member, int position, OnMemberClickListener listener) {
            binding.tvMemberName.setText(member.getName());
            binding.tvMemberContributed.setText(String.format("$%,.2f", member.getContributionPaid()));

            // Reliability based on credit score (0-100)
            binding.tvMemberReliability.setText(member.getCreditScore() + ".0%");

            // Random or calculated rank for demo
            int rank = (position % 5) + 1;
            binding.tvMemberRank.setText("#" + rank + (rank == 1 ? " Top" : ""));
            binding.tvMemberRank.setTextColor(rank == 1 ? android.graphics.Color.parseColor("#FF8A00")
                    : android.graphics.Color.parseColor("#94A3B8"));

            // Status Badge
            if (member.isActive()) {
                binding.tvStatusBadge.setText("ACTIVE");
                binding.tvStatusBadge.setBackgroundResource(com.example.save.R.drawable.bg_status_active);
                binding.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                binding.tvStatusBadge.setText("INACTIVE");
                binding.tvStatusBadge.setBackgroundResource(com.example.save.R.drawable.bg_status_inactive_badge);
                binding.tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#64748B"));
            }

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onMemberClick(member));
                binding.btnMoreActions.setOnClickListener(v -> listener.onMoreActionsClick(v, member, position));
            }
        }
    }
}
