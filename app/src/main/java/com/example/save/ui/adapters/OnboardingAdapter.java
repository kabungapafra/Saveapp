package com.example.save.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.databinding.OnboardingSlideBinding;
import com.example.save.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<com.example.save.data.models.OnboardingItem> items;
    private final OnButtonClickListener listener;

    public interface OnButtonClickListener {
        void onNextClick(int position);

        void onBackClick(int position); // Kept for interface compatibility
    }

    public OnboardingAdapter(List<com.example.save.data.models.OnboardingItem> items, OnButtonClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OnboardingSlideBinding binding = OnboardingSlideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OnboardingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        com.example.save.data.models.OnboardingItem item = items.get(position);

        holder.illustration.setImageResource(item.getImage());
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());

        // Update progress dots logic
        holder.dot1.setBackgroundResource(R.drawable.progress_dot_inactive);
        holder.dot2.setBackgroundResource(R.drawable.progress_dot_inactive);
        holder.dot3.setBackgroundResource(R.drawable.progress_dot_inactive);

        if (position == 0) {
            holder.dot1.setBackgroundResource(R.drawable.progress_dot_active);
        } else if (position == 1) {
            holder.dot2.setBackgroundResource(R.drawable.progress_dot_active);
        } else if (position == 2) {
            holder.dot3.setBackgroundResource(R.drawable.progress_dot_active);
        }

        // Button Text Logic
        if (position == items.size() - 1) {
            holder.nextButton.setText("Get Started");
        } else {
            holder.nextButton.setText("Next");
        }

        // Click Listeners
        holder.nextButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNextClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        final OnboardingSlideBinding binding;
        ImageView illustration;
        TextView title;
        TextView subtitle;
        MaterialButton nextButton;
        View dot1, dot2, dot3;

        OnboardingViewHolder(OnboardingSlideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            illustration = binding.illustration;
            title = binding.title;
            subtitle = binding.subtitle;
            nextButton = binding.nextButton;
            dot1 = binding.dot1;
            dot2 = binding.dot2;
            dot3 = binding.dot3;
        }
    }
}