package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Title screen, how-to-play, settings and pause overlay. */
public class MenuRenderer {

    private static final String[] HOW = {
            "Tap and drag anywhere to aim the crosshair. Release to fire a laser.",
            "Hit mosquitoes before they escape. Each escape costs one life, and losing all three ends the night.",
            "Consecutive hits build a combo. Keep the chain alive to raise your multiplier up to x5.",
            "Every 12 kills grants a random power-up charge. Tap a charge in the bottom bar to use it.",
            "SLOW slows the swarm. MULTI fires three pellets per shot. FREEZE stops the swarm. BOOST doubles points.",
            "Golden mosquitoes are worth 50 points. Giant mosquitoes take two hits. Tiny ones are quick and worth 25."
    };

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();

    public void drawScreen(Canvas c, GameView.Screen screen, float w, float h, float time,
                           SettingsManager s, ScoreManager sc) {
        switch (screen) {
            case MENU:
                drawMenu(c, w, h, time, sc);
                break;
            case HOW_TO:
                drawHowTo(c, w, h);
                break;
            case SETTINGS:
                drawSettings(c, w, h, sc);
                break;
            default:
                break;
        }
    }

    private void drawMenu(Canvas c, float w, float h, float time, ScoreManager sc) {
        float ty = h * 0.20f + (float) Math.sin(time * 1.6f) * Ui.dp(4f);
        Ui.textGlow(c, "MOSQUITO", w / 2f, ty, 38f, Ui.C_TEXT, Ui.C_PURPLE, true, Paint.Align.CENTER);
        Ui.textGlow(c, "HUNTER", w / 2f, ty + Ui.dp(44f), 46f, Ui.C_BLUE, Ui.C_PURPLE, true, Paint.Align.CENTER);
        Ui.text(c, "NIGHT SHIFT ARCADE", w / 2f, ty + Ui.dp(72f), 12f, Ui.C_MUTED, true, Paint.Align.CENTER);

        float cx = w / 2f + (float) Math.cos(time * 0.9f) * w * 0.36f;
        float cy = h * 0.33f + (float) Math.sin(time * 1.8f) * Ui.dp(14f);
        float dx = -(float) Math.sin(time * 0.9f) * 0.9f * w * 0.36f;
        float dy = (float) Math.cos(time * 1.8f) * 1.8f * Ui.dp(14f);
        Mosquito.drawShape(c, cx, cy, (float) Math.toDegrees(Math.atan2(dy, dx)), Ui.dp(16f),
                time * 40f, time * 6f, Mosquito.Type.GOLDEN, false);

        Ui.text(c, "BEST SCORE", w / 2f, h * 0.47f, 12f, Ui.C_MUTED, true, Paint.Align.CENTER);
        Ui.textGlow(c, String.valueOf(sc.getBest()), w / 2f, h * 0.47f + Ui.dp(30f), 28f,
                Ui.C_GOLD, 0x66FFD54A, true, Paint.Align.CENTER);
        Ui.text(c, "Tap PLAY, then drag to aim and release to fire.", w / 2f, h - Ui.dp(26f), 11f,
                Ui.C_MUTED, false, Paint.Align.CENTER);
    }

    private void drawHowTo(Canvas c, float w, float h) {
        box.set(w * 0.06f, h * 0.06f, w * 0.94f, h * 0.84f);
        Ui.glass(c, box, Ui.dp(22f));
        Ui.textGlow(c, "HOW TO PLAY", w / 2f, box.top + Ui.dp(46f), 26f, Ui.C_TEXT, Ui.C_PURPLE, true, Paint.Align.CENTER);
        float y = box.top + Ui.dp(86f);
        float maxW = box.width() - Ui.dp(36f);
        for (String line : HOW) {
            y = wrap(c, line, box.left + Ui.dp(18f), y, maxW, 14f, Ui.C_TEXT) + Ui.dp(10f);
        }
    }

    private void drawSettings(Canvas c, float w, float h, ScoreManager sc) {
        Ui.textGlow(c, "SETTINGS", w / 2f, h * 0.13f, 30f, Ui.C_TEXT, Ui.C_PURPLE, true, Paint.Align.CENTER);
        Ui.text(c, "Difficulty changes mosquito speed and lifetime.", w / 2f, h * 0.18f, 12f,
                Ui.C_MUTED, false, Paint.Align.CENTER);
        Ui.text(c, "Games played " + sc.getGamesPlayed() + "    Total kills " + sc.getTotalKills(),
                w / 2f, h * 0.18f + Ui.dp(20f), 12f, Ui.C_MUTED, false, Paint.Align.CENTER);
    }

    public void drawPauseOverlay(Canvas c, float w, float h) {
        p.reset();
        p.setAntiAlias(true);
        p.setColor(0xB8070A1F);
        c.drawRect(0, 0, w, h, p);
        Ui.textGlow(c, "PAUSED", w / 2f, h * 0.24f, 34f, Ui.C_TEXT, Ui.C_BLUE, true, Paint.Align.CENTER);
        Ui.text(c, "The swarm is holding still.", w / 2f, h * 0.30f, 13f, Ui.C_MUTED, false, Paint.Align.CENTER);
    }

    /** Word-wraps text into the given width and returns the y position after the last line. */
    private float wrap(Canvas c, String text, float x, float y, float maxW, float sizeDp, int color) {
        String[] words = text.split(" ");
        String cur = "";
        float lh = Ui.dp(sizeDp * 1.45f);
        for (String word : words) {
            String test = cur.isEmpty() ? word : cur + " " + word;
            if (!cur.isEmpty() && Ui.measure(test, sizeDp, false) > maxW) {
                Ui.text(c, cur, x, y, sizeDp, color, false, Paint.Align.LEFT);
                y += lh;
                cur = word;
            } else {
                cur = test;
            }
        }
        if (!cur.isEmpty()) {
            Ui.text(c, cur, x, y, sizeDp, color, false, Paint.Align.LEFT);
            y += lh;
        }
        return y;
    }
}
