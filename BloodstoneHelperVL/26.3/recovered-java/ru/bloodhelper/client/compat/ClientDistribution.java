/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommands
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.compat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.gui.BloodHelperScreen;

public final class ClientDistribution {
    private static boolean pendingMenu;

    private ClientDistribution() {
    }

    public static boolean lunarClient() {
        return false;
    }

    public static int defaultMenuKey() {
        return ClientDistribution.lunarClient() ? 73 : 229;
    }

    public static String defaultMenuKeyName() {
        return ClientDistribution.lunarClient() ? "Insert" : "Right Shift";
    }

    public static String menuBindingId() {
        return ClientDistribution.lunarClient() ? "key.bloodhelper.menu_lunar" : "key.bloodhelper.menu";
    }

    public static String displaySuffix() {
        return ClientDistribution.lunarClient() ? " \u00b7 Lunar" : "";
    }

    public static void initialize() {
        if (!ClientDistribution.lunarClient()) {
            return;
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal((String)"bloodhelper").executes(context -> {
            pendingMenu = true;
            return 1;
        })));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!pendingMenu) {
                return;
            }
            pendingMenu = false;
            if (client.player != null && client.level != null && client.gui.overlay() == null && (client.gui.screen() == null || client.gui.screen() instanceof ChatScreen)) {
                client.gui.setScreen((Screen)new BloodHelperScreen(null));
            }
        });
        LoggerFactory.getLogger((String)"BloodHelper/Lunar").info("Lunar distribution: independent Insert menu binding and local /bloodhelper command enabled");
    }
}

