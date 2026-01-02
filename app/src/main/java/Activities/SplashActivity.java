package Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.save.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 25000;
    private ImageView letterS, fullLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Views
        letterS = findViewById(R.id.letter_s);
        fullLogo = findViewById(R.id.full_logo);
        View shimmerView = findViewById(R.id.shimmer_view);

        // Initial State
        letterS.setVisibility(android.view.View.VISIBLE);
        letterS.setAlpha(0f);
        letterS.setScaleX(0.5f);
        letterS.setScaleY(0.5f);

        fullLogo.setVisibility(android.view.View.VISIBLE);
        fullLogo.setAlpha(0f);

        // 1. S Scales In (Duration 800ms)
        letterS.animate()
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

                    letterS.animate()
                            .translationX(slideDistance)
                            .alpha(0f) // Fade out S as it moves
                            .setDuration(600)
                            .setStartDelay(200)
                            .start();

                    fullLogo.setTranslationX(100f); // Start slightly right
                    fullLogo.animate()
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
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3500);
    }
}