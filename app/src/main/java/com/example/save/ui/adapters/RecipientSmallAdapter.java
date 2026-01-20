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

    public void updateList(List<Member> newList) {
        this.recipients = newList != null ? newList : new ArrayList<>();
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
    }

    @Override
    public int getItemCount() {
        return recipients.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInitial, tvName;

        ViewHolder(View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tvRecipientInitial);
            tvName = itemView.findViewById(R.id.tvRecipientName);
        }
    }
}
