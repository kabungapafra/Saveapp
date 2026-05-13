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
import com.example.save.databinding.FragmentPayoutConfirmationBinding;

public class PayoutConfirmationFragment extends Fragment {

    private FragmentPayoutConfirmationBinding binding;
    private String txId, amountStr;

    public static PayoutConfirmationFragment newInstance(String txId, String amount) {
        PayoutConfirmationFragment fragment = new PayoutConfirmationFragment();
        Bundle args = new Bundle();
        args.putString("tx_id", txId);
        args.putString("amount", amount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPayoutConfirmationBinding.inflate(inflater, container, false);
        
        if (getArguments() != null) {
            txId = getArguments().getString("tx_id");
            amountStr = getArguments().getString("amount");
            if (amountStr != null) {
                binding.tvPayoutAmount.setText(amountStr.replace("UGX ", ""));
                binding.tvCircleContribution.setText(amountStr + " UGX");
                binding.tvNetPayoutAmount.setText(amountStr + " UGX");
            }
        }

        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        binding.btnConfirmPayout.setOnClickListener(v -> {
            applyClickAnimation(v);
            
            com.example.save.ui.viewmodels.MembersViewModel viewModel = 
                new androidx.lifecycle.ViewModelProvider(requireActivity()).get(com.example.save.ui.viewmodels.MembersViewModel.class);
            
            com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext());
            
            if (txId != null) {
                viewModel.processApproval(new com.example.save.ui.adapters.ApprovalsAdapter.ApprovalItem() {
                    @Override public String getId() { return txId; }
                    @Override public String getType() { return "PAYOUT"; }
                    @Override public String getTitle() { return ""; }
                    @Override public double getAmount() { return 0; }
                    @Override public String getDescription() { return ""; }
                    @Override public java.util.Date getDate() { return new java.util.Date(); }
                    @Override public String getStatus() { return ""; }
                    @Override public boolean hasApproved() { return false; }
                }, true, (success, message) -> {
                    if (success) {
                        navigateToSuccess();
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                navigateToSuccess();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void navigateToSuccess() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragment_container, LoanApprovedSuccessFragment.newPayoutInstance("Recipient", amountStr))
                    .addToBackStack(null)
                    .commit();
        }
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
