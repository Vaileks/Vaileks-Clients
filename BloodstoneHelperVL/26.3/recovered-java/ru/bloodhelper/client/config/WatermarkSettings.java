/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ru.bloodhelper.client.config.ClientConfig;

public final class WatermarkSettings
extends ClientConfig.WidgetSettings {
    public String text = "BloodstoneHelperVL";
    public boolean showFps = true;
    public boolean showPing = true;
    public boolean showServer = true;
    public boolean showCoordinates = false;
    public boolean showClock = false;
    public boolean showSession = false;
    public boolean showOnline = false;
    public String layout = "GROUPED";
    public Map<String, ClientConfig.HudPosition> parts = new LinkedHashMap<String, ClientConfig.HudPosition>();
    public int panelVersion = 0;
    public int placementVersion = 0;

    public ClientConfig.HudPosition partPosition(String key) {
        int index = switch (key) {
            case "fps" -> 1;
            case "ping" -> 2;
            case "server" -> 3;
            case "coordinates" -> 4;
            case "clock" -> 5;
            case "session" -> 6;
            case "online" -> 7;
            default -> 0;
        };
        return this.parts.computeIfAbsent(key, ignored -> new ClientConfig.HudPosition(0.03 + (double)(index % 4) * 0.24, 0.025 + (double)(index / 4) * 0.07));
    }

    public WatermarkSettings() {
        this.position = new ClientConfig.HudPosition(0.5, 0.02);
        this.alignment = "CENTER";
    }

    public void sanitize() {
        if (this.parts == null) {
            this.parts = new LinkedHashMap<String, ClientConfig.HudPosition>();
        }
        this.parts.entrySet().removeIf(e -> e.getKey() == null || !Set.of("brand", "fps", "ping", "server", "coordinates", "clock", "session", "online").contains(e.getKey()) || e.getValue() == null);
        this.parts.values().forEach(p -> {
            p.x = Double.isFinite(p.x) ? Math.clamp(p.x, 0.0, 1.0) : 0.02;
            p.y = Double.isFinite(p.y) ? Math.clamp(p.y, 0.0, 1.0) : 0.02;
            p.scale = Double.isFinite(p.scale) ? Math.clamp(p.scale, 0.35, 2.0) : 0.65;
        });
        this.text = WatermarkSettings.cleanText(this.text);
        if (!"SEPARATE".equals(this.layout)) {
            this.layout = "GROUPED";
        }
        if (this.panelVersion < 1) {
            this.background = true;
            this.panelVersion = 1;
        }
        if (this.placementVersion < 1) {
            if (this.position != null && this.position.x == 0.02 && this.position.y == 0.02 && this.position.scale == 0.65 && "LEFT".equals(this.alignment) && !this.bottom) {
                this.position.x = 0.5;
                this.alignment = "CENTER";
            }
            this.placementVersion = 1;
        }
    }

    public static String cleanText(String value) {
        if (value == null) {
            return "BloodstoneHelperVL";
        }
        String clean = value.replaceAll("\u00a7.", "").replaceAll("[\\p{Cntrl}\\p{Cf}]", "").strip();
        return clean.codePoints().limit(48L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }
}

