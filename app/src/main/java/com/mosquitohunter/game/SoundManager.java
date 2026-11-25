package com.mosquitohunter.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/** SoundPool for short effects and a looping MediaPlayer for background music. */
public class SoundManager {

    private final SoundPool pool;
    private final SettingsManager settings;
    private final MediaPlayer music;
    private final Vibrator vibrator;
    private final int sShoot, sHit, sCombo, sPower, sClick, sOver, sBuzz;
    private int buzzStream;
    private boolean musicWanted = true;

    public SoundManager(Context ctx, SettingsManager settings) {
        this.settings = settings;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(12).setAudioAttributes(attrs).build();
        sShoot = pool.load(ctx, R.raw.shoot, 1);
        sHit = pool.load(ctx, R.raw.hit, 1);
        sCombo = pool.load(ctx, R.raw.combo, 1);
        sPower = pool.load(ctx, R.raw.powerup, 1);
        sClick = pool.load(ctx, R.raw.click, 1);
        sOver = pool.load(ctx, R.raw.gameover, 1);
        sBuzz = pool.load(ctx, R.raw.buzz, 1);

        music = MediaPlayer.create(ctx, R.raw.music);
        if (music != null) {
            music.setLooping(true);
            music.setVolume(0.35f, 0.35f);
        }
        vibrator = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void play(int id, float volume) {
        if (settings.isSoundOn()) pool.play(id, volume, volume, 1, 0, 1f);
    }

    public void playShoot() { play(sShoot, 0.6f); }
    public void playHit() { play(sHit, 0.8f); }
    public void playCombo() { play(sCombo, 0.7f); }
    public void playPower() { play(sPower, 0.8f); }
    public void playClick() { play(sClick, 0.6f); }
    public void playGameOver() { play(sOver, 0.9f); }

    /** Starts or stops the looping buzz of mosquitoes in the room. */
    public void setBuzzing(boolean on) {
        if (on && buzzStream == 0 && settings.isSoundOn()) {
            buzzStream = pool.play(sBuzz, 0.25f, 0.25f, 1, -1, 1f);
        } else if (!on && buzzStream != 0) {
            pool.stop(buzzStream);
            buzzStream = 0;
        }
    }

    public void stopBuzz() {
        setBuzzing(false);
    }

    public void setMusicWanted(boolean wanted) {
        musicWanted = wanted;
        applyMusic();
    }

    public void applyMusic() {
        if (music == null) return;
        try {
            if (musicWanted && settings.isMusicOn()) {
                if (!music.isPlaying()) music.start();
            } else if (music.isPlaying()) {
                music.pause();
            }
        } catch (IllegalStateException ignored) {
        }
    }

    public void pauseAll() {
        stopBuzz();
        if (music == null) return;
        try {
            if (music.isPlaying()) music.pause();
        } catch (IllegalStateException ignored) {
        }
    }

    public void resumeMusic() {
        musicWanted = true;
        applyMusic();
    }

    public void vibrate(long ms) {
        if (!settings.isVibrationOn() || vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(ms);
        }
    }

    public void release() {
        stopBuzz();
        pool.release();
        if (music != null) {
            try {
                music.release();
            } catch (IllegalStateException ignored) {
            }
        }
    }
}
