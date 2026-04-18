package com.example.save.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentTicketSuccessBinding;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

public class TicketSuccessFragment extends Fragment {

    private FragmentTicketSuccessBinding binding;
    private static final String ARG_CATEGORY = "arg_category";

    public static TicketSuccessFragment newInstance(String category) {
        TicketSuccessFragment fragment = new TicketSuccessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTicketSuccessBinding.inflate(inflater, container, false);
        
        String category = "General Inquiry";
        if (getArguments() != null) {
            category = getArguments().getString(ARG_CATEGORY, "General Inquiry");
        }
        binding.tvCategoryName.setText(category);
        
        // Randomize ticket id slightly to feel real
        int randomId = new java.util.Random().nextInt(90000) + 10000;
        binding.tvTicketId.setText("#TK-" + randomId);

        setupListeners();
        
        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> closeAndReturn());
        binding.btnDone.setOnClickListener(v -> closeAndReturn());
        
        binding.btnStatus.setOnClickListener(v -> {
            applyClickAnimation(v);
            android.widget.Toast.makeText(getContext(), "Ticket status tracking coming soon", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void closeAndReturn() {
        if (getActivity() != null) {
            // Jump back to the start of the support flow
            boolean popped = getParentFragmentManager().popBackStackImmediate("SupportRoot", 0);
            if (!popped) {
                // Fallback if tag not found (safety)
                getParentFragmentManager().popBackStack();
            }
        }
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
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
                ((MemberMainActivity) getActivity()).setHeaderVisible();
            }

            // Immersive Zero-Bar Mode
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            // Restore System UI
            View decorView = getActivity().getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
