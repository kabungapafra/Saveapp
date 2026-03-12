package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3500;
    private ActivitySplashBinding binding;
    private ValueAnimator progressAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch system config early
        MemberRepository.getInstance(this).fetchSystemConfig(null);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ── 1. Animate gradient blobs (breathing / floating effect) ──────────────
        animateGradientBlobs();

        // ── 2. Logo entrance ─────────────────────────────────────────────────────
        binding.fullLogo.setVisibility(View.VISIBLE);
        binding.fullLogo.setAlpha(0f);
        binding.fullLogo.setScaleX(0.75f);
        binding.fullLogo.setScaleY(0.75f);

        binding.fullLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(900)
                .setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .withEndAction(() -> {
                    // Shimmer pass over logo
                    binding.shimmerView.setVisibility(View.VISIBLE);
                    binding.shimmerView.setTranslationX(-300f);
                    binding.shimmerView.animate()
                            .translationX(350f)
                            .setDuration(700)
                            .withEndAction(() -> binding.shimmerView.setVisibility(View.GONE))
                            .start();

                    // Fade-in tagline & footer
                    binding.tvTagline.animate().alpha(1f).setDuration(600).setStartDelay(200).start();
                    binding.footerLayout.animate().alpha(1f).setDuration(600).setStartDelay(400).start();
                })
                .start();

        // ── 3. Animated progress bar ───────────────────────────────────────────
        // Delay slightly so logo animation starts first
        new Handler(Looper.getMainLooper()).postDelayed(this::startProgressAnimation, 500);

        // ── 4. Navigate away after SPLASH_DURATION ─────────────────────────────
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressAnimator != null)
                progressAnimator.cancel();
            navigateToNextScreen();
        }, SPLASH_DURATION);
    }

    // ─── Blob pulse animation ────────────────────────────────────────────────────
    private void animateGradientBlobs() {
        // Top blob: slow pulse scale + slight translation
        animateBlob(binding.gradientTop, 0, 0.92f, 1.08f, 2800);
        // Bottom blob: offset pulse
        animateBlob(binding.gradientBottom, 400, 0.95f, 1.05f, 3200);
    }

    private void animateBlob(View blob, long delay, float scaleFrom, float scaleTo, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(scaleFrom, scaleTo);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            blob.setScaleX(value);
            blob.setScaleY(value);
        });
        animator.start();
    }

    // ─── Gradient progress bar animation ────────────────────────────────────────
    private void startProgressAnimation() {
        int totalWidth = getResources().getDisplayMetrics().widthPixels
                - (int) (80 * getResources().getDisplayMetrics().density); // paddingHorizontal 40dp each side

        progressAnimator = ValueAnimator.ofInt(0, totalWidth);
        progressAnimator.setDuration(2600);
        progressAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        progressAnimator.addUpdateListener(animation -> {
            int width = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = binding.progressBar.getLayoutParams();
            params.width = width;
            binding.progressBar.setLayoutParams(params);

            // Update % text
            int percent = (int) (animation.getAnimatedFraction() * 100);
            binding.tvProgressPercent.setText(percent + "%");
        });
        progressAnimator.start();
    }

    private void navigateToNextScreen() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

        if (isFirstTime) {
            startActivity(new Intent(this, OnboardingActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(this);
            session.logoutUser();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressAnimator != null)
            progressAnimator.cancel();
    }
}