package com.example.save;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        OnboardingItem item = items.get(position);

        holder.illustration.setImageResource(item.getImage());
        holder.title.setText(item.getTitle());
        holder.subtitle.setText(item.getSubtitle());

        // Update progress dots
        holder.dot1.setBackgroundResource(
                R.drawable.progress_dot_inactive
        );
        holder.dot2.setBackgroundResource(
                R.drawable.progress_dot_inactive
        );
        holder.dot3.setBackgroundResource(
                R.drawable.progress_dot_inactive
        );

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
        ImageView illustration;
        TextView title;
        TextView subtitle;
        FloatingActionButton nextButton;
        FloatingActionButton backButton;
        View dot1, dot2, dot3;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            illustration = itemView.findViewById(R.id.illustration);
            title = itemView.findViewById(R.id.title);
            subtitle = itemView.findViewById(R.id.subtitle);
            nextButton = itemView.findViewById(R.id.nextButton);
            backButton = itemView.findViewById(R.id.backButton);
            dot1 = itemView.findViewById(R.id.dot1);
            dot2 = itemView.findViewById(R.id.dot2);
            dot3 = itemView.findViewById(R.id.dot3);
        }
    }
}