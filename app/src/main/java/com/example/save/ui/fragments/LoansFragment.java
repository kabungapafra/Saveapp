package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.databinding.FragmentLoansBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.data.models.LoanRequest;

public class LoansFragment extends Fragment {

    private FragmentLoansBinding binding;
    private MembersViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoansBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupListeners();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.cardRequestLoan.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(com.example.save.R.id.fragment_container, new LoanApplicationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.cardPayLoan.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(com.example.save.R.id.fragment_container, new LoanPaymentFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
