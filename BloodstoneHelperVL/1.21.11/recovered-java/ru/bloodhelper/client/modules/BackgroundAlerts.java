/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_419
 *  net.minecraft.class_442
 *  net.minecraft.class_500
 *  net.minecraft.class_634
 *  org.lwjgl.glfw.GLFWNativeWin32
 */
package ru.bloodhelper.client.modules;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_419;
import net.minecraft.class_442;
import net.minecraft.class_500;
import net.minecraft.class_634;
import org.lwjgl.glfw.GLFWNativeWin32;
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
    private static long glfwWindow;
    private static long nativeWindow;

    private BackgroundAlerts() {
    }

    public static void tick(class_310 client) {
        if (client == null) {
            return;
        }
        BackgroundAlerts.prepare(client);
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        if (!settings.enabled) {
            return;
        }
        if (pendingDisconnectUntil == 0L && client.field_1724 != null && client.field_1687 != null && client.method_1562() != null && POLICY.combatStarted(CombatStateModule.active())) {
            BackgroundAlerts.emit(client, BackgroundAlertPolicy.Event.COMBAT);
        }
        if (pendingDisconnectUntil != 0L) {
            if (client.method_1569()) {
                BackgroundAlerts.clearDisconnect();
            } else if (client.field_1755 instanceof class_419) {
                BackgroundAlerts.clearDisconnect();
                BackgroundAlerts.emit(client, BackgroundAlertPolicy.Event.DISCONNECT);
            } else if (client.field_1755 instanceof class_442 || client.field_1755 instanceof class_500 || BackgroundAlerts.now() > pendingDisconnectUntil) {
                BackgroundAlerts.clearDisconnect();
            }
        }
    }

    public static void onMessage(class_310 client, class_2561 display, String body, boolean own) {
        int from;
        String text;
        if (NotificationInbox.snoozed()) {
            return;
        }
        if (client == null || own || pendingDisconnectUntil != 0L || !BackgroundAlerts.settings().enabled || !BackgroundAlerts.settings().mentions || client.method_1569() || client.field_1724 == null || client.field_1687 == null || client.method_1562() == null) {
            return;
        }
        BackgroundAlerts.prepare(client);
        String string = text = body != null ? body : display.method_10858(8193);
        if (text.isEmpty() || text.length() > 8192) {
            return;
        }
        String name = client.method_1548().method_1676();
        int n = from = body == null ? ChatMentionMatcher.systemBodyStart(text) : 0;
        if (body == null && ChatMentionMatcher.systemIsOwnMessage(text, name, from)) {
            return;
        }
        if (!ChatMentionMatcher.find(text, name, from, text.length()).isEmpty()) {
            BackgroundAlerts.emit(client, BackgroundAlertPolicy.Event.MENTION);
        }
    }

    public static void onDisconnected(class_310 client) {
        if (client == null) {
            return;
        }
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        Object departing = connection == null ? client.method_1562() : connection;
        pendingDisconnectUntil = settings.enabled && settings.disconnect && departing != null && !client.method_1569() && !client.method_1496() ? BackgroundAlerts.now() + 30000L : 0L;
        disconnectingConnection = pendingDisconnectUntil == 0L ? null : departing;
        DESKTOP.context(settings.enabled, client.method_1569(), ++session, nativeWindow);
        POLICY.reset();
        connection = null;
    }

    private static void prepare(class_310 client) {
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        boolean reset = configuration != BloodHelperClient.config() || previousSettings != settings || enabled != settings.enabled;
        class_634 next = client.method_1562();
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
        if (settings.enabled && DESKTOP.supported() && glfwWindow != client.method_22683().method_4490()) {
            glfwWindow = client.method_22683().method_4490();
            try {
                nativeWindow = GLFWNativeWin32.glfwGetWin32Window((long)glfwWindow);
            }
            catch (LinkageError | RuntimeException failure) {
                nativeWindow = 0L;
            }
        }
        DESKTOP.context(settings.enabled, client.method_1569(), session, nativeWindow);
    }

    private static void clearDisconnect() {
        pendingDisconnectUntil = 0L;
        disconnectingConnection = null;
    }

    private static void emit(class_310 client, BackgroundAlertPolicy.Event event) {
        BackgroundAlertsSettings settings = BackgroundAlerts.settings();
        if (!POLICY.acquire(event, settings, client.method_1569(), BackgroundAlerts.now())) {
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
        class_310 client = class_310.method_1551();
        if (client == null) {
            return;
        }
        DESKTOP.context(BackgroundAlerts.settings().enabled, client.method_1569(), ++session, nativeWindow);
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

