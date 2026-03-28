package com.example.smartplannerclock.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.smartplannerclock.R;
import com.example.smartplannerclock.SettingsFragment;
import com.example.smartplannerclock.TaskFragment;
import com.example.smartplannerclock.ThemePreferences;
import com.example.smartplannerclock.WhiteNoiseFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemePreferences themePreferences = new ThemePreferences(this);

        if (themePreferences.isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        if (savedInstanceState == null) {
            loadFragment(new TaskFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_tasks) {
                loadFragment(new TaskFragment());
                return true;
            } else if (id == R.id.nav_white_noise) {
                loadFragment(new WhiteNoiseFragment());
                return true;
            } else if (id == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}