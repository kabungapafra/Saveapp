package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.save.databinding.FragmentNewPasswordBinding;

/**
 * NewPasswordFragment - Fully Static Version
 * No Database, No Network.
 */
public class NewPasswordFragment extends Fragment {
    private FragmentNewPasswordBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewPasswordBinding.inflate(inflater, container, false);
        
        binding.resetButton.setOnClickListener(v -> {
            binding.resetButton.setEnabled(false);
            new Handler().postDelayed(() -> {
                Toast.makeText(getContext(), "Password reset successful!", Toast.LENGTH_SHORT).show();
                binding.resetButton.setEnabled(true);
            }, 1000);
        });

        return binding.getRoot();
    }
}