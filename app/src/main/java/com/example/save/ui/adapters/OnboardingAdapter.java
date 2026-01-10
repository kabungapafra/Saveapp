package com.example.save.ui.adapters;
import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.databinding.OnboardingSlideBinding;
import com.example.save.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> items;
    private final OnButtonClickListener listener;

    public interface OnButtonClickListener {
        void onNextClick(int position);

        void onBackClick(int position);
    }

    public OnboardingAdapter(List<OnboardingItem> items, OnButtonClickListener listener) {
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
        OnboardingItem item = items.get(position);

        holder.illustration.setImageResource(item.getImage());
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());

        // Update progress dots
        holder.dot1.setBackgroundResource(
                R.drawable.progress_dot_inactive);
        holder.dot2.setBackgroundResource(
                R.drawable.progress_dot_inactive);
        holder.dot3.setBackgroundResource(
                R.drawable.progress_dot_inactive);

        // Show/hide back button based on position
        if (position == 0) {
            holder.backButton.setVisibility(View.GONE);
        } else {
            holder.backButton.setVisibility(View.VISIBLE);
        }

        // Next button click
        holder.nextButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNextClick(position);
            }
        });

        // Back button click
        holder.backButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackClick(position);
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
        FloatingActionButton nextButton;
        FloatingActionButton backButton;
        View dot1, dot2, dot3;

        OnboardingViewHolder(OnboardingSlideBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            illustration = binding.illustration;
            title = binding.title;
            subtitle = binding.subtitle;
            nextButton = binding.nextButton;
            backButton = binding.backButton;
            dot1 = binding.dot1;
            dot2 = binding.dot2;
            dot3 = binding.dot3;
        }
    }
}