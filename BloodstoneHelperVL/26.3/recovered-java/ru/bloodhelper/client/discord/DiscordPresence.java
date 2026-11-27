/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package ru.bloodhelper.client.discord;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import ru.bloodhelper.client.config.DiscordPresenceSettings;
import ru.bloodhelper.client.music.MediaSnapshot;

public record DiscordPresence(String details, String state, String imageText, long startedAtSeconds, String avatarNickname, String serverIconAddress) {
    public static final long TRACK_MAX_AGE_NANOS = 5000000000L;

    public DiscordPresence(String details, String state, String imageText, long started, String nickname) {
        this(details, state, imageText, started, nickname, "");
    }

    public DiscordPresence(String details, String state) {
        this(details, state, "BloodHelper \u00b7 Fabric", 0L, "");
    }

    public DiscordPresence(String details, String state, String imageText, long startedAtSeconds) {
        this(details, state, imageText, startedAtSeconds, "");
    }

    public static DiscordPresence capture(DiscordPresenceSettings settings, boolean streamer, boolean inWorld, String version, String nickname, String server, boolean singleplayer, MediaSnapshot media, long now, boolean russian) {
        return DiscordPresence.capture(settings, streamer, inWorld, version, nickname, server, singleplayer, media, now, russian, 0L);
    }

    public static DiscordPresence capture(DiscordPresenceSettings settings, boolean streamer, boolean inWorld, String version, String nickname, String server, boolean singleplayer, MediaSnapshot media, long now, boolean russian, long joinedAtSeconds) {
        return DiscordPresence.capture(settings, streamer, inWorld, version, nickname, server, singleplayer, media, now, russian, joinedAtSeconds, "");
    }

    public static DiscordPresence capture(DiscordPresenceSettings settings, boolean streamer, boolean inWorld, String version, String nickname, String server, boolean singleplayer, MediaSnapshot media, long now, boolean russian, long joinedAtSeconds, String publicUid) {
        Object detail;
        String nick;
        if (!settings.enabled || streamer) {
            return null;
        }
        String release = DiscordPresenceSettings.text(version, 24);
        String string = nick = settings.showNickname ? DiscordPresenceSettings.text(nickname, 48) : "";
        if (!inWorld) {
            detail = russian ? "\u0412 \u0433\u043b\u0430\u0432\u043d\u043e\u043c \u043c\u0435\u043d\u044e" : "In the main menu";
        } else if ("CUSTOM".equals(settings.activityMode)) {
            detail = DiscordPresenceSettings.text(settings.customText, 128);
            if (((String)detail).isEmpty()) {
                detail = russian ? "\u0418\u0433\u0440\u0430\u0435\u0442 \u0432 Minecraft" : "Playing Minecraft";
            }
        } else {
            String address;
            detail = singleplayer ? (russian ? "\u041e\u0434\u0438\u043d\u043e\u0447\u043d\u0430\u044f \u0438\u0433\u0440\u0430" : "Singleplayer") : ((address = DiscordPresenceSettings.text(server, 128)).isEmpty() ? (russian ? "\u0421\u0435\u0442\u0435\u0432\u0430\u044f \u0438\u0433\u0440\u0430" : "Multiplayer") : (russian ? "\u0418\u0433\u0440\u0430\u0435\u0442 \u043d\u0430 " : "Playing on ") + address);
        }
        String track = DiscordPresence.track(settings.showTrack, media, now);
        Object state = nick;
        if (!nick.isEmpty() && settings.showNickname && publicUid != null && publicUid.matches("[1-9][0-9]{0,11}")) {
            state = (String)state + " \u00b7 UID: " + publicUid;
        }
        if (!release.isEmpty()) {
            Object object = state = ((String)state).isEmpty() ? "v" + release : (String)state + " \u00b7 v" + release;
        }
        if (!track.isEmpty()) {
            String listening = "\u266b " + track;
            state = ((String)state).isEmpty() ? listening : (String)state + " \u00b7 " + listening;
        }
        return new DiscordPresence(DiscordPresenceSettings.text((String)detail, 128), DiscordPresenceSettings.text((String)state, 128), "BloodHelper" + (String)(release.isEmpty() ? "" : " " + release) + " \u00b7 Fabric", settings.showElapsed ? Math.max(0L, joinedAtSeconds) : 0L, nick, inWorld && !singleplayer && settings.showServerIcon ? DiscordPresenceSettings.text(server, 128) : "");
    }

    static String track(boolean enabled, MediaSnapshot media, long now) {
        if (!enabled || media == null || !media.playing()) {
            return "";
        }
        long age = now - media.observedAtNanos();
        if (age < 0L || age > 5000000000L) {
            return "";
        }
        return DiscordPresenceSettings.text(media.title(), 110);
    }

    static String serverIcon(String address) {
        if (address == null || address.length() > 128 || !address.matches("[A-Za-z0-9.\\[\\]:_-]+")) {
            return "";
        }
        String host = address.toLowerCase(Locale.ROOT);
        if (host.equals("localhost") || host.startsWith("localhost:") || host.endsWith(".local") || host.contains(".local:") || host.matches("(?:127|10|0)\\..*") || host.startsWith("192.168.") || host.startsWith("169.254.") || host.matches("172\\.(?:1[6-9]|2[0-9]|3[01])\\..*") || host.startsWith("[::1]") || host.startsWith("[fc") || host.startsWith("[fd") || host.startsWith("[fe80")) {
            return "";
        }
        return "https://api.mcsrvstat.us/icon/" + URLEncoder.encode(address, StandardCharsets.UTF_8).replace("+", "%20");
    }

    JsonObject json() {
        JsonObject activity = new JsonObject();
        activity.addProperty("type", (Number)0);
        activity.addProperty("details", this.details);
        if (!this.state.isEmpty()) {
            activity.addProperty("state", this.state);
        }
        JsonObject assets = new JsonObject();
        String nickname = this.avatarNickname;
        boolean visible = nickname.matches("[A-Za-z0-9_]{3,16}");
        assets.addProperty("large_image", "https://mc-heads.net/avatar/" + (visible ? nickname : "Steve") + "/128.png");
        assets.addProperty("large_text", visible ? nickname : "Minecraft");
        String icon = DiscordPresence.serverIcon(this.serverIconAddress);
        if (!icon.isEmpty()) {
            assets.addProperty("small_image", icon);
            assets.addProperty("small_text", this.serverIconAddress);
        }
        activity.add("assets", (JsonElement)assets);
        if (this.startedAtSeconds > 0L) {
            JsonObject timestamps = new JsonObject();
            timestamps.addProperty("start", (Number)this.startedAtSeconds);
            activity.add("timestamps", (JsonElement)timestamps);
        }
        return activity;
    }
}

