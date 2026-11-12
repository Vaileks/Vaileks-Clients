/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.utilites.animation;

import java.util.HashMap;
import java.util.Map;

public final class Animator {
    private final Map<String, Float> values = new HashMap<String, Float>();
    private long lastFrameNanos = -1L;
    private float dtSec = 0.016666668f;

    public void beginFrame(long frameNanos) {
        if (this.lastFrameNanos > 0L) {
            this.dtSec = (float)(frameNanos - this.lastFrameNanos) / 1.0E9f;
            this.dtSec = this.clamp(this.dtSec, 0.004166667f, 0.08f);
        }
        this.lastFrameNanos = frameNanos;
    }

    public float animate(String key, boolean target, float speed) {
        return this.animate(key, target ? 1.0f : 0.0f, speed);
    }

    public float appear(String key, boolean visible, float speed) {
        float target;
        float f = target = visible ? 1.0f : 0.0f;
        if (!this.values.containsKey(key) && visible) {
            this.values.put(key, Float.valueOf(0.0f));
        }
        return this.animate(key, target, speed);
    }

    public float animate(String key, float target, float speed) {
        float current = this.values.getOrDefault(key, Float.valueOf(target)).floatValue();
        float alpha = 1.0f - (float)Math.exp(-Math.max(0.01f, speed) * this.dtSec);
        float next = current + (target - current) * alpha;
        this.values.put(key, Float.valueOf(next));
        return next;
    }

    public void set(String key, float value) {
        this.values.put(key, Float.valueOf(value));
    }

    public void reset(String key) {
        this.values.remove(key);
    }

    public void resetAll() {
        this.values.clear();
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

