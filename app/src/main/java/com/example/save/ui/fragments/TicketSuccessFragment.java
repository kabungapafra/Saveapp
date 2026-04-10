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
            if (getActivity() instanceof MemberMainActivity) {
                // Return to dashboard
                // We could also pop until SupportFragment but returning home is typical for success flows
                // Pop all fragments to return to dashboard
                getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            } else if (getActivity() instanceof AdminMainActivity) {
                getParentFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
                ((MemberMainActivity) getActivity()).setHeaderVisible(false);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restoration is handled by the Activity's self-healing logic when transitioning back
        binding = null;
    }
}
