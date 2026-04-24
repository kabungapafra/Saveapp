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

import com.example.save.databinding.FragmentQueueBinding;
import com.example.save.data.models.Member;
import com.example.save.ui.adapters.MemberAdapter;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.List;

public class QueueFragment extends Fragment {

    private FragmentQueueBinding binding;
    private com.example.save.ui.adapters.PayoutQueueAdapter adapter;

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
        MembersViewModel viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeDetail();
        setupGenericInteractions();
    }

    private void setupGenericInteractions() {
        binding.btnBack.setOnClickListener(
                v -> ((com.example.save.ui.activities.MemberMainActivity) requireActivity()).switchToDashboard());
    }

    private void setupRecyclerView() {
        // Disabled for static UI
    }

    private void observeDetail() {
        // Disabled for static UI
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
