package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getDelegate().setLocalNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);

        startPremiumLogoAnimations();
        startCascadingAnimations();
    }

    private void startPremiumLogoAnimations() {
        // 1. Scale + fade-in on load
        binding.logoImage.setAlpha(0f);
        binding.logoImage.setScaleX(0.6f);
        binding.logoImage.setScaleY(0.6f);
        binding.logoImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(700)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                .start();

        // 2. Floating motion — smooth vertical oscillation
        android.animation.ObjectAnimator floatAnim = android.animation.ObjectAnimator.ofFloat(
                binding.logoContainer, "translationY", 0f, -14f);
        floatAnim.setDuration(2200);
        floatAnim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        floatAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        floatAnim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        floatAnim.setStartDelay(700);
        floatAnim.start();

        // 3. Glow pulse — alpha breathing on the glow ring
        android.animation.ObjectAnimator glowPulse = android.animation.ObjectAnimator.ofFloat(
                binding.logoGlow, "alpha", 0.3f, 0.85f);
        glowPulse.setDuration(1800);
        glowPulse.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        glowPulse.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        glowPulse.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        glowPulse.start();

        // 4. Subtle glow scale pulse
        android.animation.ObjectAnimator glowScale = android.animation.ObjectAnimator.ofFloat(
                binding.logoGlow, "scaleX", 1f, 1.15f);
        android.animation.ObjectAnimator glowScaleY = android.animation.ObjectAnimator.ofFloat(
                binding.logoGlow, "scaleY", 1f, 1.15f);
        for (android.animation.ObjectAnimator anim : new android.animation.ObjectAnimator[]{glowScale, glowScaleY}) {
            anim.setDuration(1800);
            anim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            anim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            anim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
            anim.start();
        }
    }

    private void startCascadingAnimations() {
        android.view.animation.Animation slideUp = android.view.animation.AnimationUtils.loadAnimation(this,
                R.anim.slide_up_fade);
        slideUp.setStartOffset(300);

        // Title and Tagline (Delayed)
        binding.titleText.startAnimation(slideUp);
        binding.taglineText.startAnimation(slideUp);

        // Features (Delayed)
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(500);
        binding.featureCardsLayout.startAnimation(slideUp);

        // Buttons (Delayed)
        slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up_fade);
        slideUp.setStartOffset(700);
        binding.memberButton.startAnimation(slideUp);
        binding.adminButton.startAnimation(slideUp);
    }

    /**
     * Handle Admin button click from XML
     */
    public void onAdminClick(View view) {
        Intent intent = new Intent(this, AdminLoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Handle Member button click from XML
     */
    public void onMemberClick(View view) {
        Intent intent = new Intent(this, OtpRequestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}