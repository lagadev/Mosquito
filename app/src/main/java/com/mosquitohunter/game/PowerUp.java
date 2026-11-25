package com.mosquitohunter.game;

/** Collectible power-ups. Earned every 12 kills, activated from the bottom bar. */
public enum PowerUp {
    SLOW("SLOW", 5f, 0xFF22D3EE),
    MULTI("MULTI", 8f, 0xFFFF4FD8),
    FREEZE("FREEZE", 3f, 0xFF8FD8FF),
    BOOST("BOOST", 8f, 0xFFFFD54A);

    public final String label;
    public final float duration;
    public final int color;

    PowerUp(String label, float duration, int color) {
        this.label = label;
        this.duration = duration;
        this.color = color;
    }
}
