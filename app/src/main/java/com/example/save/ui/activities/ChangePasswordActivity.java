package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityChangePasswordBinding;
import com.example.save.utils.ValidationUtils;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private com.example.save.ui.viewmodels.MembersViewModel viewModel;
    private String memberEmail;
    private String memberName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new androidx.lifecycle.ViewModelProvider(this)
                .get(com.example.save.ui.viewmodels.MembersViewModel.class);

        // Get member details from intent
        memberEmail = getIntent().getStringExtra("member_email");
        memberName = getIntent().getStringExtra("member_name");

        if (memberEmail == null) {
            Toast.makeText(this, "Error: Member not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set welcome message
        binding.tvWelcomeMessage.setText("Welcome, " + (memberName != null ? memberName : "Member") + "!");
        binding.tvSubtitle.setText("Please create a new password for your account");

        // Setup button listener
        binding.btnChangePassword.setOnClickListener(v -> handlePasswordChange());

        // Prevent back press (force password change)
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(ChangePasswordActivity.this,
                        "You must create a new password to continue", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePasswordChange() {
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (!ValidationUtils.isNotEmpty(newPassword)) {
            ValidationUtils.showError(binding.etNewPassword, "Password is required");
            return;
        }

        if (!ValidationUtils.isValidPassword(newPassword)) {
            ValidationUtils.showError(binding.etNewPassword,
                    "Password must be at least 8 characters");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            ValidationUtils.showError(binding.etConfirmPassword,
                    "Passwords do not match");
            return;
        }

        // Disable button during processing
        binding.btnChangePassword.setEnabled(false);
        binding.btnChangePassword.setText("Changing Password...");

        // Change password in background thread
        new Thread(() -> {
            com.example.save.data.models.Member member = viewModel.getMemberByEmail(memberEmail);

            if (member != null) {
                // Change the password (this will set isFirstLogin to false)
                viewModel.changePassword(member, newPassword);

                // Small delay to ensure database update completes
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Password changed successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to member dashboard
                    Intent intent = new Intent(ChangePasswordActivity.this, MemberMainActivity.class);
                    intent.putExtra("member_email", memberEmail);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                });
            } else {
                runOnUiThread(() -> {
                    binding.btnChangePassword.setEnabled(true);
                    binding.btnChangePassword.setText("Change Password");
                    Toast.makeText(ChangePasswordActivity.this,
                            "Error: Member not found", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
