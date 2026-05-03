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
import com.example.save.databinding.FragmentGroupSettingsBinding;

public class GroupSettingsFragment extends Fragment {

    private FragmentGroupSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupSettingsBinding.inflate(inflater, container, false);

        setupButtons();
        setupToggles();

        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
        if (!session.isCreator()) {
            binding.btnDeleteGroup.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    private void setupButtons() {
        // No Back or Save buttons as per immersive redesign
        
        binding.btnInviteOthers.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Invite link copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        binding.btnDeleteGroup.setOnClickListener(v -> {
            applyClickAnimation(v);
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Group")
                .setMessage("Are you sure you want to delete this group? This will permanently remove all members, transactions, and group data. This action cannot be undone.")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    deleteGroup();
                })
                .setNegativeButton("CANCEL", null)
                .show();
        });
    }

    private void deleteGroup() {
        com.example.save.data.network.ApiService apiService = com.example.save.data.network.RetrofitClient.getClient(requireContext()).create(com.example.save.data.network.ApiService.class);
        apiService.deleteGroup().enqueue(new retrofit2.Callback<com.example.save.data.network.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.save.data.network.ApiResponse> call, retrofit2.Response<com.example.save.data.network.ApiResponse> response) {
                if (isAdded()) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(getContext(), "Group deleted successfully", Toast.LENGTH_LONG).show();
                        com.example.save.utils.SessionManager.getInstance(requireContext()).logoutUser();
                    } else {
                        String error = "Failed to delete group";
                        if (response.code() == 403) error = "Only the group creator can delete the group";
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.save.data.network.ApiResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupToggles() {
        binding.switchAutomaticPayouts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Automatic Payouts " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchScheduledContributions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Scheduled Contributions " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchSmartRoundups.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Smart Round-ups " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchAutomatedCycle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Automated Cycle " + state, Toast.LENGTH_SHORT).show();
        });

        binding.switchLoanRequests.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Loan Requests " + state, Toast.LENGTH_SHORT).show();
        });


        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean("group_push_notifications", isChecked).apply();
            String state = isChecked ? "enabled" : "disabled";
            Toast.makeText(getContext(), "Push Notifications " + state, Toast.LENGTH_SHORT).show();
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
