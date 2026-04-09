package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.R;
import com.example.save.databinding.ActivityMemberLoginBinding;

public class MemberLoginActivity extends AppCompatActivity {

    private ActivityMemberLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Animate Logo Image (Heartbeat)
        android.view.animation.Animation heartbeat = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.heartbeat);
        binding.logoImage.startAnimation(heartbeat);

        setupListeners();
        setupPasswordToggle();
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> {
            // Smooth press dip
            android.view.animation.Animation press = android.view.animation.AnimationUtils
                    .loadAnimation(this, R.anim.login_btn_press);
            press.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override public void onAnimationStart(android.view.animation.Animation a) {}
                @Override public void onAnimationRepeat(android.view.animation.Animation a) {}
                @Override public void onAnimationEnd(android.view.animation.Animation a) {
                    android.view.animation.Animation release = android.view.animation.AnimationUtils
                            .loadAnimation(MemberLoginActivity.this, R.anim.login_btn_release);
                    v.startAnimation(release);
                    if (com.example.save.utils.DesignMode.IS_DESIGN_MODE) {
                        Intent intent = new Intent(MemberLoginActivity.this, MemberMainActivity.class);
                        intent.putExtra("member_name", "Design Member");
                        intent.putExtra("member_email", "member@design.com");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        overridePendingTransition(R.anim.transition_fade_in_slow, R.anim.transition_fade_out_slow);
                        finish();
                    } else {
                        // TODO: implement real member login
                        finish();
                    }
                }
            });
            v.startAnimation(press);
        });

        binding.googleButton.setOnClickListener(v -> {
            // Future Google Login
        });

        binding.forgotPasswordText.setOnClickListener(v -> {
            // Future Forgot Password
        });

        binding.adminPortalLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminLoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private void setupPasswordToggle() {
        binding.passwordToggle.setOnClickListener(v -> {
            boolean isVisible = binding.passwordInput.getTransformationMethod() == null;
            if (isVisible) {
                binding.passwordInput.setTransformationMethod(new android.text.method.PasswordTransformationMethod());
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility_off);
            } else {
                binding.passwordInput.setTransformationMethod(null);
                binding.passwordToggle.setImageResource(R.drawable.ic_visibility);
            }
            binding.passwordInput.setSelection(binding.passwordInput.getText().length());
        });
    }
}
