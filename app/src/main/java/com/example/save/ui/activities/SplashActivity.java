package com.example.save.ui.activities;

import com.example.save.ui.activities.*;
import com.example.save.ui.fragments.*;
import com.example.save.ui.adapters.*;
import com.example.save.data.models.*;
import com.example.save.data.repository.*;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;
import com.example.save.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3500; // Match animation duration
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Views - now using View Binding
        View shimmerView = binding.shimmerView;

        // Initial State
        binding.letterS.setVisibility(android.view.View.VISIBLE);
        binding.letterS.setAlpha(0f);
        binding.letterS.setScaleX(0.5f);
        binding.letterS.setScaleY(0.5f);

        binding.fullLogo.setVisibility(android.view.View.VISIBLE);
        binding.fullLogo.setAlpha(0f);

        // 1. S Scales In (Duration 800ms)
        binding.letterS.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .withEndAction(() -> {

                    // 2. Slide S to left and fade in Full Logo (Duration 600ms)
                    // We simply fade S out and Logo in, assuming they are centered.
                    // To give the "Slide" effect, we translate S left and Logo In.

                    float slideDistance = -100f; // Move left

                    binding.letterS.animate()
                            .translationX(slideDistance)
                            .alpha(0f) // Fade out S as it moves
                            .setDuration(600)
                            .setStartDelay(200)
                            .start();

                    binding.fullLogo.setTranslationX(100f); // Start slightly right
                    binding.fullLogo.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(600)
                            .setStartDelay(200)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .withEndAction(() -> {

                                // 3. Shimmer Effect (Pass over the logo)
                                shimmerView.setVisibility(android.view.View.VISIBLE);
                                shimmerView.setTranslationX(-300f);
                                shimmerView.animate()
                                        .translationX(300f)
                                        .setDuration(800)
                                        .withEndAction(() -> shimmerView.setVisibility(android.view.View.GONE))
                                        .start();

                            })
                            .start();
                })
                .start();

        // Navigate to Next Screen (Total delay ~3.5s)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

            if (isFirstTime) {
                Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            } else {
                // Always require fresh login
                com.example.save.utils.SessionManager session = new com.example.save.utils.SessionManager(this);
                session.logoutUser(); // Clear any existing session to be safe

                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 3500);
    }
}