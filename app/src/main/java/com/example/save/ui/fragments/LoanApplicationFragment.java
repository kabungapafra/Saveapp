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
import com.example.save.data.models.DashboardSummaryResponse;
import com.example.save.data.models.LoanRequest;
import com.example.save.data.models.Member;
import com.example.save.data.models.SystemConfig;
import com.example.save.data.network.ApiService;
import com.example.save.data.network.RetrofitClient;
import com.example.save.databinding.FragmentLoanApplicationBinding;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoanApplicationFragment extends Fragment {

    private FragmentLoanApplicationBinding binding;
    private MembersViewModel viewModel;

    private double interestRate = 0.15; // default; replaced by server config on load
    private double totalGroupBalance = 0;
    private double nextPayoutAmount = 0;

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
                // Clear existing dynamic chips (excluding the Add button)
                int i = 0;
                while (i < binding.chipsContainer.getChildCount()) {
                    View child = binding.chipsContainer.getChildAt(i);
                    if (child != binding.btnAddGuarantor) {
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
        populateApplicantProfile();
        fetchConfigAndBalance();
    }

    private void setupInitialState() {}

    private void fetchConfigAndBalance() {
        ApiService api = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        api.getSystemConfig().enqueue(new Callback<SystemConfig>() {
            @Override
            public void onResponse(Call<SystemConfig> call, Response<SystemConfig> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    interestRate = response.body().getLoanInterestRate() / 100.0;
                    nextPayoutAmount = response.body().getContributionAmount()
                            * (1 - response.body().getRetentionPercentage() / 100.0);
                    updateSummary();
                    updateSavingsDisplay();
                }
            }
            @Override public void onFailure(Call<SystemConfig> call, Throwable t) {}
        });

        api.getDashboardSummary().enqueue(new Callback<DashboardSummaryResponse>() {
            @Override
            public void onResponse(Call<DashboardSummaryResponse> call, Response<DashboardSummaryResponse> response) {
                if (!isAdded() || binding == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    totalGroupBalance = response.body().getTotalBalance();
                    updateSavingsDisplay();
                }
            }
            @Override public void onFailure(Call<DashboardSummaryResponse> call, Throwable t) {}
        });
    }

    private void updateSavingsDisplay() {
        if (binding == null) return;
        double available = totalGroupBalance - nextPayoutAmount;
        if (available <= 0 || totalGroupBalance <= 0) return;
        // Re-read member savings from what was already set in populateApplicantProfile
        // We just reformat if we now have the balance
        viewModel.getMemberByNameLive(SessionManager.getInstance(requireContext()).getUserName())
                .observe(getViewLifecycleOwner(), member -> {
                    if (member == null || binding == null) return;
                    double savings = member.getContributionPaid();
                    double pct = available > 0 ? (savings / available) * 100.0 : 0;
                    binding.tvApplicantSavings.setText(String.format(Locale.US, "%.1f%%", pct));
                });
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
        int duration = binding.seekDuration.getProgress() + 1;

        double interest = amount * interestRate;
        double total = amount + interest;
        double monthly = duration > 0 ? (total / duration) : 0;

        NumberFormat fmt = NumberFormat.getNumberInstance(Locale.US);
        // Show rate % and the computed interest amount
        binding.tvSummaryInterest.setText(String.format(Locale.US, "%.1f%%  (UGX %s)", interestRate * 100, fmt.format(interest)));
        binding.tvSummaryTotal.setText("UGX " + fmt.format(total));
        binding.tvSummaryMonthly.setText("UGX " + fmt.format(Math.round(monthly)));
    }

    private void showAddGuarantorDialog() {
        if (availableMembers.isEmpty()) {
            Toast.makeText(getContext(), "No group members available to select as guarantors", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to AddGuarantorFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new AddGuarantorFragment())
                .addToBackStack(null)
                .commit();
    }

    /** Populate the LOAN SUMMARY applicant profile banner with real member data. */
    private void populateApplicantProfile() {
        SessionManager session = SessionManager.getInstance(requireContext());
        String userName = session.getUserName();
        if (userName == null || userName.isEmpty()) return;

        // Set name immediately from session
        if (binding.tvApplicantName != null) {
            binding.tvApplicantName.setText(userName);
        }

        // Observe member record for savings & credit score
        viewModel.getMemberByNameLive(userName).observe(getViewLifecycleOwner(), member -> {
            if (member == null || binding == null) return;

            // Savings
            if (binding.tvApplicantSavings != null) {
                java.text.NumberFormat fmt = java.text.NumberFormat.getNumberInstance(Locale.US);
                binding.tvApplicantSavings.setText("UGX " + fmt.format(member.getContributionPaid()));
            }

            // Credit score
            if (binding.tvApplicantCredit != null) {
                double score = member.getCreditScore();
                String label = score >= 90 ? "SAFE" : score >= 75 ? "STABLE" : score >= 60 ? "MODERATE" : "AT RISK";
                binding.tvApplicantCredit.setText(String.format(Locale.US, "%.0f (%s)", score, label));
            }
        });
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
        if (requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
            if (outValue.resourceId != 0) {
                removeBtn.setBackgroundResource(outValue.resourceId);
            }
        }
        
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

        // Use null when no guarantor selected — empty strings fail backend min_length validation
        String guarantorName = null;
        String guarantorPhone = null;
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
