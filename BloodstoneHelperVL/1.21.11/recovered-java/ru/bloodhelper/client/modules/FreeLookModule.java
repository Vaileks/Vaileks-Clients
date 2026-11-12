/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_5498
 */
package ru.bloodhelper.client.modules;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_5498;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.compat.OptionalMixinHooks;
import ru.bloodhelper.client.config.PlayerDisplaySettings;

public final class FreeLookModule {
    private static class_304 binding;
    private static class_5498 previous;
    private static boolean active;
    private static boolean ignoreHeldKey;
    private static float yaw;
    private static float pitch;

    private FreeLookModule() {
    }

    public static void register(class_304.class_11900 category) {
        binding = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.freeLook", class_3675.class_307.field_1668, 86, category));
    }

    public static void tick(class_310 mc) {
        boolean requested;
        PlayerDisplaySettings.FreeLook s = BloodHelperClient.config().freeLook;
        boolean press = false;
        if (binding != null) {
            while (binding.method_1436()) {
                press = !press;
            }
        }
        if (!OptionalMixinHooks.freeLook() || !s.enabled || mc.field_1724 == null || mc.method_1560() != mc.field_1724 || mc.field_1687 == null || mc.field_1755 != null || mc.method_18506() != null || !mc.method_1569() || !mc.field_1729.method_1613() || mc.field_1724.method_29504() || mc.field_1724.method_6113()) {
            FreeLookModule.reset();
            return;
        }
        if (active && mc.field_1690.method_31044() != class_5498.field_26665) {
            active = false;
            previous = null;
            ignoreHeldKey = !s.toggle;
            return;
        }
        if (ignoreHeldKey) {
            if (!s.toggle && binding != null && binding.method_1434()) {
                return;
            }
            ignoreHeldKey = false;
        }
        boolean bl = s.toggle ? (press ? !active : active) : (requested = binding != null && binding.method_1434());
        if (requested && !active) {
            previous = mc.field_1690.method_31044();
            yaw = mc.field_1724.method_36454();
            pitch = mc.field_1724.method_36455();
            mc.field_1690.method_31043(class_5498.field_26665);
            active = true;
        } else if (!requested) {
            FreeLookModule.reset();
        }
    }

    public static boolean turn(double dx, double dy) {
        FreeLookModule.tick(class_310.method_1551());
        if (!active) {
            return false;
        }
        double sensitivity = BloodHelperClient.config().freeLook.sensitivity;
        yaw = (float)(((double)yaw + dx * 0.15 * sensitivity) % 360.0);
        pitch = (float)Math.clamp((double)pitch + dy * 0.15 * sensitivity, -90.0, 90.0);
        return true;
    }

    public static boolean active() {
        return active && BloodHelperClient.config().freeLook.enabled && class_310.method_1551().field_1690.method_31044() == class_5498.field_26665;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static String keyName() {
        return binding == null ? "V" : binding.method_16007().getString();
    }

    public static void reset() {
        if (active && previous != null && class_310.method_1551().field_1690.method_31044() == class_5498.field_26665) {
            class_310.method_1551().field_1690.method_31043(previous);
        }
        active = false;
        previous = null;
        ignoreHeldKey = false;
    }
}

