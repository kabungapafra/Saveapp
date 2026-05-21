package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;

import java.util.ArrayList;
import java.util.List;

public class RecipientSmallAdapter extends RecyclerView.Adapter<RecipientSmallAdapter.ViewHolder> {

    private List<Member> recipients = new ArrayList<>();
    private String basePayoutDate;

    public void updateList(List<Member> newList, String basePayoutDate) {
        this.recipients = newList != null ? newList : new ArrayList<>();
        this.basePayoutDate = basePayoutDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipient_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = recipients.get(position);
        holder.tvName.setText(member.getName().split(" ")[0]);
        holder.tvInitial.setText(String.valueOf(member.getName().charAt(0)).toUpperCase());

        // Calculate date
        holder.tvDate.setText(calculateDate(position, holder.itemView.getContext()));
    }

    private String calculateDate(int position, android.content.Context context) {
        if (basePayoutDate == null || basePayoutDate.isEmpty() || basePayoutDate.contains("Not")
                || basePayoutDate.equals("TBD")) {
            return "TBD";
        }
        
        android.content.SharedPreferences prefs = context.getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
        String frequency = prefs.getString("rule_frequency", "Monthly");
        String recipientsStr = prefs.getString("rule_edit_recipients", "1 Member").replaceAll("[^0-9]", "");
        int recipients = 1;
        try {
            recipients = Integer.parseInt(recipientsStr);
            if (recipients < 1) recipients = 1;
        } catch (NumberFormatException e) {
            recipients = 1;
        }

        int cyclesToAdd = position / recipients;

        try {
            java.text.SimpleDateFormat inSdf = new java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault());
            java.util.Date date = inSdf.parse(basePayoutDate);
            if (date == null) return basePayoutDate;

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);

            for (int i = 0; i < cyclesToAdd; i++) {
                switch (frequency) {
                    case "Daily": cal.add(java.util.Calendar.DAY_OF_YEAR, 1); break;
                    case "Weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 1); break;
                    case "Bi-weekly": cal.add(java.util.Calendar.WEEK_OF_YEAR, 2); break;
                    case "Monthly": cal.add(java.util.Calendar.MONTH, 1); break;
                    case "Every 2 Months": cal.add(java.util.Calendar.MONTH, 2); break;
                    case "Every 3 Months": cal.add(java.util.Calendar.MONTH, 3); break;
                    case "Every 4 Months": cal.add(java.util.Calendar.MONTH, 4); break;
                    case "Every 5 Months": cal.add(java.util.Calendar.MONTH, 5); break;
                    case "Every 6 Months": cal.add(java.util.Calendar.MONTH, 6); break;
                }
            }

            java.text.SimpleDateFormat outSdf = new java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault());
            return outSdf.format(cal.getTime());
        } catch (Exception e) {
            return basePayoutDate;
        }
    }

    @Override
    public int getItemCount() {
        return recipients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvRecipientInitial);
            tvName = itemView.findViewById(R.id.tvRecipientName);
            tvDate = itemView.findViewById(R.id.tvRecipientDate);
        }
    }
}
