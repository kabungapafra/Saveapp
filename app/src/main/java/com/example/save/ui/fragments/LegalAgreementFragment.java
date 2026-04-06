package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminSetupWizardActivity;

public class LegalAgreementFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_legal_agreement, container, false);

        // Header Navigation
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof AdminSetupWizardActivity) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Tabs
        View tabTerms = view.findViewById(R.id.tabTerms);
        View tabPrivacy = view.findViewById(R.id.tabPrivacy);
        
        if (tabTerms != null && tabPrivacy != null) {
            tabTerms.setOnClickListener(v -> {
                tabTerms.setBackgroundResource(R.drawable.bg_tab_selected_legal);
                ((android.widget.TextView)tabTerms).setTextColor(getResources().getColor(R.color.blue_600));
                ((android.widget.TextView)tabTerms).setTypeface(null, android.graphics.Typeface.BOLD);

                tabPrivacy.setBackground(null);
                ((android.widget.TextView)tabPrivacy).setTextColor(getResources().getColor(R.color.gray_500));
                ((android.widget.TextView)tabPrivacy).setTypeface(null, android.graphics.Typeface.NORMAL);
            });

            tabPrivacy.setOnClickListener(v -> {
                tabPrivacy.setBackgroundResource(R.drawable.bg_tab_selected_legal);
                ((android.widget.TextView)tabPrivacy).setTextColor(getResources().getColor(R.color.blue_600));
                ((android.widget.TextView)tabPrivacy).setTypeface(null, android.graphics.Typeface.BOLD);

                tabTerms.setBackground(null);
                ((android.widget.TextView)tabTerms).setTextColor(getResources().getColor(R.color.gray_500));
                ((android.widget.TextView)tabTerms).setTypeface(null, android.graphics.Typeface.NORMAL);
            });
        }

        Button btnAccept = view.findViewById(R.id.btnAccept);
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                if (getActivity() instanceof AdminSetupWizardActivity) {
                    ((AdminSetupWizardActivity) getActivity()).nextStep();
                }
            });
        }

        return view;
    }
}
