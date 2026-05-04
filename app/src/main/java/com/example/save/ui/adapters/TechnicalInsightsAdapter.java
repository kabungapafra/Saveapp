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
            binding.tvLastPayDate.setText(member.getJoinedDate() != null ? member.getJoinedDate() : "Oct 12");

            // Server-authoritative logic
            String label = member.getReliabilityLabel();
            String colorStr = member.getReliabilityColor();
            int color = android.graphics.Color.parseColor(colorStr);

            binding.tvTierBadge.setText(label);
            binding.tvReliabilityScore.setTextColor(color);
            binding.imgTrend.setColorFilter(color);

            // Adjust trend rotation based on label
            if (label.equals("SAFE") || label.equals("STABLE")) {
                binding.imgTrend.setRotation(0);
            } else {
                binding.imgTrend.setRotation(45);
            }

            // Styling for the badge based on the server-provided label
            if (label.equals("SAFE")) {
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_vip);
                binding.tvTierBadge.setTextColor(android.graphics.Color.parseColor("#0369A1"));
            } else if (label.equals("STABLE")) {
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_pro);
                binding.tvTierBadge.setTextColor(android.graphics.Color.parseColor("#B91C1C"));
            } else {
                binding.tvTierBadge.setBackgroundResource(R.drawable.bg_tier_basic);
                binding.tvTierBadge.setTextColor(android.graphics.Color.parseColor("#475569"));
            }
        }
    }
}
