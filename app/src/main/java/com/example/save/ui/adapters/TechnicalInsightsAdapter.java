package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.data.models.Member;
import com.example.save.databinding.ItemTechnicalInsightBinding;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TechnicalInsightsAdapter extends RecyclerView.Adapter<TechnicalInsightsAdapter.ViewHolder> {

    private List<Member> list = new ArrayList<>();

    public void updateList(List<Member> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTechnicalInsightBinding binding = ItemTechnicalInsightBinding.inflate(
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
        private final ItemTechnicalInsightBinding binding;

        ViewHolder(ItemTechnicalInsightBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Member member) {
            binding.tvInsightName.setText(member.getName() != null ? member.getName() : "");

            // Contributed amount
            double contributed = member.getContributionPaid();
            if (contributed > 0) {
                NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
                nf.setMaximumFractionDigits(0);
                binding.tvInsightContributed.setText(nf.format(contributed));
            } else {
                binding.tvInsightContributed.setText("--");
            }

            // Reliability label and color from server
            String label = member.getReliabilityLabel();
            String colorStr = member.getReliabilityColor();
            int color = android.graphics.Color.parseColor(colorStr);
            binding.tvReliabilityScore.setText(label);
            binding.tvReliabilityScore.setTextColor(color);
            binding.imgTrend.setColorFilter(color);
            binding.imgTrend.setRotation("SAFE".equals(label) || "STABLE".equals(label) ? 0 : 45);

            // Join date from created_at
            String raw = member.getJoinedDate();
            binding.tvJoinDate.setText(raw != null && !raw.isEmpty() ? formatDate(raw) : "");
        }

        private String formatDate(String iso) {
            String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd"
            };
            for (String fmt : formats) {
                try {
                    Date d = new SimpleDateFormat(fmt, Locale.getDefault()).parse(iso);
                    if (d != null) {
                        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(d);
                    }
                } catch (ParseException ignored) {}
            }
            return iso;
        }
    }
}
