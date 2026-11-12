/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Minecraft;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.ChatDraftState;

public final class ChatDraftsModule {
    private static final ChatDraftState STATE = new ChatDraftState();

    private ChatDraftsModule() {
    }

    public static boolean active() {
        Minecraft client = Minecraft.getInstance();
        return BloodHelperClient.config().chatDrafts.enabled && client.getConnection() != null && client.player != null && client.level != null;
    }

    public static void tick(Minecraft client) {
        STATE.update(client.getConnection(), BloodHelperClient.config().chatDrafts, ChatDraftsModule.active());
    }

    public static ChatDraftState.Draft restore(String initial) {
        ChatDraftsModule.tick(Minecraft.getInstance());
        return STATE.restore(initial);
    }

    public static void closed(Object owner, String text, int cursor, int anchor, boolean sent, boolean commandEntry) {
        ChatDraftsModule.tick(Minecraft.getInstance());
        STATE.closed(owner, text, cursor, anchor, sent, commandEntry);
    }

    public static void settingsChanged() {
        ChatDraftsModule.tick(Minecraft.getInstance());
    }

    public static void reset() {
        STATE.clear();
    }
}

