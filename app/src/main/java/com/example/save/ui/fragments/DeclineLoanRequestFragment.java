package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.google.android.material.card.MaterialCardView;

public class DeclineLoanRequestFragment extends Fragment {

    private enum DeclineReason {
        INSUFFICIENT_SAVINGS,
        LOW_CREDIT_SCORE,
        INCOMPLETE_DOCUMENTATION,
        MAX_RISK,
        OTHER
    }

    private DeclineReason selectedReason = DeclineReason.INSUFFICIENT_SAVINGS;

    private MaterialCardView cardInsufficient, cardLowCredit, cardIncomplete, cardMaxRisk, cardOther;
    private ImageView radioInsufficient, radioLowCredit, radioIncomplete, radioMaxRisk, radioOther;
    private EditText etOtherReason, etFeedback;
    private TextView tvCharCounter;

    public static DeclineLoanRequestFragment newInstance() {
        return new DeclineLoanRequestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_decline_loan_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupReasonSelection();
        setupFeedbackCounter();
        setupButtons(view);
    }

    private void initViews(View view) {
        cardInsufficient = view.findViewById(R.id.cardReasonInsufficient);
        cardLowCredit = view.findViewById(R.id.cardReasonLowCredit);
        cardIncomplete = view.findViewById(R.id.cardReasonIncomplete);
        cardMaxRisk = view.findViewById(R.id.cardReasonMaxRisk);
        cardOther = view.findViewById(R.id.cardReasonOther);

        radioInsufficient = view.findViewById(R.id.radioInsufficient);
        radioLowCredit = view.findViewById(R.id.radioLowCredit);
        radioIncomplete = view.findViewById(R.id.radioIncomplete);
        radioMaxRisk = view.findViewById(R.id.radioMaxRisk);
        radioOther = view.findViewById(R.id.radioOther);

        etOtherReason = view.findViewById(R.id.etOtherReason);
        etFeedback = view.findViewById(R.id.etFeedback);
        tvCharCounter = view.findViewById(R.id.tvCharCounter);
    }

    private void setupReasonSelection() {
        cardInsufficient.setOnClickListener(v -> updateReason(DeclineReason.INSUFFICIENT_SAVINGS));
        cardLowCredit.setOnClickListener(v -> updateReason(DeclineReason.LOW_CREDIT_SCORE));
        cardIncomplete.setOnClickListener(v -> updateReason(DeclineReason.INCOMPLETE_DOCUMENTATION));
        cardMaxRisk.setOnClickListener(v -> updateReason(DeclineReason.MAX_RISK));
        cardOther.setOnClickListener(v -> updateReason(DeclineReason.OTHER));

        updateReasonUI();
    }

    private void updateReason(DeclineReason reason) {
        selectedReason = reason;
        updateReasonUI();
    }

    private void updateReasonUI() {
        resetCard(cardInsufficient, radioInsufficient);
        resetCard(cardLowCredit, radioLowCredit);
        resetCard(cardIncomplete, radioIncomplete);
        resetCard(cardMaxRisk, radioMaxRisk);
        resetCard(cardOther, radioOther);

        etOtherReason.setVisibility(selectedReason == DeclineReason.OTHER ? View.VISIBLE : View.GONE);

        switch (selectedReason) {
            case INSUFFICIENT_SAVINGS: selectCard(cardInsufficient, radioInsufficient); break;
            case LOW_CREDIT_SCORE: selectCard(cardLowCredit, radioLowCredit); break;
            case INCOMPLETE_DOCUMENTATION: selectCard(cardIncomplete, radioIncomplete); break;
            case MAX_RISK: selectCard(cardMaxRisk, radioMaxRisk); break;
            case OTHER: selectCard(cardOther, radioOther); break;
        }
    }

    private void selectCard(MaterialCardView card, ImageView radio) {
        card.setStrokeColor(getResources().getColor(R.color.blue_600)); // Use a defined blue color
        card.setStrokeWidth(dpToPx(2));
        radio.setImageResource(R.drawable.ic_radio_checked);
    }

    private void resetCard(MaterialCardView card, ImageView radio) {
        card.setStrokeColor(0xFFF1F5F9);
        card.setStrokeWidth(dpToPx(1));
        radio.setImageResource(R.drawable.ic_radio_unchecked);
    }

    private void setupFeedbackCounter() {
        etFeedback.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharCounter.setText(s.length() + " / 500");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtons(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        view.findViewById(R.id.btnConfirmDecline).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Loan Decline Confirmed", Toast.LENGTH_SHORT).show();
            // Implement actual logic here
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
