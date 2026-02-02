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
        holder.tvDate.setText(calculateDate(position));
    }

    private String calculateDate(int position) {
        if (basePayoutDate == null || basePayoutDate.isEmpty() || basePayoutDate.contains("Not")
                || basePayoutDate.equals("TBD")) {
            return "TBD";
        }
        try {
            String[] parts = basePayoutDate.split("/");
            if (parts.length != 3)
                return basePayoutDate;

            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, day);
            cal.add(java.util.Calendar.MONTH, position);

            return cal.get(java.util.Calendar.DAY_OF_MONTH) + "/" + (cal.get(java.util.Calendar.MONTH) + 1);
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
