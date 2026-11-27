/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HudMotion {
    private final Map<String, Value> values = new LinkedHashMap<String, Value>();

    public double value(String key, double target, long now, boolean enabled, double milliseconds) {
        if (this.values.size() >= 256 && !this.values.containsKey(key)) {
            this.values.remove(this.values.keySet().iterator().next());
        }
        return this.values.computeIfAbsent(key, ignored -> new Value()).to(target, now, enabled, milliseconds);
    }

    public void reset() {
        this.values.clear();
    }

    public static final class Value {
        private double current;
        private long previous;
        private boolean initialized;

        public double to(double target, long now, boolean enabled, double milliseconds) {
            if (!Double.isFinite(target)) {
                target = 0.0;
            }
            long elapsed = now - this.previous;
            if (!this.initialized || !enabled || milliseconds <= 0.0 || elapsed < 0L || elapsed > 500000000L) {
                this.current = target;
                this.initialized = true;
            } else if (elapsed > 0L) {
                double amount = -Math.expm1((double)(-elapsed) / (milliseconds * 250000.0));
                this.current += (target - this.current) * Math.clamp(amount, 0.0, 1.0);
                if (Math.abs(target - this.current) < 0.001) {
                    this.current = target;
                }
            }
            this.previous = now;
            return this.current;
        }

        public void reset() {
            this.initialized = false;
            this.previous = 0L;
        }
    }
}

