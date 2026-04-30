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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.save.R;
import com.example.save.databinding.FragmentApprovalsBinding;
import com.example.save.ui.adapters.ApprovalsAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.data.local.entities.TransactionEntity;
import com.example.save.data.models.LoanRequest;
import java.util.ArrayList;
import java.util.List;

public class ApprovalsFragment extends Fragment {

    private FragmentApprovalsBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentApprovalsBinding.inflate(inflater, container, false);

        setupNavigation();

        return binding.getRoot();
    }

    private void setupNavigation() {
        View.OnClickListener openPayoutDetail = v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PayoutConfirmationFragment())
                    .addToBackStack(null)
                    .commit();
        };

        binding.btnViewAlexander.setOnClickListener(openPayoutDetail);
        binding.btnViewVictoria.setOnClickListener(openPayoutDetail);
        binding.btnViewAris.setOnClickListener(openPayoutDetail);

        binding.btnRejectJulian.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DeclineLoanRequestFragment.newInstance("loan_julian", "Julian Wan", "$12,000", "Emergency Loan"))
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnRejectElena.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, DeclineLoanRequestFragment.newInstance("loan_elena", "Elena Stix", "$5,500", "Personal Loan"))
                    .addToBackStack(null)
                    .commit();
        });

        View.OnClickListener openConfirmApproval = v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ConfirmLoanApprovalFragment())
                    .addToBackStack(null)
                    .commit();
        };

        binding.btnAcceptJulian.setOnClickListener(openConfirmApproval);
        binding.btnAcceptElena.setOnClickListener(openConfirmApproval);
    }
}


