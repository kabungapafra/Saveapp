package com.example.save.ui.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.LoanRequest;
import com.example.save.data.models.Member;
import com.example.save.databinding.FragmentLoanApplicationBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoanApplicationFragment extends Fragment {

    private FragmentLoanApplicationBinding binding;
    private MembersViewModel viewModel;

    private static final double INTEREST_RATE = 0.15;
    private static final double PROCESSING_FEE = 25000.0;

    private List<Member> availableMembers = new ArrayList<>();
    private List<Member> selectedGuarantors = new ArrayList<>();

    private int placeholderColorIndex = 0;
    private final String[] AVATAR_COLORS = {"#2563EB", "#7C3AED", "#059669", "#DC2626", "#D97706"};

    public static LoanApplicationFragment newInstance() {
        return new LoanApplicationFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoanApplicationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        setupInitialState();
        setupListeners();
        
        getParentFragmentManager().setFragmentResultListener("guarantor_request", getViewLifecycleOwner(), (requestKey, result) -> {
            java.util.ArrayList<String> ids = result.getStringArrayList("selected_guarantor_ids");
            if (ids != null) {
                // Hide mock chips
                binding.chipJaneDoe.setVisibility(View.GONE);
                binding.chipMikeKalema.setVisibility(View.GONE);
                
                // Clear existing dynamic chips
                int i = 0;
                while (i < binding.chipsContainer.getChildCount()) {
                    View child = binding.chipsContainer.getChildAt(i);
                    if (child != binding.chipJaneDoe && child != binding.chipMikeKalema && child != binding.btnAddGuarantor) {
                        binding.chipsContainer.removeView(child);
                    } else {
                        i++;
                    }
                }
                
                selectedGuarantors.clear();
                
                for (String id : ids) {
                    for (Member m : availableMembers) {
                        if (m.getId().equals(id)) {
                            selectedGuarantors.add(m);
                            String name = m.getName();
                            String[] parts = name.split(" ");
                            String initials = parts.length > 1 ? 
                                    (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase() : 
                                    (name.length() > 1 ? name.substring(0, 2).toUpperCase() : "G");
                            addGuarantorChip(initials, name, getNextColor());
                            break;
                        }
                    }
                }
            }
        });
        updateSummary();
        loadMembers();
    }

    private void setupInitialState() {
        // Wire up the mock chips included in the XML
        binding.removeChipJane.setOnClickListener(v -> binding.chipJaneDoe.setVisibility(View.GONE));
        binding.removeChipMike.setOnClickListener(v -> binding.chipMikeKalema.setVisibility(View.GONE));
        
        // Initial values matching XML defaults
        binding.seekDuration.setProgress(5); // Progress 5 = 6 Months Since max=11 -> 0-11
    }

    private void loadMembers() {
        viewModel.getMembers().observe(getViewLifecycleOwner(), members -> {
            if (members != null) {
                availableMembers.clear();
                availableMembers.addAll(members);
            }
        });
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.etLoanAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSummary();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.seekDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int months = progress + 1; // 0..11 -> 1..12
                binding.tvDurationLabel.setText(months + (months == 1 ? " Month" : " Months"));
                updateSummary();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.btnAddGuarantor.setOnClickListener(v -> showAddGuarantorDialog());

        binding.btnSubmitLoan.setOnClickListener(v -> submitApplication());
    }

    private void updateSummary() {
        String amountStr = binding.etLoanAmount.getText().toString().replaceAll("[^0-9]", "");
        double amount = amountStr.isEmpty() ? 0 : Double.parseDouble(amountStr);
        int duration = binding.seekDuration.getProgress() + 1; // 1 to 12

        double interest = amount * INTEREST_RATE;
        double total = amount + interest + PROCESSING_FEE;
        double monthly = duration > 0 ? (total / duration) : 0;

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        binding.tvSummaryInterest.setText("UGX " + fmt.format(interest));
        binding.tvSummaryFee.setText("UGX " + fmt.format(PROCESSING_FEE));
        binding.tvSummaryTotal.setText("UGX " + fmt.format(total));
        binding.tvSummaryMonthly.setText("UGX " + Math.round(monthly)); 
    }

    private void showAddGuarantorDialog() {
        if (availableMembers.isEmpty()) {
            addGuarantorChip("AN", "Alex Nkutu", getNextColor());
            return;
        }

        String[] memberNames = new String[availableMembers.size()];
        for (int i = 0; i < availableMembers.size(); i++) {
            memberNames[i] = availableMembers.get(i).getName();
        }

        // Navigate to AddGuarantorFragment instead of showing AlertDialog
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddGuarantorFragment())
                .addToBackStack(null)
                .commit();
    }

    private String getNextColor() {
        String color = AVATAR_COLORS[placeholderColorIndex % AVATAR_COLORS.length];
        placeholderColorIndex++;
        return color;
    }

    private void addGuarantorChip(String initials, String name, String colorHex) {
        LinearLayout chip = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(38)
        );
        params.setMarginEnd(dpToPx(10));
        chip.setLayoutParams(params);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER_VERTICAL);
        chip.setBackgroundResource(R.drawable.bg_guarantor_chip);
        chip.setPadding(dpToPx(10), 0, dpToPx(10), 0);

        // Avatar
        TextView avatar = new TextView(getContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dpToPx(26), dpToPx(26));
        avatarParams.setMarginEnd(dpToPx(8));
        avatar.setLayoutParams(avatarParams);
        avatar.setGravity(Gravity.CENTER);
        avatar.setText(initials);
        avatar.setTextColor(Color.WHITE);
        avatar.setTextSize(10);
        avatar.setTypeface(null, android.graphics.Typeface.BOLD);
        
        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor(colorHex));
        avatar.setBackground(avatarBg);
        chip.addView(avatar);

        // Name
        TextView nameView = new TextView(getContext());
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        nameView.setText(name);
        nameView.setTextColor(Color.parseColor("#1A1D2E"));
        nameView.setTextSize(13);
        chip.addView(nameView);

        // Remove button
        TextView removeBtn = new TextView(getContext());
        removeBtn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        removeBtn.setText("  ×");
        removeBtn.setTextColor(Color.parseColor("#9CA3AF"));
        removeBtn.setTextSize(16);
        removeBtn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        // Native Ripple
        TypedValue outValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        removeBtn.setBackgroundResource(outValue.resourceId);
        
        removeBtn.setOnClickListener(v -> binding.chipsContainer.removeView(chip));
        chip.addView(removeBtn);

        int addIndex = binding.chipsContainer.indexOfChild(binding.btnAddGuarantor);
        if (addIndex < 0) addIndex = binding.chipsContainer.getChildCount();
        binding.chipsContainer.addView(chip, addIndex);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void submitApplication() {
        String amountStr = binding.etLoanAmount.getText().toString().replaceAll("[^0-9]", "");
        int duration = binding.seekDuration.getProgress() + 1;
        String reason = binding.etPurpose.getText().toString().trim();

        if (amountStr.isEmpty() || Double.parseDouble(amountStr) <= 0) {
            Toast.makeText(getContext(), "Please enter a valid loan amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Just take the first selected guarantor if any, else default mock info needed for API
        String guarantorName = "Jane Doe";
        String guarantorPhone = "0700000000";
        if (!selectedGuarantors.isEmpty()) {
            guarantorName = selectedGuarantors.get(0).getName();
            guarantorPhone = selectedGuarantors.get(0).getPhone();
        }

        SessionManager session = SessionManager.getInstance(requireContext());
        String rawUserName = session.getUserName();
        final String finalUserName = (rawUserName != null) ? rawUserName : "Active Member";

        double amount = Double.parseDouble(amountStr);

        LoanRequest loanRequest = new LoanRequest(
                finalUserName,
                amount,
                duration,
                guarantorName,
                guarantorPhone,
                reason
        );

        // UI state for loading
        binding.btnSubmitLoan.setText("Submitting…");
        binding.btnSubmitLoan.setEnabled(false);
        binding.btnSubmitLoan.setAlpha(0.85f);

        viewModel.submitLoanRequest(loanRequest, (success, message) -> {
            if (success) {
                // Success visual state
                binding.btnSubmitLoan.setText("✓ Request Submitted!");
                binding.btnSubmitLoan.setAlpha(1.0f);
                binding.btnSubmitLoan.setBackgroundResource(0);
                binding.btnSubmitLoan.setBackgroundColor(Color.parseColor("#059669")); // Success Green
                
                binding.btnSubmitLoan.postDelayed(() -> {
                    if (isAdded() && getActivity() != null) {
                        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
                        String formattedAmount = "UGX " + fmt.format(amount);

                        LoanSubmittedSuccessFragment successFrag = LoanSubmittedSuccessFragment.newInstance(finalUserName, formattedAmount);
                        
                        // Use Activity navigation helper if available, else standard transaction
                        if (getActivity() instanceof com.example.save.ui.activities.MemberMainActivity) {
                            ((com.example.save.ui.activities.MemberMainActivity) getActivity()).loadFragment(successFrag, true);
                        } else {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, successFrag)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                }, 800);
            } else {
                binding.btnSubmitLoan.setText("Submit Loan Request  →");
                binding.btnSubmitLoan.setEnabled(true);
                binding.btnSubmitLoan.setAlpha(1.0f);
                Toast.makeText(getContext(), message != null ? message : "Failed to submit loan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
