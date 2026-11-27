/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.inventory.Slot
 */
package ru.bloodhelper.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
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
        return ReGlassGui.active() && containerFrame != null && Minecraft.getInstance().gui.screen() instanceof AbstractContainerScreen && (rgb == 0x404040 || rgb == 0x555555) ? -1511945 : color;
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

    public static boolean containerReady(GuiGraphicsExtractor g) {
        return ReGlassGui.active() && containerFrame == g;
    }

    public static void beginContainer() {
        containerFrame = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean panel(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int tint) {
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
    public static boolean sprite(GuiGraphicsExtractor g, Identifier id, int x, int y, int w, int h, int color) {
        int tint;
        boolean bright;
        if (inside || !ReGlassGui.active() || !id.getNamespace().equals("minecraft") || color >>> 24 == 0) {
            return false;
        }
        String p = id.getPath();
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

    private static void icon(GuiGraphicsExtractor g, String p, int x, int y, int w, int h, int color) {
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
                            g.fill(cx + (i - 4) * u, cy + i * u, cx + (i - 2) * u, cy + (i + 2) * u, color);
                        }
                        for (i = 0; i < 6; ++i) {
                            g.fill(cx + (i - 2) * u, cy + (2 - i) * u, cx + i * u, cy + (4 - i) * u, color);
                        }
                        break block5;
                    }
                    if (!p.startsWith("widget/locked_button") && !p.startsWith("widget/unlocked_button")) break block6;
                    UiDraw.roundedSolid(g, cx - 4 * u, cy, 8 * u, 6 * u, 2 * u, color);
                    g.fill(cx - 3 * u, cy - 5 * u, cx - u, cy + u, color);
                    g.fill(cx - 3 * u, cy - 6 * u, cx + 3 * u, cy - 4 * u, color);
                    g.fill(cx + u, cy - 5 * u, cx + 3 * u, cy + (p.startsWith("widget/unlocked") ? -2 : 1) * u, color);
                    g.fill(cx, cy + 2 * u, cx + u, cy + 4 * u, -14076857);
                    break block5;
                }
                if (!p.startsWith("widget/cross_button")) break block7;
                for (int i = -3; i <= 3; ++i) {
                    g.fill(cx + i * u, cy + i * u, cx + (i + 2) * u, cy + (i + 2) * u, color);
                    g.fill(cx + i * u, cy - i * u, cx + (i + 2) * u, cy + (2 - i) * u, color);
                }
                break block5;
            }
            if (!p.startsWith("widget/page_forward") && !p.startsWith("widget/page_backward")) break block5;
            int sign = p.startsWith("widget/page_forward") ? 1 : -1;
            for (int i = -3; i <= 3; ++i) {
                int dx = sign * (2 - Math.abs(i));
                g.fill(cx + dx * u, cy + i * u, cx + (dx + 2) * u, cy + (i + 2) * u, color);
            }
        }
    }

    public static boolean texture(GuiGraphicsExtractor g, Identifier id, int x, int y, int w, int h) {
        if (inside || !ReGlassGui.active() || !id.getNamespace().equals("minecraft") || !ReGlassGuiRules.container(id.getPath(), w, h)) {
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

    public static boolean fill(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        int h;
        boolean drawn;
        if (inside || !ReGlassGui.active() || ReGlassChatContext.drawing() && !BloodHelperClient.config().reglassChatMessages) {
            return false;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen != null && screen.getClass().getName().startsWith("ru.bloodhelper.")) {
            return false;
        }
        if (screen instanceof AbstractContainerScreen && !ReGlassGui.containerReady(g)) {
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
    public static void slot(GuiGraphicsExtractor g, Slot slot) {
        if (!ReGlassGui.containerReady(g)) {
            return;
        }
        if (slot.hasItem() && slot.getItem().has(DataComponents.CUSTOM_MODEL_DATA)) {
            return;
        }
        int x = slot.x;
        int y = slot.y;
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

