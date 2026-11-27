/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
 *  net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  org.slf4j.LoggerFactory
 */
package ru.bloodhelper.client.compat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.slf4j.LoggerFactory;
import ru.bloodhelper.client.gui.BloodHelperScreen;

public final class ClientDistribution {
    private static boolean pendingMenu;

    private ClientDistribution() {
    }

    public static boolean lunarClient() {
        return true;
    }

    public static int defaultMenuKey() {
        return ClientDistribution.lunarClient() ? 260 : 344;
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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register((LiteralArgumentBuilder)ClientCommandManager.literal((String)"bloodhelper").executes(context -> {
            pendingMenu = true;
            return 1;
        })));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!pendingMenu) {
                return;
            }
            pendingMenu = false;
            if (client.field_1724 != null && client.field_1687 != null && client.method_18506() == null && (client.field_1755 == null || client.field_1755 instanceof class_408)) {
                client.method_1507((class_437)new BloodHelperScreen(null));
            }
        });
        LoggerFactory.getLogger((String)"BloodHelper/Lunar").info("Lunar distribution: independent Insert menu binding and local /bloodhelper command enabled");
    }
}

