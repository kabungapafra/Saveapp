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
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.ui.viewmodels.MembersViewModel;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveStandingsFragment extends Fragment {

    public static ActiveStandingsFragment newInstance() {
        return new ActiveStandingsFragment();
    }

    private RecyclerView rvStandings;
    private MembersViewModel viewModel;
    private StandingsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_standings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        // Back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Notifications button
        View btnBell = view.findViewById(R.id.btnBell);
        if (btnBell != null) btnBell.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                ((com.example.save.ui.activities.AdminMainActivity) getActivity()).showNotifications();
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).showNotifications();
            }
        });

        rvStandings = view.findViewById(R.id.rvStandings);
        if (rvStandings != null) {
            adapter = new StandingsAdapter();
            rvStandings.setLayoutManager(new LinearLayoutManager(getContext()));
            rvStandings.setAdapter(adapter);
        }

        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null && adapter != null) {
                // Sort by contribution paid descending
                List<Member> sorted = new ArrayList<>(members);
                sorted.sort((a, b) -> Double.compare(b.getContributionPaid(), a.getContributionPaid()));
                adapter.setMembers(sorted);
            }
        });
    }

    // Inner adapter
    private static class StandingsAdapter extends RecyclerView.Adapter<StandingsAdapter.VH> {
        private List<Member> members = new ArrayList<>();
        private static final String[] AVATAR_COLORS = {"#FBBF24","#F87171","#A78BFA","#34D399","#60A5FA"};

        void setMembers(List<Member> m) { this.members = m; notifyDataSetChanged(); }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_member_status_v2, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Member m = members.get(pos);
            if (h.tvName != null) h.tvName.setText(m.getName());
            if (h.tvRank != null) h.tvRank.setText("#" + (pos + 1));
            if (h.tvInitials != null) {
                String[] parts = m.getName().trim().split("\\s+");
                String initials = parts.length > 1
                        ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
                        : m.getName().substring(0, Math.min(2, m.getName().length())).toUpperCase();
                h.tvInitials.setText(initials);
            }
            if (h.tvAmount != null) {
                double amt = m.getContributionPaid();
                if (amt >= 1_000_000) h.tvAmount.setText(String.format(Locale.getDefault(), "UGX %.1fM", amt / 1_000_000));
                else if (amt >= 1000) h.tvAmount.setText(String.format(Locale.getDefault(), "UGX %.0fK", amt / 1000));
                else h.tvAmount.setText(String.format(Locale.getDefault(), "UGX %.0f", amt));
            }
            if (h.tvStatus != null) {
                String label = m.getReliabilityLabel();
                h.tvStatus.setText(label != null ? label : "—");
            }
            int colorIdx = Math.abs(m.getName().hashCode()) % AVATAR_COLORS.length;
            if (h.ivAvatar != null) {
                h.ivAvatar.setBackgroundTintList(
                        ColorStateList.valueOf(Color.parseColor(AVATAR_COLORS[colorIdx])));
            }
        }

        @Override public int getItemCount() { return members.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvInitials, tvAmount, tvStatus, tvRank;
            com.google.android.material.imageview.ShapeableImageView ivAvatar;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvMemberName);
                tvInitials = v.findViewById(R.id.tvMemberInitials);
                tvAmount = v.findViewById(R.id.tvMemberAmount);
                tvStatus = v.findViewById(R.id.tvMemberStatusPill);
                tvRank = v.findViewById(R.id.tvMemberTurnInfo);
                ivAvatar = v.findViewById(R.id.ivMemberAvatar);
            }
        }
    }
}
