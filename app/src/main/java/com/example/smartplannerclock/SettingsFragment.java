package com.example.smartplannerclock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Switch switchDarkMode = view.findViewById(R.id.switchDarkMode);
        Switch switchNotifications = view.findViewById(R.id.switchNotifications);
        TextView tvAbout = view.findViewById(R.id.tvAbout);

        ThemePreferences themePreferences = new ThemePreferences(requireContext());
        switchDarkMode.setChecked(themePreferences.isDarkTheme());

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themePreferences.saveTheme(isChecked);

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        SharedPreferences prefs = requireContext().getSharedPreferences("app_settings", 0);
        boolean enabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(enabled);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        );

        tvAbout.setText("Smart Planner Clock\nBuilt with Java, Room, RecyclerView, AlarmManager, and MediaPlayer.");
    }
}