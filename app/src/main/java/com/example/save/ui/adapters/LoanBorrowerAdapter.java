package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.LoanEntity;
import com.example.save.databinding.ItemLoanBorrowerCardBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoanBorrowerAdapter extends RecyclerView.Adapter<LoanBorrowerAdapter.ViewHolder> {
    private List<LoanEntity> requests = new ArrayList<>();
    private OnLoanActionListener listener;

    public interface OnLoanActionListener {
        void onApprove(LoanEntity request);

        void onDecline(LoanEntity request);

        void onRemind(LoanEntity request);

        void onViewDetails(LoanEntity request);

        void onSendAlert(LoanEntity request);
    }

    public LoanBorrowerAdapter(OnLoanActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<LoanEntity> newList) {
        this.requests = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLoanBorrowerCardBinding itemBinding = ItemLoanBorrowerCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(requests.get(position));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ItemLoanBorrowerCardBinding binding;

        ViewHolder(ItemLoanBorrowerCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LoanEntity request) {
            binding.tvBorrowerName.setText(request.getMemberName());
            // Amount with 0 decimal places and UGX as per local context
            String formattedAmount = "UGX " + String.format(Locale.US, "%,.0f", request.getAmount());
            binding.tvLoanAmount.setText(formattedAmount);
            binding.tvInterestRate.setText("Interest: " + request.getInterest() + "%");

            String status = request.getStatus();
            binding.tvStatusBadge.setText(status);

            // Default state
            binding.lateStripe.setVisibility(View.GONE);
            binding.tvOverdueInfo.setVisibility(View.GONE);
            binding.tvRightInfo.setTypeface(null, Typeface.NORMAL);
            binding.tvRightInfo.setVisibility(View.VISIBLE);

            if ("PENDING".equals(status)) {
                binding.tvStatusBadge.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.status_pending_bg));
                binding.tvStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.status_pending_text));
                binding.loanProgress.setProgress(15);
                binding.loanProgress.setIndicatorColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.status_pending_text));

                binding.tvRightInfo.setText("Reviewing request...");
                binding.tvRightInfo.setTypeface(null, Typeface.ITALIC);

                binding.btnLeft.setText("Approve");
                binding.btnLeft.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.brand_blue));
                binding.btnLeft.setTextColor(Color.WHITE);

                binding.btnRight.setText("Decline");
                binding.btnRight.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.v_input_bg));
                binding.btnRight.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.v_text_dark));

                binding.btnLeft.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onApprove(request);
                });
                binding.btnRight.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onDecline(request);
                });
            } else if ("LATE".equals(status)) {
                binding.lateStripe.setVisibility(View.VISIBLE);
                binding.tvStatusBadge.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.h_red_50));
                binding.tvStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.h_red_700));
                binding.loanProgress.setProgress(90);
                binding.loanProgress.setIndicatorColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.h_red_700));

                binding.tvRightInfo.setVisibility(View.GONE);
                binding.tvOverdueInfo.setVisibility(View.VISIBLE);
                binding.tvOverdueInfo.setText("Overdue: 4 days");

                binding.btnLeft.setText("View Details");
                binding.btnLeft.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.v_input_bg));
                binding.btnLeft.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.v_text_dark));

                binding.btnRight.setText("Send Alert");
                binding.btnRight.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.h_red_700));
                binding.btnRight.setTextColor(Color.WHITE);

                binding.btnLeft.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onViewDetails(request);
                });
                binding.btnRight.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onSendAlert(request);
                });
            } else { // ACTIVE
                binding.tvStatusBadge.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.h_green_50));
                binding.tvStatusBadge.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.h_green_700));
                binding.loanProgress.setProgress(45);
                binding.loanProgress.setIndicatorColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.brand_blue));

                binding.tvRightInfo.setText("Due: " + (request.getDateRequested() != null ? request.getDateRequested().toString() : ""));

                binding.btnLeft.setText("View Details");
                binding.btnLeft.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.v_input_bg));
                binding.btnLeft.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.v_text_dark));

                binding.btnRight.setText("Remind");
                binding.btnRight.setBackgroundTintList(androidx.core.content.ContextCompat.getColorStateList(itemView.getContext(), R.color.h_blue_50));
                binding.btnRight.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), R.color.brand_blue));

                binding.btnLeft.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onViewDetails(request);
                });
                binding.btnRight.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onRemind(request);
                });
            }
        }
    }
}
