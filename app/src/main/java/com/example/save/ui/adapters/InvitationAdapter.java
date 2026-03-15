package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import com.example.save.data.models.Invitation;
import com.example.save.databinding.ItemMemberInvitationBinding;
import java.util.ArrayList;
import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.ViewHolder> {

    private List<Invitation> invitations = new ArrayList<>();
    private OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onResend(Invitation invitation);
        void onDelete(Invitation invitation);
    }

    public InvitationAdapter(OnInvitationActionListener listener) {
        this.listener = listener;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberInvitationBinding binding = ItemMemberInvitationBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Invitation invitation = invitations.get(position);
        holder.binding.tvInviteEmail.setText(invitation.getEmail());
        holder.binding.tvInviteDate.setText("INVITED: " + invitation.getDate());
        
        if (invitation.getStatus() == Invitation.Status.EXPIRED) {
            holder.binding.statusBadge.setText("EXPIRED");
            holder.binding.statusBadge.setBackgroundResource(R.drawable.bg_status_expired);
            holder.binding.statusBadge.setTextColor(Color.parseColor("#64748B"));
            holder.binding.btnResend.setImageResource(R.drawable.ic_refresh);
        } else {
            holder.binding.statusBadge.setText("PENDING");
            holder.binding.statusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            holder.binding.statusBadge.setTextColor(Color.parseColor("#D97706"));
            holder.binding.btnResend.setImageResource(R.drawable.ic_send);
        }
        
        holder.binding.btnResend.setOnClickListener(v -> {
            if (listener != null) listener.onResend(invitation);
        });
        
        holder.binding.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(invitation);
        });
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemMemberInvitationBinding binding;

        ViewHolder(ItemMemberInvitationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
