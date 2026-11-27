/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemCooldowns
 *  net.minecraft.world.item.ItemStack
 */
package ru.bloodhelper.client.hud;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.mixin.CooldownInstanceAccessor;
import ru.bloodhelper.client.mixin.ItemCooldownsAccessor;

public final class HeldCooldownWidget {
    private static HudRenderer.Bounds bounds;

    public static HudRenderer.Bounds bounds() {
        return bounds;
    }

    public static void clear() {
        bounds = null;
    }

    private HeldCooldownWidget() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(GuiGraphicsExtractor g, ClientConfig c) {
        bounds = null;
        Minecraft mc = Minecraft.getInstance();
        if (!c.cooldowns.enabled || !c.cooldowns.nearCrosshair || mc.player == null || mc.gui.screen() != null || !mc.options.getCameraType().isFirstPerson() || mc.player.isSpectator()) {
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        ItemCooldowns manager = mc.player.getCooldowns();
        Identifier group = manager.getCooldownGroup(stack);
        if (group.toString().equals(c.pvp.cooldownGroup)) {
            return;
        }
        ItemCooldownsAccessor access = (ItemCooldownsAccessor)manager;
        Object raw = access.bloodhelper$cooldowns().get(group);
        if (!(raw instanceof CooldownInstanceAccessor)) {
            return;
        }
        CooldownInstanceAccessor cd = (CooldownInstanceAccessor)raw;
        int left = cd.bloodhelper$endTime() - access.bloodhelper$tickCount();
        if (left <= 0) {
            return;
        }
        String label = String.format(Locale.ROOT, "%.1f", (double)left / 20.0) + HudStyle.tr("\u0441", "s");
        int x = g.guiWidth() / 2 + 13;
        int y = g.guiHeight() / 2 - 9;
        int width = 23 + HudStyle.width(label);
        bounds = new HudRenderer.Bounds(x, y, width, 20);
        g.pose().pushMatrix();
        try {
            g.pose().translate((float)Math.min(x, Math.max(0, g.guiWidth() - width)), (float)y);
            UiDraw.rounded(g, 0, 0, width, 20, 6, -1341124331);
            g.item(stack, 2, 2);
            HudStyle.text(g, label, 21, 5, -921101, true);
        }
        finally {
            g.pose().popMatrix();
        }
    }
}

