/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.Display$ItemDisplay
 *  net.minecraft.world.entity.Display$ItemDisplay$ItemRenderState
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.component.CustomModelData
 *  net.minecraft.world.level.Level
 */
package pyz1k.killeffects;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import pyz1k.Pyz1k;
import pyz1k.killeffects.KillEffectEngine;
import pyz1k.module.impl.visuals.KillEffects;

public final class ServerKillEffectFilter {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath((String)"bettermodel", (String)"bm_models");
    private static final Set<Display.ItemDisplay> CAPTURED_LIMBS = Collections.newSetFromMap(new WeakHashMap());
    private static final Set<Display.ItemDisplay> CAPTURED_EFFECTS = Collections.newSetFromMap(new WeakHashMap());
    private static final ArrayDeque<HiddenPart> RECENT_PARTS = new ArrayDeque();
    private static Level observedLevel;
    private static final int[][] KILL_EFFECT_IDS;

    private ServerKillEffectFilter() {
    }

    public static boolean shouldHide(Display.ItemDisplay display) {
        Display.ItemDisplay.ItemRenderState item;
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
        if (observedLevel != display.level()) {
            CAPTURED_LIMBS.clear();
            CAPTURED_EFFECTS.clear();
            RECENT_PARTS.clear();
            observedLevel = display.level();
        }
        if ((item = display.itemRenderState()) == null) {
            return false;
        }
        ItemStack stack = item.itemStack();
        Identifier itemModel = (Identifier)stack.get(DataComponents.ITEM_MODEL);
        if (itemModel != null && "bettermodel".equals(itemModel.getNamespace()) && itemModel.getPath().startsWith("player_limb/")) {
            if (CAPTURED_LIMBS.contains(display)) {
                return true;
            }
            if (display.tickCount <= 60 && (ServerKillEffectFilter.shouldHideHere(module, display) || ServerKillEffectFilter.nearHiddenEffect(display))) {
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
        CustomModelData data = (CustomModelData)stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null) {
            return false;
        }
        Float value = data.getFloat(0);
        if (value == null || !Float.isFinite(value.floatValue()) || value.floatValue() < 1.0f || value.floatValue() >= 882.0f) {
            return false;
        }
        int id = (int)Math.floor(value.floatValue());
        for (int[] range : KILL_EFFECT_IDS) {
            if (id < range[0] || id > range[1] || display.tickCount > 60 || !ServerKillEffectFilter.shouldHideHere(module, display)) continue;
            CAPTURED_EFFECTS.add(display);
            ServerKillEffectFilter.recordHiddenPart(display);
            return true;
        }
        return false;
    }

    private static boolean shouldHideHere(KillEffects module, Display.ItemDisplay display) {
        double z;
        double y;
        double x = display.getX();
        if (KillEffectEngine.nearConfirmedKill(x, y = display.getY(), z = display.getZ())) {
            return module.hideServerEffects();
        }
        return module.hideOtherServerEffects() && KillEffectEngine.nearOtherKill(x, y, z, 16.0);
    }

    private static void recordHiddenPart(Display.ItemDisplay display) {
        long now = System.currentTimeMillis();
        if (RECENT_PARTS.size() >= 64) {
            RECENT_PARTS.removeFirst();
        }
        RECENT_PARTS.addLast(new HiddenPart(display.getX(), display.getY(), display.getZ(), now));
    }

    private static boolean nearHiddenEffect(Display.ItemDisplay display) {
        long now = System.currentTimeMillis();
        while (!RECENT_PARTS.isEmpty() && now - ServerKillEffectFilter.RECENT_PARTS.peekFirst().time > 1200L) {
            RECENT_PARTS.removeFirst();
        }
        for (HiddenPart part : RECENT_PARTS) {
            double z;
            double y;
            double x = display.getX() - part.x;
            if (!(x * x + (y = display.getY() - part.y) * y + (z = display.getZ() - part.z) * z < 6.25)) continue;
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

