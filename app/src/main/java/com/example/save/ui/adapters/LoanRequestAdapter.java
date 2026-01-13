package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.LoanRequest;
import com.example.save.databinding.ItemLoanRequestBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying loan requests
 */
public class LoanRequestAdapter extends RecyclerView.Adapter<LoanRequestAdapter.ViewHolder> {
    private List<LoanRequest> requests = new ArrayList<>();
    private OnLoanActionListener listener;

    public interface OnLoanActionListener {
        void onApprove(LoanRequest request);

        void onReject(LoanRequest request);
    }

    public LoanRequestAdapter(OnLoanActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<LoanRequest> newList) {
        this.requests = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLoanRequestBinding itemBinding = ItemLoanRequestBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoanRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemLoanRequestBinding binding;

        ViewHolder(ItemLoanRequestBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanRequest request) {
            binding.tvMemberName.setText(request.getMemberName());
            binding.tvStatus.setText(request.getStatus());
            binding.tvAmount.setText("UGX " + NumberFormat.getNumberInstance(Locale.US).format(request.getAmount()));
            binding.tvDuration.setText(request.getDurationMonths() + " months");
            binding.tvRepayment
                    .setText("UGX " + NumberFormat.getNumberInstance(Locale.US).format(request.getTotalRepayment()));

            if (request.getGuarantor() != null && !request.getGuarantor().isEmpty()) {
                binding.tvGuarantor.setText("Guarantor: " + request.getGuarantor());
                binding.tvGuarantor.setVisibility(View.VISIBLE);
            } else {
                binding.tvGuarantor.setVisibility(View.GONE);
            }

            if (request.getReason() != null && !request.getReason().isEmpty()) {
                binding.tvReason.setText("Reason: " + request.getReason());
                binding.tvReason.setVisibility(View.VISIBLE);
            } else {
                binding.tvReason.setVisibility(View.GONE);
            }

            binding.tvDate.setText("Requested: " + request.getRequestDate());

            // Show/hide action buttons based on status
            if ("PENDING".equals(request.getStatus())) {
                binding.layoutActions.setVisibility(View.VISIBLE);

                binding.btnApprove.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onApprove(request);
                });

                binding.btnReject.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onReject(request);
                });
            } else {
                binding.layoutActions.setVisibility(View.GONE);
            }

            // Update status badge color
            if ("APPROVED".equals(request.getStatus())) {
                binding.tvStatus.setBackgroundColor(0xFF4CAF50);
            } else if ("REJECTED".equals(request.getStatus())) {
                binding.tvStatus.setBackgroundColor(0xFFD32F2F);
            } else {
                binding.tvStatus.setBackgroundColor(0xFFFF9800);
            }
        }
    }
}
