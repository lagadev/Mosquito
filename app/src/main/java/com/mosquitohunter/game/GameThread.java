package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/** Fixed-target 60 FPS render loop. Update and draw run under the view's lock. */
public class GameThread extends Thread {

    private static final long FRAME_NS = 1_000_000_000L / 60L;

    private final SurfaceHolder holder;
    private final GameView view;
    private volatile boolean running;

    public GameThread(SurfaceHolder holder, GameView view) {
        this.holder = holder;
        this.view = view;
    }

    public void setRunning(boolean value) {
        running = value;
    }

    @Override
    public void run() {
        long last = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = Math.min((now - last) / 1_000_000_000f, 0.05f);
            last = now;

            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    synchronized (view.getLock()) {
                        view.update(dt);
                        view.renderFrame(canvas);
                    }
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }

            long sleepMs = (FRAME_NS - (System.nanoTime() - now)) / 1_000_000L;
            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
