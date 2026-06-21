package com.example.save.ui.fragments;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.databinding.FragmentMemberSummaryBinding;
import com.example.save.databinding.ItemMemberConsistencyBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MemberSummaryFragment extends Fragment {

    private FragmentMemberSummaryBinding binding;

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

        setupRecyclerView();
        startAnimations();
    }

    private void setupRecyclerView() {
        binding.rvConsistency.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        List<MemberConsistency> data = new ArrayList<>();
        // TODO: Populate with real member consistency data from backend

        binding.rvConsistency.setAdapter(new ConsistencyAdapter(data));
    }

    private void startAnimations() {
        // [G.1] Staggered Entrance
        binding.heroCard.setAlpha(0f);
        binding.heroCard.setTranslationY(14f);
        binding.riskSection.setAlpha(0f);
        binding.riskSection.setTranslationY(14f);
        binding.alertCard.setAlpha(0f);
        binding.alertCard.setTranslationY(14f);
        binding.consistencySection.setAlpha(0f);
        binding.consistencySection.setTranslationY(14f);

        binding.heroCard.animate().alpha(1f).translationY(0f).setDuration(300).setStartDelay(0).start();
        binding.riskSection.animate().alpha(1f).translationY(0f).setDuration(300).setStartDelay(100).start();
        binding.alertCard.animate().alpha(1f).translationY(0f).setDuration(300).setStartDelay(150).start();
        binding.consistencySection.animate().alpha(1f).translationY(0f).setDuration(300).setStartDelay(200).start();

        // Get view model
        com.example.save.ui.viewmodels.MembersViewModel viewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(com.example.save.ui.viewmodels.MembersViewModel.class);
        com.example.save.utils.SessionManager session = com.example.save.utils.SessionManager.getInstance(requireContext().getApplicationContext());
        String phone = session.getUserPhone();

        // [G.2] Hero Counter
        viewModel.getDashboardSummary((success, summaryObj, message) -> {
            if (success && isAdded() && summaryObj instanceof com.example.save.data.models.DashboardSummaryResponse) {
                com.example.save.data.models.DashboardSummaryResponse summary = (com.example.save.data.models.DashboardSummaryResponse) summaryObj;
                requireActivity().runOnUiThread(() -> {
                    double balance = summary.getTotalBalance();
                    ValueAnimator savingsAnimator = ValueAnimator.ofInt(0, (int) balance);
                    savingsAnimator.setDuration(1400);
                    savingsAnimator.setInterpolator(new DecelerateInterpolator());
                    DecimalFormat df = new DecimalFormat("#,###,###");
                    savingsAnimator.addUpdateListener(animation -> {
                        binding.tvTotalSavings.setText(df.format(animation.getAnimatedValue()));
                    });
                    savingsAnimator.start();
                });
            }
        });

        // Current Member Stats (Score, Pulse)
        if (phone != null && !phone.isEmpty()) {
            viewModel.getMemberByPhoneLive(phone).observe(getViewLifecycleOwner(), member -> {
                if (member != null) {
                    int score = member.getCreditScore();
                    // Animate Score
                    ValueAnimator scoreAnimator = ValueAnimator.ofInt(0, score);
                    scoreAnimator.setDuration(800);
                    scoreAnimator.addUpdateListener(animation -> {
                        binding.tvScore.setText(animation.getAnimatedValue().toString());
                    });
                    scoreAnimator.start();

                    // Calculate Pulse percentage (300 to 850 range)
                    int pulseProgress = (int) (((Math.max(300, score) - 300) / 550.0) * 100);
                    binding.pulseProgressBar.setProgress(0);
                    ValueAnimator progressAnimator = ValueAnimator.ofInt(0, pulseProgress);
                    progressAnimator.setDuration(1000);
                    progressAnimator.setStartDelay(200);
                    progressAnimator.setInterpolator(new DecelerateInterpolator());
                    progressAnimator.addUpdateListener(animation -> {
                        binding.pulseProgressBar.setProgress((int) animation.getAnimatedValue());
                    });
                    progressAnimator.start();

                    binding.tvReliabilityLabel.setText(member.getReliabilityLabel());
                }
            });
        }

        // [G.6] Alert Card Pulse Border
        ObjectAnimator pulseAnimator = ObjectAnimator.ofArgb(binding.alertCard, "strokeColor", 
                requireContext().getColor(R.color.v_alert_border), 
                requireContext().getColor(R.color.v_red_missed));
        pulseAnimator.setDuration(2000);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.start();

        // Populate Alert and Consistency
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null && !members.isEmpty()) {
                List<com.example.save.data.models.Member> sortedMembers = new ArrayList<>(members);
                java.util.Collections.sort(sortedMembers, (m1, m2) -> Integer.compare(m2.getCreditScore(), m1.getCreditScore()));

                // Priority Risk Alert - find lowest score
                com.example.save.data.models.Member highestRisk = sortedMembers.get(sortedMembers.size() - 1);
                if (highestRisk.getCreditScore() < 600) {
                    binding.riskSection.setVisibility(View.VISIBLE);
                    binding.alertCard.setVisibility(View.VISIBLE);
                    binding.tvAlertName.setText(highestRisk.getName());
                    binding.tvAlertDescription.setText("Credit score dropped to " + highestRisk.getCreditScore());
                    
                    int missedRiskPct = 100 - (int) (((Math.max(300, highestRisk.getCreditScore()) - 300) / 550.0) * 100);
                    binding.tvAlertPercentage.setText(missedRiskPct + "%");
                } else {
                    binding.riskSection.setVisibility(View.GONE);
                    binding.alertCard.setVisibility(View.GONE);
                }

                // Populate Consistency List
                List<MemberConsistency> consistencyData = new ArrayList<>();
                for (int i = 0; i < sortedMembers.size(); i++) {
                    com.example.save.data.models.Member m = sortedMembers.get(i);
                    int[] segments = new int[6];
                    int score = m.getCreditScore();
                    
                    // Simulate history based on credit score
                    if (score >= 750) {
                        segments = new int[]{0, 0, 0, 0, 0, 0};
                    } else if (score >= 650) {
                        segments = new int[]{0, 0, 1, 0, 0, 0};
                    } else if (score >= 550) {
                        segments = new int[]{0, 1, 2, 0, 1, 0};
                    } else {
                        segments = new int[]{1, 2, 2, 1, 2, 1};
                    }
                    
                    int pulseProgress = (int) (((Math.max(300, score) - 300) / 550.0) * 100);
                    consistencyData.add(new MemberConsistency("#" + (i + 1), m.getName(), pulseProgress + "%", segments));
                }
                binding.rvConsistency.setAdapter(new ConsistencyAdapter(consistencyData));
            }
        });
    }

    private static class MemberConsistency {
        String rank;
        String name;
        String percentage;
        int[] segments; // 0=PAID, 1=LATE, 2=MISSED

        MemberConsistency(String rank, String name, String percentage, int[] segments) {
            this.rank = rank;
            this.name = name;
            this.percentage = percentage;
            this.segments = segments;
        }
    }

    private class ConsistencyAdapter extends RecyclerView.Adapter<ConsistencyAdapter.ViewHolder> {
        private final List<MemberConsistency> items;

        ConsistencyAdapter(List<MemberConsistency> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMemberConsistencyBinding itemBinding = ItemMemberConsistencyBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MemberConsistency member = items.get(position);
            holder.binding.tvRank.setText(member.rank);
            holder.binding.tvMemberName.setText(member.name);
            holder.binding.tvPercentage.setText(member.percentage);
            
            // Set segment colors based on status
            setSegmentColor(holder.binding.m1, member.segments[0]);
            setSegmentColor(holder.binding.m2, member.segments[1]);
            setSegmentColor(holder.binding.m3, member.segments[2]);
            setSegmentColor(holder.binding.m4, member.segments[3]);
            setSegmentColor(holder.binding.m5, member.segments[4]);
            setSegmentColor(holder.binding.m6, member.segments[5]);

            // [G.1] Staggered Row Animation
            holder.binding.getRoot().setAlpha(0f);
            holder.binding.getRoot().setTranslationY(14f);
            holder.binding.getRoot().animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(280L + (position * 80L))
                    .start();

            // [G.4] Segment Chart Animation
            animateSegment(holder.binding.m1, position, 0);
            animateSegment(holder.binding.m2, position, 1);
            animateSegment(holder.binding.m3, position, 2);
            animateSegment(holder.binding.m4, position, 3);
            animateSegment(holder.binding.m5, position, 4);
            animateSegment(holder.binding.m6, position, 5);
        }

        private void setSegmentColor(View view, int status) {
            int color;
            switch(status) {
                case 1: color = requireContext().getColor(R.color.v_brown_late); break;
                case 2: color = requireContext().getColor(R.color.v_red_missed); break;
                default: color = requireContext().getColor(R.color.v_blue); break;
            }
            view.setBackgroundTintList(ColorStateList.valueOf(color));
        }

        private void animateSegment(View view, int memberIndex, int segmentIndex) {
            view.setScaleX(0f);
            view.setAlpha(0f);
            view.animate()
                    .scaleX(1f)
                    .alpha(1f)
                    .setDuration(250)
                    .setStartDelay((memberIndex * 200L) + (segmentIndex * 60L))
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final ItemMemberConsistencyBinding binding;
            ViewHolder(ItemMemberConsistencyBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
