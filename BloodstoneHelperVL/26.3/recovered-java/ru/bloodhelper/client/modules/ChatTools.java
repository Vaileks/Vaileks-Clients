/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
 *  net.minecraft.network.chat.MutableComponent
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
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
    private static final ChatHistory<Component> HISTORY = new ChatHistory(500);
    private static final ArrayDeque<WeakReference<MutableComponent>> TIMESTAMPS = new ArrayDeque();
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
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
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

    public static void tick(Minecraft client) {
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
        Iterator<WeakReference<MutableComponent>> iterator = TIMESTAMPS.iterator();
        while (iterator.hasNext()) {
            MutableComponent prefix = (MutableComponent)iterator.next().get();
            if (prefix == null) {
                iterator.remove();
                continue;
            }
            prefix.withColor(color);
            changed = true;
        }
        if (changed) {
            Minecraft.getInstance().gui.hud.getChat().rescaleChat();
        }
    }

    private static boolean ensureSession(Minecraft client) {
        UUID nextAccount;
        ClientPacketListener next = client.getConnection();
        UUID uUID = nextAccount = next == null ? null : next.getLocalGameProfile().id();
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

    public static Component receiveForDisplay(Component original, MessageSignature signature, Object tag) {
        Object e;
        ChatTimestamps.Saved saved = ChatTimestamps.read(original);
        boolean external = ExternalChatPatches.loaded() || saved != null && saved.chatPatches();
        boolean restored = ExternalChatPatches.restoring(tag);
        Component body = saved == null ? original : saved.body();
        UnaryOperator decorate = part -> HeadsModule.decorate(CoordinateLinksModule.receiveForDisplay(restored ? StaffAccess.decorateNicknames(part) : StaffAccess.display(ChatMentionsModule.receiveForDisplay(part, signature), signature)), part);
        Component display = saved == null ? (Component)decorate.apply(body) : saved.mapBody(decorate);
        boolean connected = ChatTools.ensureSession(Minecraft.getInstance());
        if (!connected && !external || !BloodHelperClient.config().chatTools.enabled) {
            return display;
        }
        Instant received = ChatTimestamps.receivedAt(saved, external, restored, Instant::now);
        HISTORY.add((Component)body.copy(), ChatFormatting.stripFormatting((String)body.getString()), signature, received);
        Component withClock = ChatTimestamps.showSavedClock(display, saved, BloodHelperClient.config().chatTools.timestamps, ThemeColors.timestamp());
        if (withClock != display && (e = withClock.getSiblings().getFirst()) instanceof MutableComponent) {
            MutableComponent clock = (MutableComponent)e;
            TIMESTAMPS.addLast(new WeakReference<MutableComponent>(clock));
            while (TIMESTAMPS.size() > 4096) {
                TIMESTAMPS.removeFirst();
            }
        }
        display = withClock;
        if (saved != null || external || received == null || !BloodHelperClient.config().chatTools.timestamps) {
            return display;
        }
        MutableComponent prefix = ChatTimestamps.prefix(received, ThemeColors.timestamp());
        TIMESTAMPS.addLast(new WeakReference<MutableComponent>(prefix));
        while (TIMESTAMPS.size() > 4096) {
            TIMESTAMPS.removeFirst();
        }
        return Component.empty().append((Component)prefix).append((Component)display.copy());
    }

    public static Component nicknameLinks(Component original) {
        ChatTimestamps.Saved saved = ChatTimestamps.read(original);
        return saved == null ? ChatTools.nicknameBody(original) : saved.mapBody(ChatTools::nicknameBody);
    }

    private static Component nicknameBody(Component original) {
        Component body = HeadsModule.withoutOwnHead(original);
        return HeadsModule.decorate(StaffAccess.decorateNicknames(body), original);
    }

    public static String time(Instant instant) {
        return ChatTimestamps.time(instant);
    }

    public static List<ChatHistory.Row<Component>> rows(String query) {
        ChatTools.ensureSession(Minecraft.getInstance());
        return HISTORY.rows(query, ExternalChatPatches.loaded() ? ExternalChatPatches.option("counter", BloodHelperClient.config().chatTools.groupDuplicates) : BloodHelperClient.config().chatTools.groupDuplicates);
    }

    public static int size() {
        return HISTORY.size();
    }

    public static void delete(MessageSignature signature) {
        HISTORY.delete(signature);
    }

    public static void clear() {
        HISTORY.clear();
    }

    public static void openScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new ChatHistoryScreen(parent));
    }
}

