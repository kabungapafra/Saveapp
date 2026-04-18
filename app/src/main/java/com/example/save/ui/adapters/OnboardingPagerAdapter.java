package com.example.save.ui.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.save.databinding.ItemOnboardingWelcomeBinding;
import com.example.save.databinding.ItemOnboardingTransparencyBinding;
import com.example.save.databinding.ItemOnboardingSecureBinding;
import com.example.save.ui.activities.MemberLoginActivity;

public class OnboardingPagerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_WELCOME = 0;
    private static final int VIEW_TYPE_TRANSPARENCY = 1;
    private static final int VIEW_TYPE_SECURE = 2;

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_WELCOME:
                return new WelcomeViewHolder(ItemOnboardingWelcomeBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_TRANSPARENCY:
                return new TransparencyViewHolder(ItemOnboardingTransparencyBinding.inflate(inflater, parent, false));
            case VIEW_TYPE_SECURE:
                return new SecureViewHolder(ItemOnboardingSecureBinding.inflate(inflater, parent, false));
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SecureViewHolder) {
            ((SecureViewHolder) holder).binding.btnLoginRedirect.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), MemberLoginActivity.class);
                v.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class WelcomeViewHolder extends RecyclerView.ViewHolder {
        final ItemOnboardingWelcomeBinding binding;
        WelcomeViewHolder(ItemOnboardingWelcomeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class TransparencyViewHolder extends RecyclerView.ViewHolder {
        final ItemOnboardingTransparencyBinding binding;
        TransparencyViewHolder(ItemOnboardingTransparencyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class SecureViewHolder extends RecyclerView.ViewHolder {
        final ItemOnboardingSecureBinding binding;
        SecureViewHolder(ItemOnboardingSecureBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
