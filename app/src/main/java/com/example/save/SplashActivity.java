package com.example.save;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 10000;
    private ImageView letterS, fullLogo;
    private ValueAnimator colorAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        letterS = findViewById(R.id.letter_s);
        fullLogo = findViewById(R.id.full_logo);

        Animation netflixZoom = AnimationUtils.loadAnimation(this, R.anim.netflix_zoom);
        Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        Animation glowShine = AnimationUtils.loadAnimation(this, R.anim.glow_shine);

        netflixZoom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                letterS.setAlpha(1.0f);
                letterS.setScaleX(0.3f);
                letterS.setScaleY(0.3f);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                letterS.setScaleX(1.0f);
                letterS.setScaleY(1.0f);
                letterS.setAlpha(1.0f);
                letterS.startAnimation(slideOutRight);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        letterS.startAnimation(netflixZoom);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            letterS.setVisibility(android.view.View.INVISIBLE);
            letterS.setAlpha(0f);

            fullLogo.setVisibility(android.view.View.VISIBLE);
            fullLogo.setAlpha(0f);
            fullLogo.startAnimation(slideInLeft);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fullLogo.setAlpha(1.0f);
                fullLogo.setScaleX(1.0f);
                fullLogo.setScaleY(1.0f);
                fullLogo.startAnimation(glowShine);

                // Start color transition animation
                startColorTransition();
            }, 700);
        }, 1800);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (colorAnimator != null) {
                colorAnimator.cancel();
            }

            // Check if this is the first time opening the app
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            boolean isFirstTime = prefs.getBoolean("isFirstTime", true);

            Intent intent;
            if (isFirstTime) {
                // First time - show onboarding
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Not first time - go directly to login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }

    private void startColorTransition() {
        // Create color transition: white → light blue → blue → light blue → white
        int colorWhite = 0xFFFFFFFF;
        int colorLightBlue = 0xFFADD8E6;
        int colorBlue = 0xFF1565C0;

        colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                colorWhite, colorLightBlue, colorBlue, colorLightBlue, colorWhite);
        colorAnimator.setDuration(3000); // 3 seconds for full cycle
        colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimator.setRepeatMode(ValueAnimator.RESTART);

        colorAnimator.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            fullLogo.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        });

        colorAnimator.start();
    }
}