/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.hud.AlertTracker;
import ru.bloodhelper.client.hud.NotificationInbox;
import ru.bloodhelper.client.hud.Notifications;

public final class FeatureAlerts {
    private static final Map<String, AlertTracker> CHANNELS = Map.of("chatViolations", new AlertTracker(), "chatMentions", new AlertTracker(), "cooldownReady", new AlertTracker(), "spectatorTimer", new AlertTracker());
    private static Object connection;
    private static ClientConfig configuration;

    private FeatureAlerts() {
    }

    public static boolean push(String owner, String key, String title, String body, String icon) {
        boolean added;
        Minecraft client = Minecraft.getInstance();
        ClientConfig config = BloodHelperClient.config();
        FeatureAlerts.syncSession(client);
        if (client.level == null || client.player == null || client.getConnection() == null || !FeatureAlerts.ownerEnabled(owner, config)) {
            return false;
        }
        AlertTracker tracker = CHANNELS.get(owner);
        boolean bl = added = tracker != null && tracker.push("feature:" + owner + ":" + key, title, body, icon, Notifications.now(), (long)config.notifications.durationSeconds * 1000L, 16);
        if (added) {
            NotificationInbox.record(owner, title, body);
        }
        return added;
    }

    public static void tick(Minecraft client) {
        FeatureAlerts.syncSession(client);
        ClientConfig config = BloodHelperClient.config();
        CHANNELS.forEach((owner, tracker) -> {
            if (!FeatureAlerts.ownerEnabled(owner, config)) {
                tracker.clear();
            }
        });
    }

    private static void syncSession(Minecraft client) {
        if (connection != client.getConnection() || client.level == null || configuration != BloodHelperClient.config()) {
            FeatureAlerts.clear();
            connection = client.getConnection();
            configuration = BloodHelperClient.config();
        }
    }

    public static boolean ownerEnabled(String owner, ClientConfig config) {
        return switch (owner) {
            case "spectatorTimer" -> config.spectatorTimer.enabled;
            case "chatMentions" -> {
                if (config.chatMentions.enabled && config.chatMentions.notification && !config.streamer.hidesChat()) {
                    yield true;
                }
                yield false;
            }
            case "chatViolations" -> {
                if (config.chatViolations.enabled && config.chatViolations.notification && !config.streamer.hidesChat()) {
                    yield true;
                }
                yield false;
            }
            case "cooldownReady" -> {
                if (config.cooldownReady.enabled && config.cooldownReady.notify) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    public static boolean enabled(ClientConfig config) {
        return FeatureAlerts.ownerEnabled("chatViolations", config) || FeatureAlerts.ownerEnabled("chatMentions", config) || FeatureAlerts.ownerEnabled("cooldownReady", config) || FeatureAlerts.ownerEnabled("spectatorTimer", config);
    }

    public static boolean displayAllowed(String key, ClientConfig config) {
        if (key.startsWith("feature:spectatorTimer:")) {
            return FeatureAlerts.ownerEnabled("spectatorTimer", config);
        }
        if (key.startsWith("feature:chatViolations:")) {
            return FeatureAlerts.ownerEnabled("chatViolations", config);
        }
        if (key.startsWith("feature:chatMentions:")) {
            return FeatureAlerts.ownerEnabled("chatMentions", config);
        }
        if (key.startsWith("feature:cooldownReady:")) {
            return FeatureAlerts.ownerEnabled("cooldownReady", config);
        }
        return config.notifications.enabled;
    }

    public static List<AlertTracker.Notice> snapshot() {
        if (NotificationInbox.snoozed()) {
            return List.of();
        }
        ClientConfig config = BloodHelperClient.config();
        ArrayList<AlertTracker.Notice> notices = new ArrayList<AlertTracker.Notice>();
        CHANNELS.forEach((owner, tracker) -> {
            if (FeatureAlerts.ownerEnabled(owner, config)) {
                notices.addAll(tracker.snapshot(Notifications.now()));
            } else {
                tracker.clear();
            }
        });
        notices.sort(Comparator.comparingLong(AlertTracker.Notice::createdAt));
        return List.copyOf(notices);
    }

    public static void clearOwner(String owner) {
        AlertTracker tracker = CHANNELS.get(owner);
        if (tracker != null) {
            tracker.clear();
        }
    }

    public static void clear() {
        CHANNELS.values().forEach(AlertTracker::clear);
    }
}

