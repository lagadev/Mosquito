package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.Random;

/** A pooled mosquito with wandering, wobbling flight and procedural wing/leg animation. */
public class Mosquito {

    public enum Type {
        NORMAL(18f, 95f, 10, 9f, 1, 0xFF3B2F4A),
        FAST(14f, 170f, 20, 7f, 1, 0xFF2EC5FF),
        GOLDEN(18f, 110f, 50, 6f, 1, 0xFFFFC93C),
        GIANT(30f, 60f, 30, 11f, 2, 0xFF8E3B6E),
        TINY(10f, 135f, 25, 8f, 1, 0xFF9BE37A);

        public final float radius;
        public final float speed;
        public final float life;
        public final int points;
        public final int health;
        public final int color;

        Type(float radius, float speed, int points, float life, int health, int color) {
            this.radius = radius;
            this.speed = speed;
            this.points = points;
            this.life = life;
            this.health = health;
            this.color = color;
        }
    }

    private static final Paint BODY = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint WING = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint DARK = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint GLOW = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF OVAL = new RectF();

    public Type type = Type.NORMAL;
    public boolean active;
    public boolean escaped;
    public float x, y, vx, vy, angle;
    public float wingPhase, wobble, phase, retarget, hover, lifetime, maxLife, hitFlash, tx, ty;
    public int health;

    public void reset(Type t, float sx, float sy, float lifeScale, Random rnd) {
        type = t;
        x = sx;
        y = sy;
        tx = sx;
        ty = sy;
        vx = 0f;
        vy = 0f;
        health = t.health;
        active = true;
        escaped = false;
        phase = rnd.nextFloat() * 6.28f;
        wingPhase = rnd.nextFloat() * 6.28f;
        wobble = rnd.nextFloat() * 6.28f;
        maxLife = t.life * lifeScale;
        lifetime = maxLife;
        retarget = 0f;
        hover = 0f;
        hitFlash = 0f;
    }

    /**
     * Advances flight. Escape is signalled by active=false and escaped=true.
     */
    public void update(float dt, float speedScale, boolean frozen, RectF b,
                       float bedX, float bedY, Random rnd) {
        if (!active) return;
        wingPhase += dt * (type == Type.TINY ? 58f : (type == Type.GIANT ? 30f : 44f));
        hitFlash = Math.max(0f, hitFlash - dt);
        if (frozen) return;

        lifetime -= dt;
        if (lifetime <= 0f) {
            active = false;
            escaped = true;
            return;
        }

        wobble += dt;
        retarget -= dt;
        hover -= dt;
        float dist = (float) Math.hypot(tx - x, ty - y);
        if (retarget <= 0f || dist < Ui.dp(16f)) {
            retarget = 0.5f + rnd.nextFloat() * 1.3f;
            if (rnd.nextFloat() < 0.35f) {
                tx = bedX + (rnd.nextFloat() - 0.5f) * b.width() * 0.55f;
                ty = bedY + (rnd.nextFloat() - 0.5f) * b.height() * 0.45f;
            } else {
                tx = b.left + rnd.nextFloat() * b.width();
                ty = b.top + rnd.nextFloat() * b.height();
            }
            tx = Ui.clamp(tx, b.left, b.right);
            ty = Ui.clamp(ty, b.top, b.bottom);
            if (rnd.nextFloat() < 0.25f) hover = 0.35f + rnd.nextFloat() * 0.4f;
        }

        float dx = tx - x;
        float dy = ty - y;
        float d = (float) Math.sqrt(dx * dx + dy * dy) + 0.001f;
        float maxSp = Ui.dp(type.speed) * speedScale * (hover > 0f ? 0.3f : 1f);
        float reach = Math.min(maxSp, d * 2.5f);
        float wantVx = dx / d * reach;
        float wantVy = dy / d * reach;
        float k = Math.min(1f, dt * 3f);
        vx += (wantVx - vx) * k;
        vy += (wantVy - vy) * k;

        float sp = (float) Math.sqrt(vx * vx + vy * vy) + 0.001f;
        float px = -vy / sp;
        float py = vx / sp;
        float amp = Ui.dp(38f) * speedScale * (float) Math.sin(wobble * 2.1f + phase);
        x += (vx + px * amp) * dt;
        y += (vy + py * amp) * dt;
        x = Ui.clamp(x, b.left, b.right);
        y = Ui.clamp(y, b.top, b.bottom);
        angle = (float) Math.toDegrees(Math.atan2(vy, vx));
    }

    public float hitRadius() {
        return Ui.dp(type.radius + 7f);
    }

    public void draw(Canvas c) {
        drawShape(c, x, y, angle, Ui.dp(type.radius), wingPhase, wobble, type, hitFlash > 0f);
    }

    /** Draws a mosquito facing +x at the given position. Shared with menu animations. */
    public static void drawShape(Canvas c, float x, float y, float angleDeg, float r,
                                 float wing, float legT, Type type, boolean flashed) {
        c.save();
        c.translate(x, y);
        c.rotate(angleDeg);
        float flap = (float) Math.sin(wing);
        float wingOpen = 0.55f + 0.45f * Math.abs(flap);

        if (type == Type.GOLDEN) {
            GLOW.reset();
            GLOW.setAntiAlias(true);
            GLOW.setStyle(Paint.Style.FILL);
            GLOW.setColor(0x44FFD54A);
            c.drawCircle(0f, 0f, r * 1.9f, GLOW);
        }

        WING.reset();
        WING.setAntiAlias(true);
        WING.setStyle(Paint.Style.FILL);
        WING.setColor(0x9AE8F6FF);
        for (int side = -1; side <= 1; side += 2) {
            c.save();
            c.translate(-r * 0.45f, side * r * 0.35f);
            c.rotate(side * (30f - flap * 14f));
            OVAL.set(-r * 1.05f, -r * 0.30f * wingOpen, r * 0.55f, r * 0.30f * wingOpen);
            c.drawOval(OVAL, WING);
            c.restore();
        }

        DARK.reset();
        DARK.setAntiAlias(true);
        DARK.setStyle(Paint.Style.STROKE);
        DARK.setStrokeWidth(Math.max(1.5f, r * 0.08f));
        DARK.setColor(0xFF1A1226);
        for (int i = 0; i < 3; i++) {
            float lx = r * (0.25f - i * 0.28f);
            float kick = (float) Math.sin(legT * 3f + i) * r * 0.12f;
            c.drawLine(lx, -r * 0.12f, lx - r * 0.25f + kick, -r * 0.95f, DARK);
            c.drawLine(lx, r * 0.12f, lx - r * 0.25f - kick, r * 0.95f, DARK);
        }

        BODY.reset();
        BODY.setAntiAlias(true);
        BODY.setStyle(Paint.Style.FILL);
        BODY.setColor(flashed ? 0xFFFFFFFF : type.color);
        OVAL.set(-r * 1.15f, -r * 0.24f, -r * 0.05f, r * 0.24f);
        c.drawOval(OVAL, BODY);
        c.drawCircle(0f, 0f, r * 0.36f, BODY);

        if (type == Type.FAST || type == Type.TINY) {
            DARK.setStyle(Paint.Style.STROKE);
            DARK.setStrokeWidth(Math.max(1f, r * 0.09f));
            DARK.setColor(0xCCEAF0FF);
            for (int i = 0; i < 3; i++) {
                float sx = -r * (0.35f + i * 0.28f);
                c.drawLine(sx, -r * 0.22f, sx + r * 0.05f, r * 0.22f, DARK);
            }
        }

        DARK.setStyle(Paint.Style.FILL);
        DARK.setColor(0xFF1A1226);
        c.drawCircle(r * 0.62f, 0f, r * 0.26f, DARK);

        GLOW.reset();
        GLOW.setAntiAlias(true);
        GLOW.setStyle(Paint.Style.FILL);
        GLOW.setColor(0xFFFFFFFF);
        c.drawCircle(r * 0.68f, -r * 0.13f, r * 0.07f, GLOW);

        DARK.setStyle(Paint.Style.STROKE);
        DARK.setStrokeWidth(Math.max(1f, r * 0.07f));
        DARK.setColor(0xFF1A1226);
        c.drawLine(r * 0.85f, 0f, r * 1.45f, 0f, DARK);
        c.restore();
    }
}
