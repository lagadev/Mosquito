package com.mosquitohunter.game;

/** A single laser pellet. Pooled inside GameManager. */
public class Projectile {
    public boolean active;
    public float x, y, prevX, prevY, dx, dy, speed, traveled, maxDist;
    public int volley;

    public void fire(float sx, float sy, float ux, float uy, float maxDistance, float spd, int slot) {
        x = sx;
        y = sy;
        prevX = sx;
        prevY = sy;
        dx = ux;
        dy = uy;
        traveled = 0f;
        maxDist = maxDistance;
        speed = spd;
        volley = slot;
        active = true;
    }
}
