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
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_437
 */
package ru.bloodhelper.client.modules;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_437;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.QuickCommandsEditorScreen;
import ru.bloodhelper.client.modules.QuickCommandsScreen;
import ru.bloodhelper.client.modules.ZoomModule;

public final class QuickCommandsModule {
    private static class_304 binding;

    private QuickCommandsModule() {
    }

    public static void register(class_304.class_11900 category) {
        binding = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.quick_commands", class_3675.class_307.field_1672, 2, category));
    }

    public static void tick(class_310 client) {
        boolean pressed = false;
        if (binding != null) {
            while (binding.method_1436()) {
                pressed = true;
            }
        }
        if (pressed && QuickCommandsModule.canOpen(client)) {
            QuickCommandsModule.open(client);
        }
    }

    private static boolean canOpen(class_310 client) {
        return binding != null && BloodHelperClient.config().quickCommands.enabled && client.field_1724 != null && client.method_1562() != null && client.field_1755 == null && client.method_18506() == null && !ZoomModule.claimsBinding(client, binding);
    }

    public static boolean mousePress(class_310 client, long window, class_11910 info, int action) {
        if (action != 1 || window != client.method_22683().method_4490() || !QuickCommandsModule.canOpen(client)) {
            return false;
        }
        if (!binding.method_1433(new class_11909(0.0, 0.0, info))) {
            return false;
        }
        QuickCommandsModule.open(client);
        return true;
    }

    public static boolean keyPress(class_310 client, long window, int action, class_11908 event) {
        if (action != 1 || window != client.method_22683().method_4490() || !QuickCommandsModule.canOpen(client) || !binding.method_1417(event)) {
            return false;
        }
        QuickCommandsModule.open(client);
        return true;
    }

    private static void open(class_310 client) {
        client.method_1507((class_437)new QuickCommandsScreen(BloodHelperClient.config().quickCommands));
    }

    public static void openEditor(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new QuickCommandsEditorScreen(parent));
    }

    public static String bindingName() {
        return binding == null ? "MMB" : binding.method_16007().getString();
    }

    public static String keyName() {
        return QuickCommandsModule.bindingName();
    }
}

