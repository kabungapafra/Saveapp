package com.example.save.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.save.utils.SessionManager;

public abstract class BaseActivity extends AppCompatActivity {
    protected SessionManager session;
    private static final long INACTIVITY_TIMEOUT = 3 * 60 * 1000; // 3 minutes
    private static final long BACKGROUND_LOCK_THRESHOLD = 3 * 60 * 1000; // 3 minutes grace period before lock
    private android.os.Handler inactivityHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable inactivityRunnable = () -> lockApp();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = SessionManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBackgroundLock();
        startInactivityTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        session.saveBackgroundTime(System.currentTimeMillis());
        stopInactivityTimer();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityTimer();
    }

    private void checkBackgroundLock() {
        if (session.isLoggedIn()) {
            long lastBackgroundTime = session.getBackgroundTime();
            if (lastBackgroundTime != 0) {
                long diff = System.currentTimeMillis() - lastBackgroundTime;
                if (diff > BACKGROUND_LOCK_THRESHOLD) {
                    lockApp();
                }
            }
        }
    }

    private void startInactivityTimer() {
        if (session.isLoggedIn()) {
            inactivityHandler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT);
        }
    }

    private void stopInactivityTimer() {
        inactivityHandler.removeCallbacks(inactivityRunnable);
    }

    private void resetInactivityTimer() {
        stopInactivityTimer();
        startInactivityTimer();
    }

    private void lockApp() {
        // Only lock if we are not already on the WelcomeBack screen
        if (!getClass().getSimpleName().equals("WelcomeBackActivity")) {
            Intent intent = new Intent(this, WelcomeBackActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
