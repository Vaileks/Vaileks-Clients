/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.tree.CommandNode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 */
package ru.bloodhelper.client.modules;

import com.mojang.brigadier.tree.CommandNode;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.CommandLayout;

public final class CommandLayoutModule {
    private CommandLayoutModule() {
    }

    public static String correct(String message) {
        if (!BloodHelperClient.config().commandLayout || !BloodHelperClient.config().serverAllowsModule("commandLayout")) {
            return message;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return message;
        }
        HashSet<String> known = new HashSet<String>();
        for (CommandNode command : connection.getCommands().getRoot().getChildren()) {
            known.add(command.getName());
        }
        return CommandLayout.correct(message, known);
    }
}

