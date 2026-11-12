/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.DisconnectedScreen
 *  net.minecraft.client.gui.screens.TitleScreen
 *  net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.chat.Component
 *  org.lwjgl.sdl.SDLProperties
 *  org.lwjgl.sdl.SDLVideo
 */
package ru.bloodhelper.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDLVideo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.BackgroundAlertsSettings;
import ru.bloodhelper.client.hud.NotificationInbox;
import ru.bloodhelper.client.modules.BackgroundAlertPolicy;
import ru.bloodhelper.client.modules.ChatMentionMatcher;
import ru.bloodhelper.client.modules.CombatStateModule;
import ru.bloodhelper.client.notifications.DesktopNotification;
import ru.bloodhelper.client.notifications.DesktopNotificationService;

public final class BackgroundAlerts {
    private static final BackgroundAlertPolicy POLICY = new BackgroundAlertPolicy();
    private static DesktopNotificationService DESKTOP = new DesktopNotificationService();
    private static Object connection;
    private static Object configuration;
    private static Object disconnectingConnection;
    private static BackgroundAlertsSettings previousSettings;
    private static boolean enabled;
    private static long session;
    private static long pendingDisconnectUntil;
    private static long sdlWindow;
    private static long nativeWindow;

    private BackgroundAlerts() {
    }

    public static void tick(Minecraft client) {
        if (client == null) {
            return;
        }
        BackgroundAlerts.prepare(client);
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        if (!settings.enabled) {
            return;
        }
        if (pendingDisconnectUntil == 0L && client.player != null && client.level != null && client.getConnection() != null && POLICY.combatStarted(CombatStateModule.active())) {
            BackgroundAlerts.emit(client, BackgroundAlertPolicy.Event.COMBAT);
        }
        if (pendingDisconnectUntil != 0L) {
            if (client.isWindowActive()) {
                BackgroundAlerts.clearDisconnect();
            } else if (client.gui.screen() instanceof DisconnectedScreen) {
                BackgroundAlerts.clearDisconnect();
                BackgroundAlerts.emit(client, BackgroundAlertPolicy.Event.DISCONNECT);
            } else if (client.gui.screen() instanceof TitleScreen || client.gui.screen() instanceof JoinMultiplayerScreen || BackgroundAlerts.now() > pendingDisconnectUntil) {
                BackgroundAlerts.clearDisconnect();
            }
        }
    }

    public static void onMessage(Minecraft client, Component display, String body, boolean own) {
        int from;
        String text;
        if (NotificationInbox.snoozed()) {
            return;
        }
        if (client == null || own || pendingDisconnectUntil != 0L || !BackgroundAlerts.settings().enabled || !BackgroundAlerts.settings().mentions || client.isWindowActive() || client.player == null || client.level == null || client.getConnection() == null) {
            return;
        }
        BackgroundAlerts.prepare(client);
        String string = text = body != null ? body : display.getString(8193);
        if (text.isEmpty() || text.length() > 8192) {
            return;
        }
        String name = client.getUser().getName();
        int n = from = body == null ? ChatMentionMatcher.systemBodyStart(text) : 0;
        if (body == null && ChatMentionMatcher.systemIsOwnMessage(text, name, from)) {
            return;
        }
        if (!ChatMentionMatcher.find(text, name, from, text.length()).isEmpty()) {
            BackgroundAlerts.emit(client, BackgroundAlertPolicy.Event.MENTION);
        }
    }

    public static void onDisconnected(Minecraft client) {
        if (client == null) {
            return;
        }
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        Object departing = connection == null ? client.getConnection() : connection;
        pendingDisconnectUntil = settings.enabled && settings.disconnect && departing != null && !client.isWindowActive() && !client.hasSingleplayerServer() ? BackgroundAlerts.now() + 30000L : 0L;
        disconnectingConnection = pendingDisconnectUntil == 0L ? null : departing;
        DESKTOP.context(settings.enabled, client.isWindowActive(), ++session, nativeWindow);
        POLICY.reset();
        connection = null;
    }

    private static void prepare(Minecraft client) {
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        boolean reset = configuration != BloodHelperClient.config() || previousSettings != settings || enabled != settings.enabled;
        ClientPacketListener next = client.getConnection();
        if (next != null && next != connection && next != disconnectingConnection) {
            connection = next;
            pendingDisconnectUntil = 0L;
            reset = true;
        }
        if (reset) {
            configuration = BloodHelperClient.config();
            previousSettings = settings;
            enabled = settings.enabled;
            POLICY.reset();
            BackgroundAlerts.clearDisconnect();
            ++session;
        }
        if (settings.enabled && DESKTOP.supported() && sdlWindow != client.getWindow().handle()) {
            sdlWindow = client.getWindow().handle();
            try {
                nativeWindow = SDLProperties.SDL_GetPointerProperty((int)SDLVideo.SDL_GetWindowProperties((long)sdlWindow), (CharSequence)"SDL.window.win32.hwnd", (long)0L);
            }
            catch (LinkageError | RuntimeException failure) {
                nativeWindow = 0L;
            }
        }
        DESKTOP.context(settings.enabled, client.isWindowActive(), session, nativeWindow);
    }

    private static void clearDisconnect() {
        pendingDisconnectUntil = 0L;
        disconnectingConnection = null;
    }

    private static void emit(Minecraft client, BackgroundAlertPolicy.Event event) {
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        if (!POLICY.acquire(event, settings, client.isWindowActive(), BackgroundAlerts.now())) {
            return;
        }
        boolean ru = "ru".equals(BloodHelperClient.config().language);
        String body = switch (event) {
            default -> throw new MatchException(null, null);
            case BackgroundAlertPolicy.Event.MENTION -> {
                if (ru) {
                    yield "\u0412\u0430\u0441 \u0443\u043f\u043e\u043c\u044f\u043d\u0443\u043b\u0438 \u0432 \u0447\u0430\u0442\u0435";
                }
                yield "Someone mentioned you in chat";
            }
            case BackgroundAlertPolicy.Event.COMBAT -> {
                if (ru) {
                    yield "\u041d\u0430\u0447\u0430\u043b\u0441\u044f PvP. \u0412\u0435\u0440\u043d\u0438\u0442\u0435\u0441\u044c \u0432 \u0438\u0433\u0440\u0443";
                }
                yield "PvP started. Return to the game";
            }
            case BackgroundAlertPolicy.Event.DISCONNECT -> ru ? "\u0421\u043e\u0435\u0434\u0438\u043d\u0435\u043d\u0438\u0435 \u0441 \u0441\u0435\u0440\u0432\u0435\u0440\u043e\u043c \u043f\u0440\u0435\u0440\u0432\u0430\u043d\u043e" : "Disconnected from the server";
        };
        DESKTOP.submit(new DesktopNotification("BloodstoneHelperVL", body, settings.sound));
    }

    public static void settingsChanged() {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        DESKTOP.context(BackgroundAlerts.settings().enabled, client.isWindowActive(), ++session, nativeWindow);
        BackgroundAlerts.prepare(client);
    }

    public static String status() {
        return DESKTOP.status();
    }

    public static void close() {
        BackgroundAlerts.clearDisconnect();
        DESKTOP.close();
    }

    private static BackgroundAlertsSettings settings() {
        return BloodHelperClient.config().backgroundAlerts;
    }

    private static long now() {
        return System.nanoTime() / 1000000L;
    }
}

