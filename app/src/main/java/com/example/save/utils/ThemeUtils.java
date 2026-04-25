package com.example.save.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeUtils {
    private static final String PREF_NAME = "ThemePrefs";
    private static final String KEY_IS_DARK_MODE = "isDarkMode";

    public static void applyTheme(Context context, String role) {
        if (isDarkMode(context, role)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void toggleTheme(Context context, String role) {
        setDarkMode(context, role, !isDarkMode(context, role));
        applyTheme(context, role);
    }

    private static String getThemeKey(String role) {
        if (role == null || role.isEmpty()) {
            return KEY_IS_DARK_MODE;
        }
        return KEY_IS_DARK_MODE + "_" + role;
    }

    public static boolean isDarkMode(Context context, String role) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(getThemeKey(role), false);
    }

    private static void setDarkMode(Context context, String role, boolean isDark) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(getThemeKey(role), isDark).apply();
    }
}
