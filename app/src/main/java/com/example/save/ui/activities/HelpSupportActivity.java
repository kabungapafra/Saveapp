package com.example.save.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityHelpSupportBinding;

public class HelpSupportActivity extends AppCompatActivity {

    private ActivityHelpSupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHelpSupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        loadHelpContent();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void loadHelpContent() {
        // FAQ Content
        String faqContent = "Frequently Asked Questions\n\n" +
                "Q: How do I make a contribution?\n" +
                "A: Go to the Payment tab, enter the amount and your phone number, then tap Pay Now.\n\n" +
                "Q: How do I apply for a loan?\n" +
                "A: Navigate to Loans section, tap Apply for Loan, fill in the details and submit.\n\n" +
                "Q: When will I receive my payout?\n" +
                "A: Payouts are scheduled based on your position in the queue. Check the Payouts section for details.\n\n" +
                "Q: How do I reset my password?\n" +
                "A: Go to Login screen, tap Forgot Password, and follow the instructions.\n\n" +
                "Q: How do I contact support?\n" +
                "A: Use the Contact Admin feature in Settings or email support@save.com\n\n" +
                "Q: What if I miss a contribution?\n" +
                "A: Late penalties may apply. Contact your group admin for assistance.\n\n" +
                "Q: Can I change my payment method?\n" +
                "A: Yes, go to Settings > Payment Method to update your preferred method.";

        binding.tvHelpContent.setText(faqContent);

        // Contact Information
        binding.tvContactInfo.setText("Contact Support:\n\n" +
                "Email: support@save.com\n" +
                "Phone: +256 700 000 000\n" +
                "Hours: Mon-Fri, 9AM-5PM EAT");
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
