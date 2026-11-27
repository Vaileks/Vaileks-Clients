/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.CameraType
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.compat.OptionalMixinHooks;
import ru.bloodhelper.client.config.PlayerDisplaySettings;

public final class FreeLookModule {
    private static KeyMapping binding;
    private static CameraType previous;
    private static boolean active;
    private static boolean ignoreHeldKey;
    private static float yaw;
    private static float pitch;

    private FreeLookModule() {
    }

    public static void register(KeyMapping.Category category) {
        binding = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.freeLook", InputConstants.Type.KEYBOARD, 25, category));
    }

    public static void tick(Minecraft mc) {
        boolean requested;
        PlayerDisplaySettings.FreeLook s = BloodHelperClient.config().freeLook;
        boolean press = false;
        if (binding != null) {
            while (binding.consumeClick()) {
                press = !press;
            }
        }
        if (!OptionalMixinHooks.freeLook() || !s.enabled || mc.player == null || mc.getCameraEntity() != mc.player || mc.level == null || mc.gui.screen() != null || mc.gui.overlay() != null || !mc.isWindowActive() || !mc.mouseHandler.isMouseGrabbed() || mc.player.isDeadOrDying() || mc.player.isSleeping()) {
            FreeLookModule.reset();
            return;
        }
        if (active && mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            active = false;
            previous = null;
            ignoreHeldKey = !s.toggle;
            return;
        }
        if (ignoreHeldKey) {
            if (!s.toggle && binding != null && binding.isDown()) {
                return;
            }
            ignoreHeldKey = false;
        }
        boolean bl = s.toggle ? (press ? !active : active) : (requested = binding != null && binding.isDown());
        if (requested && !active) {
            previous = mc.options.getCameraType();
            yaw = mc.player.getYRot();
            pitch = mc.player.getXRot();
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            active = true;
        } else if (!requested) {
            FreeLookModule.reset();
        }
    }

    public static boolean turn(double dx, double dy) {
        FreeLookModule.tick(Minecraft.getInstance());
        if (!active) {
            return false;
        }
        double sensitivity = BloodHelperClient.config().freeLook.sensitivity;
        yaw = (float)(((double)yaw + dx * 0.15 * sensitivity) % 360.0);
        pitch = (float)Math.clamp((double)pitch + dy * 0.15 * sensitivity, -90.0, 90.0);
        return true;
    }

    public static boolean active() {
        return active && BloodHelperClient.config().freeLook.enabled && Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK;
    }

    public static float yaw() {
        return yaw;
    }

    public static float pitch() {
        return pitch;
    }

    public static String keyName() {
        return binding == null ? "V" : binding.getTranslatedKeyMessage().getString();
    }

    public static void reset() {
        if (active && previous != null && Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            Minecraft.getInstance().options.setCameraType(previous);
        }
        active = false;
        previous = null;
        ignoreHeldKey = false;
    }
}

