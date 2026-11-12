/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.input.MouseButtonInfo
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ZoomSettings;
import ru.bloodhelper.client.modules.ZoomState;

public final class ZoomModule {
    private static final ZoomState STATE = new ZoomState();
    private static KeyMapping binding;
    private static int pendingPresses;
    private static InputConstants.Key observedKey;
    private static ZoomSettings observedSettings;
    private static boolean physicalHeld;
    private static boolean capturedMouse;
    private static boolean capturedKey;

    private ZoomModule() {
    }

    public static void register(KeyMapping.Category category) {
        binding = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.zoom", InputConstants.Type.MOUSE, 2, category));
    }

    public static void tick(Minecraft client) {
        ZoomModule.update(client);
    }

    private static void update(Minecraft client) {
        boolean guiRelease;
        ZoomModule.syncBinding();
        ClientConfig config = BloodHelperClient.config();
        if (observedSettings != config.zoom) {
            observedSettings = config.zoom;
            STATE.reset();
        }
        STATE.configure(config.zoom);
        if (binding != null) {
            while (binding.consumeClick()) {
            }
        }
        int presses = pendingPresses;
        pendingPresses = 0;
        boolean available = ZoomModule.available(client);
        boolean bl = guiRelease = config.zoom.enabled && binding != null && client.level != null && client.player != null && !client.player.isDeadOrDying() && client.gui.screen() != null && client.isWindowActive();
        if (guiRelease) {
            STATE.suspend(physicalHeld, config.zoom.toggle);
        } else {
            STATE.input(available, physicalHeld, presses, config.zoom.toggle);
        }
        if (available || guiRelease) {
            STATE.advance(System.nanoTime());
        }
    }

    private static boolean available(Minecraft client) {
        return BloodHelperClient.config().zoom.enabled && binding != null && client.level != null && client.player != null && !client.player.isDeadOrDying() && client.gui.screen() == null && client.gui.overlay() == null && client.isWindowActive() && client.mouseHandler.isMouseGrabbed();
    }

    public static boolean claimsBinding(Minecraft client, KeyMapping candidate) {
        return ZoomModule.available(client) && candidate != null && KeyMappingHelper.getBoundKeyOf((KeyMapping)binding).equals((Object)KeyMappingHelper.getBoundKeyOf((KeyMapping)candidate));
    }

    public static boolean keyAction(Minecraft client, long window, int action, KeyEvent event) {
        if (binding == null || window != client.getWindow().handle()) {
            return false;
        }
        ZoomModule.syncBinding();
        if (!binding.matches(event)) {
            return false;
        }
        if (action == 0) {
            physicalHeld = false;
            boolean captured = capturedKey;
            capturedKey = false;
            ZoomModule.update(client);
            return captured;
        }
        if (action == -1) {
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

    public static boolean mouseAction(Minecraft client, long window, MouseButtonInfo info, int action) {
        if (binding == null || window != client.getWindow().handle()) {
            return false;
        }
        ZoomModule.syncBinding();
        if (!binding.matchesMouse(new MouseButtonEvent(0.0, 0.0, info))) {
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

    public static boolean scroll(Minecraft client, long window, double vertical) {
        if (window != client.getWindow().handle() || !ZoomModule.available(client) || !Double.isFinite(vertical) || vertical == 0.0) {
            return false;
        }
        ZoomModule.update(client);
        double delta = ((Boolean)client.options.discreteMouseScroll().get() != false ? Math.signum(vertical) : vertical) * (Double)client.options.mouseWheelSensitivity().get();
        boolean consumed = STATE.scroll(delta);
        if (consumed) {
            STATE.advance(System.nanoTime());
        }
        return consumed;
    }

    private static void syncBinding() {
        InputConstants.Key current;
        InputConstants.Key key = current = binding == null ? null : KeyMappingHelper.getBoundKeyOf((KeyMapping)binding);
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
        ZoomModule.update(Minecraft.getInstance());
        return STATE.fov(originalFov);
    }

    public static double bobbingScale() {
        ZoomSettings settings = BloodHelperClient.config().zoom;
        if (!settings.enabled || !settings.relativeViewBobbing) {
            return 1.0;
        }
        ZoomModule.update(Minecraft.getInstance());
        return 1.0 / (1.0 + 0.2 * (STATE.divisor() - 1.0));
    }

    public static double sensitivityScale() {
        if (!BloodHelperClient.config().zoom.enabled) {
            ZoomModule.resetDisabledState();
            return 1.0;
        }
        ZoomModule.update(Minecraft.getInstance());
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
        return binding == null ? "MMB" : binding.getTranslatedKeyMessage().getString();
    }
}

