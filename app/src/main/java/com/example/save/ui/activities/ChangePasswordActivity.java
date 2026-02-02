package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        // Retrieve current password (OTP) passed from login
        String intentCurrentPassword = getIntent().getStringExtra("current_password");

        if (intentCurrentPassword != null) {
            binding.tvSubtitle.setText("Please create a new password for your account");
            binding.tilCurrentPassword.setVisibility(View.GONE);
        } else {
            binding.tvSubtitle.setText("Update your account security");
            binding.tilCurrentPassword.setVisibility(View.VISIBLE);
        }

        // Setup button listener
        binding.btnChangePassword.setOnClickListener(v -> handlePasswordChange(intentCurrentPassword));

        // Disable "force password change" if not first login/OTP flow
        if (intentCurrentPassword != null) {
            getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Toast.makeText(ChangePasswordActivity.this,
                            "You must create a new password to continue", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handlePasswordChange(String intentPassword) {
        String currentPassword = intentPassword;
        if (currentPassword == null) {
            currentPassword = binding.etCurrentPassword.getText().toString().trim();
        }

        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate inputs
        if (binding.tilCurrentPassword.getVisibility() == View.VISIBLE
                && !ValidationUtils.isNotEmpty(currentPassword)) {
            ValidationUtils.showError(binding.etCurrentPassword, "Current password is required");
            return;
        }

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

        String finalCurrentPassword = currentPassword;

        // Change password via API - backend will hash and validate
        viewModel.changePassword(memberEmail, currentPassword, newPassword,
                new com.example.save.data.repository.MemberRepository.PasswordChangeCallback() {
                    @Override
                    public void onResult(boolean success, String message) {
                        binding.btnChangePassword.setEnabled(true);
                        binding.btnChangePassword.setText("Change Password");

                        if (success) {
                            // Update local session status
                            com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(
                                    getApplicationContext());
                            session.setFirstLoginStatus(false);

                            Toast.makeText(ChangePasswordActivity.this,
                                    message != null ? message : "Password changed successfully!",
                                    Toast.LENGTH_SHORT).show();

                            // If this was a forced change, go to dashboard. Otherwise just finish.
                            if (intentPassword != null) {
                                Intent intent = new Intent(ChangePasswordActivity.this, MemberMainActivity.class);
                                intent.putExtra("member_email", memberEmail);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                finish();
                            }
                        } else {
                            Toast.makeText(ChangePasswordActivity.this,
                                    message != null ? message : "Failed to change password",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
