package com.example.save.ui.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityPrivacyPolicyBinding;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ActivityPrivacyPolicyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrivacyPolicyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        loadPrivacyContent();
    }

    private void setupListeners() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void loadPrivacyContent() {
        String privacyContent = "PRIVACY POLICY\n\n" +
                "Last Updated: January 2026\n\n" +
                "1. INFORMATION WE COLLECT\n" +
                "We collect information you provide directly, including name, email, phone number, and financial transaction data.\n\n" +
                "2. HOW WE USE YOUR INFORMATION\n" +
                "We use your information to:\n" +
                "- Provide and improve our services\n" +
                "- Process transactions and manage your account\n" +
                "- Send notifications and updates\n" +
                "- Ensure security and prevent fraud\n\n" +
                "3. DATA SHARING\n" +
                "We do not sell your personal information. Data is shared only with:\n" +
                "- Your group administrators (for group management)\n" +
                "- Service providers (for app functionality)\n" +
                "- Legal authorities (when required by law)\n\n" +
                "4. DATA SECURITY\n" +
                "We implement industry-standard security measures to protect your information.\n\n" +
                "5. YOUR RIGHTS\n" +
                "You have the right to:\n" +
                "- Access your personal data\n" +
                "- Request data correction\n" +
                "- Request data deletion\n" +
                "- Opt-out of certain communications\n\n" +
                "6. DATA RETENTION\n" +
                "We retain your data as long as your account is active or as needed for legal compliance.\n\n" +
                "7. CHILDREN'S PRIVACY\n" +
                "Our service is not intended for users under 18 years of age.\n\n" +
                "8. CHANGES TO THIS POLICY\n" +
                "We may update this policy. We will notify you of significant changes.\n\n" +
                "9. CONTACT US\n" +
                "For privacy concerns, contact: privacy@save.com";

        binding.tvPrivacyContent.setText(privacyContent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
