/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1792
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_1935
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_7923
 */
package pyz1k.utilites.cooldown;

import java.util.Collections;
import java.util.Map;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1935;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;
import pyz1k.mixin.CooldownInstanceAccessor;
import pyz1k.mixin.ItemCooldownsAccessor;

public final class CooldownHudUtil {
    public static final class_2960 PVP_COOLDOWN = class_2960.method_60654((String)"minecraft:trident");

    private CooldownHudUtil() {
    }

    public static Map<class_2960, Object> getCooldowns(class_310 mc) {
        if (mc == null || mc.field_1724 == null) {
            return Collections.emptyMap();
        }
        class_1796 cooldowns = mc.field_1724.method_7357();
        return ((ItemCooldownsAccessor)cooldowns).pyz1k$getCooldowns();
    }

    public static float getRemainingSeconds(class_310 mc, Object cooldownInstance) {
        if (mc == null || mc.field_1724 == null || cooldownInstance == null) {
            return 0.0f;
        }
        ItemCooldownsAccessor cooldowns = (ItemCooldownsAccessor)mc.field_1724.method_7357();
        CooldownInstanceAccessor cooldown = (CooldownInstanceAccessor)cooldownInstance;
        return Math.max(0.0f, (float)(cooldown.pyz1k$getEndTime() - cooldowns.pyz1k$getTickCount()) / 20.0f);
    }

    public static class_1799 findStackForCooldown(class_310 mc, class_2960 cooldownId) {
        if (mc == null || mc.field_1724 == null || cooldownId == null) {
            return class_1799.field_8037;
        }
        for (class_1799 stack : mc.field_1724.method_31548().method_67533()) {
            if (stack.method_7960() || !stack.method_57353().toString().contains(cooldownId.toString())) continue;
            return stack;
        }
        class_1792 item = class_7923.field_41178.method_17966(cooldownId).orElse(null);
        return item == null ? class_1799.field_8037 : new class_1799((class_1935)item);
    }
}

