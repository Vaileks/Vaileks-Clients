/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.minecraft.class_1304
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 *  net.minecraft.class_1935
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_6880
 *  net.minecraft.class_9334
 */
package ru.bloodhelper.client.hud;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_1935;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_6880;
import net.minecraft.class_9334;
import ru.bloodhelper.client.config.BlastLeggingsSettings;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.hud.HudRenderer;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.CombatStateModule;

public final class BlastLeggingsWidget {
    private BlastLeggingsWidget() {
    }

    public static int blastLevel(class_1799 item) {
        if (item.method_7960()) {
            return 0;
        }
        for (Object2IntMap.Entry entry : item.method_58657().method_57539()) {
            if (!((class_6880)entry.getKey()).method_40225(class_1893.field_9107)) continue;
            return entry.getIntValue();
        }
        return 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_332 g, ClientConfig config, boolean preview) {
        int level;
        class_310 mc = class_310.method_1551();
        BlastLeggingsSettings settings = config.combatHud.blastLeggings;
        if (!preview && (mc.field_1755 != null || mc.field_1724 == null || !mc.field_1724.method_5805() || settings.onlyInCombat && !CombatStateModule.active())) {
            return;
        }
        class_1799 item = preview ? new class_1799((class_1935)class_1802.field_8348) : mc.field_1724.method_6118(class_1304.field_6172);
        int n = level = preview ? 4 : BlastLeggingsWidget.blastLevel(item);
        if (level <= 0) {
            return;
        }
        if (preview) {
            item.method_57379(class_9334.field_49641, (Object)true);
        }
        HudRenderer.begin(g, "blastLeggings", settings.position, "LEFT", false, 20, settings.showLevel ? 29 : 20);
        try {
            g.method_51427(item, 2, 2);
            if (settings.showLevel) {
                String text = Integer.toString(level);
                HudStyle.label(g, text, (20 - HudStyle.labelWidth(text)) / 2, 20, -921101, true);
            }
        }
        finally {
            g.method_51448().popMatrix();
        }
    }
}

