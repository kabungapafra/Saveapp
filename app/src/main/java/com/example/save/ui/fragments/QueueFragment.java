package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.save.databinding.FragmentQueueBinding;
import com.example.save.data.models.Member;
import com.example.save.ui.adapters.PayoutQueueAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;

public class QueueFragment extends Fragment {

    private FragmentQueueBinding binding;
    private PayoutQueueAdapter adapter;
    private MembersViewModel viewModel;

    public static QueueFragment newInstance() {
        return new QueueFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentQueueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeData();
        setupGenericInteractions();
    }

    private void setupGenericInteractions() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }

    private void setupRecyclerView() {
        String payoutDate = requireContext()
                .getSharedPreferences("SaveAppPrefs", android.content.Context.MODE_PRIVATE)
                .getString("sched_payout_date", "TBD");
        double payoutAmount = viewModel.getPayoutAmount();
        adapter = new PayoutQueueAdapter(new ArrayList<>(), true, payoutAmount, payoutDate);
        binding.rvQueue.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvQueue.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members == null || members.isEmpty()) {
                binding.rvQueue.setVisibility(View.GONE);
                if (binding.emptyState != null) binding.emptyState.setVisibility(View.VISIBLE);
                return;
            }
            binding.rvQueue.setVisibility(View.VISIBLE);
            if (binding.emptyState != null) binding.emptyState.setVisibility(View.GONE);

            String payoutDate = requireContext()
                    .getSharedPreferences("SaveAppPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("sched_payout_date", "TBD");
            adapter.updateList(members, payoutDate);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
