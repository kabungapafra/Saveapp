package com.example.save.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.data.models.LoanRequest;
import com.example.save.databinding.ItemLoanBorrowerCardBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoanBorrowerAdapter extends RecyclerView.Adapter<LoanBorrowerAdapter.ViewHolder> {
    private List<LoanRequest> requests = new ArrayList<>();
    private OnLoanActionListener listener;

    public interface OnLoanActionListener {
        void onApprove(LoanRequest request);

        void onDecline(LoanRequest request);

        void onRemind(LoanRequest request);

        void onViewDetails(LoanRequest request);

        void onSendAlert(LoanRequest request);
    }

    public LoanBorrowerAdapter(OnLoanActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<LoanRequest> newList) {
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

        void bind(LoanRequest request) {
            binding.tvBorrowerName.setText(request.getMemberName());
            // Amount with 2 decimal places and $ as per mockup
            String formattedAmount = "$" + String.format(Locale.US, "%,.2f", request.getAmount());
            binding.tvLoanAmount.setText(formattedAmount);
            binding.tvInterestRate.setText("Interest: " + request.getInterestRate() + "%");

            String status = request.getStatus();
            binding.tvStatusBadge.setText(status);

            // Default state
            binding.lateStripe.setVisibility(View.GONE);
            binding.tvOverdueInfo.setVisibility(View.GONE);
            binding.tvRightInfo.setTypeface(null, Typeface.NORMAL);
            binding.tvRightInfo.setVisibility(View.VISIBLE);

            if ("PENDING".equals(status)) {
                binding.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                binding.tvStatusBadge.setTextColor(Color.parseColor("#D97706"));
                binding.loanProgress.setProgress(15);
                binding.loanProgress.setIndicatorColor(Color.parseColor("#D97706"));

                binding.tvRightInfo.setText("Reviewing request...");
                binding.tvRightInfo.setTypeface(null, Typeface.ITALIC);

                binding.btnLeft.setText("Approve");
                binding.btnLeft.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2563EB")));
                binding.btnLeft.setTextColor(Color.WHITE);

                binding.btnRight.setText("Decline");
                binding.btnRight.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                binding.btnRight.setTextColor(Color.parseColor("#090F1C"));

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
                binding.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                binding.tvStatusBadge.setTextColor(Color.parseColor("#E11D48"));
                binding.loanProgress.setProgress(90);
                binding.loanProgress.setIndicatorColor(Color.parseColor("#E11D48"));

                binding.tvRightInfo.setVisibility(View.GONE);
                binding.tvOverdueInfo.setVisibility(View.VISIBLE);
                binding.tvOverdueInfo.setText("Overdue: 4 days");

                binding.btnLeft.setText("View Details");
                binding.btnLeft.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                binding.btnLeft.setTextColor(Color.parseColor("#090F1C"));

                binding.btnRight.setText("Send Alert");
                binding.btnRight.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E11D48")));
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
                binding.tvStatusBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E8F5E9")));
                binding.tvStatusBadge.setTextColor(Color.parseColor("#10B981"));
                binding.loanProgress.setProgress(45);
                binding.loanProgress.setIndicatorColor(Color.parseColor("#2563EB"));

                binding.tvRightInfo.setText("Due: " + request.getRequestDate());

                binding.btnLeft.setText("View Details");
                binding.btnLeft.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                binding.btnLeft.setTextColor(Color.parseColor("#090F1C"));

                binding.btnRight.setText("Remind");
                binding.btnRight.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
                binding.btnRight.setTextColor(Color.parseColor("#2563EB"));

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
