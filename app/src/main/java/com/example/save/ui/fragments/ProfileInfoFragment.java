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
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.databinding.FragmentProfileInfoBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;

public class ProfileInfoFragment extends Fragment {

    private FragmentProfileInfoBinding binding;
    private MembersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        setupListeners();
        loadProfileInfo();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnChangePhoto.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Photo upload coming soon", Toast.LENGTH_SHORT).show();
        });

        if (binding.btnSaveProfile != null) {
            binding.btnSaveProfile.setOnClickListener(v -> {
                applyClickAnimation(v);
                Toast.makeText(getContext(), "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }

    private void loadProfileInfo() {
        if (getContext() == null) return;
        
        SessionManager session = SessionManager.getInstance(getContext());
        String userPhone = session.getUserPhone();

        if (userPhone == null || userPhone.isEmpty()) {
            // Fallback to email if phone is missing (unlikely now)
            userPhone = session.getUserEmail();
        }

        // Initially load from session
        String sessionName = session.getUserName();
        String sessionEmail = session.getUserEmail();
        
        binding.etFullName.setText(sessionName);
        binding.etPhone.setText(userPhone);
        binding.etEmail.setText(sessionEmail);

        // Fetch real data from DB
        viewModel.getMemberByPhoneLive(userPhone).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                binding.etFullName.setText(member.getName());
                binding.etPhone.setText(member.getPhone());
                binding.etEmail.setText(member.getEmail());
            } else {
                // Trigger a sync if no data found locally
                viewModel.syncMembers();
            }
        });
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
