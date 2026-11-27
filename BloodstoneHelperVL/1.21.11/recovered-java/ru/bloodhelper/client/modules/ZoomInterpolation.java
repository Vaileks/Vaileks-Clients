/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.modules;

final class ZoomInterpolation {
    private ZoomInterpolation() {
    }

    static double scroll(double current, double target, double seconds, boolean instant) {
        if (instant || Math.abs(target - current) <= 1.0E-8) {
            return target;
        }
        if (seconds <= 0.0) {
            return current;
        }
        double remaining = Math.pow(0.7, seconds / 0.05);
        double value = target + (current - target) * remaining;
        return Math.abs(target - value) <= 1.0E-8 ? target : value;
    }

    private static double apply(double t, boolean goingIn) {
        if (t <= 0.0) {
            return 0.0;
        }
        if (t >= 1.0) {
            return 1.0;
        }
        return goingIn ? -Math.expm1(-10.0 * Math.log(2.0) * t) * 1.0009775171065494 : Math.expm1(10.0 * Math.log(2.0) * t) / 1023.0;
    }

    private static double inverse(double value, boolean goingIn) {
        if (value <= 0.0) {
            return 0.0;
        }
        if (value >= 1.0) {
            return 1.0;
        }
        return goingIn ? -Math.log1p(-value * 0.9990234375) / (10.0 * Math.log(2.0)) : Math.log1p(value * 1023.0) / (10.0 * Math.log(2.0));
    }

    static final class Transition {
        private double value;
        private double phase;
        private boolean goingIn = true;

        Transition() {
        }

        double value() {
            return this.value;
        }

        void set(double value) {
            this.value = Math.clamp(value, 0.0, 1.0);
            this.phase = ZoomInterpolation.inverse(this.value, this.goingIn);
        }

        void advance(double target, double seconds, double timeIn, double timeOut) {
            double duration;
            boolean nextDirection;
            if ((target = Math.clamp(target, 0.0, 1.0)) == this.value) {
                return;
            }
            boolean bl = nextDirection = target > this.value;
            if (nextDirection != this.goingIn) {
                this.goingIn = nextDirection;
                this.phase = ZoomInterpolation.inverse(this.value, this.goingIn);
            }
            double d = duration = this.goingIn ? timeIn : timeOut;
            if (duration == 0.0) {
                this.set(target);
                return;
            }
            if (seconds <= 0.0) {
                return;
            }
            double targetPhase = ZoomInterpolation.inverse(target, this.goingIn);
            this.phase = this.goingIn ? Math.min(targetPhase, this.phase + seconds / duration) : Math.max(targetPhase, this.phase - seconds / duration);
            this.value = this.phase == targetPhase ? target : ZoomInterpolation.apply(this.phase, this.goingIn);
        }
    }
}

