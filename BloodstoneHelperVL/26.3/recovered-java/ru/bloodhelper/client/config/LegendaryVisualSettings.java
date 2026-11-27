/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class LegendaryVisualSettings {
    public boolean enabled = false;
    public boolean trails = true;
    public boolean loot = true;
    public boolean self = true;
    public boolean others = true;
    public boolean katana = true;
    public boolean mace = true;
    public boolean staff = true;
    public boolean sculk = true;
    public boolean shadow = true;
    public boolean timeBow = true;
    public boolean orbital = true;
    public boolean orbitalPreview = true;
    public double orbitalRadius = 8.0;
    public double orbitalSeconds = 3.0;
    public int orbitalColor = -21915;
    public double trailSeconds = 10.0;
    public double shadowSeconds = 15.0;
    public double timeBowSeconds = 20.0;
    public boolean legendary = true;
    public boolean totems = true;
    public boolean enchantedApples = true;
    public String lootStyle = "MARKERS";
    public boolean names = true;
    public boolean beams = true;
    public double distance = 48.0;
    public double labelScale = 1.0;
    public int katanaColor = -36743;
    public int maceColor = -5597441;
    public int staffColor = -8988525;
    public int sculkColor = -11741744;
    public int shadowColor = -4745994;
    public int timeBowColor = -9388033;
    public int legendaryColor = -14739;
    public int totemColor = -8658790;
    public int appleColor = -1531666;

    public void sanitize() {
        this.orbitalRadius = LegendaryVisualSettings.finite(this.orbitalRadius, 8.0, 1.0, 24.0);
        this.orbitalSeconds = LegendaryVisualSettings.finite(this.orbitalSeconds, 3.0, 0.5, 10.0);
        this.orbitalColor |= 0xFF000000;
        this.trailSeconds = LegendaryVisualSettings.finite(this.trailSeconds, 10.0, 2.0, 60.0);
        this.shadowSeconds = LegendaryVisualSettings.finite(this.shadowSeconds, 15.0, 3.0, 60.0);
        this.timeBowSeconds = LegendaryVisualSettings.finite(this.timeBowSeconds, 20.0, 3.0, 60.0);
        this.distance = LegendaryVisualSettings.finite(this.distance, 48.0, 8.0, 128.0);
        this.labelScale = LegendaryVisualSettings.finite(this.labelScale, 1.0, 0.6, 1.5);
        if (!"OUTLINE".equals(this.lootStyle)) {
            this.lootStyle = "MARKERS";
        }
        this.katanaColor |= 0xFF000000;
        this.maceColor |= 0xFF000000;
        this.staffColor |= 0xFF000000;
        this.sculkColor |= 0xFF000000;
        this.shadowColor |= 0xFF000000;
        this.timeBowColor |= 0xFF000000;
        this.legendaryColor |= 0xFF000000;
        this.totemColor |= 0xFF000000;
        this.appleColor |= 0xFF000000;
    }

    private static double finite(double v, double fallback, double min, double max) {
        return Double.isFinite(v) ? Math.clamp(v, min, max) : fallback;
    }
}

