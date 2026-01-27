package com.example.save.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityTermsOfServiceBinding;

public class TermsOfServiceActivity extends AppCompatActivity {

    private ActivityTermsOfServiceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTermsOfServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        loadTermsContent();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void loadTermsContent() {
        String termsContent = "TERMS OF SERVICE\n\n" +
                "Last Updated: January 2026\n\n" +
                "1. ACCEPTANCE OF TERMS\n" +
                "By using the Save app, you agree to be bound by these Terms of Service.\n\n" +
                "2. USE OF SERVICE\n" +
                "You agree to use the service only for lawful purposes and in accordance with these terms.\n\n" +
                "3. USER ACCOUNTS\n" +
                "You are responsible for maintaining the confidentiality of your account credentials.\n\n" +
                "4. FINANCIAL TRANSACTIONS\n" +
                "All financial transactions are subject to verification and approval by group administrators.\n\n" +
                "5. LOAN AGREEMENTS\n" +
                "Loans are subject to group policies, interest rates, and repayment schedules as defined by your group.\n\n" +
                "6. CONTRIBUTIONS\n" +
                "Members are required to make contributions according to the group's schedule and rules.\n\n" +
                "7. DATA PRIVACY\n" +
                "Your data is protected according to our Privacy Policy. We do not share your information with third parties.\n\n" +
                "8. LIMITATION OF LIABILITY\n" +
                "The app is provided 'as is'. We are not liable for any losses arising from use of the service.\n\n" +
                "9. MODIFICATIONS\n" +
                "We reserve the right to modify these terms at any time. Continued use constitutes acceptance.\n\n" +
                "10. CONTACT\n" +
                "For questions about these terms, contact support@save.com";

        binding.tvTermsContent.setText(termsContent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
