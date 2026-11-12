/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

public final class HudLayout {
    private HudLayout() {
    }

    public static Placement clip(Placement box, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0 || !Double.isFinite(box.scale()) || box.scale() <= 0.0) {
            return null;
        }
        int left = Math.max(box.x(), box.x() + (int)Math.round((double)x * box.scale()));
        int top = Math.max(box.y(), box.y() + (int)Math.round((double)y * box.scale()));
        int right = Math.min(box.x() + box.width(), box.x() + (int)Math.round((double)(x + width) * box.scale()));
        int bottom = Math.min(box.y() + box.height(), box.y() + (int)Math.round((double)(y + height) * box.scale()));
        return right > left && bottom > top ? new Placement(left, top, right - left, bottom - top, box.scale()) : null;
    }

    public static double scale(int screenWidth, int screenHeight, int width, int height, double requested) {
        double safe = Double.isFinite(requested) ? Math.clamp(requested, 0.35, 2.0) : 0.65;
        return Math.min(safe *= Math.min(1.0, Math.min((double)Math.max(1, screenWidth) / 640.0, (double)Math.max(1, screenHeight) / 400.0)), Math.min((double)Math.max(1, screenWidth - 4) / (double)Math.max(1, width), (double)Math.max(1, screenHeight - 4) / (double)Math.max(1, height)));
    }

    public static Placement place(int screenWidth, int screenHeight, int width, int height, double requestedScale, double anchorX, double anchorY, String alignment, boolean bottom) {
        double scale = HudLayout.scale(screenWidth, screenHeight, width, height, requestedScale);
        int scaledWidth = (int)Math.ceil((double)width * scale);
        int scaledHeight = (int)Math.ceil((double)height * scale);
        int x = (int)((double)screenWidth * anchorX) - ("CENTER".equals(alignment) ? scaledWidth / 2 : ("RIGHT".equals(alignment) ? scaledWidth : 0));
        int y = bottom ? (int)((double)screenHeight * (1.0 - anchorY)) - scaledHeight : (int)((double)screenHeight * anchorY);
        x = Math.clamp((long)x, 0, Math.max(0, screenWidth - scaledWidth));
        y = Math.clamp((long)y, 0, Math.max(0, screenHeight - scaledHeight));
        return new Placement(x, y, scaledWidth, scaledHeight, scale);
    }

    public record Placement(int x, int y, int width, int height, double scale) {
    }
}

