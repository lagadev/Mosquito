package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** Animated game-over panel: slides in with an overshoot, then stats fade in. */
public class GameOverRenderer {

    private final Paint p = new Paint(Paint.ANTI_ALIAS);
    private final RectF box = new RectF();

    public void draw(Canvas c, float w, float h, GameManager g, float anim, float time) {
        float a = Ui.clamp(anim, 0f, 1f);
        p.reset();
        p.setAntiAlias(true);
        p.setColor(Ui.argb(0.75f * a, Ui.C_NAVY));
        c.drawRect(0, 0, w, h, p);

        float e = Ui.easeOutBack(a);
        float top = h * 0.12f + (1f - e) * (-h * 0.25f);
        box.set(w * 0.08f, top, w * 0.92f, top + h * 0.52f);
        Ui.glass(c, box, Ui.dp(24f));

        float cx = w / 2f;
        float s = 1f + (float) Math.sin(time * 3f) * 0.02f;
        c.save();
        c.scale(s, s, cx, box.top + Ui.dp(60f));
        Ui.textGlow(c, "GAME OVER", cx, box.top + Ui.dp(64f), 34f, Ui.argb(a, Ui.C_PINK), Ui.argb(a, Ui.C_PURPLE),
                true, Paint.Align.CENTER);
        c.restore();

        float y = box.top + Ui.dp(120f);
        row(c, "FINAL SCORE", String.valueOf(g.getScore()), y, a, Ui.C_GOLD);
        row(c, "TOTAL KILLS", String.valueOf(g.getKills()), y + Ui.dp(42f), a, Ui.C_TEXT);
        row(c, "HIGHEST WAVE", String.valueOf(g.getWave()), y + Ui.dp(84f), a, Ui.C_TEXT);
        row(c, "BEST COMBO", g.getBestCombo() + " hits", y + Ui.dp(126f), a, Ui.C_TEXT);
        row(c, "BEST SCORE", String.valueOf(g.getBestScore()), y + Ui.dp(168f), a, Ui.C_TEXT);

        if (g.isNewBest() && a > 0.6f) {
            Ui.textGlow(c, "NEW BEST!", cx, y + Ui.dp(218f), 22f, Ui.argb(a, Ui.C_GREEN),
                    Ui.argb(a, Ui.C_GREEN), true, Paint.Align.CENTER);
        }
    }

    private void row(Canvas c, String label, String value, float y, float a, int color) {
        Ui.text(c, label, box.left + Ui.dp(30f), y, 13f, Ui.argb(a, Ui.C_MUTED), true, Paint.Align.LEFT);
        Ui.text(c, value, box.right - Ui.dp(30f), y, 18f, Ui.argb(a, color), true, Paint.Align.RIGHT);
    }
}
