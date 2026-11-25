package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/** The fictional mosquito-hunter turret at the bottom of the screen. */
public class Player {

    private static final Paint BASE = new Paint(Paint.ANTI_ALIAS);
    private static final Paint GLOW = new Paint(Paint.ANTI_ALIAS);
    private static final RectF RECT = new RectF();

    public float x, y, recoil, glow, flash;

    public void setPosition(float px, float py) {
        x = px;
        y = py;
    }

    public void onShot() {
        recoil = 1f;
        glow = 1f;
        flash = 1f;
    }

    public void update(float dt) {
        recoil = Math.max(0f, recoil - dt * 6f);
        glow = Math.max(0f, glow - dt * 3f);
        flash = Math.max(0f, flash - dt * 12f);
    }

    public float muzzleX() {
        return x;
    }

    public float muzzleY() {
        return y - Ui.dp(38f) + recoil * Ui.dp(5f);
    }

    public void draw(Canvas c, float time) {
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 3f);

        GLOW.reset();
        GLOW.setAntiAlias(true);
        GLOW.setStyle(Paint.Style.FILL);
        GLOW.setColor(Ui.argb(0.10f + 0.08f * pulse + 0.25f * glow, Ui.C_BLUE));
        c.drawCircle(x, y - Ui.dp(10f), Ui.dp(50f), GLOW);

        BASE.reset();
        BASE.setAntiAlias(true);
        BASE.setStyle(Paint.Style.FILL);
        BASE.setColor(0xFF2A1C5C);
        RECT.set(x - Ui.dp(34f), y - Ui.dp(12f), x + Ui.dp(34f), y + Ui.dp(16f));
        c.drawRoundRect(RECT, Ui.dp(12f), Ui.dp(12f), BASE);

        BASE.setColor(Ui.C_PINK);
        RECT.set(x - Ui.dp(22f), y - Ui.dp(4f), x + Ui.dp(22f), y - Ui.dp(1f));
        c.drawRoundRect(RECT, Ui.dp(2f), Ui.dp(2f), BASE);

        BASE.setColor(0xFF3B2A7A);
        RECT.set(x - Ui.dp(6f), y - Ui.dp(34f) + recoil * Ui.dp(5f), x + Ui.dp(6f), y - Ui.dp(6f));
        c.drawRoundRect(RECT, Ui.dp(4f), Ui.dp(4f), BASE);

        BASE.setColor(Ui.argb(0.6f + 0.4f * pulse, Ui.C_BLUE));
        c.drawCircle(x, y - Ui.dp(34f) + recoil * Ui.dp(5f), Ui.dp(5f), BASE);

        if (flash > 0.01f) {
            GLOW.setColor(Ui.argb(flash, 0xFFBDF4FF));
            c.drawCircle(muzzleX(), muzzleY(), Ui.dp(12f) * flash + Ui.dp(4f), GLOW);
        }
    }
}
