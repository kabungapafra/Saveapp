package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Poll;
import com.example.save.data.models.PollNominee;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.CastVoteRequestBody;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CastVoteFragment extends Fragment {

    private static final int SELECTION_NONE = 0;
    private static final int SELECTION_NOMINEE = 1;
    private static final int SELECTION_ABSTAIN = 2;

    private int currentSelection = SELECTION_NONE;
    private Poll currentPoll = null;
    private int totalMembers = 1;

    // Views
    private ImageView radioAbstain;
    private View cardAbstain;
    private TextView tvGroupName, tvPollTitle, tvVoteProgress, tvTimeRemaining;
    private LinearProgressIndicator pbVoting;
    private View emptyState, loadingIndicator;
    private RecyclerView rvNominees;
    private NomineeAdapter nomineeAdapter;

    public static CastVoteFragment newInstance() {
        return new CastVoteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cast_vote, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardAbstain = view.findViewById(R.id.cardAbstain);
        radioAbstain = view.findViewById(R.id.radioAbstain);
        tvGroupName = view.findViewById(R.id.tvGroupName);
        tvPollTitle = view.findViewById(R.id.tvPollTitle);
        tvVoteProgress = view.findViewById(R.id.tvVoteProgress);
        tvTimeRemaining = view.findViewById(R.id.tvTimeRemaining);
        pbVoting = view.findViewById(R.id.pbVoting);
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);
        rvNominees = view.findViewById(R.id.rvNominees);

        nomineeAdapter = new NomineeAdapter();
        rvNominees.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNominees.setAdapter(nomineeAdapter);
        rvNominees.setNestedScrollingEnabled(false);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        view.findViewById(R.id.btnInfo).setOnClickListener(v ->
                Toast.makeText(getContext(), "Election Rules", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnSubmitVote).setOnClickListener(this::onSubmit);

        cardAbstain.setOnClickListener(v -> {
            nomineeAdapter.clearSelection();
            currentSelection = SELECTION_ABSTAIN;
            radioAbstain.setImageResource(R.drawable.ic_check_circle_blue);
        });

        MembersViewModel membersViewModel =
                new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        membersViewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null && !members.isEmpty()) totalMembers = members.size();
        });

        loadActivePoll();
    }

    private void loadActivePoll() {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);

        RetrofitClient.getClient(requireContext())
                .create(ApiService.class)
                .getPolls()
                .enqueue(new Callback<List<Poll>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Poll>> call,
                                           @NonNull Response<List<Poll>> response) {
                        if (!isAdded()) return;
                        if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            for (Poll p : response.body()) {
                                if ("active".equals(p.getStatus()) && !p.isHasVoted()) {
                                    bindPoll(p);
                                    return;
                                }
                            }
                        }
                        showEmpty();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Poll>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        if (loadingIndicator != null) loadingIndicator.setVisibility(View.GONE);
                        showEmpty();
                    }
                });
    }

    private void bindPoll(Poll poll) {
        currentPoll = poll;

        // Group name label
        String group = poll.getGroupName();
        tvGroupName.setText(group != null && !group.isEmpty() ? group.toUpperCase() : "CURRENT ELECTION");

        // Poll title/role
        tvPollTitle.setText("Nomination for " + poll.getRole());

        // Participation progress bar
        int voted = poll.getTotalVotes();
        int total = Math.max(totalMembers, 1);
        int pct = Math.min((int) ((voted / (float) total) * 100), 100);
        tvVoteProgress.setText(voted + " / " + total + " voted");
        pbVoting.setProgress(pct);

        // Time remaining countdown
        String timeLeft = formatTimeRemaining(poll.getExpiresAt());
        if (tvTimeRemaining != null) {
            tvTimeRemaining.setText(timeLeft);
            tvTimeRemaining.setVisibility(timeLeft.isEmpty() ? View.GONE : View.VISIBLE);
        }

        // Populate nominees RecyclerView
        List<PollNominee> nominees = poll.getNominees();
        nomineeAdapter.setNominees(nominees != null ? nominees : new ArrayList<>());
    }

    private void showEmpty() {
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (rvNominees != null) rvNominees.setVisibility(View.GONE);
    }

    private void onSubmit(View v) {
        if (currentSelection == SELECTION_NONE) {
            Toast.makeText(getContext(), "Please select an option to cast your vote.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentSelection == SELECTION_ABSTAIN) {
            Toast.makeText(getContext(), "You chose to abstain.", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (getActivity() != null) getActivity().onBackPressed();
            }, 800);
            return;
        }

        String nomineeId = nomineeAdapter.getSelectedNomineeId();
        if (currentPoll == null || nomineeId == null) {
            Toast.makeText(getContext(), "Please select a nominee.", Toast.LENGTH_SHORT).show();
            return;
        }

        v.setEnabled(false);
        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start();

        RetrofitClient.getClient(requireContext())
                .create(ApiService.class)
                .castVote(currentPoll.getId(), new CastVoteRequestBody(nomineeId))
                .enqueue(new Callback<Poll>() {
                    @Override
                    public void onResponse(@NonNull Call<Poll> call, @NonNull Response<Poll> response) {
                        if (!isAdded()) return;
                        v.setEnabled(true);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Vote successfully cast!", Toast.LENGTH_LONG).show();
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (getActivity() != null) getActivity().onBackPressed();
                            }, 1000);
                        } else {
                            Toast.makeText(getContext(), "Could not cast vote. Try again.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Poll> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        v.setEnabled(true);
                        Toast.makeText(getContext(), "Network error. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ── Time formatting ───────────────────────────────────────────────────────

    private static String formatTimeRemaining(String expiresAt) {
        if (expiresAt == null || expiresAt.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date expiry = sdf.parse(expiresAt);
            if (expiry == null) return "";
            long diffMs = expiry.getTime() - System.currentTimeMillis();
            if (diffMs <= 0) return "Expired";
            long diffHours = diffMs / (1000 * 60 * 60);
            long diffMins = (diffMs % (1000 * 60 * 60)) / (1000 * 60);
            if (diffHours < 1) {
                if (diffMins < 5) return "Closes soon";
                return diffMins + "m left";
            }
            return diffHours + "h " + diffMins + "m left";
        } catch (Exception e) {
            return "";
        }
    }

    // ── Initials helper ───────────────────────────────────────────────────────

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // ── Inner adapter ─────────────────────────────────────────────────────────

    class NomineeAdapter extends RecyclerView.Adapter<NomineeAdapter.VH> {

        private List<PollNominee> nominees = new ArrayList<>();
        private int selectedIndex = -1;
        private String selectedNomineeId = null;

        private final int[] avatarBgs = {
            R.drawable.bg_avatar_purple,
            R.drawable.bg_avatar_blue,
            R.drawable.bg_avatar_peach,
            R.drawable.bg_badge_lavender
        };

        void setNominees(List<PollNominee> list) {
            nominees = list;
            selectedIndex = -1;
            selectedNomineeId = null;
            notifyDataSetChanged();
        }

        @Nullable
        String getSelectedNomineeId() {
            return selectedNomineeId;
        }

        boolean hasSelection() {
            return selectedIndex >= 0;
        }

        void clearSelection() {
            selectedIndex = -1;
            selectedNomineeId = null;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cast_vote_nominee, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            PollNominee nominee = nominees.get(pos);
            boolean selected = (pos == selectedIndex);

            h.avatarBg.setBackgroundResource(avatarBgs[pos % avatarBgs.length]);
            h.tvNomineeInitials.setText(initials(nominee.getMemberName()));
            h.tvNomineeName.setText(nominee.getMemberName());
            h.tvNomineeRole.setText(currentPoll != null ? currentPoll.getRole() : "");
            h.ivRadio.setImageResource(selected
                    ? R.drawable.ic_check_circle_blue : R.drawable.bg_circle_outline_gray);

            h.itemView.setOnClickListener(v -> {
                int newPos = h.getAdapterPosition();
                if (newPos == RecyclerView.NO_POSITION) return;
                selectedIndex = newPos;
                selectedNomineeId = nominees.get(newPos).getId();
                currentSelection = SELECTION_NOMINEE;
                // Deselect abstain radio
                radioAbstain.setImageResource(R.drawable.bg_circle_outline_gray);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return nominees.size();
        }

        class VH extends RecyclerView.ViewHolder {
            RelativeLayout avatarBg;
            TextView tvNomineeInitials, tvNomineeName, tvNomineeRole;
            ImageView ivRadio;

            VH(View v) {
                super(v);
                avatarBg = v.findViewById(R.id.avatarBg);
                tvNomineeInitials = v.findViewById(R.id.tvNomineeInitials);
                tvNomineeName = v.findViewById(R.id.tvNomineeName);
                tvNomineeRole = v.findViewById(R.id.tvNomineeRole);
                ivRadio = v.findViewById(R.id.ivRadio);
            }
        }
    }
}
