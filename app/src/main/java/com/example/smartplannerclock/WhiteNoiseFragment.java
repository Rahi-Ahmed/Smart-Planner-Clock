package com.example.smartplannerclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class WhiteNoiseFragment extends Fragment {

    private static final String PREFS_NAME = "white_noise_prefs";
    private static final String KEY_PRESET_NAMES = "preset_names";

    private LinearLayout soundContainer;
    private TextView tvTimer;
    private EditText etPresetName;
    private Spinner spinnerPresets;

    private CountDownTimer countDownTimer;
    private SharedPreferences prefs;

    private ArrayAdapter<String> presetAdapter;
    private final List<String> presetNames = new ArrayList<>();

    private final List<SoundItem> soundItems = new ArrayList<>();
    private final Map<String, SoundItem> soundMap = new HashMap<>();

    private static class SoundDefinition {
        final String key;
        final String label;
        final int rawResId;

        SoundDefinition(String key, String label, int rawResId) {
            this.key = key;
            this.label = label;
            this.rawResId = rawResId;
        }
    }

    private static class SoundItem {
        final String key;
        final String label;
        final int rawResId;
        MediaPlayer player;
        Switch soundSwitch;
        SeekBar volumeSeekBar;
        int currentVolume = 50;

        SoundItem(String key, String label, int rawResId) {
            this.key = key;
            this.label = label;
            this.rawResId = rawResId;
        }
    }

    /**
     * Format for adding new default sound:
     * new SoundDefinition("forest", "Forest", R.raw.forest)
     * new SoundDefinition("wind", "Wind", R.raw.wind)
     */
    private List<SoundDefinition> getSoundDefinitions() {
        List<SoundDefinition> defs = new ArrayList<>();
        defs.add(new SoundDefinition("rain", "Rain", R.raw.rain));
        defs.add(new SoundDefinition("ocean", "Ocean", R.raw.ocean));
        return defs;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_white_noise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        soundContainer = view.findViewById(R.id.soundContainer);
        tvTimer = view.findViewById(R.id.tvTimer);
        etPresetName = view.findViewById(R.id.etPresetName);
        spinnerPresets = view.findViewById(R.id.spinnerPresets);

        Button btnStopAll = view.findViewById(R.id.btnStopAll);
        Button btnTimer10 = view.findViewById(R.id.btnTimer10);
        Button btnTimer30 = view.findViewById(R.id.btnTimer30);
        Button btnTimer60 = view.findViewById(R.id.btnTimer60);
        Button btnTimerCustom = view.findViewById(R.id.btnTimerCustom);
        Button btnCancelTimer = view.findViewById(R.id.btnCancelTimer);

        Button btnSavePreset = view.findViewById(R.id.btnSavePreset);
        Button btnLoadPreset = view.findViewById(R.id.btnLoadPreset);
        Button btnDeletePreset = view.findViewById(R.id.btnDeletePreset);

        buildSoundRows();

        setupPresetSpinner();

        btnStopAll.setOnClickListener(v -> stopAllSounds());

        btnTimer10.setOnClickListener(v -> startSleepTimer(10 * 60 * 1000L));
        btnTimer30.setOnClickListener(v -> startSleepTimer(30 * 60 * 1000L));
        btnTimer60.setOnClickListener(v -> startSleepTimer(60 * 60 * 1000L));
        btnTimerCustom.setOnClickListener(v -> showCustomTimerDialog());
        btnCancelTimer.setOnClickListener(v -> cancelSleepTimer());

        btnSavePreset.setOnClickListener(v -> savePreset());
        btnLoadPreset.setOnClickListener(v -> loadSelectedPreset());
        btnDeletePreset.setOnClickListener(v -> deleteSelectedPreset());
    }

    private void buildSoundRows() {
        soundItems.clear();
        soundMap.clear();
        soundContainer.removeAllViews();

        for (SoundDefinition def : getSoundDefinitions()) {
            SoundItem item = new SoundItem(def.key, def.label, def.rawResId);
            item.player = MediaPlayer.create(requireContext(), def.rawResId);

            if (item.player != null) {
                item.player.setLooping(true);
                setPlayerVolume(item.player, item.currentVolume);
            }

            LinearLayout rowContainer = new LinearLayout(requireContext());
            rowContainer.setOrientation(LinearLayout.VERTICAL);
            rowContainer.setPadding(0, dp(8), 0, dp(8));

            Switch soundSwitch = new Switch(requireContext());
            soundSwitch.setText(def.label);

            SeekBar seekBar = new SeekBar(requireContext());
            seekBar.setMax(100);
            seekBar.setProgress(50);

            item.soundSwitch = soundSwitch;
            item.volumeSeekBar = seekBar;

            soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (item.player == null) return;

                if (isChecked) {
                    if (!item.player.isPlaying()) {
                        item.player.start();
                    }
                } else {
                    if (item.player.isPlaying()) {
                        item.player.pause();
                    }
                }
            });

            seekBar.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    item.currentVolume = progress;
                    setPlayerVolume(item.player, progress);
                }
            });

            rowContainer.addView(soundSwitch);
            rowContainer.addView(seekBar);
            soundContainer.addView(rowContainer);

            soundItems.add(item);
            soundMap.put(item.key, item);
        }
    }

    private void setupPresetSpinner() {
        presetAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                presetNames
        );
        presetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPresets.setAdapter(presetAdapter);
        refreshPresetList();
    }

    private void refreshPresetList() {
        presetNames.clear();
        Set<String> savedNames = prefs.getStringSet(KEY_PRESET_NAMES, new HashSet<>());
        if (savedNames != null) {
            presetNames.addAll(savedNames);
            Collections.sort(presetNames);
        }
        presetAdapter.notifyDataSetChanged();
    }

    private void savePreset() {
        String presetName = etPresetName.getText().toString().trim();

        if (TextUtils.isEmpty(presetName)) {
            Toast.makeText(requireContext(), "Enter a preset name", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        Set<String> savedNames = new HashSet<>(prefs.getStringSet(KEY_PRESET_NAMES, new HashSet<>()));
        savedNames.add(presetName);
        editor.putStringSet(KEY_PRESET_NAMES, savedNames);

        String base = "preset_" + presetName + "_";

        for (SoundItem item : soundItems) {
            editor.putBoolean(base + item.key + "_enabled", item.soundSwitch.isChecked());
            editor.putInt(base + item.key + "_volume", item.volumeSeekBar.getProgress());
        }

        editor.apply();
        refreshPresetList();
        setSpinnerSelection(presetName);

        Toast.makeText(requireContext(), "Preset saved", Toast.LENGTH_SHORT).show();
    }

    private void loadSelectedPreset() {
        String presetName = getSelectedPresetName();
        if (presetName == null) {
            Toast.makeText(requireContext(), "No preset selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String base = "preset_" + presetName + "_";

        stopAllSounds();

        for (SoundItem item : soundItems) {
            boolean enabled = prefs.getBoolean(base + item.key + "_enabled", false);
            int volume = prefs.getInt(base + item.key + "_volume", 50);

            item.volumeSeekBar.setProgress(volume);
            item.soundSwitch.setChecked(enabled);
        }

        Toast.makeText(requireContext(), "Preset loaded", Toast.LENGTH_SHORT).show();
    }

    private void deleteSelectedPreset() {
        String presetName = getSelectedPresetName();
        if (presetName == null) {
            Toast.makeText(requireContext(), "No preset selected", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<String> savedNames = new HashSet<>(prefs.getStringSet(KEY_PRESET_NAMES, new HashSet<>()));
        if (!savedNames.remove(presetName)) {
            Toast.makeText(requireContext(), "Preset not found", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_PRESET_NAMES, savedNames);

        String base = "preset_" + presetName + "_";
        for (SoundItem item : soundItems) {
            editor.remove(base + item.key + "_enabled");
            editor.remove(base + item.key + "_volume");
        }

        editor.apply();
        refreshPresetList();
        etPresetName.setText("");

        Toast.makeText(requireContext(), "Preset deleted", Toast.LENGTH_SHORT).show();
    }

    private String getSelectedPresetName() {
        if (presetNames.isEmpty()) return null;

        Object selected = spinnerPresets.getSelectedItem();
        if (selected == null) return null;

        String name = selected.toString().trim();
        return name.isEmpty() ? null : name;
    }

    private void setSpinnerSelection(String presetName) {
        for (int i = 0; i < presetNames.size(); i++) {
            if (presetNames.get(i).equals(presetName)) {
                spinnerPresets.setSelection(i);
                return;
            }
        }
    }

    private void showCustomTimerDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("Enter minutes");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setGravity(Gravity.CENTER);

        new AlertDialog.Builder(requireContext())
                .setTitle("Custom Sleep Timer")
                .setMessage("Enter the number of minutes")
                .setView(input)
                .setPositiveButton("Set", (dialog, which) -> {
                    String value = input.getText().toString().trim();

                    if (TextUtils.isEmpty(value)) {
                        Toast.makeText(requireContext(), "Please enter minutes", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int minutes;
                    try {
                        minutes = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Invalid number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (minutes <= 0) {
                        Toast.makeText(requireContext(), "Minutes must be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    startSleepTimer(minutes * 60L * 1000L);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startSleepTimer(long millis) {
        cancelSleepTimer();

        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long minutes = totalSeconds / 60;
                long seconds = totalSeconds % 60;
                tvTimer.setText(String.format(Locale.getDefault(),
                        "Timer: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                stopAllSounds();
                tvTimer.setText("Timer finished");
            }
        }.start();
    }

    private void cancelSleepTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        tvTimer.setText("Timer: not set");
    }

    private void stopAllSounds() {
        for (SoundItem item : soundItems) {
            if (item.player != null && item.player.isPlaying()) {
                item.player.pause();
                item.player.seekTo(0);
            }
            if (item.soundSwitch != null) {
                item.soundSwitch.setChecked(false);
            }
        }
    }

    private void setPlayerVolume(MediaPlayer player, int progress) {
        if (player == null) return;
        float volume = progress / 100f;
        player.setVolume(volume, volume);
    }

    private int dp(int value) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        for (SoundItem item : soundItems) {
            if (item.player != null) {
                item.player.release();
                item.player = null;
            }
        }

        soundItems.clear();
        soundMap.clear();
    }

    abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}