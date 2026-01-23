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
        TextView tvName, tvRole, tvSavings;
        View statusView;

        SimpleViewHolder(ItemMemberSimpleBinding binding) {
            super(binding.getRoot());
            tvName = binding.tvMemberName;
            tvRole = binding.tvMemberRole;
            tvSavings = binding.tvMemberSavings;
            statusView = binding.statusIndicator;
        }

        void bind(Member member) {
            tvName.setText(member.getName());
            tvRole.setText(member.getRole());
            tvSavings.setText(String.format("UGX %,.0f", member.getContributionPaid()));
            int color = member.isActive()
                    ? androidx.core.content.ContextCompat.getColor(itemView.getContext(),
                            android.R.color.holo_green_dark)
                    : androidx.core.content.ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
            statusView.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    // ViewHolder for ItemMemberAdmin
    static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvSavings;
        View statusView;
        View btnMore;

        AdminViewHolder(ItemMemberAdminBinding binding) {
            super(binding.getRoot());
            tvName = binding.tvMemberName;
            tvRole = binding.tvMemberRole;
            tvSavings = binding.tvMemberSavings;
            statusView = binding.statusIndicator;
            btnMore = binding.btnMoreActions;
        }

        void bind(Member member, int position, OnMemberClickListener listener) {
            tvName.setText(member.getName());
            tvRole.setText(member.getRole());
            tvSavings.setText(String.format("UGX %,.0f", member.getContributionPaid()));

            int color = member.isActive()
                    ? androidx.core.content.ContextCompat.getColor(itemView.getContext(),
                            android.R.color.holo_green_dark)
                    : androidx.core.content.ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
            statusView.setBackgroundTintList(ColorStateList.valueOf(color));

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onMemberClick(member));
                btnMore.setOnClickListener(v -> listener.onMoreActionsClick(v, member, position));
            }
        }
    }
}
