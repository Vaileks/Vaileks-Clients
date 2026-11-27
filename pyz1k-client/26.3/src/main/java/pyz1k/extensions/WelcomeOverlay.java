/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 */
package pyz1k.extensions;

import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import pyz1k.render.RenderUtil;
import pyz1k.render.font.GFonts;
import pyz1k.screen.screen.HudVisualStyle;
import pyz1k.utilites.color.ClientColors;
import pyz1k.utilites.color.ColorUtil;

public final class WelcomeOverlay {
    public static final String MESSAGE = "АНЧСП Vaileks, он желает вам только добра";
    private static final Set<String> NAMES = Set.of("tukisg", "trard", "darkcake");
    private static long started;
    private static boolean pending;

    public static boolean matches(String name) {
        return name != null && NAMES.contains(name.toLowerCase(Locale.ROOT));
    }

    public static void joined() {
        pending = true;
        started = 0L;
    }

    public static void disconnected() {
        pending = false;
        started = 0L;
    }

    public static void tick() {
        if(!pending)return;
        Minecraft mc = Minecraft.getInstance();
        if (pending && mc.player != null) {
            pending = false;
            if (WelcomeOverlay.matches(mc.player.getName().getString())) {
                started = System.nanoTime();
            }
        }
    }

    public static void render(GuiGraphicsExtractor ctx) {
        if (started == 0L) {
            return;
        }
        float elapsed = (float)(System.nanoTime() - started) / 1.0E9f;
        if (elapsed >= 8.0f) {
            started = 0L;
            return;
        }
        float alpha = Math.max(0.0f, Math.min(1.0f, Math.min(elapsed / 0.3f, (8.0f - elapsed) / 0.6f)));
        Minecraft mc = Minecraft.getInstance();
        float size = 11.0f;
        float available = mc.getWindow().getGuiScaledWidth() - 30;
        float textWidth = RenderUtil.getTextWidth(MESSAGE, GFonts.MEDIUM, size);
        if (textWidth > available) {
            size *= available / textWidth;
            textWidth = available;
        }
        float w = textWidth + 20.0f;
        float x = ((float)mc.getWindow().getGuiScaledWidth() - w) / 2.0f;
        HudVisualStyle.drawPanel(ctx, x, 22.0f, w, 34.0f, alpha);
        RenderUtil.drawText(ctx, MESSAGE, x + 10.0f, 34.0f, ColorUtil.multiplyAlpha(ClientColors.CATEGORY_ACCENT_SECOND, alpha), GFonts.MEDIUM, size);
    }
}

