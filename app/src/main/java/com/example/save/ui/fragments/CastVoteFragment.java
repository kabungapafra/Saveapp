package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.Poll;
import com.example.save.data.models.PollNominee;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.CastVoteRequestBody;
import com.example.save.data.network.RetrofitClient;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CastVoteFragment extends Fragment {

    private static final int SELECTION_NONE = 0;
    private static final int SELECTION_NOMINEE1 = 1;
    private static final int SELECTION_NOMINEE2 = 2;
    private static final int SELECTION_ABSTAIN = 3;

    private int currentSelection = SELECTION_NONE;
    private String selectedNomineeId = null;
    private Poll currentPoll = null;
    private int totalMembers = 1;

    // Views
    private ImageView radioJordan, radioAlex, radioAbstain;
    private View cardJordan, cardAlex, cardAbstain;
    private TextView tvGroupName, tvPollTitle, tvVoteProgress;
    private LinearProgressIndicator pbVoting;
    private View emptyState, loadingIndicator;

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

        cardJordan = view.findViewById(R.id.cardJordan);
        cardAlex = view.findViewById(R.id.cardAlex);
        cardAbstain = view.findViewById(R.id.cardAbstain);
        radioJordan = view.findViewById(R.id.radioJordan);
        radioAlex = view.findViewById(R.id.radioAlex);
        radioAbstain = view.findViewById(R.id.radioAbstain);
        tvGroupName = view.findViewById(R.id.tvGroupName);
        tvPollTitle = view.findViewById(R.id.tvPollTitle);
        tvVoteProgress = view.findViewById(R.id.tvVoteProgress);
        pbVoting = view.findViewById(R.id.pbVoting);
        emptyState = view.findViewById(R.id.emptyState);
        loadingIndicator = view.findViewById(R.id.loadingIndicator);

        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        view.findViewById(R.id.btnInfo).setOnClickListener(v ->
                Toast.makeText(getContext(), "Election Rules", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btnSubmitVote).setOnClickListener(v -> onSubmit(v));

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

        // Nominees
        List<PollNominee> nominees = poll.getNominees();
        int count = nominees != null ? nominees.size() : 0;

        if (count >= 1) {
            PollNominee n1 = nominees.get(0);
            cardJordan.setVisibility(View.VISIBLE);
            ((TextView) cardJordan.findViewById(R.id.tvNominee1Name)).setText(n1.getMemberName());
            ((TextView) cardJordan.findViewById(R.id.tvNominee1Initials)).setText(initials(n1.getMemberName()));
            ((TextView) cardJordan.findViewById(R.id.tvNominee1Role)).setText(poll.getRole());
            cardJordan.setOnClickListener(v -> selectOption(SELECTION_NOMINEE1, n1.getId()));
        } else {
            cardJordan.setVisibility(View.GONE);
        }

        if (count >= 2) {
            PollNominee n2 = nominees.get(1);
            cardAlex.setVisibility(View.VISIBLE);
            ((TextView) cardAlex.findViewById(R.id.tvNominee2Name)).setText(n2.getMemberName());
            ((TextView) cardAlex.findViewById(R.id.tvNominee2Initials)).setText(initials(n2.getMemberName()));
            ((TextView) cardAlex.findViewById(R.id.tvNominee2Role)).setText(poll.getRole());
            cardAlex.setOnClickListener(v -> selectOption(SELECTION_NOMINEE2, n2.getId()));
        } else {
            cardAlex.setVisibility(View.GONE);
        }

        cardAbstain.setOnClickListener(v -> selectOption(SELECTION_ABSTAIN, null));
    }

    private void showEmpty() {
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        // Hide the voting UI
        cardJordan.setVisibility(View.GONE);
        cardAlex.setVisibility(View.GONE);
    }

    private void selectOption(int selection, String nomineeId) {
        currentSelection = selection;
        selectedNomineeId = nomineeId;

        radioJordan.setImageResource(R.drawable.bg_circle_outline_gray);
        radioAlex.setImageResource(R.drawable.bg_circle_outline_gray);
        radioAbstain.setImageResource(R.drawable.bg_circle_outline_gray);

        switch (selection) {
            case SELECTION_NOMINEE1: radioJordan.setImageResource(R.drawable.ic_check_circle_blue); break;
            case SELECTION_NOMINEE2: radioAlex.setImageResource(R.drawable.ic_check_circle_blue); break;
            case SELECTION_ABSTAIN:  radioAbstain.setImageResource(R.drawable.ic_check_circle_blue); break;
        }
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

        if (currentPoll == null || selectedNomineeId == null) return;

        v.setEnabled(false);
        v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() ->
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()).start();

        RetrofitClient.getClient(requireContext())
                .create(ApiService.class)
                .castVote(currentPoll.getId(), new CastVoteRequestBody(selectedNomineeId))
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

    private static String initials(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
