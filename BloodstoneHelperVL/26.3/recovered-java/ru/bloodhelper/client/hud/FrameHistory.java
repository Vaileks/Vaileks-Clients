/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

final class FrameHistory {
    private final double[] values;
    private int cursor;
    private int size;
    private double sum;
    private long previous = -1L;

    FrameHistory(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity");
        }
        this.values = new double[capacity];
    }

    void frame(long now, boolean active) {
        if (!active) {
            this.reset();
            return;
        }
        if (this.previous >= 0L) {
            long elapsed = now - this.previous;
            if (elapsed <= 0L || elapsed > 1000000000L) {
                this.reset();
            } else {
                if (this.size == this.values.length) {
                    this.sum -= this.values[this.cursor];
                }
                this.values[this.cursor] = (double)elapsed / 1000000.0;
                this.sum += this.values[this.cursor];
                this.cursor = (this.cursor + 1) % this.values.length;
                this.size = Math.min(this.size + 1, this.values.length);
            }
        }
        this.previous = now;
    }

    void reset() {
        this.size = 0;
        this.cursor = 0;
        this.sum = 0.0;
        this.previous = -1L;
    }

    double[] samples() {
        double[] result = new double[this.size];
        for (int i = 0; i < this.size; ++i) {
            result[i] = this.values[(this.cursor - this.size + i + this.values.length) % this.values.length];
        }
        return result;
    }

    double mean() {
        return this.size == 0 ? Double.NaN : this.sum / (double)this.size;
    }
}

