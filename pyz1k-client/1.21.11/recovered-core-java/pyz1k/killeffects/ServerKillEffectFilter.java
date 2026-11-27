/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1799
 *  net.minecraft.class_1937
 *  net.minecraft.class_2960
 *  net.minecraft.class_8113$class_8122
 *  net.minecraft.class_8113$class_8122$class_8226
 *  net.minecraft.class_9280
 *  net.minecraft.class_9334
 */
package pyz1k.killeffects;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_8113;
import net.minecraft.class_9280;
import net.minecraft.class_9334;
import pyz1k.Pyz1k;
import pyz1k.killeffects.KillEffectEngine;
import pyz1k.module.impl.visuals.KillEffects;

public final class ServerKillEffectFilter {
    private static final class_2960 MODEL = class_2960.method_60655((String)"bettermodel", (String)"bm_models");
    private static final Set<class_8113.class_8122> CAPTURED_LIMBS = Collections.newSetFromMap(new WeakHashMap());
    private static final Set<class_8113.class_8122> CAPTURED_EFFECTS = Collections.newSetFromMap(new WeakHashMap());
    private static final ArrayDeque<HiddenPart> RECENT_PARTS = new ArrayDeque();
    private static class_1937 observedLevel;
    private static final int[][] KILL_EFFECT_IDS;

    private ServerKillEffectFilter() {
    }

    public static boolean shouldHide(class_8113.class_8122 display) {
        class_8113.class_8122.class_8226 item;
        Pyz1k client = Pyz1k.getInstance();
        if (client == null) {
            return false;
        }
        KillEffects module = client.getModuleManager().getModule(KillEffects.class);
        if (module == null || !module.isEnable() || !module.hideServerEffects() && !module.hideOtherServerEffects()) {
            CAPTURED_LIMBS.clear();
            CAPTURED_EFFECTS.clear();
            RECENT_PARTS.clear();
            return false;
        }
        if (observedLevel != display.method_73183()) {
            CAPTURED_LIMBS.clear();
            CAPTURED_EFFECTS.clear();
            RECENT_PARTS.clear();
            observedLevel = display.method_73183();
        }
        if ((item = display.method_49782()) == null) {
            return false;
        }
        class_1799 stack = item.comp_1322();
        class_2960 itemModel = (class_2960)stack.method_58694(class_9334.field_54199);
        if (itemModel != null && "bettermodel".equals(itemModel.method_12836()) && itemModel.method_12832().startsWith("player_limb/")) {
            if (CAPTURED_LIMBS.contains(display)) {
                return true;
            }
            if (display.field_6012 <= 60 && (ServerKillEffectFilter.shouldHideHere(module, display) || ServerKillEffectFilter.nearHiddenEffect(display))) {
                CAPTURED_LIMBS.add(display);
                return true;
            }
            return false;
        }
        if (!MODEL.equals((Object)itemModel)) {
            return false;
        }
        if (CAPTURED_EFFECTS.contains(display)) {
            ServerKillEffectFilter.recordHiddenPart(display);
            return true;
        }
        class_9280 data = (class_9280)stack.method_58694(class_9334.field_49637);
        if (data == null) {
            return false;
        }
        Float value = data.method_65362(0);
        if (value == null || !Float.isFinite(value.floatValue()) || value.floatValue() < 1.0f || value.floatValue() >= 882.0f) {
            return false;
        }
        int id = (int)Math.floor(value.floatValue());
        for (int[] range : KILL_EFFECT_IDS) {
            if (id < range[0] || id > range[1] || display.field_6012 > 60 || !ServerKillEffectFilter.shouldHideHere(module, display)) continue;
            CAPTURED_EFFECTS.add(display);
            ServerKillEffectFilter.recordHiddenPart(display);
            return true;
        }
        return false;
    }

    private static boolean shouldHideHere(KillEffects module, class_8113.class_8122 display) {
        double z;
        double y;
        double x = display.method_23317();
        if (KillEffectEngine.nearConfirmedKill(x, y = display.method_23318(), z = display.method_23321())) {
            return module.hideServerEffects();
        }
        return module.hideOtherServerEffects() && KillEffectEngine.nearOtherKill(x, y, z, 16.0);
    }

    private static void recordHiddenPart(class_8113.class_8122 display) {
        long now = System.currentTimeMillis();
        if (RECENT_PARTS.size() >= 64) {
            RECENT_PARTS.removeFirst();
        }
        RECENT_PARTS.addLast(new HiddenPart(display.method_23317(), display.method_23318(), display.method_23321(), now));
    }

    private static boolean nearHiddenEffect(class_8113.class_8122 display) {
        long now = System.currentTimeMillis();
        while (!RECENT_PARTS.isEmpty() && now - ServerKillEffectFilter.RECENT_PARTS.peekFirst().time > 1200L) {
            RECENT_PARTS.removeFirst();
        }
        for (HiddenPart part : RECENT_PARTS) {
            double z;
            double y;
            double x = display.method_23317() - part.x;
            if (!(x * x + (y = display.method_23318() - part.y) * y + (z = display.method_23321() - part.z) * z < 6.25)) continue;
            return true;
        }
        return false;
    }

    static {
        KILL_EFFECT_IDS = new int[][]{{1, 182}, {207, 230}, {253, 295}, {304, 398}, {404, 434}, {441, 698}, {704, 709}, {717, 726}, {759, 805}, {809, 881}};
    }

    private record HiddenPart(double x, double y, double z, long time) {
    }
}

