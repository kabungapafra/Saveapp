package com.example.save.ui.activities;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.databinding.ActivityOnboardingBinding;
import com.example.save.R;

public class OnboardingActivity extends AppCompatActivity {
    private ActivityOnboardingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start animations for the first slide (Circles)
        startSlide1Animations();

        binding.nextButton.setOnClickListener(v -> {
            int currentSlide = binding.viewFlipper.getDisplayedChild();
            if (currentSlide == 0) {
                // Transition to Slide 2 (Loans)
                binding.viewFlipper.showNext();
                updatePagination(1);
                binding.nextButton.setText("Continue");
                startSlide2Animations();
            } else if (currentSlide == 1) {
                // Transition to Slide 3 (Security)
                binding.viewFlipper.showNext();
                updatePagination(2);
                binding.nextButton.setText("Get Started");
                startSlide3Animations();
            } else {
                // Mark onboarding as completed
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putBoolean("isFirstTime", false).apply();

                // Go to LoginActivity
                Intent intent = new Intent(OnboardingActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void updatePagination(int slideIndex) {
        // Reset all to inactive small dots first
        resetDots();

        View activeDot;
        if (slideIndex == 0) activeDot = binding.dot1;
        else if (slideIndex == 1) activeDot = binding.dot2;
        else activeDot = binding.dot3;

        // Make active dot a blue pill
        android.view.ViewGroup.LayoutParams params = activeDot.getLayoutParams();
        params.width = (int) (24 * getResources().getDisplayMetrics().density);
        activeDot.setLayoutParams(params);
        activeDot.setBackgroundResource(R.drawable.bg_white_rounded);
        activeDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.neumorphic_blue_active)));
    }

    private void resetDots() {
        View[] dots = {binding.dot1, binding.dot2, binding.dot3};
        for (View dot : dots) {
            android.view.ViewGroup.LayoutParams params = dot.getLayoutParams();
            params.width = (int) (8 * getResources().getDisplayMetrics().density);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.circle_background);
            dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    getResources().getColor(R.color.neumorphic_dot_inactive)));
        }
    }

    private void startSlide1Animations() {
        // Slide 1 Logic...
        View outerRing = findViewById(R.id.outer_ring);
        View bubble1 = findViewById(R.id.bubble_top_right);
        View bubble2 = findViewById(R.id.bubble_left_center);
        View bubble3 = findViewById(R.id.bubble_bottom_right);

        if (outerRing != null) {
            ObjectAnimator pulse = ObjectAnimator.ofPropertyValuesHolder(outerRing,
                    PropertyValuesHolder.ofFloat("scaleX", 1f, 1.03f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f, 1.03f));
            pulse.setDuration(3000);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.start();
        }
        if (bubble1 != null) startFloating(bubble1, 0);
        if (bubble2 != null) startFloating(bubble2, 500);
        if (bubble3 != null) startFloating(bubble3, 1000);
    }

    private void startSlide2Animations() {
        View card1 = findViewById(R.id.card_1);
        View card2 = findViewById(R.id.card_2);
        View card3 = findViewById(R.id.card_3);
        View card4 = findViewById(R.id.card_4);

        if (card1 != null) animateEntry(card1, 0);
        if (card2 != null) animateEntry(card2, 80);
        if (card3 != null) animateEntry(card3, 160);
        if (card4 != null) animateEntry(card4, 240);
    }

    private void startSlide3Animations() {
        // As per the absolute pixel-perfect spec, there are no continuous 
        // or fancy entrance animations for the static third slide.
        // It strictly adheres to the provided clean layout.
    }

    private void animateScaleIn(View view, long delay) {
        view.setAlpha(0f);
        view.setScaleX(0.7f);
        view.setScaleY(0.7f);
        view.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(400).setStartDelay(delay).start();
    }

    private void startFloating(View view, long delay) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", 0f, -20f);
        anim.setDuration(4000);
        anim.setStartDelay(delay);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
    }

    private void animateEntry(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        view.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(delay).start();
    }
}