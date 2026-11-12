/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1735
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_437
 *  net.minecraft.class_465
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.gui;

import net.minecraft.class_1735;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_9334;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.EmbeddedReGlass;
import ru.bloodhelper.client.gui.LiquidGlass;
import ru.bloodhelper.client.gui.ReGlassChatContext;
import ru.bloodhelper.client.gui.ReGlassGuiRules;
import ru.bloodhelper.client.gui.ReGlassResources;
import ru.bloodhelper.client.gui.UiDraw;

public final class ReGlassGui {
    private static boolean inside;
    private static Object containerFrame;
    private static long draws;
    private static long slots;
    private static long buttons;
    private static long chatPanels;

    private ReGlassGui() {
    }

    public static boolean active() {
        return EmbeddedReGlass.active() && BloodHelperClient.config().reglassFullGui;
    }

    public static int labelColor(int color) {
        int rgb = color & 0xFFFFFF;
        return ReGlassGui.active() && containerFrame != null && class_310.method_1551().field_1755 instanceof class_465 && (rgb == 0x404040 || rgb == 0x555555) ? -1511945 : color;
    }

    public static long draws() {
        return draws;
    }

    public static long slots() {
        return slots;
    }

    public static long buttons() {
        return buttons;
    }

    public static long chatPanels() {
        return chatPanels;
    }

    public static boolean containerReady(class_332 g) {
        return ReGlassGui.active() && containerFrame == g;
    }

    public static void beginContainer() {
        containerFrame = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean panel(class_332 g, int x, int y, int w, int h, int radius, int tint) {
        if (inside || !ReGlassGui.active() || w <= 0 || h <= 0) {
            return false;
        }
        inside = true;
        try {
            if (!EmbeddedReGlass.draw(g, x, y, w, h, radius, tint)) {
                boolean bl = false;
                return bl;
            }
            UiDraw.outline(g, x, y, w, h, radius, 1082102190);
            ++draws;
            boolean bl = true;
            return bl;
        }
        finally {
            inside = false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean sprite(class_332 g, class_2960 id, int x, int y, int w, int h, int color) {
        int tint;
        boolean bright;
        if (inside || !ReGlassGui.active() || !id.method_12836().equals("minecraft") || color >>> 24 == 0) {
            return false;
        }
        String p = id.method_12832();
        if (!ReGlassGuiRules.sprite(p) && !ReGlassGuiRules.outline(p) || ReGlassResources.customContainer() || !ReGlassResources.vanillaSprite(id)) {
            return false;
        }
        if (ReGlassGuiRules.outline(p) && LiquidGlass.hasCurrentSnapshot()) {
            inside = true;
            try {
                UiDraw.outline(g, x, y, w, h, Math.min(5, h / 2), p.equals("tooltip/frame") ? 1889317590 : -985601);
            }
            finally {
                inside = false;
            }
            return true;
        }
        if (!ReGlassGuiRules.sprite(p)) {
            return false;
        }
        boolean bl = bright = p.contains("highlighted") || p.endsWith("selected") && !p.contains("unselected");
        int n = p.contains("disabled") ? -1089070558 : (tint = bright ? -12168340 : -484037317);
        if (!ReGlassGui.panel(g, x, y, w, h, Math.min(6, h / 2), UiDraw.alpha(tint, (tint >>> 24) * (color >>> 24) / 255))) {
            return false;
        }
        if (p.equals("widget/button") || p.startsWith("widget/button_")) {
            ++buttons;
        }
        inside = true;
        try {
            ReGlassGui.icon(g, p, x, y, w, h, p.contains("disabled") ? -7630175 : -1511945);
        }
        finally {
            inside = false;
        }
        return true;
    }

    private static void icon(class_332 g, String p, int x, int y, int w, int h, int color) {
        block5: {
            int cy;
            int cx;
            int u;
            block7: {
                block6: {
                    block4: {
                        int i;
                        u = Math.max(1, Math.min(w, h) / 20);
                        cx = x + w / 2;
                        cy = y + h / 2;
                        if (!p.startsWith("widget/checkbox") || !p.contains("selected")) break block4;
                        for (i = 0; i < 3; ++i) {
                            g.method_25294(cx + (i - 4) * u, cy + i * u, cx + (i - 2) * u, cy + (i + 2) * u, color);
                        }
                        for (i = 0; i < 6; ++i) {
                            g.method_25294(cx + (i - 2) * u, cy + (2 - i) * u, cx + i * u, cy + (4 - i) * u, color);
                        }
                        break block5;
                    }
                    if (!p.startsWith("widget/locked_button") && !p.startsWith("widget/unlocked_button")) break block6;
                    UiDraw.roundedSolid(g, cx - 4 * u, cy, 8 * u, 6 * u, 2 * u, color);
                    g.method_25294(cx - 3 * u, cy - 5 * u, cx - u, cy + u, color);
                    g.method_25294(cx - 3 * u, cy - 6 * u, cx + 3 * u, cy - 4 * u, color);
                    g.method_25294(cx + u, cy - 5 * u, cx + 3 * u, cy + (p.startsWith("widget/unlocked") ? -2 : 1) * u, color);
                    g.method_25294(cx, cy + 2 * u, cx + u, cy + 4 * u, -14076857);
                    break block5;
                }
                if (!p.startsWith("widget/cross_button")) break block7;
                for (int i = -3; i <= 3; ++i) {
                    g.method_25294(cx + i * u, cy + i * u, cx + (i + 2) * u, cy + (i + 2) * u, color);
                    g.method_25294(cx + i * u, cy - i * u, cx + (i + 2) * u, cy + (2 - i) * u, color);
                }
                break block5;
            }
            if (!p.startsWith("widget/page_forward") && !p.startsWith("widget/page_backward")) break block5;
            int sign = p.startsWith("widget/page_forward") ? 1 : -1;
            for (int i = -3; i <= 3; ++i) {
                int dx = sign * (2 - Math.abs(i));
                g.method_25294(cx + dx * u, cy + i * u, cx + (dx + 2) * u, cy + (i + 2) * u, color);
            }
        }
    }

    public static boolean texture(class_332 g, class_2960 id, int x, int y, int w, int h) {
        if (inside || !ReGlassGui.active() || !id.method_12836().equals("minecraft") || !ReGlassGuiRules.container(id.method_12832(), w, h)) {
            return false;
        }
        if (ReGlassResources.customContainer() || !ReGlassResources.vanillaTexture(id)) {
            containerFrame = null;
            return false;
        }
        if (!ReGlassGui.panel(g, x, y, w, h, 9, -265999303)) {
            return false;
        }
        containerFrame = g;
        return true;
    }

    public static boolean fill(class_332 g, int x1, int y1, int x2, int y2, int color) {
        int h;
        boolean drawn;
        if (inside || !ReGlassGui.active() || ReGlassChatContext.drawing() && !BloodHelperClient.config().reglassChatMessages) {
            return false;
        }
        class_437 screen = class_310.method_1551().field_1755;
        if (screen != null && screen.getClass().getName().startsWith("ru.bloodhelper.")) {
            return false;
        }
        if (screen instanceof class_465 && !ReGlassGui.containerReady(g)) {
            return false;
        }
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        int w = Math.abs(x2 - x1);
        boolean bl = drawn = ReGlassGuiRules.background(w, h = Math.abs(y2 - y1), color) && ReGlassGui.panel(g, x, y, w, h, Math.min(4, h / 2), UiDraw.alpha(2107188, color >>> 24));
        if (drawn && ReGlassChatContext.drawing()) {
            ++chatPanels;
        }
        return drawn;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void slot(class_332 g, class_1735 slot) {
        if (!ReGlassGui.containerReady(g)) {
            return;
        }
        if (slot.method_7681() && slot.method_7677().method_57826(class_9334.field_49637)) {
            return;
        }
        int x = slot.field_7873;
        int y = slot.field_7872;
        inside = true;
        try {
            UiDraw.roundedSolid(g, x - 1, y - 1, 18, 18, 3, 1478502970);
            UiDraw.outline(g, x - 1, y - 1, 18, 18, 3, 1216319918);
            ++slots;
        }
        finally {
            inside = false;
        }
    }
}

