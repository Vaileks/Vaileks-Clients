/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_124
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 *  net.minecraft.class_634
 *  net.minecraft.class_7469
 */
package ru.bloodhelper.client.modules;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_634;
import net.minecraft.class_7469;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.compat.ExternalChatPatches;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.modules.ChatHistory;
import ru.bloodhelper.client.modules.ChatHistoryScreen;
import ru.bloodhelper.client.modules.ChatMentionsModule;
import ru.bloodhelper.client.modules.ChatTimestamps;
import ru.bloodhelper.client.modules.CoordinateLinksModule;
import ru.bloodhelper.client.modules.HeadsModule;

public final class ChatTools {
    private static final ChatHistory<class_2561> HISTORY = new ChatHistory(500);
    private static final ArrayDeque<WeakReference<class_5250>> TIMESTAMPS = new ArrayDeque();
    private static int timestampColor = -1;
    private static Object connection;
    private static UUID account;
    private static long nicknameGeneration;
    private static boolean nicknameMode;
    private static boolean quickMode;
    private static boolean staffActive;
    private static boolean headsMode;
    private static boolean chatHeadsMode;
    private static Object nicknameConnection;

    private ChatTools() {
    }

    public static long nicknameGeneration() {
        ClientConfig cfg = BloodHelperClient.config();
        boolean copy = cfg.nicknameCopy;
        boolean quick = cfg.sanctions.enabled && cfg.sanctions.quickSanction;
        boolean active = StaffAccess.active();
        class_634 connection = class_310.method_1551().method_1562();
        if (cfg.heads.enabled != headsMode || cfg.heads.chat != chatHeadsMode || copy != nicknameMode || quick != quickMode || active != staffActive || connection != nicknameConnection) {
            headsMode = cfg.heads.enabled;
            chatHeadsMode = cfg.heads.chat;
            nicknameMode = copy;
            quickMode = quick;
            staffActive = active;
            nicknameConnection = connection;
            ++nicknameGeneration;
        }
        return nicknameGeneration;
    }

    public static void tick(class_310 client) {
        ChatTools.ensureSession(client);
        ChatTools.refreshTimestampColors();
        ChatTools.nicknameGeneration();
    }

    public static void refreshTimestampColors() {
        int color = ThemeColors.timestamp();
        if (color == timestampColor) {
            return;
        }
        timestampColor = color;
        boolean changed = false;
        Iterator<WeakReference<class_5250>> iterator = TIMESTAMPS.iterator();
        while (iterator.hasNext()) {
            class_5250 prefix = (class_5250)iterator.next().get();
            if (prefix == null) {
                iterator.remove();
                continue;
            }
            prefix.method_54663(color);
            changed = true;
        }
        if (changed) {
            class_310.method_1551().field_1705.method_1743().method_1817();
        }
    }

    private static boolean ensureSession(class_310 client) {
        UUID nextAccount;
        class_634 next = client.method_1562();
        UUID uUID = nextAccount = next == null ? null : next.method_2879().id();
        if (next != connection || !Objects.equals(nextAccount, account)) {
            if (!ExternalChatPatches.loaded() || account != null && nextAccount != null && !Objects.equals(nextAccount, account)) {
                HISTORY.clear();
            }
            connection = next;
            if (nextAccount != null || !ExternalChatPatches.loaded()) {
                account = nextAccount;
            }
        }
        HISTORY.setCapacity(BloodHelperClient.config().chatTools.maxMessages);
        return connection != null;
    }

    public static class_2561 receiveForDisplay(class_2561 original, class_7469 signature, Object tag) {
        Object e;
        ChatTimestamps.Saved saved = ChatTimestamps.read(original);
        boolean external = ExternalChatPatches.loaded() || saved != null && saved.chatPatches();
        boolean restored = ExternalChatPatches.restoring(tag);
        class_2561 body = saved == null ? original : saved.body();
        UnaryOperator decorate = part -> HeadsModule.decorate(CoordinateLinksModule.receiveForDisplay(restored ? StaffAccess.decorateNicknames(part) : StaffAccess.display(ChatMentionsModule.receiveForDisplay(part, signature), signature)), part);
        class_2561 display = saved == null ? (class_2561)decorate.apply(body) : saved.mapBody(decorate);
        boolean connected = ChatTools.ensureSession(class_310.method_1551());
        if (!connected && !external || !BloodHelperClient.config().chatTools.enabled) {
            return display;
        }
        Instant received = ChatTimestamps.receivedAt(saved, external, restored, Instant::now);
        HISTORY.add((class_2561)body.method_27661(), class_124.method_539((String)body.getString()), signature, received);
        class_2561 withClock = ChatTimestamps.showSavedClock(display, saved, BloodHelperClient.config().chatTools.timestamps, ThemeColors.timestamp());
        if (withClock != display && (e = withClock.method_10855().getFirst()) instanceof class_5250) {
            class_5250 clock = (class_5250)e;
            TIMESTAMPS.addLast(new WeakReference<class_5250>(clock));
            while (TIMESTAMPS.size() > 4096) {
                TIMESTAMPS.removeFirst();
            }
        }
        display = withClock;
        if (saved != null || external || received == null || !BloodHelperClient.config().chatTools.timestamps) {
            return display;
        }
        class_5250 prefix = ChatTimestamps.prefix(received, ThemeColors.timestamp());
        TIMESTAMPS.addLast(new WeakReference<class_5250>(prefix));
        while (TIMESTAMPS.size() > 4096) {
            TIMESTAMPS.removeFirst();
        }
        return class_2561.method_43473().method_10852((class_2561)prefix).method_10852((class_2561)display.method_27661());
    }

    public static class_2561 nicknameLinks(class_2561 original) {
        ChatTimestamps.Saved saved = ChatTimestamps.read(original);
        return saved == null ? ChatTools.nicknameBody(original) : saved.mapBody(ChatTools::nicknameBody);
    }

    private static class_2561 nicknameBody(class_2561 original) {
        class_2561 body = HeadsModule.withoutOwnHead(original);
        return HeadsModule.decorate(StaffAccess.decorateNicknames(body), original);
    }

    public static String time(Instant instant) {
        return ChatTimestamps.time(instant);
    }

    public static List<ChatHistory.Row<class_2561>> rows(String query) {
        ChatTools.ensureSession(class_310.method_1551());
        return HISTORY.rows(query, ExternalChatPatches.loaded() ? ExternalChatPatches.option("counter", BloodHelperClient.config().chatTools.groupDuplicates) : BloodHelperClient.config().chatTools.groupDuplicates);
    }

    public static int size() {
        return HISTORY.size();
    }

    public static void delete(class_7469 signature) {
        HISTORY.delete(signature);
    }

    public static void clear() {
        HISTORY.clear();
    }

    public static void openScreen(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new ChatHistoryScreen(parent));
    }
}

