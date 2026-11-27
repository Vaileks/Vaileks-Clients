/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.utilites.animation;

public final class AnimatedValue {
    private float value;
    private boolean initialized;
    private long lastUpdateNanos = -1L;

    public float update(float target, float smoothTime, long nowNanos) {
        if (!this.initialized) {
            this.value = target;
            this.initialized = true;
            this.lastUpdateNanos = nowNanos;
            return this.value;
        }
        if (smoothTime <= 0.0f) {
            this.value = target;
            this.lastUpdateNanos = nowNanos;
            return this.value;
        }
        if (this.lastUpdateNanos < 0L) {
            this.lastUpdateNanos = nowNanos;
        }
        float deltaTime = (float)(nowNanos - this.lastUpdateNanos) / 1.0E9f;
        this.lastUpdateNanos = nowNanos;
        float safeDeltaTime = this.clamp(deltaTime, 0.0f, 0.1f);
        float safeSmoothTime = Math.max(1.0E-4f, smoothTime);
        float factor = 1.0f - (float)Math.exp(-safeDeltaTime / safeSmoothTime);
        this.value += (target - this.value) * factor;
        if (Math.abs(target - this.value) <= 5.0E-4f) {
            this.value = target;
        }
        return this.value;
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        this.value = value;
        this.initialized = true;
        this.lastUpdateNanos = -1L;
    }

    public void reset() {
        this.value = 0.0f;
        this.initialized = false;
        this.lastUpdateNanos = -1L;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

