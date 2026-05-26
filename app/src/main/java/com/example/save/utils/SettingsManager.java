package com.example.save.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SettingsManager {
    private static SettingsManager instance;
    private final SharedPreferences prefs;
    private SharedPreferences encryptedPrefs;

    // Keys
    public static final String KEY_PUSH_NOTIFICATIONS = "push_notifications_enabled";
    public static final String KEY_DARK_MODE = "dark_mode_enabled";
    public static final String KEY_BIOMETRIC_LOGIN = "biometric_login_enabled";
    public static final String KEY_APP_PIN = "app_pin_secure";

    // Financial Rules Keys (Cached locally but synced to server)
    public static final String KEY_RULE_CONTRIBUTION_AMOUNT = "rule_edit_contribution_amount";
    public static final String KEY_RULE_PAYOUT_AMOUNT = "rule_edit_payout_amount";
    public static final String KEY_RULE_LATE_FEE = "rule_edit_late_fee";
    public static final String KEY_RULE_LOAN_INTEREST = "rule_edit_loan_interest";
    public static final String KEY_RULE_LOAN_LATE_FEE = "rule_edit_loan_late_fee";
    public static final String KEY_RULE_RECIPIENTS = "rule_edit_recipients";
    public static final String KEY_RULE_FREQUENCY = "rule_frequency";
    public static final String KEY_RULE_START_DATE = "rule_start_date";

    public static final String KEY_SWITCH_AUTO_PAYOUTS = "switch_automatic_payouts";
    public static final String KEY_SWITCH_SCHEDULED_CONTRIBUTIONS = "switch_scheduled_contributions";
    public static final String KEY_SWITCH_SMART_ROUNDUPS = "switch_smart_roundups";
    public static final String KEY_SWITCH_AUTOMATED_CYCLE = "switch_automated_cycle";
    public static final String KEY_SWITCH_LOAN_REQUESTS = "switch_loan_requests";

    private SettingsManager(Context context) {
        // Standard unencrypted preferences (emulating DefaultSharedPreferences)
        prefs = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);

        // Encrypted preferences for sensitive data
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "secret_settings_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            encryptedPrefs = null;
        }
    }

    public static synchronized SettingsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SettingsManager(context.getApplicationContext());
        }
        return instance;
    }

    // --- Standard Settings ---

    public void setPushNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply();
    }

    public boolean isPushNotificationsEnabled() {
        return prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true);
    }

    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setBiometricLoginEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOGIN, enabled).apply();
    }

    public boolean isBiometricLoginEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_LOGIN, false);
    }

    // --- Sensitive Settings (Encrypted) ---

    public void setAppPin(String pin) {
        if (encryptedPrefs != null) {
            encryptedPrefs.edit().putString(KEY_APP_PIN, pin).apply();
        }
    }

    public String getAppPin() {
        if (encryptedPrefs != null) {
            return encryptedPrefs.getString(KEY_APP_PIN, null);
        }
        return null;
    }

    // --- Group Financial Rules (Local Cache) ---

    public void setRuleString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getRuleString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void setRuleBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public boolean getRuleBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    // Direct access to SharedPreferences if needed for bulk edits or listeners
    public SharedPreferences getPrefs() {
        return prefs;
    }
}
