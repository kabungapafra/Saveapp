package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.save.R;
import com.example.save.data.models.Poll;
import com.example.save.data.models.PollNominee;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActiveStandingsFragment extends Fragment {

    public static ActiveStandingsFragment newInstance() {
        return new ActiveStandingsFragment();
    }

    private RecyclerView rvStandings;
    private View emptyState;
    private View loadingIndicator;
    private PollsAdapter adapter;
    private boolean isAdmin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_standings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        isAdmin = getActivity() instanceof com.example.save.ui.activities.AdminMainActivity;

        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        View btnBell = view.findViewById(R.id.btnBell);
        if (btnBell != null) btnBell.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
                ((com.example.save.ui.activities.AdminMainActivity) getActivity()).showNotifications();
            } else if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                ((com.example.save.ui.activities.MemberMainActivity) getActivity()).showNotifications();
            }
        });

        rvStandings = view.findViewById(R.id.rvStandings);
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        adapter = new PollsAdapter(isAdmin, this::endPoll);
        rvStandings.setLayoutManager(new LinearLayoutManager(getContext()));
        rvStandings.setAdapter(adapter);

        loadPolls();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPolls();
    }

    private void loadPolls() {
        if (loadingIndicator != null) loadingIndicator.setVisibility(View.VISIBLE);
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (rvStandings != null) rvStandings.setVisibility(View.GONE);

        RetrofitClient.getClient(requireContext())
                .create(ApiService.class)
                .getPolls()
                .enqueue(new Callback<List<Poll>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Poll>> call,
                                           @NonNull Response<List<Poll>> response) {
                        if (!isAdded()) return;
                        loadingIndicator.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Poll> active = new ArrayList<>();
                            for (Poll p : response.body()) {
                                if ("active".equals(p.getStatus())) active.add(p);
                            }
                            if (active.isEmpty()) {
                                showEmpty();
                            } else {
                                adapter.setPolls(active);
                                rvStandings.setVisibility(View.VISIBLE);
                            }
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Poll>> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        loadingIndicator.setVisibility(View.GONE);
                        showEmpty();
                        Toast.makeText(getContext(), "Could not load polls", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void endPoll(String pollId) {
        RetrofitClient.getClient(requireContext())
                .create(ApiService.class)
                .closePoll(pollId)
                .enqueue(new Callback<Poll>() {
                    @Override
                    public void onResponse(@NonNull Call<Poll> call, @NonNull Response<Poll> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Poll ended", Toast.LENGTH_SHORT).show();
                            loadPolls();
                        } else {
                            Toast.makeText(getContext(), "Could not end poll", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Poll> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showEmpty() {
        emptyState.setVisibility(View.VISIBLE);
        rvStandings.setVisibility(View.GONE);
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private static class PollsAdapter extends RecyclerView.Adapter<PollsAdapter.VH> {

        interface EndPollCallback { void onEnd(String pollId); }

        private List<Poll> polls = new ArrayList<>();
        private final boolean isAdmin;
        private final EndPollCallback endCallback;

        PollsAdapter(boolean isAdmin, EndPollCallback endCallback) {
            this.isAdmin = isAdmin;
            this.endCallback = endCallback;
        }

        void setPolls(List<Poll> list) {
            polls = list;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_poll, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Poll p = polls.get(pos);

            h.tvRole.setText(p.getRole() != null ? p.getRole().toUpperCase() : "POLL");
            h.tvTitle.setText(p.getTitle());
            h.tvGroupName.setText(p.getGroupName());
            h.tvCreatedBy.setText("Created by " + p.getCreatedByName());

            // Time remaining
            if (h.tvTimeRemaining != null) {
                String timeLeft = formatTimeRemaining(p.getExpiresAt());
                h.tvTimeRemaining.setText(timeLeft);
                h.tvTimeRemaining.setVisibility(timeLeft.isEmpty() ? View.GONE : View.VISIBLE);
            }

            // Per-nominee vote percentages
            h.nomineesContainer.removeAllViews();
            List<PollNominee> nominees = p.getNominees();
            int total = p.getTotalVotes();

            if (nominees != null) {
                for (PollNominee nominee : nominees) {
                    int votes = nominee.getVoteCount();
                    int pct = total > 0 ? Math.round((votes / (float) total) * 100) : 0;

                    View row = LayoutInflater.from(h.itemView.getContext())
                            .inflate(R.layout.item_nominee_vote_row, h.nomineesContainer, false);

                    ((TextView) row.findViewById(R.id.tvNomineeName))
                            .setText(nominee.getMemberName());
                    ((TextView) row.findViewById(R.id.tvVotePct))
                            .setText(String.format(Locale.getDefault(), "%d%%", pct));
                    LinearProgressIndicator pb = row.findViewById(R.id.pbNomineeVotes);
                    pb.setProgress(pct);

                    h.nomineesContainer.addView(row);
                }
            }

            // End Poll — admin only
            if (isAdmin) {
                h.btnEndPoll.setVisibility(View.VISIBLE);
                h.btnEndPoll.setOnClickListener(v -> endCallback.onEnd(p.getId()));
            } else {
                h.btnEndPoll.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return polls.size(); }

        static String formatTimeRemaining(String expiresAt) {
            if (expiresAt == null || expiresAt.isEmpty()) return "";
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
                sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date expiry = sdf.parse(expiresAt);
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

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRole, tvTitle, tvGroupName, tvCreatedBy, btnEndPoll, tvTimeRemaining;
            LinearLayout nomineesContainer;

            VH(View v) {
                super(v);
                tvRole = v.findViewById(R.id.tvPollRole);
                tvTitle = v.findViewById(R.id.tvPollTitle);
                tvGroupName = v.findViewById(R.id.tvPollGroupName);
                tvCreatedBy = v.findViewById(R.id.tvCreatedBy);
                btnEndPoll = v.findViewById(R.id.btnEndPoll);
                nomineesContainer = v.findViewById(R.id.nomineesVoteContainer);
                tvTimeRemaining = v.findViewById(R.id.tvTimeRemaining);
            }
        }
    }
}
