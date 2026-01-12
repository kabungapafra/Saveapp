package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.Member;
import com.example.save.databinding.ItemMemberAdminBinding;

import java.util.List;

public class MemberAdapter extends ListAdapter<Member, MemberAdapter.MemberViewHolder> {
    private OnMemberClickListener clickListener;

    public interface OnMemberClickListener {
        void onMemberClick(Member member);

        void onMoreActionsClick(View view, Member member, int position);
    }

    // DiffUtil callback for efficient list updates
    private static final DiffUtil.ItemCallback<Member> DIFF_CALLBACK = new DiffUtil.ItemCallback<Member>() {
        @Override
        public boolean areItemsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
            // Compare by unique identifier (name in this case, ideally use ID)
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Member oldItem, @NonNull Member newItem) {
            // Compare all relevant fields
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getRole().equals(newItem.getRole()) &&
                    oldItem.isActive() == newItem.isActive() &&
                    oldItem.getContributionPaid() == newItem.getContributionPaid();
        }
    };

    public MemberAdapter() {
        super(DIFF_CALLBACK);
    }

    public MemberAdapter(OnMemberClickListener listener) {
        super(DIFF_CALLBACK);
        this.clickListener = listener;
    }

    public void updateList(List<Member> newList) {
        submitList(newList);
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberAdminBinding binding = ItemMemberAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = getItem(position);
        holder.binding.tvMemberName.setText(member.getName());
        holder.binding.tvMemberRole.setText(member.getRole());

        // Status Indicator Color
        int color = member.isActive()
                ? holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark)
                : holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray);
        holder.binding.statusIndicator.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(color));

        // Click listeners
        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onMemberClick(member));
            holder.binding.btnMoreActions.setOnClickListener(
                    v -> clickListener.onMoreActionsClick(v, member, position));
        }
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ItemMemberAdminBinding binding;

        MemberViewHolder(ItemMemberAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
