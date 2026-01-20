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
    private com.google.android.material.textfield.TextInputEditText etAmount, etPhoneNumber;
    private com.google.android.material.button.MaterialButton btnPayNow;

    // Loan Options
    private View cardLoanOptions;
    private View btnRequestLoan, btnPayLoan;

    private LottieAnimationView successAnimation;
    private View animationOverlay;

    public PaymentFragment() {
        // Required empty public constructor
    }

    public static PaymentFragment newInstance(String memberName) {
        PaymentFragment fragment = new PaymentFragment();
        Bundle args = new Bundle();
        args.putString("MEMBER_NAME", memberName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memberName = getArguments().getString("MEMBER_NAME");
        } else {
            // Default to Member if no name provided
            memberName = "Member";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);

        initializeViews(view);
        loadMemberData();
        setupListeners();
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
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
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

    private void loadMemberData() {
        // Fetch member data on background thread to avoid Room main thread violation
        new Thread(() -> {
            try {
                Member member = viewModel.getMemberByName(memberName);
                requireActivity().runOnUiThread(() -> {
                    currentMember = member;
                    if (currentMember != null) {
                        updateUI(currentMember);
                    } else {
                        Toast.makeText(getContext(), "Member not found: " + memberName, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error loading member data", Toast.LENGTH_SHORT).show();
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

        // Loan Logic: Show Loan Options only for Admin
        if (cardLoanOptions != null) {
            if ("Admin".equalsIgnoreCase(member.getRole()) || "Administrator".equalsIgnoreCase(member.getRole())) {
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
                viewModel.makePayment(currentMember, amount, phoneNumber);

                // Show success animation
                showSuccessAnimation();

                etAmount.setText("");
                etPhoneNumber.setText("");
                // Reload member data to refresh UI with updated contribution
                loadMemberData();
            }
        });

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
}
