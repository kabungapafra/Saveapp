package com.example.save.ui.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ChatFeedbackFragment extends Fragment {

    private List<LinearLayout> ratingContainers = new ArrayList<>();
    private List<ImageView> emojis = new ArrayList<>();
    private List<TextView> labels = new ArrayList<>();
    private int selectedRating = 4; // Default to GREAT as per prompt

    public static ChatFeedbackFragment newInstance() {
        return new ChatFeedbackFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_feedback, container, false);
        initViews(view);
        setupSelection(selectedRating);
        return view;
    }

    private void initViews(View view) {
        ratingContainers.add(view.findViewById(R.id.llRate1));
        ratingContainers.add(view.findViewById(R.id.llRate2));
        ratingContainers.add(view.findViewById(R.id.llRate3));
        ratingContainers.add(view.findViewById(R.id.llRate4));
        ratingContainers.add(view.findViewById(R.id.llRate5));

        emojis.add(view.findViewById(R.id.ivEmoji1));
        emojis.add(view.findViewById(R.id.ivEmoji2));
        emojis.add(view.findViewById(R.id.ivEmoji3));
        emojis.add(view.findViewById(R.id.ivEmoji4));
        emojis.add(view.findViewById(R.id.ivEmoji5));

        labels.add(view.findViewById(R.id.tvLabel1));
        labels.add(view.findViewById(R.id.tvLabel2));
        labels.add(view.findViewById(R.id.tvLabel3));
        labels.add(view.findViewById(R.id.tvLabel4));
        labels.add(view.findViewById(R.id.tvLabel5));

        for (int i = 0; i < ratingContainers.size(); i++) {
            final int index = i + 1;
            ratingContainers.get(i).setOnClickListener(v -> setupSelection(index));
        }

        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupSelection(int rating) {
        selectedRating = rating;
        int activeColor = Color.parseColor("#1A3CCC");
        int inactiveColor = Color.parseColor("#9CA3AF");

        for (int i = 0; i < emojis.size(); i++) {
            int currentPos = i + 1;
            if (currentPos == selectedRating) {
                // Highlighted
                emojis.get(i).setImageTintList(null); // Full color
                labels.get(i).setTextColor(activeColor);
                emojis.get(i).setScaleX(1.1f);
                emojis.get(i).setScaleY(1.1f);
            } else {
                // Desaturated
                emojis.get(i).setImageTintList(ColorStateList.valueOf(inactiveColor));
                labels.get(i).setTextColor(inactiveColor);
                emojis.get(i).setScaleX(1.0f);
                emojis.get(i).setScaleY(1.0f);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).setBottomNavVisible(false);
            } else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(false);
                ((MemberMainActivity) getActivity()).setHeaderVisible(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setHeaderVisible(true);
            }
            // Restore system bars (Status Bar & System Nav)
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
            } else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(true);
            }
        }
    }
}
