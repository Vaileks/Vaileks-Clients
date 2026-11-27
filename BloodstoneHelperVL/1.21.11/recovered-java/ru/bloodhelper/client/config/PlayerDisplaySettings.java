/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.Set;
import ru.bloodhelper.client.config.ClientConfig;

public final class PlayerDisplaySettings {
    private PlayerDisplaySettings() {
    }

    public static final class OtherTiers {
        public boolean enabled;
        public boolean nametags = true;
        public boolean tab = true;
        public boolean icons = true;
        public String provider = "CIS";
        public String gamemode = "BEST";
        public String position = "LEFT";

        public void sanitize() {
            if (!Set.of("CIS", "MC", "PVP", "A", "MY").contains(this.provider == null ? "" : this.provider)) {
                this.provider = "CIS";
            }
            if (this.gamemode == null || !this.gamemode.matches("[A-Za-z_]{1,24}")) {
                this.gamemode = "BEST";
            }
            if (!"RIGHT".equals(this.position)) {
                this.position = "LEFT";
            }
        }
    }

    public static final class FreeLook {
        public boolean enabled;
        public boolean toggle;
        public double sensitivity = 1.0;

        public void sanitize() {
            this.sensitivity = Double.isFinite(this.sensitivity) ? Math.clamp(this.sensitivity, 0.1, 2.0) : 1.0;
        }
    }

    public static final class Hitboxes {
        public boolean enabled;
        public boolean mobs;
        public boolean items;
        public boolean projectiles;
        public boolean eyeHeight = true;
        public boolean direction = true;
        public boolean players = true;
        public boolean boxes = true;
        public boolean others;
        public boolean directionPlayers = true;
        public boolean directionMobs = true;
        public boolean directionItems = false;
        public boolean directionProjectiles = true;
        public boolean directionOthers = false;
        public int color = -10102273;
        public int eyeColor = -39322;
        public int directionColor = -10057473;
        public double directionLength = 1.5;
        public double distance = 32.0;

        public void sanitize() {
            this.color |= 0xFF000000;
            this.eyeColor |= 0xFF000000;
            this.directionColor |= 0xFF000000;
            this.directionLength = Double.isFinite(this.directionLength) ? Math.clamp(this.directionLength, 0.1, 4.0) : 1.5;
            this.distance = Double.isFinite(this.distance) ? Math.clamp(this.distance, 4.0, 64.0) : 32.0;
        }
    }

    public static final class Saturation
    extends ClientConfig.WidgetSettings {
        public boolean exhaustion = true;
        public boolean foodPreview = true;
        public boolean healthPreview = true;
        public boolean tooltips = true;
        public boolean tooltipsAlways;
        public boolean offhand = true;

        public Saturation() {
            this.position = new ClientConfig.HudPosition(0.6, 0.72);
        }
    }

    public static final class CombatMetric
    extends ClientConfig.WidgetSettings {
        public int timeoutSeconds = 3;
        public boolean playersOnly = true;

        public CombatMetric() {
            this.position = new ClientConfig.HudPosition(0.6, 0.56);
        }

        public void sanitize() {
            ClientConfig.sanitizeWidget(this, new CombatMetric());
            this.timeoutSeconds = Math.clamp((long)this.timeoutSeconds, 1, 15);
        }
    }
}

