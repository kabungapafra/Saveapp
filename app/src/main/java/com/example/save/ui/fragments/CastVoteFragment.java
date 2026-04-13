package com.example.save.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;

public class CastVoteFragment extends Fragment {

    // Selection states
    private static final int SELECTION_NONE = 0;
    private static final int SELECTION_JORDAN = 1;
    private static final int SELECTION_ALEX = 2;
    private static final int SELECTION_ABSTAIN = 3;

    private int currentSelection = SELECTION_NONE;

    // Views
    private ImageView radioJordan, radioAlex, radioAbstain;
    private View cardJordan, cardAlex, cardAbstain;

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

        initViews(view);
        setupSelectionLogic();
        setupNavigation(view);
        setupSubmitButton(view);
    }

    private void initViews(View view) {
        cardJordan = view.findViewById(R.id.cardJordan);
        cardAlex = view.findViewById(R.id.cardAlex);
        cardAbstain = view.findViewById(R.id.cardAbstain);

        radioJordan = view.findViewById(R.id.radioJordan);
        radioAlex = view.findViewById(R.id.radioAlex);
        radioAbstain = view.findViewById(R.id.radioAbstain);
    }

    private void setupSelectionLogic() {
        cardJordan.setOnClickListener(v -> selectOption(SELECTION_JORDAN));
        cardAlex.setOnClickListener(v -> selectOption(SELECTION_ALEX));
        cardAbstain.setOnClickListener(v -> selectOption(SELECTION_ABSTAIN));
    }

    private void selectOption(int selection) {
        currentSelection = selection;

        // Reset all to unselected
        radioJordan.setImageResource(R.drawable.bg_circle_outline_gray);
        radioAlex.setImageResource(R.drawable.bg_circle_outline_gray);
        radioAbstain.setImageResource(R.drawable.bg_circle_outline_gray);

        // Set selected
        switch (selection) {
            case SELECTION_JORDAN:
                radioJordan.setImageResource(R.drawable.ic_check_circle_blue);
                break;
            case SELECTION_ALEX:
                radioAlex.setImageResource(R.drawable.ic_check_circle_blue);
                break;
            case SELECTION_ABSTAIN:
                radioAbstain.setImageResource(R.drawable.ic_check_circle_blue);
                break;
        }
    }

    private void setupSubmitButton(View view) {
        View btnSubmitVote = view.findViewById(R.id.btnSubmitVote);
        btnSubmitVote.setOnClickListener(v -> {
            if (currentSelection == SELECTION_NONE) {
                Toast.makeText(getContext(), "Please select an option to cast your vote.", Toast.LENGTH_SHORT).show();
                return;
            }

            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                Toast.makeText(getContext(), "✅ Vote successfully cast and encrypted!", Toast.LENGTH_LONG).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (getActivity() != null) getActivity().onBackPressed();
                }, 1000);
            }).start();
        });
    }

    private void setupNavigation(View view) {
        view.findViewById(R.id.btnClose).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
        view.findViewById(R.id.btnInfo).setOnClickListener(v ->
                Toast.makeText(getContext(), "Election Rules", Toast.LENGTH_SHORT).show());
    }

}
