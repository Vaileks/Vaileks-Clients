/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_10799
 *  net.minecraft.class_2561
 *  net.minecraft.class_266
 *  net.minecraft.class_268
 *  net.minecraft.class_269
 *  net.minecraft.class_270
 *  net.minecraft.class_2960
 *  net.minecraft.class_304
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_5348
 *  net.minecraft.class_9011
 *  net.minecraft.class_9022
 *  net.minecraft.class_9025
 */
package ru.bloodhelper.client.modules;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_268;
import net.minecraft.class_269;
import net.minecraft.class_270;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import net.minecraft.class_9011;
import net.minecraft.class_9022;
import net.minecraft.class_9025;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.HudItemControlsSettings;

public final class HudItemControlsModule {
    public static float hudFov = 70.0f;

    private HudItemControlsModule() {
    }

    public static HudItemControlsSettings settings() {
        return BloodHelperClient.config().hudItemControls;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderHotbar(class_332 g) {
        class_310 c = class_310.method_1551();
        HudItemControlsSettings.Hotbar s = HudItemControlsModule.settings().hotbar;
        if (c.field_1724 == null || c.field_1724.method_7325() || c.field_1690.field_1842 || !s.markersEnabled && !s.keysEnabled) {
            return;
        }
        int left = g.method_51421() / 2 - 91;
        int top = g.method_51443() - 22;
        for (int i = 0; i < 9; ++i) {
            Object label;
            int x = left + 3 + 20 * i;
            if (s.markersEnabled && s.marked[i]) {
                int y = top + (s.markerEdge == HudItemControlsSettings.Edge.TOP ? 1 : 20);
                int color = (int)Math.round(s.markerOpacity * 255.0) << 24 | s.colors[i];
                g.method_25294(x, y, x + 16, y + 2, color);
            }
            if (!s.keysEnabled) continue;
            class_304 key = c.field_1690.field_1852[i];
            Object object = label = key.method_1415() ? "\u2014" : key.method_16007().getString();
            if (key.method_1428().startsWith("key.mouse.")) {
                String suffix;
                label = switch (suffix = key.method_1428().substring("key.mouse.".length())) {
                    case "left" -> "M1";
                    case "right" -> "M2";
                    case "middle" -> "M3";
                    default -> "M" + suffix;
                };
            }
            float scale = (float)Math.min(s.keyScale, 18.0 / (double)Math.max(1, c.field_1772.method_1727((String)label)));
            int y = top + (s.keyEdge == HudItemControlsSettings.Edge.TOP ? -8 : 13) + s.keyOffset;
            y = Math.clamp((long)y, 0, Math.max(0, g.method_51443() - (int)Math.ceil(9.0f * scale)));
            g.method_51448().pushMatrix();
            try {
                g.method_51448().translate((float)(x + 8) - (float)c.field_1772.method_1727((String)label) * scale / 2.0f, (float)y);
                g.method_51448().scale(scale, scale);
                g.method_51433(c.field_1772, (String)label, 0, 0, (int)Math.round(s.keyOpacity * 255.0) << 24 | 0xFFFFFF, true);
                continue;
            }
            finally {
                g.method_51448().popMatrix();
            }
        }
    }

    public static void transform(class_332 g, HudItemControlsSettings.Placement p, float vanillaX, float vanillaY, float left, float top, float width, float height) {
        HudItemControlsSettings.Box box = HudItemControlsSettings.fit(p, g.method_51421(), g.method_51443(), width, height);
        g.method_51448().translate(box.x() - left * box.scale(), box.y() - top * box.scale());
        g.method_51448().scale(box.scale(), box.scale());
        g.method_51448().translate(-vanillaX, -vanillaY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderScoreboard(class_332 g, class_266 objective) {
        class_310 c = class_310.method_1551();
        HudItemControlsSettings.Scoreboard s = HudItemControlsModule.settings().scoreboard;
        class_269 board = objective.method_1117();
        class_9022 format = objective.method_55380((class_9022)class_9025.field_47567);
        List<ScoreLine> lines = board.method_1184(objective).stream().filter(e -> !e.method_55385()).sorted(Comparator.comparingInt(class_9011::comp_2128).reversed().thenComparing(class_9011::comp_2127, String.CASE_INSENSITIVE_ORDER)).limit(15L).map(e -> new ScoreLine((class_2561)class_268.method_1142((class_270)board.method_1164(e.comp_2127()), (class_2561)e.method_55387()), (class_2561)e.method_55386(format))).toList();
        int width = c.field_1772.method_27525((class_5348)objective.method_1114());
        for (ScoreLine line : lines) {
            width = Math.max(width, c.field_1772.method_27525((class_5348)line.name) + (c.field_1772.method_27525((class_5348)line.value) == 0 ? 0 : c.field_1772.method_27525((class_5348)line.value) + 6));
        }
        int row = 9 + s.lineSpacing;
        int height = row * (lines.size() + 1) + 6;
        float scale = (float)Math.min(s.scale, Math.min(((double)g.method_51421() - 4.0) / (double)(width += 8), ((double)g.method_51443() - 4.0) / (double)height));
        scale = Math.max(0.01f, scale);
        float x = HudItemControlsSettings.placed(s.x, g.method_51421(), (float)width * scale);
        float y = HudItemControlsSettings.placed(s.y, g.method_51443(), (float)height * scale);
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate(x, y);
            g.method_51448().scale(scale, scale);
            if (s.backgroundOpacity > 0.0) {
                g.method_25294(0, 0, width, height, (int)Math.round(s.backgroundOpacity * 255.0) << 24 | s.backgroundColor);
            }
            g.method_51439(c.field_1772, objective.method_1114(), (width - c.field_1772.method_27525((class_5348)objective.method_1114())) / 2, 3, -1, false);
            for (int i = 0; i < lines.size(); ++i) {
                ScoreLine line = lines.get(i);
                int ly = 3 + (i + 1) * row;
                g.method_51439(c.field_1772, line.name, 4, ly, -1, false);
                g.method_51439(c.field_1772, line.value, width - 4 - c.field_1772.method_27525((class_5348)line.value), ly, -1, false);
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    public static boolean replacesHearts() {
        HudItemControlsSettings.Health s = HudItemControlsModule.settings().health;
        return s.enabled && s.replaceVanilla;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void renderHealth(class_332 g) {
        int width;
        class_310 c = class_310.method_1551();
        HudItemControlsSettings.Health s = HudItemControlsModule.settings().health;
        if (!s.enabled || c.field_1724 == null || c.field_1690.field_1842 || c.field_1724.method_7325() || c.field_1761 == null || !c.field_1761.method_2908()) {
            return;
        }
        float health = Math.max(0.0f, c.field_1724.method_6032());
        float maximum = Math.max(1.0f, c.field_1724.method_6063());
        float absorption = Math.max(0.0f, c.field_1724.method_6067());
        int normalHearts = Math.min(100, (int)Math.ceil(maximum / 2.0f));
        int goldenHearts = Math.min(100, (int)Math.ceil(absorption / 2.0f));
        String normal = HudItemControlsModule.number(health) + " / " + HudItemControlsModule.number(maximum);
        String golden = absorption > 0.0f ? "  +" + HudItemControlsModule.number(absorption) : "";
        int n = width = s.style == HudItemControlsSettings.HealthStyle.HEARTS ? Math.min(10, normalHearts + goldenHearts) * 9 : Math.max(100, c.field_1772.method_1727(normal + golden));
        int height = s.style == HudItemControlsSettings.HealthStyle.HEARTS ? (normalHearts + goldenHearts + 9) / 10 * 10 : (s.style == HudItemControlsSettings.HealthStyle.BAR ? 22 : 10);
        float scale = (float)Math.min(s.scale, Math.min((double)g.method_51421() / (double)Math.max(1, width), (double)g.method_51443() / (double)Math.max(1, height)));
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate(HudItemControlsSettings.placed(s.x, g.method_51421(), (float)width * scale), HudItemControlsSettings.placed(s.y, g.method_51443(), (float)height * scale));
            g.method_51448().scale(scale, scale);
            if (s.style == HudItemControlsSettings.HealthStyle.HEARTS) {
                for (int i = 0; i < normalHearts + goldenHearts; ++i) {
                    boolean gold = i >= normalHearts;
                    float amount = gold ? absorption - (float)((i - normalHearts) * 2) : health - (float)(i * 2);
                    int x = i % 10 * 9;
                    int y = i / 10 * 10;
                    HudItemControlsModule.heart(g, "container", x, y);
                    if (!(amount > 0.0f)) continue;
                    HudItemControlsModule.heart(g, (gold ? "absorbing_" : "") + (amount <= 1.0f ? "half" : "full"), x, y);
                }
            } else {
                if (s.style == HudItemControlsSettings.HealthStyle.BAR) {
                    g.method_25294(0, 0, width, 7, -1339808726);
                    g.method_25294(0, 0, Math.round((float)width * Math.min(1.0f, health / maximum)), 7, -1219476);
                    if (absorption > 0.0f) {
                        g.method_25294(0, 8, Math.round((float)width * Math.min(1.0f, absorption / maximum)), 11, -12967);
                    }
                }
                int ty = s.style == HudItemControlsSettings.HealthStyle.BAR ? 13 : 0;
                g.method_51433(c.field_1772, normal, 0, ty, -1, true);
                if (absorption > 0.0f) {
                    g.method_51433(c.field_1772, golden, c.field_1772.method_1727(normal), ty, -12967, true);
                }
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }

    private static String number(float value) {
        return value == (float)((int)value) ? Integer.toString((int)value) : String.format(Locale.ROOT, "%.1f", Float.valueOf(value));
    }

    private static void heart(class_332 g, String name, int x, int y) {
        g.method_52706(class_10799.field_56883, class_2960.method_60656((String)("hud/heart/" + name)), x, y, 9, 9);
    }

    private record ScoreLine(class_2561 name, class_2561 value) {
    }
}

