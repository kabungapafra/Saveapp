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

    // --- Simplified Read-Only Adapter ---
    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private List<Member> list;

        MemberAdapter(List<Member> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMemberSimpleBinding itemBinding = ItemMemberSimpleBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MemberViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            Member member = list.get(position);
            holder.tvName.setText(member.getName());
            holder.tvRole.setText(member.getRole());

            // Status Indicator Color
            int color = member.isActive() ? holder.itemView.getContext().getColor(android.R.color.holo_green_dark)
                    : holder.itemView.getContext().getColor(android.R.color.darker_gray);
            holder.statusView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvRole;
            View statusView;

            MemberViewHolder(ItemMemberSimpleBinding itemBinding) {
                super(itemBinding.getRoot());
                tvName = itemBinding.tvMemberName;
                tvRole = itemBinding.tvMemberRole;
                statusView = itemBinding.statusIndicator;
            }
        }
    }
}
