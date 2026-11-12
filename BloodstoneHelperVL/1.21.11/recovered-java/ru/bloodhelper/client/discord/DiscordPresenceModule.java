/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.discord;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_310;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.DiscordPresenceSettings;
import ru.bloodhelper.client.discord.DiscordPresence;
import ru.bloodhelper.client.discord.DiscordPresenceService;
import ru.bloodhelper.client.discord.DiscordRpcSession;
import ru.bloodhelper.client.discord.PresenceCaptureGate;
import ru.bloodhelper.client.music.MediaSnapshot;

public final class DiscordPresenceModule {
    private static final DiscordPresenceService SERVICE = new DiscordPresenceService();
    private static final String VERSION = FabricLoader.getInstance().getModContainer("bloodhelper").map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("1.3.8");
    private static boolean connected;
    private static long joinedAtSeconds;
    private static final PresenceCaptureGate CAPTURE;
    private static final long STARTED_AT;

    private DiscordPresenceModule() {
    }

    public static void joined() {
        CAPTURE.invalidate();
        connected = true;
        joinedAtSeconds = System.currentTimeMillis() / 1000L;
    }

    public static void disconnected() {
        CAPTURE.invalidate();
        connected = false;
        joinedAtSeconds = 0L;
    }

    public static void tick(class_310 client, MediaSnapshot media) {
        boolean allowed;
        boolean inWorld;
        ClientConfig config = BloodHelperClient.config();
        DiscordPresenceSettings settings = config.discordPresence;
        boolean bl = inWorld = connected && client.field_1687 != null && client.field_1724 != null;
        DiscordRpcSession.Status inactive = !settings.enabled ? DiscordRpcSession.Status.OFF : (config.streamer.enabled ? DiscordRpcSession.Status.STREAMER : DiscordRpcSession.Status.NO_WORLD);
        boolean bl2 = allowed = settings.enabled && !config.streamer.enabled;
        if (!allowed) {
            CAPTURE.invalidate();
            SERVICE.update(null, inactive);
            return;
        }
        String uid = settings.showNickname ? StaffAccess.uid() : "";
        long now = System.nanoTime();
        if (!CAPTURE.due(now, ConfigStore.revision(), uid)) {
            return;
        }
        String nickname = allowed && settings.showNickname ? (client.field_1724 == null ? client.method_1548().method_1676() : client.field_1724.method_7334().name()) : "";
        String publicUid = uid;
        String server = allowed && inWorld && client.method_1558() != null ? client.method_1558().field_3761 : "";
        DiscordPresence presence = DiscordPresence.capture(settings, config.streamer.enabled, inWorld, VERSION, nickname, server, client.method_1496(), media, now, !"en".equals(config.language), inWorld ? joinedAtSeconds : STARTED_AT, publicUid);
        SERVICE.update(presence, inactive);
    }

    public static boolean needsMedia() {
        ClientConfig config = BloodHelperClient.config();
        return connected && config.discordPresence.enabled && config.discordPresence.showTrack && !config.streamer.enabled;
    }

    public static String statusText() {
        boolean ru = !"en".equals(BloodHelperClient.config().language);
        return switch (SERVICE.status()) {
            default -> throw new MatchException(null, null);
            case DiscordRpcSession.Status.OFF -> {
                if (ru) {
                    yield "\u0412\u044b\u043a\u043b\u044e\u0447\u0435\u043d\u043e";
                }
                yield "Off";
            }
            case DiscordRpcSession.Status.INVALID_ID -> {
                if (ru) {
                    yield "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u044f \u043a Discord";
                }
                yield "Discord connection error";
            }
            case DiscordRpcSession.Status.STREAMER -> {
                if (ru) {
                    yield "\u0421\u043a\u0440\u044b\u0442\u043e: Streamer Mode";
                }
                yield "Hidden: Streamer Mode";
            }
            case DiscordRpcSession.Status.NO_WORLD -> {
                if (ru) {
                    yield "\u041e\u0436\u0438\u0434\u0430\u043d\u0438\u0435 \u0432\u0445\u043e\u0434\u0430 \u0432 \u043c\u0438\u0440";
                }
                yield "Waiting for a world";
            }
            case DiscordRpcSession.Status.CONNECTING -> {
                if (ru) {
                    yield "\u041f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u043a Discord\u2026";
                }
                yield "Connecting to Discord\u2026";
            }
            case DiscordRpcSession.Status.UPDATING -> {
                if (ru) {
                    yield "\u041e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435 \u0441\u0442\u0430\u0442\u0443\u0441\u0430\u2026";
                }
                yield "Updating presence\u2026";
            }
            case DiscordRpcSession.Status.CONNECTED -> {
                if (ru) {
                    yield "Discord \u043f\u043e\u0434\u0442\u0432\u0435\u0440\u0434\u0438\u043b \u043e\u0431\u043d\u043e\u0432\u043b\u0435\u043d\u0438\u0435";
                }
                yield "Discord acknowledged the update";
            }
            case DiscordRpcSession.Status.RETRYING -> {
                if (ru) {
                    yield "Discord \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u0435\u043d / \u043e\u0442\u043a\u0430\u0437; \u043f\u0435\u0440\u0435\u043f\u043e\u0434\u043a\u043b\u044e\u0447\u0435\u043d\u0438\u0435 \u0447\u0435\u0440\u0435\u0437 1\u201310 \u0441";
                }
                yield "Discord unavailable / rejected; retry in 1\u201310 s";
            }
            case DiscordRpcSession.Status.STOPPED -> ru ? "\u041e\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u043e" : "Stopped";
        };
    }

    public static void close() {
        connected = false;
        joinedAtSeconds = 0L;
        SERVICE.close();
    }

    static {
        CAPTURE = new PresenceCaptureGate();
        STARTED_AT = System.currentTimeMillis() / 1000L;
    }
}

