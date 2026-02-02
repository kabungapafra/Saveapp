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
    private MembersViewModel viewModel;
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
        viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupRecyclerView();
        observeDetail();
        setupGenericInteractions();
    }

    private void setupGenericInteractions() {
        binding.btnBack.setOnClickListener(
                v -> ((com.example.save.ui.activities.MemberMainActivity) requireActivity()).switchToDashboard());
    }

    private void setupRecyclerView() {
        binding.rvQueue.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        android.content.SharedPreferences adminPrefs = requireActivity().getSharedPreferences("SaveAppPrefs",
                android.content.Context.MODE_PRIVATE);
        String schedPayoutDate = adminPrefs.getString("sched_payout_date", "TBD");

        // Initialize with empty list, full queue mode, and default payout
        adapter = new com.example.save.ui.adapters.PayoutQueueAdapter(new java.util.ArrayList<>(), true, 500000,
                schedPayoutDate);
        binding.rvQueue.setAdapter(adapter);
    }

    private void observeDetail() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                // Determine next recipient (first active member who hasn't received payout)
                Member nextRecipient = null;
                List<Member> queueList = new ArrayList<>();

                for (Member member : members) {
                    if (member.isActive() && !member.hasReceivedPayout()) {
                        if (nextRecipient == null) {
                            nextRecipient = member;
                        }
                        queueList.add(member);
                    }
                }

                // Update Top Card
                if (nextRecipient != null) {
                    binding.tvNextRecipient.setText(nextRecipient.getName());

                    // Use Admin Schedule if set
                    android.content.SharedPreferences adminPrefs = requireActivity()
                            .getSharedPreferences("SaveAppPrefs", android.content.Context.MODE_PRIVATE);
                    String schedPayoutDate = adminPrefs.getString("sched_payout_date",
                            nextRecipient.getNextPayoutDate());
                    binding.tvNextDate.setText(schedPayoutDate);
                } else {
                    binding.tvNextRecipient.setText("No pending payouts");
                    binding.tvNextDate.setText("-");
                }

                // Update List
                if (adapter != null) {
                    android.content.SharedPreferences adminPrefs = requireActivity()
                            .getSharedPreferences("SaveAppPrefs", android.content.Context.MODE_PRIVATE);
                    String schedPayoutDate = adminPrefs.getString("sched_payout_date", "TBD");
                    adapter.updateList(queueList, schedPayoutDate);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
