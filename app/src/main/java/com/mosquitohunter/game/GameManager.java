package com.mosquitohunter.game;

import android.graphics.RectF;

import java.util.Arrays;
import java.util.Random;

/**
 * Owns all run-time game logic: waves, spawning, shots, hits, combo,
 * power-ups, lives and pooled entities. Rendering reads from here.
 */
public class GameManager {

    public static final float WAVE_BREAK_TIME = 2.2f;
    private static final int MAX_MOSQUITOES = 24;
    private static final int MAX_SHOTS = 48;
    private static final int MAX_PARTICLES = 280;
    private static final int MAX_TEXTS = 36;
    private static final int VOLLEYS = 8;
    private static final int MAX_WAVE_SIZE = 20;

    private final SoundManager sound;
    private final SettingsManager settings;
    private final ScoreManager scores;
    private final Random rnd = new Random();

    public final Player player = new Player();
    private final Mosquito[] mosquitoes = new Mosquito[MAX_MOSQUITOES];
    private final Projectile[] shots = new Projectile[MAX_SHOTS];
    private final Particle[] particles = new Particle[MAX_PARTICLES];
    private final FloatingText[] texts = new FloatingText[MAX_TEXTS];
    private final int[] volleyPending = new int[VOLLEYS];
    private final boolean[] volleyHit = new boolean[VOLLEYS];
    private final int[] charges = new int[PowerUp.values().length];
    private final float[] powerTimers = new float[PowerUp.values().length];
    private final RectF bounds = new RectF();

    private int nextVolley;
    private float width, height, playTop, playBottom, bedX, bedY;
    private float time, announce, spawnTimer, shake, comboTimer, comboPulse;
    private int wave, lives, score, kills, comboCount, bestCombo, killsSinceCharge;
    private int remaining, bestBefore, waveSize;
    private GameState state = GameState.WAVE_BREAK;
    private boolean over, newBest, buzzing;

    public GameManager(SoundManager sound, SettingsManager settings, ScoreManager scores) {
        this.sound = sound;
        this.settings = settings;
        this.scores = scores;
        for (int i = 0; i < MAX_MOSQUITOES; i++) mosquitoes[i] = new Mosquito();
        for (int i = 0; i < MAX_SHOTS; i++) shots[i] = new Projectile();
        for (int i = 0; i < MAX_PARTICLES; i++) particles[i] = new Particle();
        for (int i = 0; i < MAX_TEXTS; i++) texts[i] = new FloatingText();
    }

    public void setSize(float w, float h) {
        width = w;
        height = h;
        playTop = Ui.dp(Ui.TOP_H);
        playBottom = h - Ui.dp(Ui.BOTTOM_H);
        bounds.set(Ui.dp(14), playTop + Ui.dp(12), w - Ui.dp(14), playBottom - Ui.dp(12));
        bedX = w * 0.57f;
        bedY = h * 0.65f;
        player.setPosition(w / 2f, playBottom - Ui.dp(18f));
    }

    public void start() {
        sound.setBuzzing(false);
        bestBefore = scores.getBest();
        for (Mosquito m : mosquitoes) m.active = false;
        for (Projectile p : shots) p.active = false;
        for (Particle p : particles) p.active = false;
        for (FloatingText t : texts) t.active = false;
        Arrays.fill(charges, 0);
        Arrays.fill(powerTimers, 0f);
        Arrays.fill(volleyPending, 0);
        Arrays.fill(volleyHit, false);
        score = 0;
        kills = 0;
        lives = 3;
        wave = 0;
        comboCount = 0;
        comboTimer = 0f;
        comboPulse = 0f;
        bestCombo = 0;
        killsSinceCharge = 0;
        remaining = 0;
        waveSize = 0;
        shake = 0f;
        over = false;
        newBest = false;
        buzzing = false;
        state = GameState.WAVE_BREAK;
        nextWave();
    }

    public void update(float dt) {
        if (over) return;
        time += dt;
        player.update(dt);
        shake *= Math.max(0f, 1f - dt * 10f);
        comboPulse = Math.max(0f, comboPulse - dt);
        for (int i = 0; i < powerTimers.length; i++) {
            powerTimers[i] = Math.max(0f, powerTimers[i] - dt);
        }
        if (comboCount > 0) {
            comboTimer -= dt;
            if (comboTimer <= 0f) comboCount = 0;
        }

        if (state == GameState.WAVE_BREAK) {
            announce -= dt;
            if (announce <= 0f) state = GameState.PLAYING;
        } else {
            spawnUpdate(dt);
        }

        boolean frozen = powerTimers[PowerUp.FREEZE.ordinal()] > 0f;
        boolean slowed = powerTimers[PowerUp.SLOW.ordinal()] > 0f;
        float speedScale = (slowed ? 0.45f : 1f) * settings.speedFactor() * (1f + 0.06f * (wave - 1));

        int active = 0;
        for (Mosquito m : mosquitoes) {
            if (!m.active) continue;
            m.update(dt, speedScale, frozen, bounds, bedX, bedY, rnd);
            if (m.escaped) {
                m.escaped = false;
                onEscape();
                if (over) return;
            } else if (m.active) {
                active++;
            }
        }

        for (Projectile p : shots) {
            if (p.active) updateShot(p, dt);
        }
        for (Particle p : particles) {
            if (p.active) p.update(dt);
        }
        for (FloatingText t : texts) {
            if (t.active) t.update(dt);
        }

        if (state == GameState.PLAYING && remaining <= 0 && active == 0) {
            nextWave();
        }

        boolean wantBuzz = state == GameState.PLAYING && active > 0;
        if (wantBuzz != buzzing) {
            buzzing = wantBuzz;
            sound.setBuzzing(wantBuzz);
        }
    }

    /** Fires one volley (1 pellet, or 3 with MULTI) toward the touch point. */
    public void fire(float tx, float ty) {
        if (over) return;
        boolean multi = powerTimers[PowerUp.MULTI.ordinal()] > 0f;
        int pellets = multi ? 3 : 1;
        float sx = player.muzzleX();
        float sy = player.muzzleY();
        int slot = nextVolley;
        nextVolley = (nextVolley + 1) % VOLLEYS;
        volleyPending[slot] = 0;
        volleyHit[slot] = false;

        float base = (float) Math.atan2(ty - sy, tx - sx);
        float reach = Math.max((float) Math.hypot(tx - sx, ty - sy), Ui.dp(200f)) + Ui.dp(40f);
        for (int i = 0; i < pellets; i++) {
            Projectile p = null;
            for (Projectile candidate : shots) {
                if (!candidate.active) {
                    p = candidate;
                    break;
                }
            }
            if (p == null) break;
            float ang = base + (i - (pellets - 1) * 0.5f) * 0.14f;
            p.fire(sx, sy, (float) Math.cos(ang), (float) Math.sin(ang), reach, Ui.dp(2400f), slot);
            volleyPending[slot]++;
        }
        if (volleyPending[slot] == 0) return;
        sound.playShoot();
        player.onShot();
    }

    public void activate(PowerUp p) {
        int i = p.ordinal();
        if (over || charges[i] <= 0) return;
        charges[i]--;
        powerTimers[i] += p.duration;
        sound.playPower();
        addText(p.label + "!", width / 2f, playTop + Ui.dp(96f), p.color, true);
    }

    // ---------------------------------------------------------------- internals

    private void nextWave() {
        wave++;
        waveSize = Math.min(2 * wave, MAX_WAVE_SIZE);
        remaining = waveSize;
        state = GameState.WAVE_BREAK;
        announce = WAVE_BREAK_TIME;
        spawnTimer = 0.6f;
        sound.playPower();
    }

    private void spawnUpdate(float dt) {
        if (remaining <= 0) return;
        spawnTimer -= dt;
        if (spawnTimer > 0f) return;
        int slot = -1;
        for (int i = 0; i < mosquitoes.length; i++) {
            if (!mosquitoes[i].active) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return;
        spawnTimer = Math.max(0.5f, 1.2f - 0.07f * wave);
        remaining--;

        float sx;
        float sy;
        switch (rnd.nextInt(4)) {
            case 0:
                sx = bounds.left + rnd.nextFloat() * bounds.width();
                sy = bounds.top;
                break;
            case 1:
                sx = bounds.right;
                sy = bounds.top + rnd.nextFloat() * bounds.height();
                break;
            case 2:
                sx = bounds.left + rnd.nextFloat() * bounds.width();
                sy = bounds.bottom;
                break;
            default:
                sx = bounds.left;
                sy = bounds.top + rnd.nextFloat() * bounds.height();
                break;
        }
        mosquitoes[slot].reset(pickType(), sx, sy, settings.lifeFactor(), rnd);
    }

    private Mosquito.Type pickType() {
        float r = rnd.nextFloat();
        if (wave >= 2 && r < 0.18f) return Mosquito.Type.FAST;
        if (wave >= 3 && r < 0.30f) return Mosquito.Type.TINY;
        if (wave >= 4 && r < 0.40f) return Mosquito.Type.GIANT;
        if (wave >= 2 && r < 0.48f) return Mosquito.Type.GOLDEN;
        return Mosquito.Type.NORMAL;
    }

    private void updateShot(Projectile p, float dt) {
        p.prevX = p.x;
        p.prevY = p.y;
        float step = p.speed * dt;
        p.x += p.dx * step;
        p.y += p.dy * step;
        p.traveled += step;

        Mosquito target = null;
        float best = Float.MAX_VALUE;
        for (Mosquito m : mosquitoes) {
            if (!m.active) continue;
            if (CollisionManager.segmentHitsCircle(p.prevX, p.prevY, p.x, p.y, m.x, m.y, m.hitRadius())) {
                float dx = m.x - p.prevX;
                float dy = m.y - p.prevY;
                float d2 = dx * dx + dy * dy;
                if (d2 < best) {
                    best = d2;
                    target = m;
                }
            }
        }
        if (target != null) {
            p.active = false;
            onStruck(target);
            pelletDone(p.volley, true);
            return;
        }
        if (p.traveled >= p.maxDist || p.x < -60f || p.x > width + 60f
                || p.y < -60f || p.y > height + 60f) {
            p.active = false;
            pelletDone(p.volley, false);
        }
    }

    /** Tracks a volley so the combo only breaks when a whole volley misses. */
    private void pelletDone(int slot, boolean hit) {
        if (hit) volleyHit[slot] = true;
        volleyPending[slot]--;
        if (volleyPending[slot] <= 0 && !volleyHit[slot]) {
            comboCount = 0;
        }
    }

    private void onStruck(Mosquito m) {
        m.health--;
        if (m.health > 0) {
            m.hitFlash = 0.18f;
            sound.playHit();
            burst(m.x, m.y, 0xFFFFFFFF, 6);
            return;
        }
        m.active = false;
        kills++;
        comboCount++;
        comboTimer = 2.4f;
        comboPulse = 0.35f;
        bestCombo = Math.max(bestCombo, comboCount);
        int pts = m.type.points * getMultiplier()
                * (powerTimers[PowerUp.BOOST.ordinal()] > 0f ? 2 : 1);
        score += pts;

        killsSinceCharge++;
        if (killsSinceCharge >= 12) {
            killsSinceCharge = 0;
            PowerUp pu = PowerUp.values()[rnd.nextInt(PowerUp.values().length)];
            charges[pu.ordinal()]++;
            addText("+1 " + pu.label, width / 2f, playTop + Ui.dp(120f), pu.color, true);
        }

        addText("+" + pts, m.x, m.y - Ui.dp(12f), Ui.C_GOLD, false);
        sound.playHit();
        if (comboCount >= 3) sound.playCombo();
        shake = Ui.dp(7f);
        burst(m.x, m.y, m.type.color, 22);
    }

    private void onEscape() {
        lives--;
        comboCount = 0;
        shake = Ui.dp(4f);
        sound.playClick();
        sound.vibrate(120);
        addText("-1 LIFE", width / 2f, playTop + Ui.dp(150f), Ui.C_RED, true);
        if (lives <= 0) endGame();
    }

    private void endGame() {
        over = true;
        state = GameState.OVER;
        newBest = score > bestBefore && score > 0;
        scores.submit(score, kills, wave);
        buzzing = false;
        sound.setBuzzing(false);
        sound.playGameOver();
        sound.vibrate(350);
    }

    private void burst(float x, float y, int color, int count) {
        if (!settings.isParticlesOn()) return;
        for (int i = 0; i < count; i++) {
            Particle p = null;
            for (Particle candidate : particles) {
                if (!candidate.active) {
                    p = candidate;
                    break;
                }
            }
            if (p == null) return;
            float a = rnd.nextFloat() * 6.2832f;
            float sp = Ui.dp(90f + rnd.nextFloat() * 260f);
            float life = 0.45f + rnd.nextFloat() * 0.5f;
            int col = (i % 3 == 0) ? Ui.C_GOLD : (i % 3 == 1 ? color : Ui.C_PINK);
            p.spawn(x, y, (float) Math.cos(a) * sp, (float) Math.sin(a) * sp, life,
                    Ui.dp(2f + rnd.nextFloat() * 3f), col);
        }
    }

    private void addText(String s, float x, float y, int color, boolean big) {
        for (FloatingText t : texts) {
            if (!t.active) {
                t.spawn(s, x, y, color, big);
                return;
            }
        }
    }

    // ------------------------------------------------------------- read access

    public boolean isOver() { return over; }
    public boolean isNewBest() { return newBest; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getPlayTop() { return playTop; }
    public float getPlayBottom() { return playBottom; }
    public int getWave() { return wave; }
    public int getWaveSize() { return waveSize; }
    public int getKills() { return kills; }
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public int getComboCount() { return comboCount; }
    public int getBestCombo() { return bestCombo; }
    public int getMultiplier() { return Math.min(5, 1 + comboCount / 3); }
    public float getComboPulse() { return comboPulse; }
    public float getAnnounce() { return announce; }
    public float getShake() { return shake; }
    public float getTime() { return time; }
    public GameState getState() { return state; }
    public int getBestScore() { return Math.max(scores.getBest(), score); }
    public int getCharges(PowerUp p) { return charges[p.ordinal()]; }
    public float getPowerTimer(PowerUp p) { return powerTimers[p.ordinal()]; }
    public Mosquito[] getMosquitoes() { return mosquitoes; }
    public Projectile[] getShots() { return shots; }
    public Particle[] getParticles() { return particles; }
    public FloatingText[] getTexts() { return texts; }
}
