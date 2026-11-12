/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.Arrays;
import java.util.Set;
import ru.bloodhelper.client.modules.CrosshairPattern;

public final class CrosshairSettings {
    public boolean enabled = false;
    public String mode = "SIMPLE";
    public String preset = "PLUS";
    public String colorMode = "STATIC";
    public int size = 5;
    public int thickness = 1;
    public int gap = 3;
    public int dotSize = 1;
    public int outline = 1;
    public boolean dot = true;
    public boolean showInF5 = true;
    public double scale = 1.0;
    public double opacity = 1.0;
    public int color = -1;
    public int enemyColor = -43691;
    public int friendlyColor = -11141240;
    public int outlineColor = -16250869;
    public int gridSize = 31;
    public int[] pixels = new int[31];

    public void sanitize() {
        if (!Set.of("SIMPLE", "PRESET", "CUSTOM").contains(this.mode == null ? "" : this.mode)) {
            this.mode = "SIMPLE";
        }
        if (this.preset == null || !CrosshairPattern.PRESETS.contains(this.preset)) {
            this.preset = "PLUS";
        }
        if (!Set.of("STATIC", "INVERT", "DYNAMIC", "CHROMA").contains(this.colorMode == null ? "" : this.colorMode)) {
            this.colorMode = "STATIC";
        }
        this.size = Math.clamp((long)this.size, 1, 12);
        this.thickness = Math.clamp((long)this.thickness, 1, 6);
        this.gap = Math.clamp((long)this.gap, 0, 10);
        this.dotSize = Math.clamp((long)this.dotSize, 1, 6);
        this.outline = Math.clamp((long)this.outline, 0, 3);
        this.scale = Double.isFinite(this.scale) ? Math.clamp(this.scale, 0.5, 4.0) : 1.0;
        this.opacity = Double.isFinite(this.opacity) ? Math.clamp(this.opacity, 0.05, 1.0) : 1.0;
        this.gridSize = Math.clamp((long)this.gridSize, 3, 31);
        if (this.pixels == null) {
            this.pixels = new int[this.gridSize];
        }
        if (this.pixels.length != this.gridSize) {
            this.pixels = Arrays.copyOf(this.pixels, this.gridSize);
        }
        int allowed = (int)((1L << this.gridSize) - 1L);
        int i = 0;
        while (i < this.pixels.length) {
            int n = i++;
            this.pixels[n] = this.pixels[n] & allowed;
        }
    }
}

