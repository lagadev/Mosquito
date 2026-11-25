package com.mosquitohunter.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Hosts the render thread, the screen state machine, touch input and the
 * button layer for every screen (menu, how-to, settings, play, pause, game over).
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    public enum Screen { MENU, HOW_TO, SETTINGS, PLAYING, PAUSED, GAME_OVER }

    private final Object lock = new Object();
    private final SettingsManager settings;
    private final ScoreManager scores;
    private final SoundManager sound;
    private final GameManager game;
    private final GameRenderer gameRenderer = new GameRenderer();
    private final MenuRenderer menuRenderer = new MenuRenderer();
    private final GameOverRenderer overRenderer = new GameOverRenderer();
    private final List<UiButton> buttons = new ArrayList<>();
    private final Paint fadePaint = new Paint();

    private GameThread thread;
    private boolean surfaceReady;
    private Screen screen = Screen.MENU;
    private int width;
    private int height;
    private float time;
    private float transition = 1f;
    private float overAnim;
    private float aimX;
    private float aimY;
    private boolean aiming;
    private UiButton pressed;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Ui.density = context.getResources().getDisplayMetrics().density;
        settings = new SettingsManager(context);
        scores = new ScoreManager(context);
        sound = new SoundManager(context, settings);
        game = new GameManager(sound, settings, scores);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    public Object getLock() {
        return lock;
    }

    // ----------------------------------------------------------- screen flow

    private void setScreen(Screen next) {
        synchronized (lock) {
            if (next == Screen.PAUSED || next == Screen.GAME_OVER) sound.stopBuzz();
            if (next == Screen.GAME_OVER) overAnim = 0f;
            screen = next;
            transition = 0f;
            pressed = null;
            aiming = false;
            sound.setMusicWanted(next != Screen.PAUSED);
            layoutButtons();
        }
    }

    private void layoutButtons() {
        buttons.clear();
        if (width == 0 || height == 0) return;
        float bw = Math.min(width * 0.74f, Ui.dp(300f));
        float bh = Ui.dp(54f);
        float gap = Ui.dp(16f);
        switch (screen) {
            case MENU:
                stack(new String[]{"play", "howto", "settings"}, height * 0.56f, bw, bh, gap);
                break;
            case HOW_TO:
                stack(new String[]{"back"}, height * 0.84f + Ui.dp(14f), bw, bh, gap);
                break;
            case SETTINGS:
                stack(new String[]{"sound", "music", "vibration", "particles", "difficulty", "reset", "back"},
                        height * 0.27f, bw, Ui.dp(50f), Ui.dp(12f));
                break;
            case PAUSED:
                stack(new String[]{"resume", "restart", "sound", "menu"}, height * 0.38f, bw, bh, gap);
                break;
            case GAME_OVER:
                stack(new String[]{"retry", "menu"}, height * 0.70f, bw, bh, gap);
                break;
            case PLAYING: {
                add("pause", Ui.dp(12f), Ui.dp(12f), Ui.dp(56f), Ui.dp(56f));
                float gapH = Ui.dp(8f);
                float pw = (width - Ui.dp(20f) - gapH * 3f) / 4f;
                float py = height - Ui.dp(60f);
                String[] ids = {"pw_slow", "pw_multi", "pw_freeze", "pw_boost"};
                for (int i = 0; i < ids.length; i++) {
                    float l = Ui.dp(10f) + i * (pw + gapH);
                    UiButton b = add(ids[i], l, py, l + pw, py + Ui.dp(48f));
                    b.textSize = 12f;
                }
                break;
            }
            default:
                break;
        }
    }

    private void stack(String[] ids, float startY, float bw, float bh, float gap) {
        float left = width / 2f - bw / 2f;
        for (int i = 0; i < ids.length; i++) {
            float top = startY + i * (bh + gap);
            add(ids[i], left, top, left + bw, top + bh);
        }
    }

    private UiButton add(String id, float l, float t, float r, float b) {
        int[] style = styleFor(id);
        UiButton btn = new UiButton(id, style[0], style[1]);
        btn.setBounds(l, t, r, b);
        buttons.add(btn);
        return btn;
    }

    private static int[] styleFor(String id) {
        switch (id) {
            case "play":
            case "resume":
            case "retry":
            case "restart":
                return new int[]{Ui.C_PURPLE, Ui.C_BLUE};
            case "pw_slow":
                return new int[]{0xFF1E7BFF, 0xFF22D3EE};
            case "pw_multi":
                return new int[]{0xFFFF4FD8, 0xFF7B2FF7};
            case "pw_freeze":
                return new int[]{0xFF5CE1E6, 0xFF2E5BFF};
            case "pw_boost":
                return new int[]{0xFFFFD54A, 0xFFFF8A3D};
            case "pause":
                return new int[]{0xAA1B1A45, 0xAA2D1F6B};
            default:
                return new int[]{0xCC1B1A45, 0xCC2A1F5E};
        }
    }

    private String labelFor(String id) {
        switch (id) {
            case "play": return "PLAY";
            case "howto": return "HOW TO PLAY";
            case "settings": return "SETTINGS";
            case "back": return "BACK";
            case "sound": return "SOUND: " + onOff(settings.isSoundOn());
            case "music": return "MUSIC: " + onOff(settings.isMusicOn());
            case "vibration": return "VIBRATION: " + onOff(settings.isVibrationOn());
            case "particles": return "PARTICLES: " + onOff(settings.isParticlesOn());
            case "difficulty": return "DIFFICULTY: " + settings.getDifficultyName();
            case "reset": return "RESET SCORES";
            case "resume": return "RESUME";
            case "restart": return "RESTART";
            case "menu": return "MAIN MENU";
            case "retry": return "RETRY";
            case "pause": return "II";
            case "pw_slow": return powerLabel("SLOW", PowerUp.SLOW);
            case "pw_multi": return powerLabel("MULTI", PowerUp.MULTI);
            case "pw_freeze": return powerLabel("FREEZE", PowerUp.FREEZE);
            case "pw_boost": return powerLabel("BOOST", PowerUp.BOOST);
            default: return id;
        }
    }

    private String powerLabel(String name, PowerUp p) {
        float t = game.getPowerTimer(p);
        if (t > 0f) return name + " " + (int) Math.ceil(t) + "s";
        return name + " x" + game.getCharges(p);
    }

    private boolean isEnabled(String id) {
        switch (id) {
            case "pw_slow": return game.getCharges(PowerUp.SLOW) > 0;
            case "pw_multi": return game.getCharges(PowerUp.MULTI) > 0;
            case "pw_freeze": return game.getCharges(PowerUp.FREEZE) > 0;
            case "pw_boost": return game.getCharges(PowerUp.BOOST) > 0;
            default: return true;
        }
    }

    private static String onOff(boolean on) {
        return on ? "ON" : "OFF";
    }

    private UiButton hitButton(float x, float y) {
        for (UiButton b : buttons) {
            if (b.contains(x, y)) return b;
        }
        return null;
    }

    private void activate(String id) {
        switch (id) {
            case "play":
                game.start();
                setScreen(Screen.PLAYING);
                break;
            case "howto":
                setScreen(Screen.HOW_TO);
                break;
            case "settings":
                setScreen(Screen.SETTINGS);
                break;
            case "back":
            case "menu":
                setScreen(Screen.MENU);
                break;
            case "sound":
                settings.toggleSound();
                if (!settings.isSoundOn()) sound.stopBuzz();
                sound.playClick();
                break;
            case "music":
                settings.toggleMusic();
                sound.applyMusic();
                break;
            case "vibration":
                settings.toggleVibration();
                sound.playClick();
                break;
            case "particles":
                settings.toggleParticles();
                sound.playClick();
                break;
            case "difficulty":
                settings.cycleDifficulty();
                sound.playClick();
                break;
            case "reset":
                scores.reset();
                sound.playClick();
                break;
            case "pause":
                setScreen(Screen.PAUSED);
                break;
            case "resume":
                setScreen(Screen.PLAYING);
                break;
            case "restart":
            case "retry":
                game.start();
                setScreen(Screen.PLAYING);
                break;
            case "pw_slow":
                game.activate(PowerUp.SLOW);
                break;
            case "pw_multi":
                game.activate(PowerUp.MULTI);
                break;
            case "pw_freeze":
                game.activate(PowerUp.FREEZE);
                break;
            case "pw_boost":
                game.activate(PowerUp.BOOST);
                break;
            default:
                break;
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        synchronized (lock) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    pressed = hitButton(x, y);
                    if (pressed != null) {
                        pressed.pressed = true;
                    } else if (screen == Screen.PLAYING) {
                        aiming = true;
                        aimX = x;
                        aimY = y;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (aiming) {
                        aimX = x;
                        aimY = y;
                    }
                    if (pressed != null) pressed.pressed = pressed.contains(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    if (pressed != null) {
                        boolean inside = pressed.contains(x, y);
                        pressed.pressed = false;
                        String id = pressed.id;
                        pressed = null;
                        if (inside) activate(id);
                    } else if (aiming) {
                        aiming = false;
                        game.fire(x, y);
                    }
                    aiming = false;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    if (pressed != null) {
                        pressed.pressed = false;
                        pressed = null;
                    }
                    aiming = false;
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    // ----------------------------------------------------------- update/draw

    void update(float dt) {
        time += dt;
        transition = Math.min(1f, transition + dt * 3f);
        for (UiButton b : buttons) {
            b.label = labelFor(b.id);
            b.enabled = isEnabled(b.id);
            b.update(dt);
        }
        switch (screen) {
            case PLAYING:
                game.update(dt);
                if (game.isOver()) setScreen(Screen.GAME_OVER);
                break;
            case GAME_OVER:
                overAnim = Math.min(1f, overAnim + dt * 1.5f);
                break;
            default:
                break;
        }
    }

    void renderFrame(Canvas c) {
        c.drawColor(Ui.C_NAVY);
        switch (screen) {
            case MENU:
            case HOW_TO:
            case SETTINGS:
                gameRenderer.drawBackground(c, width, height, time);
                menuRenderer.drawScreen(c, screen, width, height, time, settings, scores);
                break;
            case PLAYING:
            case PAUSED:
            case GAME_OVER:
                gameRenderer.draw(c, game, time, aiming, aimX, aimY);
                if (screen == Screen.PAUSED) menuRenderer.drawPauseOverlay(c, width, height);
                if (screen == Screen.GAME_OVER) overRenderer.draw(c, width, height, game, overAnim, time);
                break;
            default:
                break;
        }
        for (UiButton b : buttons) b.draw(c);
        if (transition < 1f) {
            fadePaint.setColor(Ui.argb(1f - transition, Ui.C_NAVY));
            c.drawRect(0, 0, width, height, fadePaint);
        }
    }

    // ------------------------------------------------------------- lifecycle

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
        startThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        synchronized (lock) {
            width = w;
            height = h;
            game.setSize(w, h);
            layoutButtons();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
        stopThread();
    }

    private void startThread() {
        if (thread != null && thread.isAlive()) return;
        thread = new GameThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    private void stopThread() {
        if (thread == null) return;
        thread.setRunning(false);
        try {
            thread.join(1500);
        } catch (InterruptedException ignored) {
        }
        thread = null;
    }

    public void onActivityResume() {
        synchronized (lock) {
            if (screen != Screen.PAUSED) sound.resumeMusic();
        }
        if (surfaceReady) startThread();
    }

    public void onActivityPause() {
        synchronized (lock) {
            if (screen == Screen.PLAYING) setScreen(Screen.PAUSED);
            sound.pauseAll();
        }
        stopThread();
    }

    public void release() {
        stopThread();
        sound.release();
    }

    /** Returns true when the back press was consumed by the game. */
    public boolean handleBack() {
        synchronized (lock) {
            switch (screen) {
                case PLAYING:
                    setScreen(Screen.PAUSED);
                    return true;
                case PAUSED:
                    setScreen(Screen.PLAYING);
                    return true;
                case HOW_TO:
                case SETTINGS:
                case GAME_OVER:
                    setScreen(Screen.MENU);
                    return true;
                default:
                    return false;
            }
        }
    }
}
