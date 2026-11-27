/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_310;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.modules.ChatDraftState;

public final class ChatDraftsModule {
    private static final ChatDraftState STATE = new ChatDraftState();

    private ChatDraftsModule() {
    }

    public static boolean active() {
        class_310 client = class_310.method_1551();
        return BloodHelperClient.config().chatDrafts.enabled && client.method_1562() != null && client.field_1724 != null && client.field_1687 != null;
    }

    public static void tick(class_310 client) {
        STATE.update(client.method_1562(), BloodHelperClient.config().chatDrafts, ChatDraftsModule.active());
    }

    public static ChatDraftState.Draft restore(String initial) {
        ChatDraftsModule.tick(class_310.method_1551());
        return STATE.restore(initial);
    }

    public static void closed(Object owner, String text, int cursor, int anchor, boolean sent, boolean commandEntry) {
        ChatDraftsModule.tick(class_310.method_1551());
        STATE.closed(owner, text, cursor, anchor, sent, commandEntry);
    }

    public static void settingsChanged() {
        ChatDraftsModule.tick(class_310.method_1551());
    }

    public static void reset() {
        STATE.clear();
    }
}

