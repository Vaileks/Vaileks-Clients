/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.gui;

public final class MusicWaveShape {
    private MusicWaveShape() {
    }

    public static int phase(double radians) {
        if (!Double.isFinite(radians)) {
            return 0;
        }
        double turn = radians / (Math.PI * 2);
        return (int)Math.floor((turn - Math.floor(turn)) * 1024.0);
    }

    public static int x(int width, double phase) {
        return width + 4 + MusicWaveShape.phase(phase) * 256;
    }

    public static int y(int height, double phase, double energy) {
        int strength = (int)Math.round(Math.clamp(Double.isFinite(energy) ? energy : 0.0, 0.0, 1.0) * 63.0);
        return height + 4 + MusicWaveShape.phase(phase * 0.62) * 256 + strength * 262144;
    }
}

