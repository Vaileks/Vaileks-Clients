/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package ru.bloodhelper.client.hud;

import java.util.Locale;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.GeneralHudSettings;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.KeyLabels;
import ru.bloodhelper.client.modules.PlayerMetrics;

public final class GeneralHudWidget {
    private GeneralHudWidget() {
    }

    public static void render(GuiGraphicsExtractor g, ClientConfig c, boolean preview) {
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        if (c.generalHud.enabled || preview) {
            for (GeneralHudSettings.Metric metric : GeneralHudSettings.Metric.values()) {
                ClientConfig.WidgetSettings s = c.generalHud.get(metric);
                if (!s.enabled && !preview) continue;
                if (metric == GeneralHudSettings.Metric.KEYS) {
                    GeneralHudWidget.keys(g, c, s, preview);
                    continue;
                }
                String value = switch (metric) {
                    case GeneralHudSettings.Metric.FPS -> Integer.toString(preview ? 144 : mc.getFps());
                    case GeneralHudSettings.Metric.CPS -> {
                        if (preview) {
                            yield "8 | 4";
                        }
                        yield PlayerMetrics.LEFT.count(now) + " | " + PlayerMetrics.RIGHT.count(now);
                    }
                    case GeneralHudSettings.Metric.RAM -> {
                        if (preview) {
                            yield "2048 / 4096 MB";
                        }
                        yield (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 0x100000L + " / " + Runtime.getRuntime().maxMemory() / 0x100000L + " MB";
                    }
                    case GeneralHudSettings.Metric.COMPASS -> {
                        if (preview || mc.player == null) {
                            yield "N \u00b7 180\u00b0";
                        }
                        yield GeneralHudWidget.compass(mc.player.getYRot());
                    }
                    case GeneralHudSettings.Metric.PLAYTIME -> GeneralHudWidget.duration(preview ? 3723L : PlayerMetrics.seconds());
                    case GeneralHudSettings.Metric.DAYS -> Long.toString(preview || mc.level == null ? 42L : Math.floorDiv(mc.level.getOverworldClockTime(), 24000));
                    case GeneralHudSettings.Metric.SPEED -> String.format(Locale.ROOT, "%.2f %s", preview ? 5.61 : PlayerMetrics.speed(), HudStyle.tr(c, "\u0431\u043b/\u0441", "b/s"));
                    default -> "";
                };
                GeneralHudWidget.text(g, c, "generalHud." + metric.key(), s, HudStyle.tr(c, metric.ru, metric.en), value);
            }
        }
        if (c.comboCounter.enabled || preview) {
            GeneralHudWidget.text(g, c, "comboCounter", c.comboCounter, HudStyle.tr(c, "\u041a\u043e\u043c\u0431\u043e", "Combo"), (preview ? 4 : PlayerMetrics.COMBAT.combo(now, (long)c.comboCounter.timeoutSeconds * 1000000000L)) + "\u00d7");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void text(GuiGraphicsExtractor g, ClientConfig c, String key, ClientConfig.WidgetSettings s, String label, String value) {
        if (c.simpleHud) {
            Object compact = switch (key) {
                case "generalHud.ram" -> "RAM " + value;
                case "generalHud.compass", "generalHud.speed", "generalHud.playtime" -> value;
                case "generalHud.days" -> HudStyle.tr(c, "\u0414\u0435\u043d\u044c ", "Day ") + value;
                default -> label + " " + value;
            };
            int width = HudStyle.width((String)compact) + 4;
            HudRenderer.begin(g, key, s.position, s.alignment, s.bottom, width, 14);
            try {
                HudStyle.text(g, (String)compact, 2, 2, -921101, s.shadow);
            }
            finally {
                g.pose().popMatrix();
            }
            return;
        }
        int width = Math.max(72, Math.max(HudStyle.strongWidth(value), c.hudTitle(key) ? HudStyle.strongWidth(label) : 0) + 16);
        int height = c.hudTitle(key) ? 38 : 24;
        HudRenderer.begin(g, key, s.position, s.alignment, s.bottom, width, height);
        try {
            if (c.background && s.background) {
                HudStyle.panel(g, width, height, c);
            }
            if (c.hudTitle(key)) {
                HudStyle.label(g, label, 8, 6, -4802625, s.shadow);
            }
            HudStyle.strong(g, value, 8, c.hudTitle(key) ? 21 : 7, -921101, s.shadow);
        }
        finally {
            g.pose().popMatrix();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void keys(GuiGraphicsExtractor g, ClientConfig c, ClientConfig.WidgetSettings s, boolean preview) {
        Options o = Minecraft.getInstance().options;
        KeyMapping[] bindings = new KeyMapping[]{o.keyUp, o.keyLeft, o.keyDown, o.keyRight, o.keyJump, o.keyAttack, o.keyUse};
        int[] x = new int[]{30, 2, 30, 58, 2, 2, 44};
        int[] y = new int[]{2, 30, 30, 30, 58, 80, 80};
        HudRenderer.begin(g, "generalHud.keys", s.position, s.alignment, s.bottom, 86, 103);
        try {
            if (!"LETTERS".equals(c.generalHud.keysStyle) && c.background && s.background) {
                HudStyle.panel(g, 86, 103, c);
            }
            for (int i = 0; i < bindings.length; ++i) {
                boolean down;
                int h;
                int w = i == 4 ? 82 : (i > 4 ? 40 : 26);
                int n = h = i >= 4 ? 20 : 26;
                boolean bl = preview ? i == 0 || i == 5 : (down = Minecraft.getInstance().gui.screen() == null && bindings[i].isDown());
                if (!c.simpleHud && !"LETTERS".equals(c.generalHud.keysStyle)) {
                    UiDraw.rounded(g, x[i], y[i], w, h, 4, down ? c.accent : 1430208326);
                }
                String label = i == 5 ? "LMB" : (i == 6 ? "RMB" : KeyLabels.label(bindings[i].saveString(), HudStyle.tr(c, "\u041f\u0440\u043e\u0431\u0435\u043b", "Space")));
                label = HudStyle.fitStrong(label, w - 4);
                int tx = x[i] + (w - HudStyle.strongWidth(label)) / 2;
                int ty = y[i] + (h - 10) / 2;
                if (c.simpleHud) {
                    HudStyle.strong(g, label, tx, ty, down ? c.accent : -921101, s.shadow);
                    continue;
                }
                if ("LETTERS".equals(c.generalHud.keysStyle)) {
                    for (int dx = -1; dx <= 1; ++dx) {
                        for (int dy = -1; dy <= 1; ++dy) {
                            if (dx == 0 && dy == 0) continue;
                            HudStyle.strong(g, label, tx + dx, ty + dy, -535817960, false);
                        }
                    }
                    HudStyle.strong(g, label, tx, ty, down ? c.accent : -921101, false);
                    continue;
                }
                HudStyle.strong(g, label, tx, ty, -921101, s.shadow);
            }
        }
        finally {
            g.pose().popMatrix();
        }
    }

    public static String duration(long seconds) {
        return String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600L, seconds / 60L % 60L, seconds % 60L);
    }

    public static String compass(float yaw) {
        double degrees = (yaw % 360.0f + 360.0f) % 360.0f;
        String[] directions = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return directions[(int)Math.floor((degrees + 22.5) / 45.0) % 8] + " \u00b7 " + Math.round(degrees) % 360L + "\u00b0";
    }
}

