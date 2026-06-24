package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.MemberEntity;

import java.util.ArrayList;
import java.util.List;

public class PoolMemberAdapter extends RecyclerView.Adapter<PoolMemberAdapter.VH> {

    private List<MemberEntity> items = new ArrayList<>();

    public void setItems(List<MemberEntity> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pool_member, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MemberEntity m = items.get(position);

        String name = m.getName() != null ? m.getName() : "Unknown";
        h.tvName.setText(name);
        h.tvPhone.setText(m.getPhone() != null ? m.getPhone() : "--");
        h.tvInitials.setText(initials(name));
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String initials(String name) {
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) return words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        return (String.valueOf(words[0].charAt(0)) + String.valueOf(words[1].charAt(0))).toUpperCase();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvInitials, tvName, tvPhone, tvContribStatus;

        VH(@NonNull View v) {
            super(v);
            tvInitials      = v.findViewById(R.id.tvMemberInitials);
            tvName          = v.findViewById(R.id.tvMemberName);
            tvPhone         = v.findViewById(R.id.tvMemberPhone);
            tvContribStatus = v.findViewById(R.id.tvMemberContribStatus);
        }
    }
}
