package com.example.save.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.save.R;
import com.example.save.data.models.Member;
import com.example.save.ui.viewmodels.MembersViewModel;
import com.example.save.utils.SessionManager;
import com.example.save.utils.ReceiptUtils;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class MakeContributionFragment extends Fragment {

    private EditText etAmount;
    private View btnAirtel, btnMTN;
    private View airtelCheck, mtnCheck;
    private View airtelEmptyCircle, mtnEmptyCircle;
    private long currentAmount = 0;
    private String selectedPaymentMethod = "Airtel";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_make_contribution, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        etAmount = view.findViewById(R.id.etAmount);
        btnAirtel = view.findViewById(R.id.btnAirtel);
        btnMTN = view.findViewById(R.id.btnMTN);
        airtelCheck = view.findViewById(R.id.airtelCheck);
        mtnCheck = view.findViewById(R.id.mtnCheck);
        airtelEmptyCircle = view.findViewById(R.id.airtelEmptyCircle);
        mtnEmptyCircle = view.findViewById(R.id.mtnEmptyCircle);

        View btnBack = view.findViewById(R.id.btnBack);
        TextView btnQuick1 = view.findViewById(R.id.btnQuickAmount1);
        TextView btnQuick2 = view.findViewById(R.id.btnQuickAmount2);
        TextView btnQuick3 = view.findViewById(R.id.btnQuickAmount3);
        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirm);

        MembersViewModel viewModel = new ViewModelProvider(requireActivity()).get(MembersViewModel.class);
        SessionManager session = new SessionManager(requireContext());
        String email = session.getUserEmail();
        
        setupAmountInput();

        // Back action
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Quick amount selection
        btnQuick1.setOnClickListener(v -> selectAmount(10000, btnQuick1, btnQuick2, btnQuick3));
        btnQuick2.setOnClickListener(v -> selectAmount(50000, btnQuick2, btnQuick1, btnQuick3));
        btnQuick3.setOnClickListener(v -> selectAmount(100000, btnQuick3, btnQuick1, btnQuick2));

        // Payment method selection
        btnAirtel.setOnClickListener(v -> selectPaymentMethod("Airtel"));
        btnMTN.setOnClickListener(v -> selectPaymentMethod("MTN"));

        // Confirm action
        btnConfirm.setOnClickListener(v -> {
            if (email == null) {
                Toast.makeText(getContext(), "User session not found", Toast.LENGTH_SHORT).show();
                return;
            }

            btnConfirm.setEnabled(false);
            btnConfirm.setText("Processing...");

            new Thread(() -> {
                Member member = viewModel.getMemberByEmail(email);
                if (member != null) {
                    getActivity().runOnUiThread(() -> {
                        viewModel.makePayment(member, (double) currentAmount, member.getPhone(), selectedPaymentMethod,
                                (success, message) -> {
                                    if (success) {
                                        Toast.makeText(getContext(), "Contribution Successful!", Toast.LENGTH_LONG).show();
                                        ReceiptUtils.generateAndShareReceipt(getContext(), member.getName(), (double) currentAmount, "Contribution", new Date());
                                        if (getActivity() != null) {
                                            getActivity().onBackPressed();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), message != null ? message : "Payment failed", Toast.LENGTH_LONG).show();
                                        btnConfirm.setEnabled(true);
                                        btnConfirm.setText("Confirm Contribution");
                                    }
                                });
                    });
                } else {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Member data not found", Toast.LENGTH_SHORT).show();
                        btnConfirm.setEnabled(true);
                        btnConfirm.setText("Confirm Contribution");
                    });
                }
            }).start();
        });

        // Initial state
        etAmount.setText(""); // Ensure empty state
        selectPaymentMethod("Airtel");
    }
    
    private void setupAmountInput() {
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);
                    
                    String cleanString = s.toString().replaceAll("[^\\d]", "");
                    
                    if (cleanString.isEmpty() || cleanString.equals("0")) {
                        currentAmount = 0;
                        current = "";
                        etAmount.setText("");
                    } else {
                        try {
                            currentAmount = Long.parseLong(cleanString);
                            String formatted = formatAmount(currentAmount);
                            current = formatted;
                            etAmount.setText(formatted);
                            etAmount.setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            currentAmount = 0;
                            current = "";
                            etAmount.setText("");
                        }
                    }
                    
                    etAmount.addTextChangedListener(this);
                }
            }
        });
    }

    private void selectAmount(long amount, TextView selected, TextView... others) {
        currentAmount = amount;
        updateAmountUI();
        
        selected.setSelected(true);
        selected.setTextColor(getResources().getColor(R.color.project_light, null));
        
        for (TextView other : others) {
            other.setSelected(false);
            other.setTextColor(0xFF1E293B); // Slate 800
        }
    }

    private void selectPaymentMethod(String method) {
        selectedPaymentMethod = method;
        
        if ("Airtel".equals(method)) {
            btnAirtel.setSelected(true);
            btnMTN.setSelected(false);
            airtelCheck.setVisibility(View.VISIBLE);
            airtelEmptyCircle.setVisibility(View.GONE);
            mtnCheck.setVisibility(View.GONE);
            mtnEmptyCircle.setVisibility(View.VISIBLE);
        } else {
            btnAirtel.setSelected(false);
            btnMTN.setSelected(true);
            airtelCheck.setVisibility(View.GONE);
            airtelEmptyCircle.setVisibility(View.VISIBLE);
            mtnCheck.setVisibility(View.VISIBLE);
            mtnEmptyCircle.setVisibility(View.GONE);
        }
    }

    private void updateAmountUI() {
        String formatted = formatAmount(currentAmount);
        etAmount.setText(formatted);
        etAmount.setSelection(formatted.length());
    }

    private String formatAmount(long amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }
}
