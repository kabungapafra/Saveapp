package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentWalletPaymentsBinding;

public class WalletPaymentsFragment extends Fragment {

    private FragmentWalletPaymentsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWalletPaymentsBinding.inflate(inflater, container, false);

        setupListeners();
        applyEntranceAnimations();

        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });


        // Placeholder listeners for cards
        View.OnClickListener cardListener = v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Account details coming soon", Toast.LENGTH_SHORT).show();
        };

        // Note: In the XML, I didn't set IDs for the cards yet, I'll update that if needed
        // but for now let's just ensure back button works as requested.
    }

    private void applyEntranceAnimations() {
        binding.getRoot().startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
