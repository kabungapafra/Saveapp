package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.databinding.ItemMemberAdminBinding;
import com.example.save.databinding.ItemMemberSimpleBinding;
import com.example.save.utils.SessionManager;

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
    private boolean isAdmin = false;

    public interface OnMemberClickListener {
        void onMemberClick(Member member);

        void onMoreActionsClick(View view, Member member, int position);

        void onDeleteClick(Member member, int position);
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

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        notifyDataSetChanged();
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
            ((AdminViewHolder) holder).bind(member, position, listener, isAdmin);
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
            int color;
            if ("PENDING".equalsIgnoreCase(member.getStatus())) {
                color = android.graphics.Color.parseColor("#F59E0B"); // Orange/Amber
            } else if (member.isActive()) {
                color = android.graphics.Color.parseColor("#10B981"); // Green
            } else {
                color = android.graphics.Color.parseColor("#94A3B8"); // Slate/Gray
            }
            binding.statusIndicator.setBackgroundTintList(ColorStateList.valueOf(color));

            // Load profile image if it's the current user
            com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(itemView.getContext());
            String currentUserPhone = session.getUserPhone();

            // Normalize both phone numbers for robust comparison
            String normalizedMemberPhone = member.getPhone() != null ? member.getPhone().replaceAll("[^0-9+]", "") : "";
            String normalizedCurrentPhone = currentUserPhone != null ? currentUserPhone.replaceAll("[^0-9+]", "") : "";

            boolean isSelf = !normalizedCurrentPhone.isEmpty() && !normalizedMemberPhone.isEmpty() && normalizedCurrentPhone.equals(normalizedMemberPhone);

            if (isSelf) {
                String savedImage = session.getProfileImage();
                if (savedImage != null && !savedImage.isEmpty()) {
                    binding.imgProfile.setImageTintList(null);
                    binding.imgProfile.setPadding(0, 0, 0, 0);
                    binding.imgProfile.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(savedImage)
                            .circleCrop()
                            .into(binding.imgProfile);
                } else {
                    setDefaultProfileIcon(binding.imgProfile);
                }
            } else {
                setDefaultProfileIcon(binding.imgProfile);
            }
        }

        private void setDefaultProfileIcon(android.widget.ImageView imageView) {
            com.bumptech.glide.Glide.with(itemView.getContext()).clear(imageView);
            imageView.setImageResource(com.example.save.R.drawable.ic_person);
            int p = (int)(14 * itemView.getContext().getResources().getDisplayMetrics().density);
            imageView.setPadding(p, p, p, p);
            imageView.setImageTintList(android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(itemView.getContext(), com.example.save.R.color.project_primary)));
            imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        }
    }

    // ViewHolder for ItemMemberAdmin
    static class AdminViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberAdminBinding binding;

        AdminViewHolder(ItemMemberAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Member member, int position, OnMemberClickListener listener, boolean isUserAdmin) {
            binding.tvMemberName.setText(member.getName());
            binding.tvMemberContributed.setText(String.format("$%,.2f", member.getContributionPaid()));

            // Reliability based on credit score (0-100)
            binding.tvMemberReliability.setText(member.getCreditScore() + ".0%");

            // Random or calculated rank for demo
            int rank = (position % 5) + 1;
            binding.tvMemberRank.setText("#" + rank + (rank == 1 ? " Top" : ""));
            binding.tvMemberRank.setTextColor(rank == 1 ? Color.parseColor("#FF8A00")
                    : Color.parseColor("#94A3B8"));

            // Status Badge
            String status = member.getStatus() != null ? member.getStatus() : "PENDING";
            if ("PENDING".equalsIgnoreCase(status)) {
                binding.tvStatusBadge.setText("PENDING");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
                binding.tvStatusBadge.setTextColor(Color.parseColor("#D97706")); // Dark Orange
            } else if (member.isActive()) {
                binding.tvStatusBadge.setText("ACTIVE");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_active);
                binding.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
            } else {
                binding.tvStatusBadge.setText("INACTIVE");
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inactive_badge);
                binding.tvStatusBadge.setTextColor(Color.parseColor("#64748B"));
            }

            binding.btnMoreActions.setVisibility(isUserAdmin ? View.VISIBLE : View.GONE);
            
            // User identification for permissions and profile picture
            SessionManager session = SessionManager.getInstance(itemView.getContext());
            String currentUserPhone = session.getUserPhone();
            
            // Normalize both phone numbers for robust comparison
            String normalizedMemberPhone = member.getPhone() != null ? member.getPhone().replaceAll("[^0-9+]", "") : "";
            String normalizedCurrentPhone = currentUserPhone != null ? currentUserPhone.replaceAll("[^0-9+]", "") : "";

            boolean isSelf = !normalizedCurrentPhone.isEmpty() && !normalizedMemberPhone.isEmpty() && normalizedCurrentPhone.equals(normalizedMemberPhone);
            
            binding.btnRemoveMember.setVisibility(isUserAdmin && !isSelf ? View.VISIBLE : View.GONE);

            if (isSelf) {
                String savedImage = session.getProfileImage();
                if (savedImage != null && !savedImage.isEmpty()) {
                    binding.imgProfile.setImageTintList(null);
                    binding.imgProfile.setPadding(0, 0, 0, 0);
                    binding.imgProfile.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    com.bumptech.glide.Glide.with(itemView.getContext())
                            .load(savedImage)
                            .circleCrop()
                            .into(binding.imgProfile);
                } else {
                    setDefaultProfileIcon(binding.imgProfile);
                }
            } else {
                setDefaultProfileIcon(binding.imgProfile);
            }

            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onMemberClick(member));
                binding.btnMoreActions.setOnClickListener(v -> listener.onMoreActionsClick(v, member, position));
                binding.btnRemoveMember.setOnClickListener(v -> listener.onDeleteClick(member, position));
            }
        }

        private void setDefaultProfileIcon(ImageView imageView) {
            com.bumptech.glide.Glide.with(itemView.getContext()).clear(imageView);
            imageView.setImageResource(R.drawable.ic_person);
            int p = (int)(14 * itemView.getContext().getResources().getDisplayMetrics().density);
            imageView.setPadding(p, p, p, p);
            imageView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.v_text_mid)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
    }
}
