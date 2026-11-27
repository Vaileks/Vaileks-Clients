/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

public final class HudItemControlsSettings {
    public Hotbar hotbar = new Hotbar();
    public Totem totem = new Totem();
    public Scoreboard scoreboard = new Scoreboard();
    public Health health = new Health();
    public Placement titles = new Placement(0.5, 0.5);
    public Placement actionbar = new Placement(0.5, 0.85);

    public void sanitize() {
        if (this.hotbar == null) {
            this.hotbar = new Hotbar();
        }
        this.hotbar.sanitize();
        if (this.totem == null) {
            this.totem = new Totem();
        }
        this.totem.sanitize();
        if (this.scoreboard == null) {
            this.scoreboard = new Scoreboard();
        }
        this.scoreboard.sanitize();
        if (this.health == null) {
            this.health = new Health();
        }
        this.health.sanitize();
        if (this.titles == null) {
            this.titles = new Placement(0.5, 0.5);
        }
        this.titles.sanitize();
        if (this.actionbar == null) {
            this.actionbar = new Placement(0.5, 0.85);
        }
        this.actionbar.sanitize();
    }

    public static double bounded(double value, double min, double max, double fallback) {
        return Double.isFinite(value) ? Math.clamp(value, min, max) : fallback;
    }

    public static Box fit(Placement p, int viewportWidth, int viewportHeight, double width, double height) {
        double scale = Math.max(0.001, Math.min(p.scale, Math.min((double)Math.max(1, viewportWidth) / Math.max(1.0, width), (double)Math.max(1, viewportHeight) / Math.max(1.0, height))));
        return new Box(HudItemControlsSettings.placed(p.x, viewportWidth, width * scale), HudItemControlsSettings.placed(p.y, viewportHeight, height * scale), (float)scale);
    }

    public static float placed(double fraction, int viewport, double extent) {
        return (float)(HudItemControlsSettings.bounded(fraction, 0.0, 1.0, 0.5) * Math.max(0.0, (double)viewport - Math.max(0.0, extent)));
    }

    public static final class Hotbar {
        public boolean markersEnabled;
        public boolean keysEnabled;
        public int[] colors = new int[]{0xFF6565, 16754779, 16769385, 9429875, 6479814, 6930175, 10849279, 15700441, 0xFFFFFF};
        public boolean[] marked = new boolean[]{true, true, true, true, true, true, true, true, true};
        public double markerOpacity = 0.8;
        public double keyOpacity = 0.9;
        public double keyScale = 0.65;
        public Edge markerEdge = Edge.BOTTOM;
        public Edge keyEdge = Edge.TOP;
        public int keyOffset = 0;

        public void sanitize() {
            if (this.colors == null || this.colors.length != 9) {
                this.colors = new Hotbar().colors;
            }
            if (this.marked == null || this.marked.length != 9) {
                this.marked = new Hotbar().marked;
            }
            int i = 0;
            while (i < this.colors.length) {
                int n = i++;
                this.colors[n] = this.colors[n] & 0xFFFFFF;
            }
            this.markerOpacity = HudItemControlsSettings.bounded(this.markerOpacity, 0.0, 1.0, 0.8);
            this.keyOpacity = HudItemControlsSettings.bounded(this.keyOpacity, 0.0, 1.0, 0.9);
            this.keyScale = HudItemControlsSettings.bounded(this.keyScale, 0.35, 1.5, 0.65);
            this.keyOffset = Math.clamp((long)this.keyOffset, -20, 20);
            if (this.markerEdge == null) {
                this.markerEdge = Edge.BOTTOM;
            }
            if (this.keyEdge == null) {
                this.keyEdge = Edge.TOP;
            }
        }
    }

    public static final class Totem
    extends Placement {
        public int durationTicks = 40;

        @Override
        public void sanitize() {
            super.sanitize();
            this.durationTicks = Math.clamp((long)this.durationTicks, 5, 120);
        }
    }

    public static final class Scoreboard
    extends Placement {
        public int backgroundColor = 0x111116;
        public int lineSpacing = 2;
        public double backgroundOpacity = 0.35;

        public Scoreboard() {
            super(0.98, 0.4);
        }

        @Override
        public void sanitize() {
            super.sanitize();
            this.backgroundColor &= 0xFFFFFF;
            this.backgroundOpacity = HudItemControlsSettings.bounded(this.backgroundOpacity, 0.0, 1.0, 0.35);
            this.lineSpacing = Math.clamp((long)this.lineSpacing, 0, 12);
        }
    }

    public static final class Health
    extends Placement {
        public HealthStyle style = HealthStyle.HEARTS;
        public boolean replaceVanilla = true;

        public Health() {
            super(0.5, 0.78);
        }

        @Override
        public void sanitize() {
            super.sanitize();
            if (this.style == null) {
                this.style = HealthStyle.HEARTS;
            }
        }
    }

    public static class Placement {
        public boolean enabled;
        public double x = 0.5;
        public double y = 0.5;
        public double scale = 1.0;

        public Placement() {
        }

        public Placement(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void sanitize() {
            this.x = HudItemControlsSettings.bounded(this.x, 0.0, 1.0, 0.5);
            this.y = HudItemControlsSettings.bounded(this.y, 0.0, 1.0, 0.5);
            this.scale = HudItemControlsSettings.bounded(this.scale, 0.25, 3.0, 1.0);
        }
    }

    public record Box(float x, float y, float scale) {
    }

    public static enum HealthStyle {
        HEARTS,
        BAR,
        NUMBER;

    }

    public static enum Edge {
        TOP,
        BOTTOM;

    }
}

