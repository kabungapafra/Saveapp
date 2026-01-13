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

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentFragment extends Fragment {

    private MembersViewModel viewModel;
    private Member currentMember;
    private String memberName;

    private ProgressBar progressBar;
    private TextView txtPaidAmount, txtTargetAmount;
    private EditText etAmount;
    private Button btnPayNow;

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
            // Default to Alice if no name provided (e.g. current Member app flow)
            memberName = "Alice Johnson";
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
        progressBar = view.findViewById(R.id.progressBarPayment);
        txtPaidAmount = view.findViewById(R.id.txtPaidAmount);
        txtTargetAmount = view.findViewById(R.id.txtTargetAmount);
        etAmount = view.findViewById(R.id.etAmount);
        btnPayNow = view.findViewById(R.id.btnPayNow);

        android.widget.ImageView btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }
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
        int progress = member.getPaymentProgress();

        progressBar.setProgress(progress);

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "UG")); // Uganda Shillings
        // Or simple format if currency not available
        String paidStr = String.format("UGX %,.0f", paid);
        String targetStr = String.format("UGX %,.0f", target);

        txtPaidAmount.setText("Paid: " + paidStr);
        txtTargetAmount.setText("Target: " + targetStr);
    }

    private void setupListeners() {
        btnPayNow.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
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
                viewModel.makePayment(currentMember, amount);
                Toast.makeText(getContext(), "Payment Successful!", Toast.LENGTH_SHORT).show();
                etAmount.setText("");
                // Reload member data to refresh UI with updated contribution
                loadMemberData();
            }
        });
    }
}
