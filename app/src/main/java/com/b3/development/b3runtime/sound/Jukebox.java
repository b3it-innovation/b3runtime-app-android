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

import java.io.IOException;
import java.util.HashMap;

/**
 * This is a class for sound effect and eventually background music
 * Loading sound files and play them
 */
public class Jukebox {

    static final String TAG = "Jukebox";

    private static final int MAX_STREAMS = 3;
    private static final int DEFAULT_SFX_VOLUME = 1;
    private static final int DEFAULT_MUSIC_VOLUME = 1;

    private static final String SOUNDS_PREF_KEY = "sound_pref_key";
    private static final String MUSIC_PREF_KEY = "sound_pref_key";

    public boolean soundEnabled;
    public boolean musicEnabled;
    private SoundPool soundPool = null;
    private HashMap<SoundEvent, Integer> soundsMap = null;
    private MediaPlayer bgPlayer = null;
    private Context context = null;

    public Jukebox(final Context context) {
        this.context = context;
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        soundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true);
        musicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true);
        loadIfNeeded();
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

    private void loadSounds() {
        createSoundPool();
        soundsMap = new HashMap();
        loadEventSound(SoundEvent.TrackStart, "sfx/jump.wav");
        loadEventSound(SoundEvent.AnswerCorrect, "sfx/correct_answer.mp3");
        loadEventSound(SoundEvent.AnswerWrong, "sfx/wrong_answer.mp3");
        loadEventSound(SoundEvent.TrackGoal, "sfx/pickup_coin.wav");
    }

    private void loadEventSound(final SoundEvent event, final String fileName){
        try {
            AssetFileDescriptor afd = context.getAssets().openFd(fileName);
            int soundId = soundPool.load(afd, 1);
            soundsMap.put(event, soundId);
        }catch(IOException e){
            Log.e(TAG, "loadEventSound: error loading sound " + e.toString());
        }
    }

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

    private void loadMusic(){
        try{
            bgPlayer = new MediaPlayer();
            AssetFileDescriptor afd = context
                    .getAssets().openFd("sfx/indoors_one.mp3");
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

