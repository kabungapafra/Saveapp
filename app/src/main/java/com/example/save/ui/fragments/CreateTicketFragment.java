package com.example.save.ui.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.save.R;
import com.example.save.databinding.FragmentCreateTicketBinding;
import com.example.save.ui.activities.AdminMainActivity;
import com.example.save.ui.activities.MemberMainActivity;

public class CreateTicketFragment extends Fragment {

    private FragmentCreateTicketBinding binding;
    private int selectedCategoryId = R.id.rowGeneral; // Default selected

    public static CreateTicketFragment newInstance() {
        return new CreateTicketFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateTicketBinding.inflate(inflater, container, false);
        
        setupListeners();
        updateCategorySelection(); // Initialize selection UI
        
        return binding.getRoot();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            applyClickAnimation(v);
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.btnMenu.setOnClickListener(v -> {
            applyClickAnimation(v);
            Toast.makeText(getContext(), "Options coming soon", Toast.LENGTH_SHORT).show();
        });

        // Category Listeners
        binding.rowGeneral.setOnClickListener(v -> onCategorySelected(v));
        binding.rowPayment.setOnClickListener(v -> onCategorySelected(v));
        binding.rowLoan.setOnClickListener(v -> onCategorySelected(v));
        binding.rowSecurity.setOnClickListener(v -> onCategorySelected(v));

        binding.btnSubmit.setOnClickListener(v -> {
            applyClickAnimation(v);
            
            // Get selected category name
            String category = "General Inquiry";
            if (selectedCategoryId == R.id.rowPayment) category = "Payment Issue";
            else if (selectedCategoryId == R.id.rowLoan) category = "Loan Question";
            else if (selectedCategoryId == R.id.rowSecurity) category = "Account Security";

            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragment_container, TicketSuccessFragment.newInstance(category))
                    .commit();
            }
        });
    }

    private void onCategorySelected(View v) {
        applyClickAnimation(v);
        selectedCategoryId = v.getId();
        updateCategorySelection();
    }

    private void updateCategorySelection() {
        // Reset all rows
        resetRow(binding.rowGeneral, binding.iconGeneral, binding.textGeneral);
        resetRow(binding.rowPayment, binding.iconPayment, binding.textPayment);
        resetRow(binding.rowLoan, binding.iconLoan, binding.textLoan);
        resetRow(binding.rowSecurity, binding.iconSecurity, binding.textSecurity);

        // Highlight selected
        if (selectedCategoryId == R.id.rowGeneral) {
            highlightRow(binding.rowGeneral, binding.iconGeneral, binding.textGeneral);
        } else if (selectedCategoryId == R.id.rowPayment) {
            highlightRow(binding.rowPayment, binding.iconPayment, binding.textPayment);
        } else if (selectedCategoryId == R.id.rowLoan) {
            highlightRow(binding.rowLoan, binding.iconLoan, binding.textLoan);
        } else if (selectedCategoryId == R.id.rowSecurity) {
            highlightRow(binding.rowSecurity, binding.iconSecurity, binding.textSecurity);
        }
    }

    private void highlightRow(View row, View icon, View text) {
        if (row instanceof com.google.android.material.card.MaterialCardView) {
            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) row;
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#1A3CCC")));
            card.setStrokeWidth(4);
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        }
        if (icon instanceof android.widget.ImageView) {
            ((android.widget.ImageView) icon).setImageTintList(ColorStateList.valueOf(Color.parseColor("#1A3CCC")));
        }
        if (text instanceof android.widget.TextView) {
            ((android.widget.TextView) text).setTextColor(Color.parseColor("#1A3CCC"));
            ((android.widget.TextView) text).setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void resetRow(View row, View icon, View text) {
        if (row instanceof com.google.android.material.card.MaterialCardView) {
            com.google.android.material.card.MaterialCardView card = (com.google.android.material.card.MaterialCardView) row;
            card.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
            card.setStrokeWidth(2);
            card.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F8F9FA")));
        }
        if (icon instanceof android.widget.ImageView) {
            ((android.widget.ImageView) icon).setImageTintList(ColorStateList.valueOf(Color.parseColor("#64748B")));
        }
        if (text instanceof android.widget.TextView) {
            ((android.widget.TextView) text).setTextColor(Color.parseColor("#334155"));
            ((android.widget.TextView) text).setTypeface(null, android.graphics.Typeface.NORMAL);
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
        if (getActivity() != null) {
            if (getActivity() instanceof AdminMainActivity) {
                ((AdminMainActivity) getActivity()).setBottomNavVisible(true);
            } else if (getActivity() instanceof MemberMainActivity) {
                ((MemberMainActivity) getActivity()).setBottomNavVisible(true);
            }
        }
        binding = null;
    }

    private void applyClickAnimation(View v) {
        if (getContext() != null) {
            v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_press));
        }
    }
}
