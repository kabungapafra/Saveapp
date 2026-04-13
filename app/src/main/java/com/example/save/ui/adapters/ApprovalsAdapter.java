package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.R;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApprovalsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface ApprovalItem {
        String getId();

        String getType();

        String getTitle();

        double getAmount();

        String getDescription();

        java.util.Date getDate();

        String getStatus();

        boolean hasApproved();
    }

    private List<ApprovalItem> items = new ArrayList<>();
    private final OnApprovalClickListener listener;
    private int requiredApprovals = 1;

    private static final int VIEW_TYPE_LOAN = 1;
    private static final int VIEW_TYPE_PAYOUT = 2;

    public interface OnApprovalClickListener {
        void onApproveClick(ApprovalItem item);
    }

    public ApprovalsAdapter(OnApprovalClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<ApprovalItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if ("LOAN".equals(items.get(position).getType())) {
            return VIEW_TYPE_LOAN;
        }
        return VIEW_TYPE_PAYOUT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_LOAN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval_loan, parent, false);
            return new LoanViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_approval_payout, parent, false);
            return new PayoutViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ApprovalItem item = items.get(position);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);

        if (holder instanceof LoanViewHolder) {
            LoanViewHolder loanHolder = (LoanViewHolder) holder;
            loanHolder.tvApplicantName.setText(item.getTitle());
            loanHolder.tvAmount.setText(format.format(item.getAmount()));
            
            // Score and Tier are hardcoded explicitly in the XML directly.

            if (item.hasApproved() || "COMPLETED".equals(item.getStatus())) {
                loanHolder.btnApprove.setText("APPROVED");
                loanHolder.btnApprove.setBackgroundTintList(holder.itemView.getContext().getResources().getColorStateList(android.R.color.holo_green_dark));
                loanHolder.btnApprove.setClickable(false);
            } else {
                loanHolder.btnApprove.setOnClickListener(v -> listener.onApproveClick(item));
            }
        } else if (holder instanceof PayoutViewHolder) {
            PayoutViewHolder payoutHolder = (PayoutViewHolder) holder;
            payoutHolder.tvApplicantName.setText(item.getTitle().replace(" ", "\n"));
            payoutHolder.tvAmount.setText(format.format(item.getAmount()));
            
            String method = item.getDescription();
            if (method == null || method.isEmpty()) method = "Direct Wire\n(Intl)";
            else method = method.replace(" ", "\n");
            
            payoutHolder.tvMethod.setText(method);

            if (item.hasApproved() || "COMPLETED".equals(item.getStatus())) {
                payoutHolder.btnApprove.setText("VERIFIED");
                payoutHolder.btnApprove.setTextColor(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                payoutHolder.btnApprove.setClickable(false);
            } else {
                payoutHolder.btnApprove.setOnClickListener(v -> listener.onApproveClick(item));
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LoanViewHolder extends RecyclerView.ViewHolder {
        TextView tvApplicantName, tvAmount, tvScore, tvTier, btnApprove;

        public LoanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvApplicantName = itemView.findViewById(R.id.tvApplicantName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            // using tvScore / tvTier implicitly by grabbing the text views inside the layout (in production, assign explicit IDs)
            // Edit: Let's assume we didn't add IDs for score and tier in item_approval_loan.xml
            // Since we know they are purely dummy right now, we can skip updating them dynamically and just leave them as-is.
            // But we can assign ids if we want.
            btnApprove = itemView.findViewById(R.id.btnApprove);
            
            // We just let the XML values stay for SCORE and TIER for now as it's static.
        }
    }

    static class PayoutViewHolder extends RecyclerView.ViewHolder {
        TextView tvApplicantName, tvAmount, tvMethod, btnApprove;

        public PayoutViewHolder(@NonNull View itemView) {
            super(itemView);
            tvApplicantName = itemView.findViewById(R.id.tvApplicantName);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvMethod = itemView.findViewById(R.id.tvMethod);
            btnApprove = itemView.findViewById(R.id.btnApprove);
        }
    }
}
