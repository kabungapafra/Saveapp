package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityMemberregBinding;
import com.google.android.material.textfield.TextInputEditText;

public class MemberregActivity extends AppCompatActivity {

    private ActivityMemberregBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberregBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup click listeners
        binding.loginButton.setOnClickListener(v -> handleLogin());
        binding.forgotPassword.setOnClickListener(v -> showForgotPasswordFragment());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // If fragment is visible, show login form again
                if (binding.fragmentContainer != null && binding.fragmentContainer.getVisibility() == View.VISIBLE) {
                    binding.loginCard.setVisibility(View.VISIBLE);
                    binding.sideTabContainer.setVisibility(View.VISIBLE);
                    binding.fragmentContainer.setVisibility(View.GONE);
                    getSupportFragmentManager().popBackStack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    /**
     * Show forgot password fragment
     */
    private void showForgotPasswordFragment() {
        // Hide login card and show fragment container
        binding.loginCard.setVisibility(View.GONE);
        binding.sideTabContainer.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        // Show forgot password fragment
        nwpasswordFragment fragment = nwpasswordFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void handleLogin() {
        String groupName = binding.groupNameInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        // Validate inputs
        if (groupName.isEmpty()) {
            binding.groupNameInput.setError("Group name is required");
            binding.groupNameInput.requestFocus();
            return;
        }

        if (phone.isEmpty() || phone.length() != 9) {
            binding.phoneInput.setError("Valid phone number is required");
            binding.phoneInput.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 8) {
            binding.passwordInput.setError("Password must be at least 8 characters");
            binding.passwordInput.requestFocus();
            return;
        }

        // TODO: Implement actual member login logic with backend
        // Navigate to member main activity
        Toast.makeText(this, "Member login successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MemberregActivity.this, MemberMainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}