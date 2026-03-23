package com.example.smartplannerclock;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class WhiteNoiseFragment extends Fragment {

    private MediaPlayer rainPlayer;
    private MediaPlayer oceanPlayer;
    private CountDownTimer countDownTimer;
    private TextView tvTimer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_white_noise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Switch switchRain = view.findViewById(R.id.switchRain);
        Switch switchOcean = view.findViewById(R.id.switchOcean);
        SeekBar seekRain = view.findViewById(R.id.seekRain);
        SeekBar seekOcean = view.findViewById(R.id.seekOcean);
        Button btnStopAll = view.findViewById(R.id.btnStopAll);
        Button btnTimer10 = view.findViewById(R.id.btnTimer10);
        tvTimer = view.findViewById(R.id.tvTimer);

        rainPlayer = MediaPlayer.create(requireContext(), R.raw.rain);
        oceanPlayer = MediaPlayer.create(requireContext(), R.raw.ocean);

        if (rainPlayer != null) rainPlayer.setLooping(true);
        if (oceanPlayer != null) oceanPlayer.setLooping(true);

        switchRain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (rainPlayer == null) return;
            if (isChecked) rainPlayer.start();
            else if (rainPlayer.isPlaying()) rainPlayer.pause();
        });

        switchOcean.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (oceanPlayer == null) return;
            if (isChecked) oceanPlayer.start();
            else if (oceanPlayer.isPlaying()) oceanPlayer.pause();
        });

        seekRain.setMax(100);
        seekRain.setProgress(50);
        seekRain.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (rainPlayer != null) {
                    float volume = progress / 100f;
                    rainPlayer.setVolume(volume, volume);
                }
            }
        });

        seekOcean.setMax(100);
        seekOcean.setProgress(50);
        seekOcean.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (oceanPlayer != null) {
                    float volume = progress / 100f;
                    oceanPlayer.setVolume(volume, volume);
                }
            }
        });

        btnStopAll.setOnClickListener(v -> stopAllSounds());
        btnTimer10.setOnClickListener(v -> startSleepTimer(10 * 60 * 1000L));
    }

    private void startSleepTimer(long millis) {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long min = millisUntilFinished / 60000;
                long sec = (millisUntilFinished % 60000) / 1000;
                tvTimer.setText("Timer: " + min + "m " + sec + "s");
            }

            @Override
            public void onFinish() {
                stopAllSounds();
                tvTimer.setText("Timer finished");
            }
        }.start();
    }

    private void stopAllSounds() {
        if (rainPlayer != null && rainPlayer.isPlaying()) rainPlayer.pause();
        if (oceanPlayer != null && oceanPlayer.isPlaying()) oceanPlayer.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        if (rainPlayer != null) {
            rainPlayer.release();
            rainPlayer = null;
        }
        if (oceanPlayer != null) {
            oceanPlayer.release();
            oceanPlayer = null;
        }
    }

    abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}