package com.example.smartplannerclock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartplannerclock.R;

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

        Switch switchNotifications = view.findViewById(R.id.switchNotifications);
        TextView tvAbout = view.findViewById(R.id.tvAbout);

        SharedPreferences prefs = requireContext().getSharedPreferences("app_settings", 0);
        boolean enabled = prefs.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(enabled);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        );

        tvAbout.setText("Smart Planner Clock\nBuilt with Java, Room, RecyclerView, AlarmManager, and MediaPlayer.");
    }
}