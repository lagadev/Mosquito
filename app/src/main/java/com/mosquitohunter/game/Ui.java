package com.mosquitohunter.game;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

/** Shared palette, typography and drawing helpers. */
public final class Ui {

    public static float density = 2.5f;
    public static final float TOP_H = 66f;
    public static final float BOTTOM_H = 150f;

    public static final int C_NAVY = 0xFF070A1F;
    public static final int C_PURPLE = 0xFF7B2FF7;
    public static final int C_BLUE = 0xFF2EC5FF;
    public static final int C_PINK = 0xFFFF4FD8;
    public static final int C_GOLD = 0xFFFFD54A;
    public static final int C_TEXT = 0xFFEAF0FF;
    public static final int C_MUTED = 0xFF9AA6CF;
    public static final int C_GREEN = 0xFF5CFF9D;
    public static final int C_RED = 0xFFFF5C7A;

    public static final Typeface BOLD = Typeface.create("sans-serif-black", Typeface.NORMAL);
    public static final Typeface REG = Typeface.create("sans-serif", Typeface.NORMAL);

    private static final Paint FILL = new Paint(Paint.ANTI_ALIAS);
    private static final Paint LINE = new Paint(Paint.ANTI_ALIAS);
    private static final Paint TEXT = new Paint(Paint.ANTI_ALIAS);

    private Ui() {
    }

    public static float dp(float v) {
        return v * density;
    }

    public static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    /** Combines an alpha factor (0..1) with a 0xRRGGBB color into an ARGB int. */
    public static int argb(float alpha, int rgb) {
        int a = (int) (clamp(alpha, 0f, 1f) * 255f);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float u = t - 1f;
        return 1f + c3 * u * u * u + c1 * u * u;
    }

    public static void glass(Canvas c, RectF r, float radius) {
        FILL.setStyle(Paint.Style.FILL);
        FILL.setShader(null);
        FILL.setColor(0x7A140E36);
        c.drawRoundRect(r, radius, radius, FILL);
        LINE.setStyle(Paint.Style.STROKE);
        LINE.setStrokeWidth(dp(1.2f));
        LINE.setShader(null);
        LINE.setColor(0x66B9A6FF);
        c.drawRoundRect(r, radius, radius, LINE);
    }

    public static void text(Canvas c, String s, float x, float y, float sizeDp, int color,
                            boolean bold, Paint.Align align) {
        setupText(sizeDp, color, bold, align);
        c.drawText(s, x, y, TEXT);
    }

    public static void textGlow(Canvas c, String s, float x, float y, float sizeDp, int color,
                                int glowColor, boolean bold, Paint.Align align) {
        setupText(sizeDp, color, bold, align);
        TEXT.setShadowLayer(dp(14f), 0f, 0f, glowColor);
        c.drawText(s, x, y, TEXT);
        TEXT.clearShadowLayer();
    }

    public static float measure(String s, float sizeDp, boolean bold) {
        setupText(sizeDp, 0xFFFFFFFF, bold, Paint.Align.LEFT);
        return TEXT.measureText(s);
    }

    private static void setupText(float sizeDp, int color, boolean bold, Paint.Align align) {
        TEXT.reset();
        TEXT.setAntiAlias(true);
        TEXT.setTextSize(dp(sizeDp));
        TEXT.setColor(color);
        TEXT.setTypeface(bold ? BOLD : REG);
        TEXT.setTextAlign(align);
    }
}
