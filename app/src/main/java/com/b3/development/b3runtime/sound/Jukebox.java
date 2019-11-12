package com.b3.development.b3runtime.sound;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;

import com.b3.development.b3runtime.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a class for sound effect and eventually background music
 * Loading sound files and play them
 */
public class Jukebox {

    static final String TAG = Jukebox.class.getSimpleName();

    private String soundsPrefKey;
    private String musicPrefKey;
    private int maxStreams;
    private int defaultSfxVolume;
    private int defaultMusicVolume;

    private boolean soundEnabled;
    private boolean musicEnabled;

    private SoundPool soundPool = null;
    private Map<SoundEvent, Integer> soundsMap = null;
    private MediaPlayer bgPlayer = null;
    private Context context;

    public Jukebox(final Context context) {
        this.context = context;
        maxStreams = context.getResources().getInteger(R.integer.sound_pool_max_streams);
        defaultSfxVolume = context.getResources().getInteger(R.integer.default_sfx_volume);
        defaultMusicVolume = context.getResources().getInteger(R.integer.default_music_volume);
        soundsPrefKey = context.getResources().getString(R.string.sound_pref_key);
        musicPrefKey = context.getResources().getString(R.string.music_pref_key);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        soundEnabled = prefs.getBoolean(soundsPrefKey, true);
        musicEnabled = prefs.getBoolean(musicPrefKey, false);
        loadIfNeeded();
    }

    private void loadIfNeeded() {
        if (soundEnabled) {
            loadSounds();
        }
        if (musicEnabled) {
            loadMusic();
        }
    }

    private void createSoundPool() {
        AudioAttributes attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(maxStreams)
                .build();

    }

    // Loads sound files
    private void loadSounds() {
        createSoundPool();
        soundsMap = new HashMap();
        loadEventSound(SoundEvent.TrackStart, context.getResources().getString(R.string.track_start_sound_file));
        loadEventSound(SoundEvent.AnswerCorrect, context.getResources().getString(R.string.correct_answer_sound_file));
        loadEventSound(SoundEvent.AnswerWrong, context.getResources().getString(R.string.wrong_answer_sound_file));
        loadEventSound(SoundEvent.TrackGoal, context.getResources().getString(R.string.track_goal_sound_file));
    }

    // Puts SoundEvent and soundId to map
    private void loadEventSound(final SoundEvent event, final String fileName) {
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(fileName);
            int soundId = soundPool.load(afd, 1);
            soundsMap.put(event, soundId);
        } catch (IOException e) {
            Log.e(TAG, "loadEventSound: error loading sound " + e.toString());
        }
    }

    // Play sound effect according to SoundEvent
    public void playSoundForGameEvent(SoundEvent event) {
        if (!soundEnabled) {
            return;
        }
        final float leftVolume = defaultSfxVolume;
        final float rightVolume = defaultSfxVolume;
        final int priority = 1;
        final int loop = 0; //-1 loop forever, 0 play once
        final float rate = 1.0f;
        final Integer soundID = soundsMap.get(event);
        if (soundID != null) {
            soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    // Loads music file for background music
    private void loadMusic() {
        try {
            bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context
                    .getAssets().openFd("");
            bgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            bgPlayer.setLooping(true);
            bgPlayer.setVolume(defaultMusicVolume, defaultMusicVolume);
            bgPlayer.prepare();
        } catch (IOException e) {
            bgPlayer = null;
            musicEnabled = false;
            Log.e(TAG, e.getMessage());
        }
    }

    private void unloadMusic() {
        if (bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.release();
            bgPlayer = null;
        }
    }

    public void pauseBgMusic() {
        if (musicEnabled) {
            bgPlayer.pause();
        }
    }

    public void resumeBgMusic() {
        if (musicEnabled) {
            bgPlayer.start();
        }
    }

    public void toggleSoundStatus() {
        soundEnabled = !soundEnabled;
        if (soundEnabled) {
            loadSounds();
        } else {
            unloadSounds();
        }
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(soundsPrefKey, soundEnabled)
                .apply();
    }

    public void toggleMusicStatus() {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            loadMusic();
        } else {
            unloadMusic();
        }
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(musicPrefKey, soundEnabled)
                .apply();
    }

    private void unloadSounds() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundsMap.clear();
        }
    }

    public void destroy() {
        unloadSounds();
        unloadMusic();
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }
}

