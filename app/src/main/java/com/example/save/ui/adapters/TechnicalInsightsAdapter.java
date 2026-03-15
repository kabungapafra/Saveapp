package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.databinding.ItemMemberInsightBinding;
import java.util.ArrayList;
import java.util.List;

public class TechnicalInsightsAdapter extends RecyclerView.Adapter<TechnicalInsightsAdapter.ViewHolder> {

    private List<Member> list = new ArrayList<>();

    public void updateList(List<Member> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberInsightBinding binding = ItemMemberInsightBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMemberInsightBinding binding;

        ViewHolder(ItemMemberInsightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Member member) {
            binding.tvInsightName.setText(member.getName());
            binding.tvReliabilityScore.setText(member.getCreditScore() + ".0");
            binding.tvLastPayDate.setText("Oct 12"); // Placeholder as requested for visual match

            // Tier logic
            int score = member.getCreditScore();
            if (score >= 95) {
                binding.tvTierBadge.setText("VIP");
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_vip);
                binding.tvTierBadge.setTextColor(android.graphics.Color.parseColor("#0369A1"));
                binding.imgTrend.setImageResource(R.drawable.ic_diagonal_arrow);
                binding.imgTrend.setRotation(0);
                binding.imgTrend.setColorFilter(android.graphics.Color.parseColor("#10B981"));
                binding.tvReliabilityScore.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else if (score >= 85) {
                binding.tvTierBadge.setText("PRO");
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_pro);
                binding.tvTierBadge.setTextColor(android.graphics.Color.parseColor("#B91C1C"));
                binding.imgTrend.setImageResource(R.drawable.ic_diagonal_arrow);
                binding.imgTrend.setRotation(0);
                binding.imgTrend.setColorFilter(android.graphics.Color.parseColor("#10B981"));
                binding.tvReliabilityScore.setTextColor(android.graphics.Color.parseColor("#10B981"));
            } else {
                binding.tvTierBadge.setText("BASIC");
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_basic);
                binding.tvTierBadge.setTextColor(android.graphics.Color.parseColor("#475569"));
                binding.imgTrend.setImageResource(R.drawable.ic_diagonal_arrow);
                binding.imgTrend.setRotation(45); // Side arrow effect
                binding.imgTrend.setColorFilter(android.graphics.Color.parseColor("#F59E0B"));
                binding.tvReliabilityScore.setTextColor(android.graphics.Color.parseColor("#F59E0B"));
            }
        }
    }
}
