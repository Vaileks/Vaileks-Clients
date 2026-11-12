/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_307
 */
package ru.bloodhelper.client.modules;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.StreamerSettings;

public final class StreamerMode {
    private static class_304 toggle;

    private StreamerMode() {
    }

    public static void register(class_304.class_11900 category) {
        toggle = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.streamer_toggle", class_3675.class_307.field_1668, class_3675.field_16237.method_1444(), category));
    }

    public static void tick(class_310 client) {
        boolean pressed = false;
        if (toggle != null) {
            while (toggle.method_1436()) {
                pressed = true;
            }
        }
        if (!pressed || client.field_1724 == null || client.field_1755 != null) {
            return;
        }
        StreamerMode.settings().enabled = !StreamerMode.settings().enabled;
        BloodHelperClient.saveConfig();
    }

    public static String keyName() {
        return toggle == null ? "None" : toggle.method_16007().getString();
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

