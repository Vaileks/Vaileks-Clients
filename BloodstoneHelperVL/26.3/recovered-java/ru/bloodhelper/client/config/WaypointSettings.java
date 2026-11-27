/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import ru.bloodhelper.client.config.ClientConfig;

public final class WaypointSettings
extends ClientConfig.WidgetSettings {
    public static final int LIMIT = 256;
    public boolean showWorld = true;
    public boolean showList = true;
    public boolean quickPing = true;
    public int pingSeconds = 15;
    public double worldScale = 1.0;
    public boolean showDistance = true;
    public boolean showCoordinates = false;
    public int maxVisible = 4;
    public List<Entry> entries = new ArrayList<Entry>();

    public WaypointSettings() {
        this.position = new ClientConfig.HudPosition(0.02, 0.16);
    }

    public void sanitize() {
        this.maxVisible = Math.clamp((long)this.maxVisible, 1, 8);
        this.pingSeconds = Math.clamp((long)this.pingSeconds, 3, 120);
        double d = this.worldScale = Double.isFinite(this.worldScale) ? Math.clamp(this.worldScale, 0.5, 1.5) : 1.0;
        if (this.position == null) {
            this.position = new ClientConfig.HudPosition(0.02, 0.16);
        }
        this.position.x = Double.isFinite(this.position.x) ? Math.clamp(this.position.x, 0.0, 1.0) : 0.02;
        this.position.y = Double.isFinite(this.position.y) ? Math.clamp(this.position.y, 0.0, 1.0) : 0.16;
        this.position.scale = Double.isFinite(this.position.scale) ? Math.clamp(this.position.scale, 0.35, 2.0) : 0.65;
        if (!List.of("LEFT", "CENTER", "RIGHT").contains(this.alignment == null ? "" : this.alignment)) {
            this.alignment = "LEFT";
        }
        ArrayList<Entry> clean = new ArrayList<Entry>();
        HashSet<String> identifiers = new HashSet<String>();
        if (this.entries != null) {
            for (Entry entry : this.entries) {
                if (entry == null || entry.scope == null || !entry.scope.matches("(?:local|remote|session):[0-9a-f]{64}") || entry.dimension == null || !entry.dimension.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || !WaypointSettings.validCoordinates(entry.x, entry.y, entry.z)) continue;
                if (entry.id == null || !entry.id.matches("[a-zA-Z0-9_-]{1,64}") || !identifiers.add(entry.id)) {
                    entry.id = UUID.randomUUID().toString();
                    identifiers.add(entry.id);
                }
                entry.name = WaypointSettings.cleanName(entry.name);
                entry.color |= 0xFF000000;
                clean.add(entry);
                if (clean.size() != 256) continue;
                break;
            }
        }
        this.entries = clean;
    }

    public static boolean validCoordinates(int x, int y, int z) {
        return x >= -30000000 && x <= 30000000 && z >= -30000000 && z <= 30000000 && y >= -2048 && y <= 2048;
    }

    public static String cleanName(String value) {
        if (value == null) {
            return "Waypoint";
        }
        StringBuilder clean = new StringBuilder();
        value.codePoints().filter(c -> !Character.isISOControl(c) && c != 167).limit(48L).forEach(clean::appendCodePoint);
        String name = clean.toString().strip();
        return name.isEmpty() ? "Waypoint" : name;
    }

    public static final class Entry {
        public String id = UUID.randomUUID().toString();
        public String name = "Waypoint";
        public String scope = "";
        public String dimension = "minecraft:overworld";
        public int x;
        public int y;
        public int z;
        public int color = -1618842;
        public boolean visible = true;

        public Entry copy() {
            Entry copy = new Entry();
            copy.id = this.id;
            copy.name = this.name;
            copy.scope = this.scope;
            copy.dimension = this.dimension;
            copy.x = this.x;
            copy.y = this.y;
            copy.z = this.z;
            copy.color = this.color;
            copy.visible = this.visible;
            return copy;
        }
    }
}

