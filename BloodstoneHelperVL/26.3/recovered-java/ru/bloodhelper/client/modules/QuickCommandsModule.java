/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.input.MouseButtonInfo
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.QuickCommandsEditorScreen;
import ru.bloodhelper.client.modules.QuickCommandsScreen;
import ru.bloodhelper.client.modules.ZoomModule;

public final class QuickCommandsModule {
    private static KeyMapping binding;

    private QuickCommandsModule() {
    }

    public static void register(KeyMapping.Category category) {
        binding = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.quick_commands", InputConstants.Type.MOUSE, 2, category));
    }

    public static void tick(Minecraft client) {
        boolean pressed = false;
        if (binding != null) {
            while (binding.consumeClick()) {
                pressed = true;
            }
        }
        if (pressed && QuickCommandsModule.canOpen(client)) {
            QuickCommandsModule.open(client);
        }
    }

    private static boolean canOpen(Minecraft client) {
        return binding != null && BloodHelperClient.config().quickCommands.enabled && client.player != null && client.getConnection() != null && client.gui.screen() == null && client.gui.overlay() == null && !ZoomModule.claimsBinding(client, binding);
    }

    public static boolean mousePress(Minecraft client, long window, MouseButtonInfo info, int action) {
        if (action != 1 || window != client.getWindow().handle() || !QuickCommandsModule.canOpen(client)) {
            return false;
        }
        if (!binding.matchesMouse(new MouseButtonEvent(0.0, 0.0, info))) {
            return false;
        }
        QuickCommandsModule.open(client);
        return true;
    }

    public static boolean keyPress(Minecraft client, long window, int action, KeyEvent event) {
        if (action != 1 || window != client.getWindow().handle() || !QuickCommandsModule.canOpen(client) || !binding.matches(event)) {
            return false;
        }
        QuickCommandsModule.open(client);
        return true;
    }

    private static void open(Minecraft client) {
        client.gui.setScreen((Screen)new QuickCommandsScreen(BloodHelperClient.config().quickCommands));
    }

    public static void openEditor(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new QuickCommandsEditorScreen(parent));
    }

    public static String bindingName() {
        return binding == null ? "MMB" : binding.getTranslatedKeyMessage().getString();
    }

    public static String keyName() {
        return QuickCommandsModule.bindingName();
    }
}

