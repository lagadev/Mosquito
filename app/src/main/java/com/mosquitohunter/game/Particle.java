package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;

/** Pooled burst particle with gravity and fade-out. */
public class Particle {
    public boolean active;
    public float x, y, vx, vy, life, maxLife, size;
    public int color;

    public void spawn(float px, float py, float pvx, float pvy, float l, float s, int col) {
        x = px;
        y = py;
        vx = pvx;
        vy = pvy;
        life = l;
        maxLife = l;
        size = s;
        color = col;
        active = true;
    }

    public void update(float dt) {
        vy += Ui.dp(240f) * dt;
        x += vx * dt;
        y += vy * dt;
        vx *= Math.max(0f, 1f - dt * 1.5f);
        life -= dt;
        if (life <= 0f) active = false;
    }

    public void draw(Canvas c, Paint p) {
        float k = Math.max(0f, life / maxLife);
        p.reset();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Ui.argb(k, color));
        c.drawCircle(x, y, size * (0.4f + 0.6f * k), p);
    }
}
