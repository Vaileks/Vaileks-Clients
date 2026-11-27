/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import ru.bloodhelper.client.config.WatermarkSettings;

public final class WatermarkText {
    private WatermarkText() {
    }

    public static String format(WatermarkSettings settings, Snapshot data, boolean hideServer, boolean russian) {
        return WatermarkText.format(settings, data, hideServer, false, russian);
    }

    public static String format(WatermarkSettings settings, Snapshot data, boolean hideServer, boolean hideCoordinates, boolean russian) {
        return String.join((CharSequence)" | ", WatermarkText.parts(settings, data, hideServer, hideCoordinates, russian).stream().map(Part::text).toList());
    }

    public static List<Part> parts(WatermarkSettings settings, Snapshot data, boolean hideServer, boolean russian) {
        return WatermarkText.parts(settings, data, hideServer, false, russian);
    }

    public static List<Part> parts(WatermarkSettings settings, Snapshot data, boolean hideServer, boolean hideCoordinates, boolean russian) {
        ArrayList<Part> parts = new ArrayList<Part>();
        String title = WatermarkSettings.cleanText(settings.text);
        if (!title.isEmpty()) {
            parts.add(new Part(Kind.BRAND, title));
        }
        if (settings.showFps) {
            parts.add(new Part(Kind.FPS, WatermarkText.number(data.fps()) + " FPS"));
        }
        if (settings.showPing) {
            parts.add(new Part(Kind.PING, WatermarkText.number(data.pingMs()) + " Ping"));
        }
        if (settings.showServer && !hideServer) {
            if (data.singleplayer()) {
                parts.add(new Part(Kind.SERVER, russian ? "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u0430\u044f \u0438\u0433\u0440\u0430" : "Singleplayer"));
            } else if (data.server() != null && !data.server().isBlank()) {
                parts.add(new Part(Kind.SERVER, data.server()));
            }
        }
        if (settings.showCoordinates && !hideCoordinates && data.coordinates() != null) {
            Coordinates point = data.coordinates();
            parts.add(new Part(Kind.COORDINATES, "X " + point.x() + " \u00b7 Y " + point.y() + " \u00b7 Z " + point.z()));
        }
        if (settings.showClock && data.clock() != null && !data.clock().isBlank()) {
            parts.add(new Part(Kind.CLOCK, data.clock()));
        }
        if (settings.showSession) {
            parts.add(new Part(Kind.SESSION, (russian ? "\u0421\u0435\u0441\u0441\u0438\u044f " : "Session ") + WatermarkText.duration(data.sessionSeconds())));
        }
        if (settings.showOnline) {
            parts.add(new Part(Kind.ONLINE, WatermarkText.number(data.online()) + (russian ? " \u043e\u043d\u043b\u0430\u0439\u043d" : " online")));
        }
        return List.copyOf(parts);
    }

    public static Coordinates coordinates(WatermarkSettings settings, boolean hideCoordinates, boolean preview, Supplier<Coordinates> livePosition) {
        if (!settings.showCoordinates || hideCoordinates) {
            return null;
        }
        return preview ? new Coordinates(123, 64, -456) : livePosition.get();
    }

    private static String number(int value) {
        return value < 0 ? "\u2014" : Integer.toString(value);
    }

    static String duration(long seconds) {
        long safe = Math.max(0L, seconds);
        return String.format(Locale.ROOT, "%02d:%02d:%02d", safe / 3600L, safe / 60L % 60L, safe % 60L);
    }

    public record Snapshot(int fps, int pingMs, String server, boolean singleplayer, int online, String clock, long sessionSeconds, Coordinates coordinates) {
        public Snapshot(int fps, int pingMs, String server, boolean singleplayer, int online, String clock, long sessionSeconds) {
            this(fps, pingMs, server, singleplayer, online, clock, sessionSeconds, null);
        }
    }

    public record Part(Kind kind, String text) {
    }

    public static enum Kind {
        BRAND,
        FPS,
        PING,
        SERVER,
        COORDINATES,
        CLOCK,
        SESSION,
        ONLINE;

    }

    public record Coordinates(int x, int y, int z) {
    }
}

