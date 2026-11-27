/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public final class HudResize {
    private HudResize() {
    }

    public static Result resize(double x, double y, double width, double height, double scale, int edges, double dx, double dy, int screenWidth, int screenHeight) {
        double availableW;
        double dh;
        boolean left = (edges & 1) != 0;
        boolean right = (edges & 2) != 0;
        boolean top = (edges & 4) != 0;
        boolean bottom = (edges & 8) != 0;
        boolean horizontal = left || right;
        boolean vertical = top || bottom;
        double dw = left ? -dx : dx;
        double d = dh = top ? -dy : dy;
        double ratio = horizontal && vertical ? 1.0 + (dw * width + dh * height) / (width * width + height * height) : (horizontal ? 1.0 + dw / Math.max(1.0, width) : 1.0 + dh / Math.max(1.0, height));
        double target = Math.clamp(scale * ratio, 0.35, 2.0);
        ratio = target / scale;
        double d2 = left ? x + width : (availableW = right ? (double)screenWidth - x : 2.0 * Math.min(x + width / 2.0, (double)screenWidth - x - width / 2.0));
        double availableH = top ? y + height : (bottom ? (double)screenHeight - y : 2.0 * Math.min(y + height / 2.0, (double)screenHeight - y - height / 2.0));
        double fit = Math.min(availableW / Math.max(1.0, width), availableH / Math.max(1.0, height));
        ratio = Math.max(0.35 / scale, Math.min(ratio, fit));
        double w = width * ratio;
        double h = height * ratio;
        return new Result(left ? x + width - w : (right ? x : x + (width - w) / 2.0), top ? y + height - h : (bottom ? y : y + (height - h) / 2.0), w, h, Math.clamp(scale * ratio, 0.35, 2.0));
    }

    public record Result(double x, double y, double width, double height, double scale) {
    }
}

