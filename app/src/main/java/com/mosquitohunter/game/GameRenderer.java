package com.mosquitohunter.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Random;

/**
 * Draws the moonlit bedroom (pre-rendered once into a bitmap), the sleeping
 * character, all entities, the aim crosshair and the HUD.
 */
public class GameRenderer {

    private static final int WIN_STARS = 18;
    private static final int DUST = 26;

    private final Paint p = new Paint(Paint.ANTI_ALIAS);
    private final RectF bed = new RectF();
    private final RectF pillow = new RectF();
    private final RectF blanket = new RectF();
    private final RectF win = new RectF();
    private final RectF tmp = new RectF();
    private final Path path = new Path();
    private final float[] starX = new float[WIN_STARS];
    private final float[] starY = new float[WIN_STARS];
    private final float[] starPh = new float[WIN_STARS];
    private final float[] dustSeed = new float[DUST];
    private final Random rnd = new Random(7);

    private Bitmap bg;
    private int bgW = -1;
    private int bgH = -1;
    private float hx;
    private float hy;
    private float hr;

    // ------------------------------------------------------------ background

    private void ensureBackground(float w, float h) {
        int iw = (int) w;
        int ih = (int) h;
        if (bg != null && bgW == iw && bgH == ih) return;
        bgW = iw;
        bgH = ih;
        layout(w, h);
        bg = Bitmap.createBitmap(iw, ih, Bitmap.Config.ARGB_8888);
        drawRoom(new Canvas(bg), w, h);
        for (int i = 0; i < DUST; i++) dustSeed[i] = rnd.nextFloat();
    }

    private void layout(float w, float h) {
        rnd.setSeed(7);
        bed.set(w * 0.20f, h * 0.56f, w * 0.94f, h * 0.74f);
        win.set(w * 0.56f, h * 0.08f, w * 0.92f, h * 0.36f);
        float bw = bed.width();
        float bh = bed.height();
        pillow.set(bed.left + bw * 0.05f, bed.top + bh * 0.10f, bed.left + bw * 0.27f, bed.top + bh * 0.50f);
        hx = pillow.centerX();
        hy = pillow.centerY() - Ui.dp(4f);
        hr = pillow.height() * 0.58f;
        for (int i = 0; i < WIN_STARS; i++) {
            starX[i] = win.left + Ui.dp(10f) + rnd.nextFloat() * (win.width() - Ui.dp(20f));
            starY[i] = win.top + Ui.dp(10f) + rnd.nextFloat() * (win.height() - Ui.dp(20f));
            starPh[i] = rnd.nextFloat() * 6.28f;
        }
    }

    private void drawRoom(Canvas c, float w, float h) {
        float floorY = h * 0.74f;
        p.reset();
        p.setAntiAlias(true);

        p.setShader(new LinearGradient(0, 0, 0, floorY, 0xFF0B0930, 0xFF2B1760, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, floorY, p);
        p.setShader(new LinearGradient(0, floorY, 0, h, 0xFF2A1655, 0xFF080418, Shader.TileMode.CLAMP));
        c.drawRect(0, floorY, w, h, p);
        p.setShader(null);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Ui.dp(1f));
        p.setColor(0x14FFFFFF);
        for (int i = 0; i <= 10; i++) {
            float x = w * i / 10f;
            c.drawLine(x, floorY, x, h, p);
        }
        for (int i = 1; i <= 6; i++) {
            float y = floorY + (h - floorY) * i / 6f;
            c.drawLine(0, y, w, y, p);
        }

        // moonlight spill and window
        p.setStyle(Paint.Style.FILL);
        p.setShader(new RadialGradient(win.centerX(), win.centerY(), win.width() * 0.9f,
                0x33B6C8FF, 0x00B6C8FF, Shader.TileMode.CLAMP));
        c.drawCircle(win.centerX(), win.centerY(), win.width() * 0.9f, p);
        p.setShader(new LinearGradient(0, win.top, 0, win.bottom, 0xFF0A1440, 0xFF1F1B63, Shader.TileMode.CLAMP));
        c.drawRoundRect(win, Ui.dp(10f), Ui.dp(10f), p);
        p.setShader(null);

        float mx = win.left + win.width() * 0.72f;
        float my = win.top + win.height() * 0.34f;
        float mr = Ui.dp(20f);
        p.setShader(new RadialGradient(mx, my, mr * 3.2f, 0x66FFF2C2, 0x00FFF2C2, Shader.TileMode.CLAMP));
        c.drawCircle(mx, my, mr * 3.2f, p);
        p.setShader(null);
        p.setColor(0xFFFFF4D6);
        c.drawCircle(mx, my, mr, p);
        p.setColor(0x22000000);
        c.drawCircle(mx - mr * 0.3f, my - mr * 0.2f, mr * 0.2f, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Ui.dp(5f));
        p.setColor(0xFF3A2A6E);
        c.drawRoundRect(win, Ui.dp(10f), Ui.dp(10f), p);
        c.drawLine(win.centerX(), win.top, win.centerX(), win.bottom, p);
        c.drawLine(win.left, win.centerY(), win.right, win.centerY(), p);
        p.setStrokeWidth(Ui.dp(3f));
        p.setColor(0xFF9C7BFF);
        c.drawLine(win.left - Ui.dp(30f), win.top - Ui.dp(16f), win.right + Ui.dp(30f), win.top - Ui.dp(16f), p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF4B1D7A);
        tmp.set(win.left - Ui.dp(26f), win.top - Ui.dp(14f), win.left + Ui.dp(2f), win.bottom + Ui.dp(24f));
        c.drawRoundRect(tmp, Ui.dp(8f), Ui.dp(8f), p);
        tmp.set(win.right - Ui.dp(2f), win.top - Ui.dp(14f), win.right + Ui.dp(26f), win.bottom + Ui.dp(24f));
        c.drawRoundRect(tmp, Ui.dp(8f), Ui.dp(8f), p);

        // bedside table and lamp
        float lx = w * 0.075f;
        float ly = h * 0.44f;
        p.setShader(new RadialGradient(lx, ly, w * 0.42f, 0x66FFC46B, 0x00FFC46B, Shader.TileMode.CLAMP));
        c.drawCircle(lx, ly, w * 0.42f, p);
        p.setShader(null);
        p.setColor(0xFF3B2658);
        tmp.set(w * 0.01f, h * 0.52f, w * 0.135f, h * 0.60f);
        c.drawRoundRect(tmp, Ui.dp(6f), Ui.dp(6f), p);
        p.setColor(0xFF6A4BA0);
        c.drawRect(lx - Ui.dp(3f), h * 0.47f, lx + Ui.dp(3f), h * 0.52f, p);
        path.reset();
        path.moveTo(lx - w * 0.035f, h * 0.40f);
        path.lineTo(lx + w * 0.035f, h * 0.40f);
        path.lineTo(lx + w * 0.02f, h * 0.47f);
        path.lineTo(lx - w * 0.02f, h * 0.47f);
        path.close();
        p.setColor(0xFFFFC46B);
        c.drawPath(path, p);

        // rug, bed, pillow
        p.setColor(0x26FF4FD8);
        c.drawOval(w * 0.12f, h * 0.78f, w * 0.88f, h * 1.02f, p);
        p.setColor(0x66000000);
        c.drawOval(bed.left, bed.bottom - Ui.dp(4f), bed.right + Ui.dp(8f), bed.bottom + Ui.dp(18f), p);
        p.setColor(0xFF2D1B4E);
        c.drawRoundRect(bed, Ui.dp(10f), Ui.dp(10f), p);
        tmp.set(bed.left - Ui.dp(18f), bed.top - Ui.dp(22f), bed.left + Ui.dp(6f), bed.bottom + Ui.dp(6f));
        c.drawRoundRect(tmp, Ui.dp(10f), Ui.dp(10f), p);
        p.setColor(0xFFC9BCEB);
        tmp.set(bed.left + Ui.dp(4f), bed.top + bed.height() * 0.10f,
                bed.right - Ui.dp(4f), bed.bottom - bed.height() * 0.12f);
        c.drawRoundRect(tmp, Ui.dp(8f), Ui.dp(8f), p);
        p.setColor(0xFFF4F0FF);
        c.drawRoundRect(pillow, Ui.dp(14f), Ui.dp(14f), p);

        // vignette
        p.setShader(new RadialGradient(w * 0.5f, h * 0.42f, Math.max(w, h) * 0.8f,
                0x00000000, 0xCC000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, p);
        p.setShader(null);
    }

    /** Background used by menus and game world alike. */
    public void drawBackground(Canvas c, float w, float h, float time) {
        ensureBackground(w, h);
        c.drawBitmap(bg, 0, 0, null);
        p.reset();
        p.setAntiAlias(true);
        for (int i = 0; i < WIN_STARS; i++) {
            float a = 0.45f + 0.55f * (float) Math.sin(time * 2.1f + starPh[i]);
            p.setColor(Ui.argb(a, 0xFFFFFFFF));
            c.drawCircle(starX[i], starY[i], Ui.dp(1.1f + (i % 3) * 0.5f), p);
        }
        for (int i = 0; i < DUST; i++) {
            float x = (dustSeed[i] * w + (float) Math.sin(time * 0.4f + i) * Ui.dp(10f) + w) % w;
            float y = h - ((time * Ui.dp(10f) * (0.4f + dustSeed[i]) + dustSeed[i] * h) % h);
            p.setColor(Ui.argb(0.25f, 0xFFFFE9B0));
            c.drawCircle(x, y, Ui.dp(1.2f), p);
        }
    }

    private void drawCharacter(Canvas c, float time) {
        float breath = (float) Math.sin(time * 2.2f);
        float bh = bed.height();

        blanket.set(bed.left + bed.width() * 0.30f,
                bed.top + bh * 0.02f - breath * Ui.dp(3f),
                bed.right - Ui.dp(2f),
                bed.bottom - bh * 0.06f);
        p.reset();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF7C5CFF);
        c.drawRoundRect(blanket, Ui.dp(16f), Ui.dp(16f), p);
        p.setColor(0x33FFFFFF);
        tmp.set(blanket.left, blanket.top, blanket.right, blanket.top + bh * 0.10f);
        c.drawRoundRect(tmp, Ui.dp(16f), Ui.dp(16f), p);
        p.setColor(0xFF6A4AE6);
        float fy = blanket.top + Ui.dp(6f) - breath * Ui.dp(1.5f);
        c.drawOval(blanket.right - Ui.dp(70f), fy, blanket.right - Ui.dp(12f), fy + bh * 0.36f, p);

        p.setColor(0xFFF2C4A0);
        c.drawCircle(hx, hy, hr, p);
        p.setColor(0xFF3A2418);
        tmp.set(hx - hr * 1.06f, hy - hr * 1.08f, hx + hr * 1.06f, hy + hr * 0.25f);
        c.drawArc(tmp, 180f, 180f, true, p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(Ui.dp(1.8f));
        p.setColor(0xFF2B1A12);
        tmp.set(hx - hr * 0.58f, hy - hr * 0.18f, hx - hr * 0.14f, hy + hr * 0.18f);
        c.drawArc(tmp, 20f, 140f, false, p);
        tmp.set(hx + hr * 0.14f, hy - hr * 0.18f, hx + hr * 0.58f, hy + hr * 0.18f);
        c.drawArc(tmp, 20f, 140f, false, p);
        tmp.set(hx - hr * 0.22f, hy + hr * 0.22f, hx + hr * 0.22f, hy + hr * 0.55f);
        c.drawArc(tmp, 20f, 140f, false, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(0x55FF7AA8);
        c.drawCircle(hx - hr * 0.42f, hy + hr * 0.28f, hr * 0.16f, p);
        c.drawCircle(hx + hr * 0.42f, hy + hr * 0.28f, hr * 0.16f, p);

        for (int i = 0; i < 3; i++) {
            float ph = (time * 0.45f + i / 3f) % 1f;
            float zx = hx + hr * (0.9f + i * 0.35f);
            float zy = hy - hr * 1.2f - ph * Ui.dp(40f) - i * Ui.dp(4f);
            Ui.text(c, "z", zx, zy, 10f + 8f * ph, Ui.argb(1f - ph, 0xFFDCD2FF), true, android.graphics.Paint.Align.LEFT);
        }
    }

    // -------------------------------------------------------------- world

    public void draw(Canvas c, GameManager g, float time, boolean aiming, float aimX, float aimY) {
        float w = g.getWidth();
        float h = g.getHeight();
        c.save();
        float sh = g.getShake();
        if (sh > 0.5f) {
            c.translate((rnd.nextFloat() * 2f - 1f) * sh, (rnd.nextFloat() * 2f - 1f) * sh);
        }
        drawBackground(c, w, h, time);
        drawCharacter(c, time);
        for (Mosquito m : g.getMosquitoes()) {
            if (m.active) m.draw(c);
        }
        for (Particle pt : g.getParticles()) {
            if (pt.active) pt.draw(c, p);
        }
        for (Projectile pr : g.getShots()) {
            if (pr.active) drawShot(c, pr);
        }
        g.player.draw(c, time);
        for (FloatingText t : g.getTexts()) {
            if (t.active) t.draw(c);
        }
        if (aiming) drawCrosshair(c, aimX, aimY, time);
        c.restore();

        drawHud(c, g, w, h);
        if (g.getState() == GameState.WAVE_BREAK) drawAnnounce(c, g, w, h);
        if (g.getState() == GameState.PLAYING && g.getComboCount() >= 2) drawComboBanner(c, g, w, h);
    }

    private void drawShot(Canvas c, Projectile s) {
        float len = Ui.dp(90f);
        p.reset();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setColor(0x6622D3EE);
        p.setStrokeWidth(Ui.dp(9f));
        c.drawLine(s.x - s.dx * len, s.y - s.dy * len, s.x, s.y, p);
        p.setColor(0xFFBDF4FF);
        p.setStrokeWidth(Ui.dp(2.5f));
        c.drawLine(s.x - s.dx * len * 0.5f, s.y - s.dy * len * 0.5f, s.x, s.y, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFFFFFFF);
        c.drawCircle(s.x, s.y, Ui.dp(3f), p);
    }

    private void drawCrosshair(Canvas c, float x, float y, float time) {
        float r = Ui.dp(20f) + (float) Math.sin(time * 9f) * Ui.dp(2f);
        p.reset();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Ui.dp(2f));
        p.setColor(Ui.C_PINK);
        c.drawCircle(x, y, r, p);
        for (int i = 0; i < 4; i++) {
            float a = i * 1.5708f + time;
            float cos = (float) Math.cos(a);
            float sin = (float) Math.sin(a);
            c.drawLine(x + cos * (r - Ui.dp(7f)), y + sin * (r - Ui.dp(7f)),
                    x + cos * (r + Ui.dp(5f)), y + sin * (r + Ui.dp(5f)), p);
        }
        p.setStyle(Paint.Style.FILL);
        p.setColor(Ui.C_PINK);
        c.drawCircle(x, y, Ui.dp(2.5f), p);
    }

    // ---------------------------------------------------------------- HUD

    private void drawHud(Canvas c, GameManager g, float w, float h) {
        tmp.set(Ui.dp(70f), Ui.dp(10f), w - Ui.dp(10f), Ui.dp(Ui.TOP_H) - Ui.dp(4f));
        Ui.glass(c, tmp, Ui.dp(16f));
        float cy = tmp.centerY() + Ui.dp(5f);
        Ui.text(c, "WAVE " + g.getWave(), tmp.left + Ui.dp(14f), cy, 14f, Ui.C_GOLD, true, Paint.Align.LEFT);
        Ui.text(c, "KILLS " + g.getKills(), tmp.right - Ui.dp(14f), cy, 14f, Ui.C_TEXT, true, Paint.Align.RIGHT);

        float bottom = h - Ui.dp(72f);
        tmp.set(Ui.dp(10f), bottom - Ui.dp(64f), w - Ui.dp(10f), bottom);
        Ui.glass(c, tmp, Ui.dp(16f));
        for (int i = 0; i < 3; i++) {
            int col = i < g.getLives() ? Ui.C_RED : 0x44FFFFFF;
            Ui.text(c, "\u2665", tmp.left + Ui.dp(18f + i * 24f), tmp.top + Ui.dp(38f), 22f, col, false, Paint.Align.LEFT);
        }
        Ui.text(c, "SCORE", tmp.centerX(), tmp.top + Ui.dp(22f), 10f, Ui.C_MUTED, true, Paint.Align.CENTER);
        Ui.text(c, String.valueOf(g.getScore()), tmp.centerX(), tmp.top + Ui.dp(46f), 20f, Ui.C_TEXT, true, Paint.Align.CENTER);
        Ui.text(c, "BEST " + g.getBestScore(), tmp.right - Ui.dp(14f), tmp.top + Ui.dp(22f), 10f, Ui.C_MUTED, true, Paint.Align.RIGHT);
        int comboCol = g.getComboCount() >= 2 ? Ui.C_PINK : Ui.C_MUTED;
        Ui.text(c, "COMBO x" + g.getMultiplier(), tmp.right - Ui.dp(14f), tmp.top + Ui.dp(46f), 13f, comboCol, true, Paint.Align.RIGHT);
    }

    private void drawAnnounce(Canvas c, GameManager g, float w, float h) {
        float left = g.getAnnounce();
        float age = GameManager.WAVE_BREAK_TIME - left;
        float in = Math.min(1f, age / 0.45f);
        float alpha = Math.min(1f, Math.min(age, left) / 0.4f);
        float scale = Ui.easeOutBack(in);
        float cy = h * 0.42f;
        c.save();
        c.scale(scale, scale, w / 2f, cy);
        Ui.textGlow(c, "WAVE " + g.getWave(), w / 2f, cy, 44f,
                Ui.argb(alpha, Ui.C_TEXT), Ui.argb(alpha * 0.9f, Ui.C_PURPLE), true, Paint.Align.CENTER);
        Ui.text(c, "Incoming: " + g.getWaveSize() + " mosquitoes", w / 2f, cy + Ui.dp(30f), 16f,
                Ui.argb(alpha, Ui.C_MUTED), true, Paint.Align.CENTER);
        c.restore();
    }

    private void drawComboBanner(Canvas c, GameManager g, float w, float h) {
        float pulse = g.getComboPulse();
        float size = 18f + pulse * 8f;
        Ui.textGlow(c, "COMBO x" + g.getMultiplier() + "  (" + g.getComboCount() + " hits)",
                w / 2f, h * 0.30f, size, Ui.C_GOLD, 0xAAFF4FD8, true, Paint.Align.CENTER);
    }
}
