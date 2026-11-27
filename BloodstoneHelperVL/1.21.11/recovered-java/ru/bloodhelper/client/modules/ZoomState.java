/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

import ru.bloodhelper.client.config.ZoomSettings;
import ru.bloodhelper.client.modules.ZoomInterpolation;

final class ZoomState {
    private static final double MAX_MAGNIFICATION = 100.0;
    private ZoomSettings settings = new ZoomSettings();
    private final ZoomInterpolation.Transition initial = new ZoomInterpolation.Transition();
    private double ratio = 1.0;
    private double initialEndpoint = 1.0;
    private double initialTarget;
    private double scrollInterpolation;
    private double scrollTarget;
    private double wheelRemainder;
    private int scrollTiers;
    private boolean target;
    private boolean activeTarget;
    private boolean toggled;
    private boolean toggleMode;
    private boolean blockedUntilRelease;
    private long lastNanos = Long.MIN_VALUE;

    ZoomState() {
    }

    void configure(ZoomSettings settings) {
        this.settings = settings;
    }

    void input(boolean available, boolean keyDown, int presses, boolean newToggleMode) {
        boolean nextTarget;
        if (!available) {
            this.cancel();
            this.blockedUntilRelease = keyDown;
            this.toggleMode = newToggleMode;
            return;
        }
        if (this.toggleMode != newToggleMode) {
            this.toggled = false;
            this.blockedUntilRelease = keyDown && presses == 0;
            this.toggleMode = newToggleMode;
        }
        if (this.blockedUntilRelease) {
            if (!keyDown || presses > 0) {
                this.blockedUntilRelease = false;
            } else {
                this.target = false;
                return;
            }
        }
        if (this.toggleMode && (presses & 1) != 0) {
            this.toggled = !this.toggled;
        }
        boolean bl = nextTarget = this.toggleMode ? this.toggled : keyDown;
        if (!nextTarget && this.target) {
            this.wheelRemainder = 0.0;
            if (!this.settings.rememberZoom) {
                this.scrollTiers = 0;
            }
        }
        this.target = nextTarget;
    }

    void suspend(boolean keyDown, boolean newToggleMode) {
        if (this.target && !this.settings.rememberZoom) {
            this.scrollTiers = 0;
        }
        this.toggled = false;
        this.target = false;
        this.wheelRemainder = 0.0;
        this.blockedUntilRelease = keyDown;
        this.toggleMode = newToggleMode;
    }

    void advance(long nowNanos) {
        double seconds;
        double d = seconds = this.lastNanos == Long.MIN_VALUE || nowNanos <= this.lastNanos ? 0.0 : (double)(nowNanos - this.lastNanos) / 1.0E9;
        if (this.lastNanos == Long.MIN_VALUE || nowNanos > this.lastNanos) {
            this.lastNanos = nowNanos;
        }
        this.advanceCurrent(seconds);
        if (this.activeTarget != this.target) {
            double baseRatio = 1.0 / this.initialMagnification();
            this.initialEndpoint = Math.min(baseRatio, this.ratio);
            this.initial.set((1.0 - this.ratio) / (1.0 - this.initialEndpoint));
            this.initialTarget = this.target ? (1.0 - baseRatio) / (1.0 - this.initialEndpoint) : 0.0;
            this.scrollInterpolation = 0.0;
            this.activeTarget = this.target;
        }
        this.scrollTarget = this.target ? Math.log(this.requestedMagnification() / this.initialMagnification()) / Math.log(this.scrollStep()) : 0.0;
        this.advanceCurrent(0.0);
    }

    private void advanceCurrent(double seconds) {
        double timeIn = ZoomState.clamp(this.settings.zoomInSeconds, 0.0, 1.0, 1.0);
        double timeOut = ZoomState.clamp(this.settings.zoomOutSeconds, 0.0, 1.0, 0.5);
        this.initial.advance(this.initialTarget, seconds, timeIn, timeOut);
        this.scrollInterpolation = ZoomInterpolation.scroll(this.scrollInterpolation, this.scrollTarget, seconds, timeIn == 0.0);
        double progress = this.initial.value();
        double baseRatio = progress == 0.0 ? 1.0 : (progress == 1.0 ? this.initialEndpoint : 1.0 + (this.initialEndpoint - 1.0) * progress);
        this.ratio = Math.clamp(baseRatio / Math.pow(this.scrollStep(), this.scrollInterpolation), 0.01, 1.0);
    }

    boolean scroll(double delta) {
        if (!(this.target && this.settings.scrollEnabled && Double.isFinite(delta) && delta != 0.0)) {
            return false;
        }
        this.wheelRemainder += Math.clamp(delta, -32.0, 32.0);
        int steps = (int)this.wheelRemainder;
        if (steps != 0) {
            this.wheelRemainder -= (double)steps;
            double base = this.initialMagnification();
            double step = this.scrollStep();
            int minimum = -((int)Math.ceil(Math.log(base) / Math.log(step)));
            int maximum = (int)Math.ceil(Math.log(100.0 / base) / Math.log(step));
            this.scrollTiers = Math.clamp((long)(this.scrollTiers + steps), minimum, maximum);
        }
        return true;
    }

    double requestedMagnification() {
        return Math.clamp(this.initialMagnification() * Math.pow(this.scrollStep(), this.scrollTiers), 1.0, 100.0);
    }

    private double initialMagnification() {
        return ZoomState.clamp(this.settings.magnification, 2.0, 100.0, 2.5);
    }

    private double scrollStep() {
        return ZoomState.clamp(this.settings.scrollStep, 1.05, 2.0, 1.5);
    }

    float fov(float original) {
        return this.ratio == 1.0 ? original : (float)((double)original * this.ratio);
    }

    double sensitivity() {
        if (this.ratio == 1.0) {
            return 1.0;
        }
        if (this.settings.relativeSensitivity) {
            return this.ratio;
        }
        double engaged = Math.clamp((1.0 - this.ratio) / (1.0 - 1.0 / this.initialMagnification()), 0.0, 1.0);
        return 1.0 + (ZoomState.clamp(this.settings.sensitivity, 0.05, 1.0, 0.35) - 1.0) * engaged;
    }

    boolean zooming() {
        return this.target;
    }

    double divisor() {
        return 1.0 / this.ratio;
    }

    double amount() {
        return 1.0 - this.ratio;
    }

    private void cancel() {
        this.initialEndpoint = 1.0;
        this.ratio = 1.0;
        this.initial.set(0.0);
        this.wheelRemainder = 0.0;
        this.scrollTarget = 0.0;
        this.scrollInterpolation = 0.0;
        this.initialTarget = 0.0;
        this.toggled = false;
        this.activeTarget = false;
        this.target = false;
        if (!this.settings.rememberZoom) {
            this.scrollTiers = 0;
        }
        this.lastNanos = Long.MIN_VALUE;
    }

    void reset() {
        this.cancel();
        this.scrollTiers = 0;
        this.blockedUntilRelease = false;
    }

    private static double clamp(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }
}

