package com.example.smartplannerclock;

import android.content.Context;
import android.content.SharedPreferences;

public class ThemePreferences {

    private static final String PREFS_NAME = "settings";
    private static final String DARK_THEME_KEY = "dark_theme";

    private SharedPreferences sharedPreferences;

    public ThemePreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveTheme(boolean isDark) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DARK_THEME_KEY, isDark);
        editor.apply();
    }

    public boolean isDarkTheme() {
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false); // default = light
    }
}