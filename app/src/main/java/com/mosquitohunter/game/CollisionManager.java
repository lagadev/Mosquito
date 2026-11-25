package com.mosquitohunter.game;

/** Stateless geometry helpers for hit detection. */
public final class CollisionManager {

    private CollisionManager() {
    }

    /** True if the segment (x0,y0)-(x1,y1) passes within radius r of (cx,cy). */
    public static boolean segmentHitsCircle(float x0, float y0, float x1, float y1,
                                            float cx, float cy, float r) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float fx = x0 - cx;
        float fy = y0 - cy;
        float len2 = dx * dx + dy * dy;
        float t = 0f;
        if (len2 > 1e-6f) {
            t = -(fx * dx + fy * dy) / len2;
            t = Math.max(0f, Math.min(1f, t));
        }
        float px = fx + dx * t;
        float py = fy + dy * t;
        return px * px + py * py <= r * r;
    }
}
