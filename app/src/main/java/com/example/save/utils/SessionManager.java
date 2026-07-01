package com.example.save.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.save.ui.activities.LoginActivity;
import com.example.save.ui.activities.WelcomeBackActivity;

import java.util.HashMap;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SessionManager {

    // ─── Encrypted prefs (session tokens, login flag) ────────────────────────
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "ChamaPrefs";

    // ─── Plain prefs (last-user memory – MUST survive encryption failures) ──
    private SharedPreferences memPref;
    private static final String MEM_PREF_NAME = "ChamaUserMemory";

    // Context
    private final Context _context;

    // ── Encrypted pref keys ──────────────────────────────────────────────────
    private static final String IS_LOGIN          = "IsLoggedIn";
    public  static final String KEY_NAME          = "name";

    public  static final String KEY_PHONE         = "phone";
    public  static final String KEY_ROLE          = "role";
    private static final String KEY_IS_FIRST_LOGIN = "is_first_login";
    private static final String KEY_IS_CREATOR    = "is_creator";
    private static final String KEY_JWT_TOKEN     = "jwt_token";
    private static final String KEY_PROFILE_IMAGE   = "profile_image_uri";

    // ── Plain (memory) pref keys — survive process kill & encryption wipe ───
    private static final String KEY_LAST_PHONE     = "last_phone";
    private static final String KEY_LAST_EMAIL     = "last_email";
    private static final String KEY_LAST_NAME      = "last_name";
    private static final String KEY_LAST_GROUP     = "last_group_name";
    private static final String KEY_LAST_ROLE      = "last_role";
    private static final String KEY_HAS_LOGGED_IN  = "has_logged_in_ever";
    private static final String KEY_BACKGROUND_TIME = "background_time";
    private static final String KEY_LOCKOUT_TIME    = "lockout_time";

    // Singleton
    private static SessionManager instance;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        this._context = context;

        // ── 1. Always open the plain memory prefs first (never fails) ────────
        memPref = _context.getSharedPreferences(MEM_PREF_NAME, Context.MODE_PRIVATE);

        // ── 2. Try encrypted prefs for sensitive session data ────────────────
        try {
            MasterKey masterKey = new MasterKey.Builder(_context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            pref = EncryptedSharedPreferences.create(
                    _context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);

            // Validate readability
            pref.getAll();

        } catch (Exception e) {
            // Encrypted prefs is corrupt — wipe ONLY the encrypted file.
            // The plain memPref (ChamaUserMemory) is intentionally NOT cleared here.
            try {
                _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        .edit().clear().apply();
            } catch (Exception ignored) {}
            // Fallback
            pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }

        editor = pref.edit();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Session creation
    // ────────────────────────────────────────────────────────────────────────

    public void createLoginSession(String name, String role, boolean isFirstLogin) {
        createLoginSession(name, "", role, isFirstLogin, false);
    }

    public void createLoginSession(String name, String role,
                                   boolean isFirstLogin, boolean isCreator) {
        createLoginSession(name, "", role, isFirstLogin, isCreator);
    }

    public void createLoginSession(String name, String phone,
                                   String role, boolean isFirstLogin, boolean isCreator) {
        // ── Sensitive data → encrypted prefs ──────────────────────────────
        editor.putBoolean(IS_LOGIN,           true);
        editor.putString(KEY_NAME,            name);

        editor.putString(KEY_PHONE,           phone);
        editor.putString(KEY_ROLE,            role);
        editor.putBoolean(KEY_IS_FIRST_LOGIN, isFirstLogin);
        editor.putBoolean(KEY_IS_CREATOR,     isCreator);
        editor.commit();

        // ── Persistent memory → plain prefs (survives encryption failure) ─
        SharedPreferences.Editor mem = memPref.edit();
        mem.putBoolean(KEY_HAS_LOGGED_IN, true);
        mem.putString(KEY_LAST_PHONE,     phone);
        mem.putString(KEY_LAST_NAME,      name);
        mem.putString(KEY_LAST_ROLE,      role);
        // NOTE: KEY_LAST_GROUP is set separately via saveLastGroup()
        mem.apply();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Getters — encrypted prefs
    // ────────────────────────────────────────────────────────────────────────

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_NAME,  pref.getString(KEY_NAME,  null));

        user.put(KEY_ROLE,  pref.getString(KEY_ROLE,  null));
        return user;
    }


    public String  getUserName()   { return pref.getString(KEY_NAME,  null); }
    public String  getUserPhone()  { return pref.getString(KEY_PHONE, "");   }
    public String  getUserRole()   { return pref.getString(KEY_ROLE,  null); }
    public boolean isLoggedIn()    { return pref.getBoolean(IS_LOGIN,         false); }
    public boolean isFirstLogin()  { return pref.getBoolean(KEY_IS_FIRST_LOGIN, false); }
    public boolean isCreator()     { return pref.getBoolean(KEY_IS_CREATOR,   false); }

    public String getJwtToken()    { return pref.getString(KEY_JWT_TOKEN, null); }

    public void saveJwtToken(String token) {
        editor.putString(KEY_JWT_TOKEN, token);
        editor.commit();
    }

    /**
     * Decodes the stored JWT and returns the "sub" claim, which the backend always
     * sets to user.phone. This is the most reliable source of the user's phone — it
     * is present in every token regardless of how the session was created (PIN or Google).
     */
    public String getPhoneFromToken() {
        String token = getJwtToken();
        if (token == null || token.isEmpty()) return "";
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "";
            byte[] decoded = android.util.Base64.decode(
                    parts[1], android.util.Base64.URL_SAFE | android.util.Base64.NO_PADDING);
            String payload = new String(decoded, "UTF-8");
            int subIdx = payload.indexOf("\"sub\"");
            if (subIdx < 0) return "";
            int colonIdx = payload.indexOf(":", subIdx);
            if (colonIdx < 0) return "";
            int openQuote = payload.indexOf("\"", colonIdx);
            if (openQuote < 0) return "";
            int closeQuote = payload.indexOf("\"", openQuote + 1);
            if (closeQuote < 0) return "";
            return payload.substring(openQuote + 1, closeQuote);
        } catch (Exception e) {
            return "";
        }
    }

    public void setFirstLoginStatus(boolean isFirst) {
        editor.putBoolean(KEY_IS_FIRST_LOGIN, isFirst);
        editor.apply();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Getters — plain memory prefs (never wiped by encryption failure)
    // ────────────────────────────────────────────────────────────────────────

    public boolean hasLoggedInBefore() { return memPref.getBoolean(KEY_HAS_LOGGED_IN, false); }
    public String  getLastPhone()      { return memPref.getString(KEY_LAST_PHONE, "");  }
    public String  getLastEmail()      { return memPref.getString(KEY_LAST_EMAIL, "");  }
    public String  getLastName()       { return memPref.getString(KEY_LAST_NAME,  "");  }
    public String  getLastRole()       { return memPref.getString(KEY_LAST_ROLE,  "");  }
    public String  getLastGroup()      { return memPref.getString(KEY_LAST_GROUP, "");  }

    public void saveLastEmail(String email) {
        memPref.edit().putString(KEY_LAST_EMAIL, email).apply();
    }

    public void saveLastGroup(String groupName) {
        memPref.edit().putString(KEY_LAST_GROUP, groupName).apply();
    }

    public void saveBackgroundTime(long time) {
        memPref.edit().putLong(KEY_BACKGROUND_TIME, time).apply();
    }

    public long getBackgroundTime() {
        return memPref.getLong(KEY_BACKGROUND_TIME, 0);
    }

    public void saveLockoutTime(long timeMillis) {
        memPref.edit().putLong(KEY_LOCKOUT_TIME, timeMillis).apply();
    }

    public long getLockoutTime() {
        return memPref.getLong(KEY_LOCKOUT_TIME, 0);
    }

    public void saveProfileImage(String uri) {
        saveProfileImage(getUserPhone(), uri);
    }

    /** Cache an avatar path/uri for a specific member phone (used to rehydrate from server). */
    public void saveProfileImage(String phone, String uri) {
        String key = normalizePhoneKey(phone);
        if (key != null && !key.isEmpty()) {
            memPref.edit().putString(KEY_PROFILE_IMAGE + "_" + key, uri).apply();
        }
    }

    public String getProfileImage() {
        return getProfileImage(getUserPhone());
    }

    public String getProfileImage(String phone) {
        String key = normalizePhoneKey(phone);
        if (key != null && !key.isEmpty()) {
            return memPref.getString(KEY_PROFILE_IMAGE + "_" + key, null);
        }
        return null;
    }

    /**
     * Normalize a phone to a stable key (digits and leading +) so that saving and looking up
     * an avatar always match, regardless of formatting. Cards, rehydration and the profile
     * screen all funnel through here, keeping the key consistent everywhere.
     */
    private static String normalizePhoneKey(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9+]", "");
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Login check redirect
    // ────────────────────────────────────────────────────────────────────────

    public void checkLogin() {
        if (!isLoggedIn()) {
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Logout
    // ────────────────────────────────────────────────────────────────────────

    public void logoutUser() {
        // Step 1 — Cancel in-flight requests, evict stale connections, zero out interceptor token.
        // Must happen FIRST so no requests fire with the old token after prefs are wiped.
        try {
            com.example.save.data.network.RetrofitClient.getInstance(_context).logout();
        } catch (Exception ignored) {}

        // Step 2 — Wipe encrypted session prefs, then refresh the editor reference.
        // Refreshing editor = pref.edit() is critical: without it, the next login's
        // createLoginSession() / saveJwtToken() writes go to a drained editor object
        // and may silently fail, causing an immediate "not logged in" state on re-login.
        try {
            editor.clear();
            editor.commit();
            editor = pref.edit(); // fresh editor for the next session
        } catch (Exception e) {
            try {
                _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        .edit().clear().apply();
            } catch (Exception ignored) {}
        }

        // Step 3 — Flush cached repository data (isolated so a repo failure never
        // blocks navigation to the login screen).
        try {
            com.example.save.data.repository.MemberRepository
                    .getInstance(_context).clearData();
        } catch (Exception ignored) {}

        // memPref (last-user memory) is deliberately NOT cleared — it powers
        // the "Welcome Back" screen on next open.

        // Route to Welcome Back if we know the user; else plain login.
        Intent i;
        if (memPref.getBoolean(KEY_HAS_LOGGED_IN, false)) {
            i = new Intent(_context, WelcomeBackActivity.class);
        } else {
            i = new Intent(_context, LoginActivity.class);
        }
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                 | Intent.FLAG_ACTIVITY_NEW_TASK
                 | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(i);
    }
}
