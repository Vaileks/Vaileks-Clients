/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.StreamerSettings;

public final class StreamerMode {
    private static KeyMapping toggle;

    private StreamerMode() {
    }

    public static void register(KeyMapping.Category category) {
        toggle = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.streamer_toggle", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
    }

    public static void tick(Minecraft client) {
        boolean pressed = false;
        if (toggle != null) {
            while (toggle.consumeClick()) {
                pressed = true;
            }
        }
        if (!pressed || client.player == null || client.gui.screen() != null) {
            return;
        }
        StreamerMode.settings().enabled = !StreamerMode.settings().enabled;
        BloodHelperClient.saveConfig();
    }

    public static String keyName() {
        return toggle == null ? "None" : toggle.getTranslatedKeyMessage().getString();
    }

    private static StreamerSettings settings() {
        return BloodHelperClient.config().streamer;
    }

    public static boolean enabled() {
        return StreamerMode.settings().enabled;
    }

    private static StreamerSettings privacySettings() {
        return ConfigStore.get().streamer;
    }

    public static boolean hideChat() {
        return StreamerMode.privacySettings().hidesChat();
    }

    public static boolean hideCoordinates() {
        return StreamerMode.privacySettings().hidesCoordinates();
    }

    public static boolean hideServerAddress() {
        return StreamerMode.privacySettings().hidesServerAddress();
    }

    public static boolean hideNickname() {
        return StreamerMode.privacySettings().hidesNickname();
    }

    public static boolean hideServerOverlays() {
        return StreamerMode.privacySettings().hidesServerOverlays();
    }

    public static boolean hideNametags() {
        return StreamerMode.privacySettings().hidesNametags();
    }

    public static boolean hideDebug() {
        return StreamerMode.privacySettings().hidesDebug();
    }

    public static String displayName(String original) {
        return StreamerMode.privacySettings().displayName(original);
    }
}

