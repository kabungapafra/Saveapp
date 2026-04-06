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
    }

    private void loadProfileInfo() {
        if (getContext() == null) return;
        
        SessionManager session = SessionManager.getInstance(getContext());
        String userEmail = session.getUserEmail();

        if (userEmail == null) {
            Toast.makeText(getContext(), "Session error", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initially load from session
        String sessionName = session.getUserName();
        android.util.Log.d("ProfileInfo", "Session - Name: " + sessionName + ", Email: " + userEmail);
        
        binding.etFullName.setText(sessionName);
        binding.etEmail.setText(userEmail);

        // Fetch real data from DB
        viewModel.getMemberByEmailLive(userEmail).observe(getViewLifecycleOwner(), member -> {
            if (member != null) {
                android.util.Log.d("ProfileInfo", "Member found in DB: " + member.getName());
                binding.etFullName.setText(member.getName());
                binding.etPhone.setText(member.getPhone());
                binding.etEmail.setText(member.getEmail());
            } else {
                android.util.Log.d("ProfileInfo", "Member NOT found in DB. Falling back to Session/Prefs.");
                
                // For admin, it might be stored as admin_name in legacy prefs or just use session
                String fallbackName = session.getUserName();
                String fallbackPhone = session.getUserPhone();
                
                android.util.Log.d("ProfileInfo", "Fallback - Name: " + fallbackName + ", Phone: " + fallbackPhone);

                if (binding.etFullName.getText().toString().isEmpty() || "null".equals(binding.etFullName.getText().toString())) {
                    binding.etFullName.setText(fallbackName);
                }
                
                if (binding.etPhone.getText().toString().isEmpty()) {
                    binding.etPhone.setText(fallbackPhone);
                }
                
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
