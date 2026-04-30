package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.save.databinding.FragmentOtpBinding;

/**
 * OtpFragment - Fully Static Version
 * No Database, No Network.
 */
public class OtpFragment extends Fragment {
    private FragmentOtpBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOtpBinding.inflate(inflater, container, false);
        
        binding.verifyButton.setOnClickListener(v -> {
            binding.verifyButton.setEnabled(false);
            new Handler().postDelayed(() -> {
                Toast.makeText(getContext(), "OTP Verified!", Toast.LENGTH_SHORT).show();
                // Navigate forward
                binding.verifyButton.setEnabled(true);
            }, 1000);
        });

        binding.resendText.setOnClickListener(v -> {
            Toast.makeText(getContext(), "OTP Resent!", Toast.LENGTH_SHORT).show();
        });

        return binding.getRoot();
    }
}