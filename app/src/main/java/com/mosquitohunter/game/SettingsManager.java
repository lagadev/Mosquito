package com.mosquitohunter.game;

import android.content.Context;
import android.content.SharedPreferences;

/** User preferences: audio, vibration, particles and difficulty. */
public class SettingsManager {

    private static final String PREFS = "mosquito_hunter_settings";
    private static final String[] NAMES = {"EASY", "NORMAL", "HARD"};
    private static final float[] SPEED = {0.75f, 1f, 1.3f};
    private static final float[] LIFE = {1.35f, 1f, 0.72f};

    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private boolean flag(String key) {
        return prefs.getBoolean(key, true);
    }

    private void toggle(String key) {
        prefs.edit().putBoolean(key, !flag(key)).apply();
    }

    public boolean isSoundOn() { return flag("sound"); }
    public void toggleSound() { toggle("sound"); }

    public boolean isMusicOn() { return flag("music"); }
    public void toggleMusic() { toggle("music"); }

    public boolean isVibrationOn() { return flag("vibration"); }
    public void toggleVibration() { toggle("vibration"); }

    public boolean isParticlesOn() { return flag("particles"); }
    public void toggleParticles() { toggle("particles"); }

    public int getDifficulty() {
        return prefs.getInt("difficulty", 1);
    }

    public void cycleDifficulty() {
        prefs.edit().putInt("difficulty", (getDifficulty() + 1) % NAMES.length).apply();
    }

    public String getDifficultyName() {
        return NAMES[getDifficulty()];
    }

    public float speedFactor() {
        return SPEED[getDifficulty()];
    }

    public float lifeFactor() {
        return LIFE[getDifficulty()];
    }
}
