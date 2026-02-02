package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.airbnb.lottie.LottieAnimationView;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentFragment extends Fragment {

    private MembersViewModel viewModel;
    private Member currentMember;
    private String memberName;

    private ProgressBar circularProgressBar;
    private TextView tvProgressPercentage;
    private TextView txtPaidAmount, txtTargetAmount, txtRemainingAmount, tvDueDate;
    private View chipClear, chipPayBalance;
    private com.google.android.material.button.MaterialButton btnPayNow;

    // Changed etPhoneNumber to EditText to match new layout
    private com.google.android.material.textfield.TextInputEditText etAmount;
    private android.widget.EditText etPhoneNumber;

    // Loan Options
    private View cardLoanOptions;
    private com.google.android.material.button.MaterialButton btnRequestLoan;
    private com.google.android.material.button.MaterialButton btnPayLoan;

    // Animation Views
    private LottieAnimationView successAnimation;
    private View animationOverlay;

    public static PaymentFragment newInstance(String memberName, String memberEmail) {
        PaymentFragment fragment = new PaymentFragment();
        Bundle args = new Bundle();
        args.putString("MEMBER_NAME", memberName);
        args.putString("MEMBER_EMAIL", memberEmail);
        fragment.setArguments(args);
        return fragment;
    }

    public static PaymentFragment newInstance(String memberName) {
        return newInstance(memberName, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        // Get member name and email from arguments
        if (getArguments() != null) {
            memberName = getArguments().getString("MEMBER_NAME");
            memberEmail = getArguments().getString("MEMBER_EMAIL");
        }

        initializeViews(view);

        // IMMEDIATE ADMIN CHECK: Don't wait for data to show admin controls
        if (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) {
            if (cardLoanOptions != null) {
                cardLoanOptions.setVisibility(View.VISIBLE);
                setupLoanListeners();
            }
        }

        setupListeners();
        loadMemberData();
    }

    private void initializeViews(View view) {
        circularProgressBar = view.findViewById(R.id.circularProgressBar);
        tvProgressPercentage = view.findViewById(R.id.tvProgressPercentage);

        txtPaidAmount = view.findViewById(R.id.txtPaidAmount);
        txtTargetAmount = view.findViewById(R.id.txtTargetAmount);
        txtRemainingAmount = view.findViewById(R.id.txtRemainingAmount);
        tvDueDate = view.findViewById(R.id.tvDueDate);

        chipClear = view.findViewById(R.id.chipClear);
        chipPayBalance = view.findViewById(R.id.chipPayBalance);

        etAmount = view.findViewById(R.id.etAmount);
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber); // This is now a standard EditText
        btnPayNow = view.findViewById(R.id.btnPayNow);

        // Loan Views
        cardLoanOptions = view.findViewById(R.id.cardLoanOptions);
        btnRequestLoan = view.findViewById(R.id.btnRequestLoan);
        btnPayLoan = view.findViewById(R.id.btnPayLoan);

        android.widget.ImageView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        // Initialize animation views
        successAnimation = view.findViewById(R.id.successAnimation);
        animationOverlay = view.findViewById(R.id.animationOverlay);
    }

    private String memberEmail;

    private void loadMemberData() {
        // Fetch member data on background thread to avoid Room main thread violation
        new Thread(() -> {
            try {
                Member member = null;
                if (memberEmail != null) {
                    member = viewModel.getMemberByEmail(memberEmail);
                }

                if (member == null && memberName != null) {
                    member = viewModel.getMemberByName(memberName);
                }

                final Member finalMember = member;
                requireActivity().runOnUiThread(() -> {
                    currentMember = finalMember;
                    if (currentMember != null) {
                        updateUI(currentMember);
                    } else {
                        // If we are strictly on admin side, we already showed the card,
                        // but we might want to log that data didn't load.
                        if (!(getActivity() instanceof com.example.save.ui.activities.AdminMainActivity)) {
                            Toast.makeText(getContext(), "Member data not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    // Fail silently or log
                });
            }
        }).start();
    }

    private void updateUI(Member member) {
        double paid = member.getContributionPaid();
        double target = member.getContributionTarget();
        double remaining = Math.max(0, target - paid);
        int progress = member.getPaymentProgress();

        if (circularProgressBar != null) {
            circularProgressBar.setProgress(progress);
        }
        if (tvProgressPercentage != null) {
            tvProgressPercentage.setText(progress + "%");
        }

        String paidStr = String.format(Locale.US, "UGX %,.0f", paid);
        // Display target nicely, e.g., 1M
        String targetStr;
        if (target >= 1000000) {
            targetStr = String.format(Locale.US, "UGX %.0fM", target / 1000000);
        } else {
            targetStr = String.format(Locale.US, "UGX %,.0f", target);
        }

        String remainingStr = String.format(Locale.US, "UGX %,.0f", remaining);

        txtPaidAmount.setText(paidStr);
        txtTargetAmount.setText(targetStr);
        txtRemainingAmount.setText(remainingStr);

        // Due Date Logic
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("ChamaPrefs",
                android.content.Context.MODE_PRIVATE);
        long cycleStartDate = prefs.getLong("cycle_start_date", 0);
        if (cycleStartDate != 0) {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTimeInMillis(cycleStartDate);
            calendar.add(java.util.Calendar.MONTH, 1); // Due date is 1 month after cycle start

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM", Locale.getDefault());
            tvDueDate.setText(sdf.format(calendar.getTime()));
        } else {
            tvDueDate.setText("Not Set");
        }

        // Setup Chip Listeners
        if (chipPayBalance != null) {
            chipPayBalance.setOnClickListener(v -> {
                etAmount.setText(String.valueOf((int) remaining));
            });
        }
        if (chipClear != null) {
            chipClear.setOnClickListener(v -> {
                etAmount.setText("");
            });
        }

        // Pre-fill phone number if available
        if (etPhoneNumber != null && member.getPhone() != null) {
            etPhoneNumber.setText(member.getPhone());
        }

        // Role-based visibility (as backup or for member view if applicable)
        if (cardLoanOptions != null) {
            boolean isAdmin = (getActivity() instanceof com.example.save.ui.activities.AdminMainActivity) ||
                    ("Admin".equalsIgnoreCase(member.getRole()) || "Administrator".equalsIgnoreCase(member.getRole()));

            if (isAdmin) {
                cardLoanOptions.setVisibility(View.VISIBLE);
                setupLoanListeners();
            } else {
                cardLoanOptions.setVisibility(View.GONE);
            }
        }

    }

    private void setupLoanListeners() {
        if (btnRequestLoan != null) {
            btnRequestLoan.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.save.ui.fragments.LoanApplicationFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
        if (btnPayLoan != null) {
            btnPayLoan.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new com.example.save.ui.fragments.LoanPaymentFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void setupListeners() {
        btnPayNow.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString();
            String amountStr = etAmount.getText().toString();

            // Get selected payment method
            String paymentMethod = "Mobile Money";
            com.google.android.material.chip.ChipGroup chipGroup = getView().findViewById(R.id.chipGroupPaymentMethod);
            if (chipGroup != null) {
                int selectedId = chipGroup.getCheckedChipId();
                if (selectedId == R.id.chipMethodBank) {
                    paymentMethod = "Bank Transfer";
                } else if (selectedId == R.id.chipMethodCash) {
                    paymentMethod = "Cash";
                }
            }

            if (TextUtils.isEmpty(phoneNumber)) {
                etPhoneNumber.setError("Enter phone number");
                return;
            }

            if (TextUtils.isEmpty(amountStr)) {
                etAmount.setError("Enter amount");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                etAmount.setError("Invalid amount");
                return;
            }

            if (currentMember != null) {
                // Process payment via API - backend will update balance, calculate streak, log
                // transaction
                viewModel.makePayment(currentMember, amount, phoneNumber, paymentMethod,
                        new com.example.save.data.repository.MemberRepository.PaymentCallback() {
                            @Override
                            public void onResult(boolean success, String message) {
                                if (success) {
                                    // Show success animation
                                    showSuccessAnimation();

                                    etAmount.setText("");
                                    etPhoneNumber.setText("");
                                    // Reload member data to refresh UI with updated contribution
                                    loadMemberData();

                                    // Generate Receipt
                                    com.example.save.utils.ReceiptUtils.generateAndShareReceipt(getContext(),
                                            currentMember.getName(), amount, "Contribution", new java.util.Date());
                                } else {
                                    Toast.makeText(getContext(),
                                            message != null ? message : "Failed to process payment",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        View btnSetupAutoPay = getView().findViewById(R.id.btnSetupAutoPay);
        if (btnSetupAutoPay != null) {
            btnSetupAutoPay.setOnClickListener(v -> showAutoPayDialog());
        }

    }

    private void showSuccessAnimation() {
        if (successAnimation != null && animationOverlay != null) {
            // Show overlay and animation
            animationOverlay.setVisibility(View.VISIBLE);
            successAnimation.setProgress(0f);
            successAnimation.playAnimation();

            // Add listener to hide animation after it completes
            successAnimation.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {
                }

                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Hide overlay after animation completes
                    if (animationOverlay != null) {
                        animationOverlay.postDelayed(() -> {
                            if (animationOverlay != null) {
                                animationOverlay.setVisibility(View.GONE);
                            }
                        }, 500); // Small delay before hiding
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    if (animationOverlay != null) {
                        animationOverlay.setVisibility(View.GONE);
                    }
                    successAnimation.removeAllAnimatorListeners();
                }

                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {
                }
            });
        }
    }

    private void showAutoPayDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_autopay_setup, null);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        com.google.android.material.textfield.TextInputEditText etAutoPayAmount = dialogView
                .findViewById(R.id.etAutoPayAmount);
        com.google.android.material.slider.Slider sliderDay = dialogView.findViewById(R.id.sliderDay);
        TextView tvSelectedDay = dialogView.findViewById(R.id.tvSelectedDay);
        Button btnSave = dialogView.findViewById(R.id.btnSaveAutoPay);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAutoPay);

        // Pre-fill if already enabled
        if (currentMember != null && currentMember.isAutoPayEnabled()) {
            etAutoPayAmount.setText(String.valueOf(currentMember.getAutoPayAmount()));
            sliderDay.setValue(currentMember.getAutoPayDay());
            tvSelectedDay.setText("Day " + currentMember.getAutoPayDay() + " of every month");
            btnSave.setText("Update Auto-Pay");
        } else {
            // Default amount: Remaining Contribution
            if (currentMember != null) {
                double remaining = Math.max(0,
                        currentMember.getContributionTarget() - currentMember.getContributionPaid());
                if (remaining > 0) {
                    etAutoPayAmount.setText(String.valueOf((int) remaining));
                } else {
                    etAutoPayAmount.setText("50000");
                }
            }
        }

        sliderDay.addOnChangeListener((slider, value, fromUser) -> {
            tvSelectedDay.setText("Day " + (int) value + " of every month");
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = etAutoPayAmount.getText().toString();
            if (TextUtils.isEmpty(amountStr)) {
                etAutoPayAmount.setError("Enter amount");
                return;
            }
            double amount = Double.parseDouble(amountStr);
            int day = (int) sliderDay.getValue();

            if (currentMember != null) {
                viewModel.enableAutoPay(currentMember, amount, day);
                Toast.makeText(getContext(), "Auto-Pay Enabled for Day " + day, Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
