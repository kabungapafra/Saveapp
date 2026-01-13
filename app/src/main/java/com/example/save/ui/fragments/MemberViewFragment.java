package com.example.save.ui.fragments;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.FragmentMemberViewBinding;
import com.example.save.databinding.ItemMemberSimpleBinding;
import com.example.save.ui.viewmodels.MembersViewModel; // Added import
import com.example.save.data.models.Member;
import androidx.lifecycle.ViewModelProvider; // Added import

import java.util.ArrayList;
import java.util.List;

public class MemberViewFragment extends Fragment {

    private FragmentMemberViewBinding binding;
    private MembersViewModel viewModel; // Use ViewModel
    private List<Member> memberList;
    private MemberAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberViewBinding.inflate(inflater, container, false);

        viewModel = new ViewModelProvider(this).get(MembersViewModel.class);

        setupRecyclerView();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupRecyclerView() {
        binding.membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        memberList = new ArrayList<>();
        adapter = new MemberAdapter(memberList);
        binding.membersRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                memberList.clear();
                memberList.addAll(members);
                updateMemberCount();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void updateMemberCount() {
        if (binding != null && binding.tvMemberCount != null) {
            binding.tvMemberCount.setText(memberList.size() + " Active Members");
        }
    }
}
