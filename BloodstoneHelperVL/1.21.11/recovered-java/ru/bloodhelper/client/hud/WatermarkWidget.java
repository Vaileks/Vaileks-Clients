/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2338
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_640
 */
package ru.bloodhelper.client.hud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_640;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.WatermarkSettings;
import ru.bloodhelper.client.gui.BrandLogo;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.gui.UiIcons;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.WatermarkLayout;
import ru.bloodhelper.client.hud.WatermarkText;

public final class WatermarkWidget {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static long joinedAt;

    private WatermarkWidget() {
    }

    public static void joined() {
        joinedAt = System.nanoTime();
    }

    public static void disconnected() {
        joinedAt = 0L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, ClientConfig config, boolean preview) {
        long seconds;
        WatermarkSettings settings = config.watermark;
        class_310 client = class_310.method_1551();
        int ping = -1;
        int online = -1;
        if (client.method_1562() != null) {
            class_640 info;
            if (client.field_1724 != null && (info = client.method_1562().method_2871(client.field_1724.method_5667())) != null) {
                ping = info.method_2959();
            }
            online = client.method_1562().method_45732().size();
        }
        boolean hideServer = config.streamer.hidesServerAddress();
        boolean hideCoordinates = config.streamer.hidesCoordinates();
        WatermarkText.Coordinates coordinates = WatermarkText.coordinates(settings, hideCoordinates, preview, () -> {
            if (client.field_1724 == null) {
                return null;
            }
            class_2338 position = client.field_1724.method_24515();
            return new WatermarkText.Coordinates(position.method_10263(), position.method_10264(), position.method_10260());
        });
        String server = !hideServer && settings.showServer && client.method_1558() != null ? client.method_1558().field_3761 : "";
        long l = seconds = joinedAt == 0L ? 0L : Math.max(0L, (System.nanoTime() - joinedAt) / 1000000000L);
        WatermarkText.Snapshot data = preview ? new WatermarkText.Snapshot(140, 10, "play.example.org", false, 24, "12:30:08", 5025L, coordinates) : new WatermarkText.Snapshot(Math.max(0, client.method_47599()), ping, server, client.method_1496(), online, settings.showClock ? LocalTime.now().format(CLOCK) : "", seconds, coordinates);
        List<WatermarkText.Part> parts = WatermarkText.parts(settings, data, hideServer, hideCoordinates, "ru".equals(config.language));
        if (!config.hudTitle("watermark")) {
            parts = parts.stream().filter(p -> p.kind() != WatermarkText.Kind.BRAND).toList();
        }
        if (parts.isEmpty()) {
            return;
        }
        if (config.simpleHud) {
            List<WatermarkText.Part> essential = parts.stream().filter(p -> p.kind() == WatermarkText.Kind.FPS || p.kind() == WatermarkText.Kind.PING || p.kind() == WatermarkText.Kind.SERVER || p.kind() == WatermarkText.Kind.COORDINATES).toList();
            if (essential.isEmpty()) {
                return;
            }
            if ("SEPARATE".equals(settings.layout)) {
                for (WatermarkText.Part part : essential) {
                    String key = part.kind().name().toLowerCase(Locale.ROOT);
                    HudRenderer.begin(g, "watermark." + key, settings.partPosition(key), "LEFT", false, HudStyle.width(part.text()) + 4, 14);
                    try {
                        HudStyle.text(g, part.text(), 2, 2, -921101, settings.shadow);
                    }
                    finally {
                        g.method_51448().popMatrix();
                    }
                }
            } else {
                String line = String.join((CharSequence)"  \u00b7  ", essential.stream().map(WatermarkText.Part::text).toList());
                HudRenderer.begin(g, "watermark", settings.position, settings.alignment, settings.bottom, HudStyle.width(line) + 4, 14);
                try {
                    HudStyle.text(g, line, 2, 2, -921101, settings.shadow);
                }
                finally {
                    g.method_51448().popMatrix();
                }
            }
            return;
        }
        if ("SEPARATE".equals(settings.layout)) {
            for (WatermarkText.Part part : parts) {
                String key = part.kind().name().toLowerCase(Locale.ROOT);
                WatermarkLayout.Cell cell = WatermarkLayout.create(List.of(part), 440, true, HudStyle::strongWidth).cells().getFirst();
                HudRenderer.begin(g, "watermark." + key, settings.partPosition(key), "LEFT", false, cell.width(), 26);
                try {
                    if (settings.background && config.background && config.backgroundAlpha > 0) {
                        HudStyle.surface(g, 0, 0, cell.width(), 26, 7, config);
                    }
                    if (part.kind() == WatermarkText.Kind.BRAND) {
                        WatermarkWidget.emblem(g, 7, 7, ThemeColors.accent());
                    } else {
                        WatermarkWidget.icon(g, part.kind(), 7, 7, ThemeColors.accent());
                    }
                    HudStyle.strong(g, part.text(), 28, 8, -921101, settings.shadow);
                }
                finally {
                    g.method_51448().popMatrix();
                }
            }
            return;
        }
        WatermarkLayout.Layout layout = WatermarkLayout.create(parts, 440, "SEPARATE".equals(settings.layout), HudStyle::strongWidth);
        HudRenderer.begin(g, "watermark", settings.position, settings.alignment, settings.bottom, layout.width(), layout.height());
        try {
            boolean surface;
            int accent = ThemeColors.accent();
            boolean bl = surface = settings.background && config.background && config.hudDetails && config.backgroundAlpha > 0;
            if (surface && !layout.separate()) {
                HudStyle.surface(g, 0, 0, layout.width(), layout.height(), 9, config);
                if (!"OUTLINE".equals(config.surfaceStyle) && !"LIQUID_GLASS".equals(config.surfaceStyle)) {
                    UiDraw.outline(g, 0, 0, layout.width(), layout.height(), 9, UiDraw.alpha(accent, config.backgroundAlpha / 5));
                }
            }
            WatermarkLayout.Cell previous = null;
            for (WatermarkLayout.Cell cell : layout.cells()) {
                boolean brand;
                int x = cell.x();
                int y = cell.y();
                boolean bl2 = brand = cell.kind() == WatermarkText.Kind.BRAND;
                if (surface && layout.separate()) {
                    HudStyle.surface(g, x, y, cell.width(), 26, 7, config);
                    if (brand && !"OUTLINE".equals(config.surfaceStyle) && !"LIQUID_GLASS".equals(config.surfaceStyle)) {
                        UiDraw.rounded(g, x, y, cell.width(), 26, 7, UiDraw.alpha(accent, config.backgroundAlpha / 14));
                        UiDraw.outline(g, x, y, cell.width(), 26, 7, UiDraw.alpha(accent, config.backgroundAlpha / 4));
                    }
                }
                if (!layout.separate() && previous != null && previous.y() == y) {
                    g.method_25294(x - 1, y + 6, x, y + 26 - 6, ThemeColors.line());
                }
                if (brand) {
                    WatermarkWidget.emblem(g, x + 7, y + 7, accent);
                } else {
                    WatermarkWidget.icon(g, cell.kind(), x + 7, y + 7, accent);
                }
                HudStyle.strong(g, cell.text(), x + 28, y + 8, -921101, settings.shadow);
                previous = cell;
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private static void emblem(class_332 g, int x, int y, int accent) {
        BrandLogo.draw(g, x, y + 2, 13);
    }

    private static void icon(class_332 g, WatermarkText.Kind kind, int x, int y, int accent) {
        int color = ThemeColors.icon(accent);
        if (kind == WatermarkText.Kind.PING) {
            for (int bar = 0; bar < 3; ++bar) {
                UiDraw.rounded(g, x + 1 + bar * 4, y + 8 - bar * 3, 2, 4 + bar * 3, 1, color);
            }
        } else if (kind == WatermarkText.Kind.SERVER) {
            for (int row = 0; row < 2; ++row) {
                UiDraw.outline(g, x, y + 1 + row * 6, 12, 5, 1, color);
                g.method_25294(x + 2, y + 3 + row * 6, x + 4, y + 4 + row * 6, color);
            }
        } else if (kind == WatermarkText.Kind.COORDINATES) {
            UiDraw.outline(g, x + 3, y + 3, 7, 7, 3, color);
            g.method_25294(x + 6, y, x + 7, y + 4, color);
            g.method_25294(x + 6, y + 9, x + 7, y + 13, color);
            g.method_25294(x, y + 6, x + 4, y + 7, color);
            g.method_25294(x + 9, y + 6, x + 13, y + 7, color);
        } else {
            UiIcons.draw(g, switch (kind) {
                case WatermarkText.Kind.FPS -> "performance";
                case WatermarkText.Kind.CLOCK -> "clock";
                case WatermarkText.Kind.SESSION -> "session";
                case WatermarkText.Kind.ONLINE -> "profiles";
                default -> "info";
            }, x - 1, y - 1, 14, accent);
        }
    }
}

