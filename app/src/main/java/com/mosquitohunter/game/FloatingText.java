package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Pooled text that drifts upward and fades (score pops, power-up notices). */
public class FloatingText {
    public boolean active;
    public String text = "";
    public float x, y, life, maxLife;
    public int color;
    public boolean big;

    public void spawn(String s, float px, float py, int col, boolean isBig) {
        text = s;
        x = px;
        y = py;
        color = col;
        big = isBig;
        maxLife = isBig ? 1.4f : 0.9f;
        life = maxLife;
        active = true;
    }

    public void update(float dt) {
        y -= Ui.dp(60f) * dt;
        life -= dt;
        if (life <= 0f) active = false;
    }

    public void draw(Canvas c) {
        float k = Math.max(0f, life / maxLife);
        float pop = k > 0.85f ? 1f + (k - 0.85f) * 2f : 1f;
        c.save();
        c.scale(pop, pop, x, y);
        Ui.textGlow(c, text, x, y, big ? 22f : 16f, Ui.argb(k, color), 0xCC000000, true,
                Paint.Align.CENTER);
        c.restore();
    }
}
