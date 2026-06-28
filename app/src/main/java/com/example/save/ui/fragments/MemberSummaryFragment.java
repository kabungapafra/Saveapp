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

import com.example.save.databinding.FragmentMemberSummaryBinding;
import com.example.save.databinding.ItemMemberPerformanceBinding;
import com.example.save.data.models.DashboardSummaryResponse;
import com.example.save.data.models.Member;
import com.example.save.ui.viewmodels.MembersViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Top Contributors — full ranked list of members by amount contributed.
 * Reached from the Financial Analysis "Top Contributors" card, so it mirrors that
 * ranking (real contribution_paid vs. yearly target) rather than credit-score risk.
 */
public class MemberSummaryFragment extends Fragment {

    private FragmentMemberSummaryBinding binding;
    private MembersViewModel viewModel;
    private final ContributorAdapter adapter = new ContributorAdapter();

    // Per-member yearly target = contribution amount × 12 (from live config)
    private double cachedContribAmount = 0;

    public static MemberSummaryFragment newInstance() {
        return new MemberSummaryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        binding.rvConsistency.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvConsistency.setAdapter(adapter);

        loadSummary();
        observeMembers();
    }

    private void loadSummary() {
        viewModel.getDashboardSummary((success, summaryObj, message) -> {
            if (!success || !isAdded() || binding == null
                    || !(summaryObj instanceof DashboardSummaryResponse)) return;
            DashboardSummaryResponse s = (DashboardSummaryResponse) summaryObj;
            cachedContribAmount = s.getContributionAmount();
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.tvTotalContributions.setText(formatCurrency(s.getYearlyContributions()));
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void observeMembers() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (binding == null) return;
            List<Member> sorted = new ArrayList<>(members != null ? members : new ArrayList<>());
            Collections.sort(sorted, (a, b) -> Double.compare(b.getContributionPaid(), a.getContributionPaid()));
            adapter.setMembers(sorted);

            boolean empty = sorted.isEmpty();
            binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvConsistency.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.tvMemberCount.setText(empty
                    ? "Members ranked by amount contributed"
                    : sorted.size() + " members ranked by amount contributed");
        });
    }

    private String formatCurrency(double amount) {
        if (amount >= 1_000_000) return String.format(Locale.getDefault(), "UGX %.1fM", amount / 1_000_000);
        if (amount >= 1_000) return String.format(Locale.getDefault(), "UGX %.1fK", amount / 1_000);
        return String.format(Locale.getDefault(), "UGX %.0f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─── ADAPTER ──────────────────────────────────────────────────────────────

    private class ContributorAdapter extends RecyclerView.Adapter<ContributorAdapter.ViewHolder> {
        private List<Member> members = new ArrayList<>();

        void setMembers(List<Member> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemMemberPerformanceBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member m = members.get(position);
            holder.binding.tvMemberName.setText(m.getName() != null ? m.getName() : "—");
            if (m.getName() != null && !m.getName().isEmpty()) {
                holder.binding.tvAvatarInitials.setText(
                        m.getName().substring(0, Math.min(2, m.getName().length())).toUpperCase());
            }

            double yearlyTarget = cachedContribAmount > 0
                    ? cachedContribAmount * 12
                    : viewModel.getContributionTarget();
            int progress = (yearlyTarget > 0)
                    ? (int) Math.min(100, Math.max(0, (m.getContributionPaid() / yearlyTarget) * 100))
                    : 0;

            holder.binding.progressEfficiency.setProgress(progress);
            holder.binding.tvEfficiencyPct.setText(progress + "%");
            holder.binding.tvEfficiencyValue.setText(
                    String.format(Locale.getDefault(), "%.1f", m.getContributionPaid() / 1_000_000.0));
            holder.binding.tvMemberSub.setText("Rank #" + (position + 1));
        }

        @Override
        public int getItemCount() { return members.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemMemberPerformanceBinding binding;
            ViewHolder(ItemMemberPerformanceBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }
}
