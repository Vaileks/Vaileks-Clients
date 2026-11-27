/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_11908
 *  net.minecraft.class_11909
 *  net.minecraft.class_11910
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675$class_306
 *  net.minecraft.class_3675$class_307
 */
package ru.bloodhelper.client.modules;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ZoomSettings;
import ru.bloodhelper.client.modules.ZoomState;

public final class ZoomModule {
    private static final ZoomState STATE = new ZoomState();
    private static class_304 binding;
    private static int pendingPresses;
    private static class_3675.class_306 observedKey;
    private static ZoomSettings observedSettings;
    private static boolean physicalHeld;
    private static boolean capturedMouse;
    private static boolean capturedKey;

    private ZoomModule() {
    }

    public static void register(class_304.class_11900 category) {
        binding = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.zoom", class_3675.class_307.field_1672, 2, category));
    }

    public static void tick(class_310 client) {
        ZoomModule.update(client);
    }

    private static void update(class_310 client) {
        boolean guiRelease;
        ZoomModule.syncBinding();
        ClientConfig config = BloodHelperClient.config();
        if (observedSettings != config.zoom) {
            observedSettings = config.zoom;
            STATE.reset();
        }
        STATE.configure(config.zoom);
        if (binding != null) {
            while (binding.method_1436()) {
            }
        }
        int presses = pendingPresses;
        pendingPresses = 0;
        boolean available = ZoomModule.available(client);
        boolean bl = guiRelease = config.zoom.enabled && binding != null && client.field_1687 != null && client.field_1724 != null && !client.field_1724.method_29504() && client.field_1755 != null && client.method_1569();
        if (guiRelease) {
            STATE.suspend(physicalHeld, config.zoom.toggle);
        } else {
            STATE.input(available, physicalHeld, presses, config.zoom.toggle);
        }
        if (available || guiRelease) {
            STATE.advance(System.nanoTime());
        }
    }

    private static boolean available(class_310 client) {
        return BloodHelperClient.config().zoom.enabled && binding != null && client.field_1687 != null && client.field_1724 != null && !client.field_1724.method_29504() && client.field_1755 == null && client.method_18506() == null && client.method_1569() && client.field_1729.method_1613();
    }

    public static boolean claimsBinding(class_310 client, class_304 candidate) {
        return ZoomModule.available(client) && candidate != null && KeyBindingHelper.getBoundKeyOf((class_304)binding).equals((Object)KeyBindingHelper.getBoundKeyOf((class_304)candidate));
    }

    public static boolean keyAction(class_310 client, long window, int action, class_11908 event) {
        if (binding == null || window != client.method_22683().method_4490()) {
            return false;
        }
        ZoomModule.syncBinding();
        if (!binding.method_1417(event)) {
            return false;
        }
        if (action == 0) {
            physicalHeld = false;
            boolean captured = capturedKey;
            capturedKey = false;
            ZoomModule.update(client);
            return captured;
        }
        if (action == 2) {
            return capturedKey && ZoomModule.available(client);
        }
        if (action != 1) {
            return false;
        }
        physicalHeld = true;
        capturedKey = ZoomModule.available(client);
        if (capturedKey) {
            ++pendingPresses;
        }
        ZoomModule.update(client);
        return capturedKey;
    }

    public static boolean mouseAction(class_310 client, long window, class_11910 info, int action) {
        if (binding == null || window != client.method_22683().method_4490()) {
            return false;
        }
        ZoomModule.syncBinding();
        if (!binding.method_1433(new class_11909(0.0, 0.0, info))) {
            return false;
        }
        if (action == 0) {
            physicalHeld = false;
            boolean captured = capturedMouse;
            capturedMouse = false;
            ZoomModule.update(client);
            return captured;
        }
        if (action != 1) {
            return false;
        }
        physicalHeld = true;
        capturedMouse = ZoomModule.available(client);
        if (capturedMouse) {
            ++pendingPresses;
        }
        ZoomModule.update(client);
        return capturedMouse;
    }

    public static boolean scroll(class_310 client, long window, double vertical) {
        if (window != client.method_22683().method_4490() || !ZoomModule.available(client) || !Double.isFinite(vertical) || vertical == 0.0) {
            return false;
        }
        ZoomModule.update(client);
        double delta = ((Boolean)client.field_1690.method_42439().method_41753() != false ? Math.signum(vertical) : vertical) * (Double)client.field_1690.method_41806().method_41753();
        boolean consumed = STATE.scroll(delta);
        if (consumed) {
            STATE.advance(System.nanoTime());
        }
        return consumed;
    }

    private static void syncBinding() {
        class_3675.class_306 current;
        class_3675.class_306 class_3062 = current = binding == null ? null : KeyBindingHelper.getBoundKeyOf((class_304)binding);
        if (current != observedKey) {
            observedKey = current;
            capturedKey = false;
            capturedMouse = false;
            physicalHeld = false;
            pendingPresses = 0;
            STATE.reset();
        }
    }

    public static float scaleFov(float originalFov) {
        if (!BloodHelperClient.config().zoom.enabled) {
            ZoomModule.resetDisabledState();
            return originalFov;
        }
        ZoomModule.update(class_310.method_1551());
        return STATE.fov(originalFov);
    }

    public static double bobbingScale() {
        ZoomSettings settings = BloodHelperClient.config().zoom;
        if (!settings.enabled || !settings.relativeViewBobbing) {
            return 1.0;
        }
        ZoomModule.update(class_310.method_1551());
        return 1.0 / (1.0 + 0.2 * (STATE.divisor() - 1.0));
    }

    public static double sensitivityScale() {
        if (!BloodHelperClient.config().zoom.enabled) {
            ZoomModule.resetDisabledState();
            return 1.0;
        }
        ZoomModule.update(class_310.method_1551());
        return STATE.sensitivity();
    }

    private static void resetDisabledState() {
        if (STATE.zooming() || STATE.amount() != 0.0 || pendingPresses != 0) {
            STATE.input(false, physicalHeld, 0, BloodHelperClient.config().zoom.toggle);
            pendingPresses = 0;
        }
    }

    public static void reset() {
        STATE.reset();
        capturedKey = false;
        capturedMouse = false;
        physicalHeld = false;
        pendingPresses = 0;
    }

    public static String keyName() {
        return binding == null ? "MMB" : binding.method_16007().getString();
    }
}

