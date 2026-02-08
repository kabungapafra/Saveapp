package com.example.save.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.save.ui.activities.LoginActivity;
import com.example.save.ui.activities.OnboardingActivity;

import java.util.HashMap;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {
    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private SharedPreferences.Editor editor;

    // Context
    private final Context _context;

    // Shared pref mode
    private static final int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "ChamaPrefs";

    // All Shared Preferences Keys
    private static final String IS_LOGIN = "IsLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";

    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    // User Role
    public static final String KEY_ROLE = "role";

    // First Login Flag
    private static final String KEY_IS_FIRST_LOGIN = "is_first_login";

    // Constructor
    public SessionManager(Context context) {
        this._context = context.getApplicationContext();
        try {
            // MasterKey will generate or retrieve an AES256-GCM key stored in the Android
            // keystore.
            MasterKey masterKey = new MasterKey.Builder(this._context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            this.pref = EncryptedSharedPreferences.create(
                    this._context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            // Test if we can read from it. If decryption/SecurityException happens, catch
            // and fallback.
            this.pref.getAll();

        } catch (Exception e) {
            // If encrypted storage fails (e.g. SecurityException due to corrupted keys),
            // wipe the XML file to recover.
            try {
                this._context.getSharedPreferences(PREF_NAME, PRIVATE_MODE).edit().clear().apply();
            } catch (Exception ex) {
                // Ignore
            }
            // Fallback to regular SharedPreferences
            this.pref = this._context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        }
        this.editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String name, String email, String role, boolean isFirstLogin) {
        // Storing login value as TRUE
        editor.putBoolean(IS_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, name);

        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        // Storing role
        editor.putString(KEY_ROLE, role);

        // Storing first login flag
        editor.putBoolean(KEY_IS_FIRST_LOGIN, isFirstLogin);

        // commit changes
        editor.apply();
    }

    /**
     * Get stored session data
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));

        // user role
        user.put(KEY_ROLE, pref.getString(KEY_ROLE, null));

        return user;
    }

    public String getUserEmail() {
        return pref.getString(KEY_EMAIL, null);
    }

    public String getUserName() {
        return pref.getString(KEY_NAME, null);
    }

    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     * Else won't do anything
     */
    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }
    }

    /**
     * Quick check for login
     **/
    // Get Login State
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }

    // Clear session details
    public void logoutUser() {
        try {
            // Clearing all data from Shared Preferences
            editor.clear();
            editor.commit();
        } catch (Exception e) {
            // If decryption fails or keystore is corrupted, the standard clear() might
            // fail.
            // Fallback: Use regular SharedPreferences to wipe the same file.
            try {
                _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE).edit().clear().apply();
            } catch (Exception ex) {
                // Last resort: If still failing, logging might be helpful but we must not crash
            }
        }

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        _context.startActivity(i);
    }

    private static final String KEY_JWT_TOKEN = "jwt_token";

    public void saveJwtToken(String token) {
        editor.putString(KEY_JWT_TOKEN, token);
        editor.apply();
    }

    public String getJwtToken() {
        return pref.getString(KEY_JWT_TOKEN, null);
    }

    public void setFirstLoginStatus(boolean isFirst) {
        editor.putBoolean(KEY_IS_FIRST_LOGIN, isFirst);
        editor.apply();
    }

    public boolean isFirstLogin() {
        return pref.getBoolean(KEY_IS_FIRST_LOGIN, false);
    }

    public String getUserRole() {
        return pref.getString(KEY_ROLE, null);
    }
}
