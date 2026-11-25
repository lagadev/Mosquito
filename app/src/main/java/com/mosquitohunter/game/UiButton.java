package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

/** Rounded gradient button with a press animation. Actions are dispatched by id in GameView. */
public class UiButton {

    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final Paint TEXT = new Paint(Paint.ANTI_ALIAS_FLAG);

    public final String id;
    public final RectF rect = new RectF();
    public String label = "";
    public boolean pressed;
    public boolean enabled = true;
    public float pressAnim;
    public float textSize = 15f;

    private final int c1;
    private final int c2;
    private Shader shader;

    public UiButton(String id, int c1, int c2) {
        this.id = id;
        this.c1 = c1;
        this.c2 = c2;
    }

    public void setBounds(float l, float t, float r, float b) {
        rect.set(l, t, r, b);
        shader = new LinearGradient(l, t, r, b, c1, c2, Shader.TileMode.CLAMP);
    }

    public boolean contains(float x, float y) {
        return rect.contains(x, y);
    }

    public void update(float dt) {
        float target = pressed ? 1f : 0f;
        pressAnim += (target - pressAnim) * Math.min(1f, dt * 18f);
    }

    public void draw(Canvas c) {
        float s = 1f - 0.07f * pressAnim;
        float rad = rect.height() / 2f;
        c.save();
        c.scale(s, s, rect.centerX(), rect.centerY());

        FILL.setStyle(Paint.Style.FILL);
        FILL.setShader(shader);
        FILL.setAlpha(enabled ? 255 : 90);
        c.drawRoundRect(rect, rad, rad, FILL);

        STROKE.setStyle(Paint.Style.STROKE);
        STROKE.setStrokeWidth(Ui.dp(1.5f));
        STROKE.setColor(enabled ? 0x66FFFFFF : 0x22FFFFFF);
        c.drawRoundRect(rect, rad, rad, STROKE);

        TEXT.reset();
        TEXT.setAntiAlias(true);
        TEXT.setTypeface(Ui.BOLD);
        TEXT.setTextSize(Ui.dp(textSize));
        TEXT.setTextAlign(Paint.Align.CENTER);
        TEXT.setColor(enabled ? Ui.C_TEXT : 0x66EAF0FF);
        float ty = rect.centerY() - (TEXT.descent() + TEXT.ascent()) / 2f;
        c.drawText(label, rect.centerX(), ty, TEXT);
        c.restore();
    }
}
