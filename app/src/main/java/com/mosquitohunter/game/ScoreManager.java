package com.mosquitohunter.game;

import android.content.Context;
import android.content.SharedPreferences;

/** Persists best score, best wave and lifetime stats in SharedPreferences. */
public class ScoreManager {

    private final SharedPreferences prefs;

    public ScoreManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences("mosquito_hunter_scores", Context.MODE_PRIVATE);
    }

    public int getBest() {
        return prefs.getInt("best_score", 0);
    }

    public int getBestWave() {
        return prefs.getInt("best_wave", 0);
    }

    public int getTotalKills() {
        return prefs.getInt("total_kills", 0);
    }

    public int getGamesPlayed() {
        return prefs.getInt("games_played", 0);
    }

    public void submit(int score, int kills, int wave) {
        SharedPreferences.Editor e = prefs.edit();
        if (score > getBest()) e.putInt("best_score", score);
        if (wave > getBestWave()) e.putInt("best_wave", wave);
        e.putInt("total_kills", getTotalKills() + kills);
        e.putInt("games_played", getGamesPlayed() + 1);
        e.apply();
    }

    public void reset() {
        prefs.edit().clear().apply();
    }
}
