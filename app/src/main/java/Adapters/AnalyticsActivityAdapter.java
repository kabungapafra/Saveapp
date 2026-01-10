package Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import java.util.List;
import java.util.Locale;
import Data.ActivityModel;

public class AnalyticsActivityAdapter extends RecyclerView.Adapter<AnalyticsActivityAdapter.ViewHolder> {

    private List<ActivityModel> activities;

    public AnalyticsActivityAdapter(List<ActivityModel> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActivityModel activity = activities.get(position);
        holder.tvTitle.setText(activity.getTitle());
        holder.tvDate.setText(activity.getDate());

        String prefix = activity.isPositive() ? "+ " : "- ";
        int color = activity.isPositive()
                ? holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark)
                : holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);

        holder.tvAmount.setText(prefix + String.format(Locale.getDefault(), "UGX %,.0f", activity.getAmount()));
        holder.tvAmount.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvAmount;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.activityTitle);
            tvDate = itemView.findViewById(R.id.activityDate);
            tvAmount = itemView.findViewById(R.id.activityAmount);
        }
    }
}
