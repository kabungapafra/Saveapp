package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.databinding.FragmentApprovalReviewBinding;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.Locale;

public class ApprovalReviewFragment extends Fragment {

    private FragmentApprovalReviewBinding binding;
    private MembersViewModel viewModel;
    private String requestId;
    private String type;
    private String name;
    private double amount;
    private String description;
    private String date;
    private String status;

    public static ApprovalReviewFragment newInstance(ApprovalsAdapter.ApprovalItem item) {
        ApprovalReviewFragment fragment = new ApprovalReviewFragment();
        Bundle args = new Bundle();
        args.putString("id", item.getId());
        args.putString("type", item.getType());
        args.putString("name", item.getTitle());
        args.putDouble("amount", item.getAmount());
        args.putString("description", item.getDescription());
        args.putLong("date", item.getDate().getTime());
        args.putString("status", item.getStatus());
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentApprovalReviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        if (getArguments() != null) {
            requestId = getArguments().getString("id");
            type = getArguments().getString("type");
            name = getArguments().getString("name");
            amount = getArguments().getDouble("amount");
            description = getArguments().getString("description");
            long dateMillis = getArguments().getLong("date");
            status = getArguments().getString("status");

            binding.tvApplicantName.setText(name);
            binding.tvAmount.setText("UGX " + String.format(Locale.US, "%,.0f", amount));
            binding.tvTypeBadge.setText(type + " REQUEST");
            binding.tvDescription.setText(description != null ? description : "No description provided");
            binding.tvDate.setText(new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new java.util.Date(dateMillis)));
            binding.tvStatus.setText(status);
        }

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.btnApprove.setOnClickListener(v -> {
            viewModel.processApproval(new MockApprovalItem(), true, (success, message) -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) requireActivity().onBackPressed();
                }
            });
        });

        binding.btnDecline.setOnClickListener(v -> {
             viewModel.processApproval(new MockApprovalItem(), false, (success, message) -> {
                if (isAdded()) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    if (success) requireActivity().onBackPressed();
                }
            });
        });
    }

    private class MockApprovalItem implements ApprovalsAdapter.ApprovalItem {
        @Override public String getId() { return requestId; }
        @Override public String getType() { return type; }
        @Override public String getTitle() { return name; }
        @Override public double getAmount() { return amount; }
        @Override public String getDescription() { return description; }
        @Override public java.util.Date getDate() { return new java.util.Date(); }
        @Override public String getStatus() { return status; }
        @Override public boolean hasApproved() { return false; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
