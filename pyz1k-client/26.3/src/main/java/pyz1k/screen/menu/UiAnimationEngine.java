/*
 * Decompiled with CFR 0.152.
 */
package pyz1k.screen.menu;

import java.util.HashMap;
import java.util.Map;

public final class UiAnimationEngine {
    private final Map<String, Float> values = new HashMap<String, Float>();
    private final Map<String, Float> pixelRawValues = new HashMap<String, Float>();
    private final Map<String, Float> textRawValues = new HashMap<String, Float>();
    private final Map<String, Float> textSnappedValues = new HashMap<String, Float>();
    private float fpsFactor = 1.0f;
    private static final float SNAP_EPSILON = 0.0015f;
    private static final float TEXT_HYSTERESIS = 0.62f;

    public void beginFrame(int fps) {
        int safeFps = Math.max(15, Math.min(360, fps));
        this.fpsFactor = 60.0f / (float)safeFps;
    }

    public float animate(String key, float target, float speed, float initial) {
        float current = this.values.getOrDefault(key, Float.valueOf(initial)).floatValue();
        if (Math.abs(target - current) <= 0.0015f) {
            this.values.put(key, Float.valueOf(target));
            return target;
        }
        float factor = this.clamp(speed * 0.01f * this.fpsFactor, 0.0f, 1.0f);
        float next = current + (target - current) * factor;
        if (Math.abs(target - next) <= 0.0015f) {
            next = target;
        }
        this.values.put(key, Float.valueOf(next));
        return next;
    }

    public float animate01(String key, float target, float speed, float initial) {
        return this.clamp(this.animate(key, target, speed, initial), 0.0f, 1.0f);
    }

    public float animateLinear(String key, float target, float unitsPerFrame60, float initial) {
        float current = this.values.getOrDefault(key, Float.valueOf(initial)).floatValue();
        float diff = target - current;
        if (Math.abs(diff) <= 0.0015f) {
            this.values.put(key, Float.valueOf(target));
            return target;
        }
        float maxStep = Math.max(0.0f, unitsPerFrame60 * this.fpsFactor);
        if (maxStep <= 0.0015f) {
            this.values.put(key, Float.valueOf(current));
            return current;
        }
        if (Math.abs(diff) <= maxStep) {
            this.values.put(key, Float.valueOf(target));
            return target;
        }
        float next = current + Math.copySign(maxStep, diff);
        this.values.put(key, Float.valueOf(next));
        return next;
    }

    public float animatePixel(String key, float target, float speed, float initial) {
        float currentRaw = this.pixelRawValues.getOrDefault(key, Float.valueOf(initial)).floatValue();
        if (Math.abs(target - currentRaw) <= 0.0015f) {
            float snapped = Math.round(target);
            this.pixelRawValues.put(key, Float.valueOf(target));
            this.values.put(key, Float.valueOf(snapped));
            return snapped;
        }
        float factor = this.clamp(speed * 0.01f * this.fpsFactor, 0.0f, 1.0f);
        float nextRaw = currentRaw + (target - currentRaw) * factor;
        this.pixelRawValues.put(key, Float.valueOf(nextRaw));
        float nextPixel = Math.round(nextRaw);
        this.values.put(key, Float.valueOf(nextPixel));
        return nextPixel;
    }

    public float animateText(String key, float target, float speed, float initial) {
        float raw = this.textRawValues.getOrDefault(key, Float.valueOf(initial)).floatValue();
        float snapped = this.textSnappedValues.getOrDefault(key, Float.valueOf(Math.round(initial))).floatValue();
        if (Math.abs(target - raw) <= 0.0015f) {
            float targetPx = Math.round(target);
            this.textRawValues.put(key, Float.valueOf(target));
            this.textSnappedValues.put(key, Float.valueOf(targetPx));
            this.values.put(key, Float.valueOf(targetPx));
            return targetPx;
        }
        float factor = this.clamp(speed * 0.01f * this.fpsFactor, 0.0f, 1.0f);
        float nextRaw = raw + (target - raw) * factor;
        this.textRawValues.put(key, Float.valueOf(nextRaw));
        while (nextRaw >= snapped + 0.62f) {
            snapped += 1.0f;
        }
        while (nextRaw <= snapped - 0.62f) {
            snapped -= 1.0f;
        }
        this.textSnappedValues.put(key, Float.valueOf(snapped));
        this.values.put(key, Float.valueOf(snapped));
        return snapped;
    }

    public float stabilizeText(String key, float rawValue) {
        float snapped = this.textSnappedValues.getOrDefault(key, Float.valueOf(Math.round(rawValue))).floatValue();
        while (rawValue >= snapped + 0.62f) {
            snapped += 1.0f;
        }
        while (rawValue <= snapped - 0.62f) {
            snapped -= 1.0f;
        }
        this.textRawValues.put(key, Float.valueOf(rawValue));
        this.textSnappedValues.put(key, Float.valueOf(snapped));
        this.values.put(key, Float.valueOf(snapped));
        return snapped;
    }

    public void snap(String key, float value) {
        this.values.put(key, Float.valueOf(value));
        this.pixelRawValues.put(key, Float.valueOf(value));
        this.textRawValues.put(key, Float.valueOf(value));
        this.textSnappedValues.put(key, Float.valueOf(Math.round(value)));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

