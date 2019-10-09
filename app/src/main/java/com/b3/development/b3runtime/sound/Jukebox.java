package com.b3.development.b3runtime.sound;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.b3.development.b3runtime.R;

import java.io.IOException;
import java.util.HashMap;

/**
 * This is a class for sound effect and eventually background music
 * Loading sound files and play them
 */
public class Jukebox {

    static final String TAG = "Jukebox";

    private static String SOUNDS_PREF_KEY;
    private static String MUSIC_PREF_KEY;

    private static int MAX_STREAMS;
    private static int DEFAULT_SFX_VOLUME;
    private static int DEFAULT_MUSIC_VOLUME;

    private static Jukebox jukebox;
    public boolean soundEnabled;
    public boolean musicEnabled;
    private SoundPool soundPool = null;
    private HashMap<SoundEvent, Integer> soundsMap = null;
    private MediaPlayer bgPlayer = null;
    private Context context = null;

    private Jukebox(final Context context) {
        this.context = context;
        MAX_STREAMS = context.getResources().getInteger(R.integer.sound_pool_max_streams);
        DEFAULT_SFX_VOLUME = context.getResources().getInteger(R.integer.default_sfx_volume);
        DEFAULT_MUSIC_VOLUME = context.getResources().getInteger(R.integer.default_music_volume);
        SOUNDS_PREF_KEY = context.getResources().getString(R.string.sound_pref_key);
        MUSIC_PREF_KEY = context.getResources().getString(R.string.music_pref_key);

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        soundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true);
        musicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true);
        loadIfNeeded();
    }

    public static Jukebox getInstance(Context context){
        if(jukebox == null){
            jukebox = new Jukebox(context);
        }
        return jukebox;
    }

    private void loadIfNeeded(){
        if(soundEnabled){
            loadSounds();
        }
        if(musicEnabled){
            loadMusic();
        }
    }

    @SuppressWarnings("deprecation")
    private void createSoundPool() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }
        else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(MAX_STREAMS)
                    .build();
        }
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
    private void loadEventSound(final SoundEvent event, final String fileName){
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(fileName);
            int soundId = soundPool.load(afd, 1);
            soundsMap.put(event, soundId);
        }catch(IOException e){
            Log.e(TAG, "loadEventSound: error loading sound " + e.toString());
        }
    }

    // Play sound effect according to SoundEvent
    public void playSoundForGameEvent(SoundEvent event){
        if(!soundEnabled){return;}
        final float leftVolume = DEFAULT_SFX_VOLUME;
        final float rightVolume = DEFAULT_SFX_VOLUME;
        final int priority = 1;
        final int loop = 0; //-1 loop forever, 0 play once
        final float rate = 1.0f;
        final Integer soundID = soundsMap.get(event);
        if(soundID != null){
            soundPool.play(soundID, leftVolume, rightVolume, priority, loop, rate);
        }
    }

    // Loads music file for background music
    private void loadMusic(){
        try{
            bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context
                    .getAssets().openFd("");
            bgPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength());
            bgPlayer.setLooping(true);
            bgPlayer.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
            bgPlayer.prepare();
        }catch(IOException e){
            bgPlayer = null;
            musicEnabled = false;
            Log.e(TAG, e.getMessage());
        }
    }

    private void unloadMusic(){
        if(bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.release();
        }
    }

    public void pauseBgMusic(){
        if(musicEnabled){
            bgPlayer.pause();
        }
    }
    public void resumeBgMusic(){
        if(musicEnabled){
            bgPlayer.start();
        }
    }

    public void toggleSoundStatus(){
        soundEnabled = !soundEnabled;
        if(soundEnabled){
            loadSounds();
        }else{
            unloadSounds();
        }
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(SOUNDS_PREF_KEY, soundEnabled)
                .commit();
    }

    public void toggleMusicStatus(){
        musicEnabled = !musicEnabled;
        if(musicEnabled){
            loadMusic();
        }else{
            unloadMusic();
        }
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(MUSIC_PREF_KEY, soundEnabled)
                .commit();
    }

    private void unloadSounds(){
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
            soundsMap.clear();
        }
    }

    public void destroy() {
        unloadSounds();
        unloadMusic();
        bgPlayer = null;
    }
}

