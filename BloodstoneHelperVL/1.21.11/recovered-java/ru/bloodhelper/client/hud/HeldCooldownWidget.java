/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 */
package ru.bloodhelper.client.hud;

import java.util.Locale;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
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
    public static void render(class_332 g, ClientConfig c) {
        bounds = null;
        class_310 mc = class_310.method_1551();
        if (!c.cooldowns.enabled || !c.cooldowns.nearCrosshair || mc.field_1724 == null || mc.field_1755 != null || !mc.field_1690.method_31044().method_31034() || mc.field_1724.method_7325()) {
            return;
        }
        class_1799 stack = mc.field_1724.method_6047();
        if (stack.method_7960()) {
            return;
        }
        class_1796 manager = mc.field_1724.method_7357();
        class_2960 group = manager.method_62836(stack);
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
        int x = g.method_51421() / 2 + 13;
        int y = g.method_51443() / 2 - 9;
        int width = 23 + HudStyle.width(label);
        bounds = new HudRenderer.Bounds(x, y, width, 20);
        g.method_51448().pushMatrix();
        try {
            g.method_51448().translate((float)Math.min(x, Math.max(0, g.method_51421() - width)), (float)y);
            UiDraw.rounded(g, 0, 0, width, 20, 6, -1341124331);
            g.method_51427(stack, 2, 2);
            HudStyle.text(g, label, 21, 5, -921101, true);
        }
        finally {
            g.method_51448().popMatrix();
        }
    }
}

