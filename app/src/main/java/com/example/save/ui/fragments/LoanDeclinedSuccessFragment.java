package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.save.R;

public class LoanDeclinedSuccessFragment extends Fragment {

    private static final String ARG_BORROWER = "borrower_name";
    private static final String ARG_AMOUNT = "loan_amount";
    private static final String ARG_REASON = "decline_reason";

    public static LoanDeclinedSuccessFragment newInstance(String borrower, String amount, String reason) {
        LoanDeclinedSuccessFragment fragment = new LoanDeclinedSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BORROWER, borrower);
        args.putString(ARG_AMOUNT, amount);
        args.putString(ARG_REASON, reason);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loan_declined_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String borrower = getArguments() != null ? getArguments().getString(ARG_BORROWER, "Marcus Wright") : "Marcus Wright";
        String amount = getArguments() != null ? getArguments().getString(ARG_AMOUNT, "$35,000.00") : "$35,000.00";
        String reason = getArguments() != null ? getArguments().getString(ARG_REASON, "Insufficient Group Savings") : "Insufficient Group Savings";

        ((TextView) view.findViewById(R.id.tvBorrowerName)).setText(borrower);
        ((TextView) view.findViewById(R.id.tvAmount)).setText(amount);
        ((TextView) view.findViewById(R.id.tvDeclineReason)).setText(reason);
        ((TextView) view.findViewById(R.id.tvDescription)).setText("The loan request for " + borrower + " has been successfully declined. The member will be notified with the reason provided.");

        // Animate Circle
        View declinedCircle = view.findViewById(R.id.declinedCircle);
        declinedCircle.setScaleX(0f);
        declinedCircle.setScaleY(0f);
        declinedCircle.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();

        view.findViewById(R.id.btnReturn).setOnClickListener(v -> {
            if (getActivity() != null) {
                // Clear backstack to return to a clean state, or just pop to dashboard
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });
    }
}
