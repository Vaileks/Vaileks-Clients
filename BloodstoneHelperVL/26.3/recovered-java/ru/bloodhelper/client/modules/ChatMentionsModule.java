/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MessageSignature
 */
package ru.bloodhelper.client.modules;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.BackgroundAlertsSettings;
import ru.bloodhelper.client.config.ChatMentionsSettings;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.NotificationSoundSettings;
import ru.bloodhelper.client.gui.ThemeColors;
import ru.bloodhelper.client.hud.FeatureAlerts;
import ru.bloodhelper.client.hud.NotificationSounds;
import ru.bloodhelper.client.modules.BackgroundAlerts;
import ru.bloodhelper.client.modules.ChatMentionAlertGate;
import ru.bloodhelper.client.modules.ChatMentionHighlight;
import ru.bloodhelper.client.modules.ChatMentionMatcher;
import ru.bloodhelper.client.modules.StreamerMode;

public final class ChatMentionsModule {
    public static final String OWNER = "chatMentions";
    private static final ArrayDeque<Incoming> PENDING = new ArrayDeque();
    private static final ChatMentionAlertGate ALERT_GATE = new ChatMentionAlertGate();
    private static Object connection;
    private static ClientConfig configuration;
    private static ChatMentionsSettings configurationSettings;
    private static UUID account;
    private static boolean registered;

    private ChatMentionsModule() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientReceiveMessageEvents.CHAT.register((display, signed, sender, params, received) -> {
            boolean own;
            Minecraft client = Minecraft.getInstance();
            if (!ChatMentionsModule.active(client) && !ChatMentionsModule.backgroundMessages(client)) {
                return;
            }
            UUID local = client.getConnection().getLocalGameProfile().id();
            boolean bl = own = signed != null && local.equals(signed.sender()) || sender != null && local.equals(sender.id());
            if (signed == null && sender == null) {
                own = client.getUser().getName().equalsIgnoreCase(params.name().getString(64));
            }
            Component body = signed == null ? null : (signed.filterMask().isEmpty() ? signed.decoratedContent() : signed.filterMask().applyWithFormatting(signed.signedContent()));
            String bodyText = body == null ? null : ChatMentionsModule.boundedText(body);
            BackgroundAlerts.onMessage(client, display, bodyText, own);
            if (ChatMentionsModule.active(client)) {
                ChatMentionsModule.observe(client, display, signed == null ? null : signed.signature(), bodyText, own);
            }
        });
        ClientReceiveMessageEvents.GAME.register((display, overlay) -> {
            Minecraft client = Minecraft.getInstance();
            if (!overlay && (ChatMentionsModule.active(client) || ChatMentionsModule.backgroundMessages(client))) {
                BackgroundAlerts.onMessage(client, display, null, false);
                if (ChatMentionsModule.active(client)) {
                    ChatMentionsModule.observe(client, display, null, null, false);
                }
            }
        });
    }

    private static void observe(Minecraft client, Component display, MessageSignature signature, String body, boolean own) {
        ChatMentionsModule.ensureSession(client);
        String text = ChatMentionsModule.boundedText(display);
        if (text == null) {
            return;
        }
        PENDING.addLast(new Incoming(display, text, signature, body, own, ChatMentionsModule.now()));
        while (PENDING.size() > 32) {
            PENDING.removeFirst();
        }
    }

    public static Component receiveForDisplay(Component original, MessageSignature signature) {
        Minecraft client = Minecraft.getInstance();
        if (!ChatMentionsModule.active(client)) {
            return original;
        }
        ChatMentionsModule.ensureSession(client);
        String text = ChatMentionsModule.boundedText(original);
        if (text == null) {
            return original;
        }
        Incoming incoming = ChatMentionsModule.take(original, text, signature);
        if (incoming == null || incoming.own) {
            return original;
        }
        String name = client.getUser().getName();
        int from = 0;
        int to = text.length();
        if (incoming.body != null) {
            if (incoming.body.isEmpty()) {
                return original;
            }
            from = text.lastIndexOf(incoming.body);
            if (from < 0) {
                return original;
            }
            to = from + incoming.body.length();
        } else {
            from = ChatMentionMatcher.systemBodyStart(text);
            if (ChatMentionMatcher.systemIsOwnMessage(text, name, from)) {
                return original;
            }
        }
        List<ChatMentionMatcher.Range> matches = ChatMentionMatcher.find(text, name, from, to);
        if (matches.isEmpty()) {
            return original;
        }
        ChatMentionsSettings settings = ChatMentionsModule.settings();
        if ((settings.notification || settings.sound) && ALERT_GATE.acquire(ChatMentionsModule.now(), settings.cooldownSeconds)) {
            if (settings.notification) {
                boolean ru = "ru".equals(BloodHelperClient.config().language);
                FeatureAlerts.push(OWNER, "mention", ru ? "\u0423\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0435" : "Chat mention", ru ? "\u0412\u0430\u0441 \u0443\u043f\u043e\u043c\u044f\u043d\u0443\u043b\u0438 \u0432 \u0447\u0430\u0442\u0435" : "Someone mentioned you in chat", "chat");
            }
            if (settings.sound && settings.soundVolume > 0.0) {
                NotificationSounds.play(client, NotificationSoundSettings.Event.CHAT_MENTION, 1.3f, (float)Math.clamp(settings.soundVolume, 0.0, 0.5));
            }
        }
        return settings.highlight ? ChatMentionHighlight.apply(original, matches, ThemeColors.timestamp()) : original;
    }

    private static Incoming take(Component component, String display, MessageSignature signature) {
        long now = ChatMentionsModule.now();
        Iterator<Incoming> iterator = PENDING.iterator();
        while (iterator.hasNext()) {
            Incoming item = iterator.next();
            if (now - item.receivedAt > 1000L) {
                iterator.remove();
                continue;
            }
            if (item.component != component && (!Objects.equals(item.signature, signature) || !item.display.equals(display))) continue;
            iterator.remove();
            return item;
        }
        return null;
    }

    public static void tick(Minecraft client) {
        if (!ChatMentionsModule.active(client)) {
            if (connection != null || !PENDING.isEmpty()) {
                ChatMentionsModule.reset();
            }
            return;
        }
        ChatMentionsModule.ensureSession(client);
        long cutoff = ChatMentionsModule.now() - 1000L;
        while (!PENDING.isEmpty() && ChatMentionsModule.PENDING.getFirst().receivedAt < cutoff) {
            PENDING.removeFirst();
        }
    }

    private static boolean backgroundMessages(Minecraft client) {
        BackgroundAlertsSettings settings = BloodHelperClient.config().backgroundAlerts;
        return settings.enabled && settings.mentions && !client.isWindowActive() && client.player != null && client.level != null && client.getConnection() != null;
    }

    private static boolean active(Minecraft client) {
        return ChatMentionsModule.settings().enabled && !StreamerMode.hideChat() && client.player != null && client.level != null && client.getConnection() != null;
    }

    private static void ensureSession(Minecraft client) {
        UUID nextAccount;
        ClientPacketListener next = client.getConnection();
        UUID uUID = nextAccount = next == null ? null : next.getLocalGameProfile().id();
        if (connection != next || !Objects.equals(account, nextAccount) || configuration != BloodHelperClient.config() || configurationSettings != ChatMentionsModule.settings()) {
            ChatMentionsModule.reset();
            connection = next;
            configuration = BloodHelperClient.config();
            configurationSettings = ChatMentionsModule.settings();
            account = nextAccount;
        }
    }

    public static void reset() {
        connection = null;
        configuration = null;
        configurationSettings = null;
        account = null;
        PENDING.clear();
        ALERT_GATE.clear();
        FeatureAlerts.clearOwner(OWNER);
    }

    private static ChatMentionsSettings settings() {
        return BloodHelperClient.config().chatMentions;
    }

    private static long now() {
        return System.nanoTime() / 1000000L;
    }

    private static String boundedText(Component component) {
        String text = component.getString(8193);
        return text.length() > 8192 ? null : text;
    }

    private record Incoming(Component component, String display, MessageSignature signature, String body, boolean own, long receivedAt) {
    }
}

