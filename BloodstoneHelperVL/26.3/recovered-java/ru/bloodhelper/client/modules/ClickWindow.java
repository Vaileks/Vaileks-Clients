/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

public final class ClickWindow {
    private final long[] times = new long[256];
    private int cursor;
    private int size;

    public void press(long now) {
        this.times[this.cursor] = now;
        this.cursor = (this.cursor + 1) % this.times.length;
        this.size = Math.min(this.size + 1, this.times.length);
    }

    public int count(long now) {
        int n = 0;
        for (int i = 0; i < this.size; ++i) {
            if (now < this.times[i] || now - this.times[i] >= 1000000000L) continue;
            ++n;
        }
        return n;
    }

    public void reset() {
        this.cursor = 0;
        this.size = 0;
    }
}

