/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.assistant;

public final class AnswerReveal {
    private long start;
    private int length;
    private boolean animate;

    public void begin(String text, long now, boolean enabled) {
        this.start = now;
        this.length = text.codePointCount(0, text.length());
        this.animate = enabled;
    }

    public int visible(long now) {
        if (!this.animate) {
            return this.length;
        }
        double elapsed = (double)Math.max(0L, now - this.start) / 1.0E9;
        return Math.min(this.length, (int)(elapsed * Math.max(105.0, (double)this.length / 4.0)));
    }

    public boolean active(long now) {
        return this.visible(now) < this.length;
    }

    public void finish() {
        this.animate = false;
    }

    public static String prefix(String text, int points) {
        int count = Math.min(Math.max(0, points), text.codePointCount(0, text.length()));
        return text.substring(0, text.offsetByCodePoints(0, count));
    }
}

