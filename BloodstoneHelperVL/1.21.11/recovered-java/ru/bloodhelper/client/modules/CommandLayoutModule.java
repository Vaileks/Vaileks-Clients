/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.tree.CommandNode
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 */
package ru.bloodhelper.client.modules;

import com.mojang.brigadier.tree.CommandNode;
import java.util.HashSet;
import net.minecraft.class_310;
import net.minecraft.class_634;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.CommandLayout;

public final class CommandLayoutModule {
    private CommandLayoutModule() {
    }

    public static String correct(String message) {
        if (!BloodHelperClient.config().commandLayout || !BloodHelperClient.config().serverAllowsModule("commandLayout")) {
            return message;
        }
        class_634 connection = class_310.method_1551().method_1562();
        if (connection == null) {
            return message;
        }
        HashSet<String> known = new HashSet<String>();
        for (CommandNode command : connection.method_2886().getRoot().getChildren()) {
            known.add(command.getName());
        }
        return CommandLayout.correct(message, known);
    }
}

