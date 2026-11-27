/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.GeneralHudSettings;
import ru.bloodhelper.client.hud.FeatureAlerts;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.WatermarkText;

public final class HudWidgets {
    private HudWidgets() {
    }

    public static boolean enabled(String key, ClientConfig c) {
        if (key != null && key.startsWith("generalHud.")) {
            return c.generalHud.enabled && c.generalHud.widgets.containsKey(key.substring(11)) && c.generalHud.widgets.get((Object)key.substring((int)11)).enabled;
        }
        return switch (HudWidgets.owner(key)) {
            case "staffStatus" -> c.staffWorkspace.status;
            case "reachDisplay" -> c.reachDisplay.enabled;
            case "comboCounter" -> c.comboCounter.enabled;
            case "saturation" -> c.saturation.enabled;
            case "blastLeggings" -> {
                if (c.combatHud.enabled && c.combatHud.blastLeggings.enabled) {
                    yield true;
                }
                yield false;
            }
            case "cooldowns" -> c.cooldowns.enabled;
            case "pvp" -> c.pvpTimerEnabled();
            case "music" -> c.music.enabled;
            case "armor" -> c.armor.enabled;
            case "itemCounter" -> c.itemCounter.enabled;
            case "notifications" -> {
                if (c.notifications.enabled || FeatureAlerts.enabled(c)) {
                    yield true;
                }
                yield false;
            }
            case "effects" -> c.effects.enabled;
            case "sizeTimer" -> c.sizeTimer.enabled;
            case "watermark" -> c.watermark.enabled;
            case "performance" -> c.performance.enabled;
            case "session" -> c.session.enabled;
            case "spectatorTimer" -> c.spectatorTimer.enabled;
            case "staffStats" -> c.staffStats.enabled;
            case "waypoints" -> c.waypoints.enabled;
            default -> false;
        };
    }

    public static void setEnabled(String key, ClientConfig c, boolean enabled) {
        if (key != null && key.startsWith("generalHud.") && c.generalHud.widgets.containsKey(key.substring(11))) {
            c.generalHud.widgets.get((Object)key.substring((int)11)).enabled = enabled;
            if (enabled) {
                c.generalHud.enabled = true;
            }
            return;
        }
        switch (HudWidgets.owner(key)) {
            case "staffStatus": {
                c.staffWorkspace.status = enabled;
                break;
            }
            case "reachDisplay": {
                c.reachDisplay.enabled = enabled;
                break;
            }
            case "comboCounter": {
                c.comboCounter.enabled = enabled;
                break;
            }
            case "saturation": {
                c.saturation.enabled = enabled;
                break;
            }
            case "blastLeggings": {
                c.combatHud.blastLeggings.enabled = enabled;
                if (!enabled) break;
                c.combatHud.enabled = true;
                break;
            }
            case "cooldowns": {
                c.cooldowns.enabled = enabled;
                break;
            }
            case "pvp": {
                c.pvp.enabled = enabled;
                if (!enabled) break;
                c.combatHud.enabled = true;
                break;
            }
            case "music": {
                c.music.enabled = enabled;
                break;
            }
            case "armor": {
                c.armor.enabled = enabled;
                break;
            }
            case "itemCounter": {
                c.itemCounter.enabled = enabled;
                break;
            }
            case "notifications": {
                c.notifications.enabled = enabled;
                if (enabled) break;
                c.chatMentions.notification = false;
                c.cooldownReady.notify = false;
                c.chatViolations.notification = false;
                break;
            }
            case "effects": {
                c.effects.enabled = enabled;
                break;
            }
            case "sizeTimer": {
                c.sizeTimer.enabled = enabled;
                break;
            }
            case "watermark": {
                c.watermark.enabled = enabled;
                break;
            }
            case "performance": {
                c.performance.enabled = enabled;
                break;
            }
            case "session": {
                c.session.enabled = enabled;
                break;
            }
            case "spectatorTimer": {
                c.spectatorTimer.enabled = enabled;
                break;
            }
            case "staffStats": {
                c.staffStats.enabled = enabled;
                break;
            }
            case "waypoints": {
                c.waypoints.enabled = enabled;
                break;
            }
        }
    }

    public static Activation activation(String key, ClientConfig c) {
        if (key.startsWith("generalHud.") && c.generalHud.widgets.containsKey(key.substring(11))) {
            return new Activation(c.generalHud.widgets.get((Object)key.substring((int)11)).enabled, false, false, c.generalHud.enabled);
        }
        return "notifications".equals(HudWidgets.owner(key)) ? new Activation(c.notifications.enabled, c.chatMentions.notification, c.cooldownReady.notify, false, c.chatViolations.notification) : new Activation(HudWidgets.enabled(key, c), false, false);
    }

    public static void restoreActivation(String key, ClientConfig c, Activation state) {
        HudWidgets.setEnabled(key, c, state.enabled);
        if (key.startsWith("generalHud.")) {
            c.generalHud.enabled = state.groupEnabled;
        }
        if ("notifications".equals(HudWidgets.owner(key))) {
            c.chatMentions.notification = state.mentionNotifications;
            c.cooldownReady.notify = state.cooldownNotifications;
            c.chatViolations.notification = state.violationNotifications;
        }
    }

    public static String owner(String key) {
        if (key == null) {
            return "";
        }
        int dot = key.indexOf(46);
        return dot < 0 ? key : key.substring(0, dot);
    }

    public static List<Widget> all(ClientConfig c) {
        ArrayList<Widget> result = new ArrayList<Widget>(List.of(HudWidgets.widget("blastLeggings", HudStyle.tr(c, "\u0412\u0437\u0440\u044b\u0432\u043e\u0443\u0441\u0442\u043e\u0439\u0447\u0438\u0432\u044b\u0435 \u043f\u043e\u043d\u043e\u0436\u0438", "Blast Protection leggings"), c.combatHud.blastLeggings), new Widget("cooldowns", HudStyle.tr(c, "\u041f\u0435\u0440\u0435\u0437\u0430\u0440\u044f\u0434\u043a\u0438", "Cooldowns"), c.cooldowns.position, c.cooldowns.alignment, c.cooldowns.bottom), new Widget("pvp", HudStyle.tr(c, "\u0422\u0430\u0439\u043c\u0435\u0440 PvP", "PvP Timer"), c.pvp.position, c.pvp.alignment, c.pvp.bottom), new Widget("music", "Dynamic Island", c.music.position, "CENTER", false), HudWidgets.widget("notifications", HudStyle.tr(c, "\u0423\u0432\u0435\u0434\u043e\u043c\u043b\u0435\u043d\u0438\u044f", "Notifications"), c.notifications), HudWidgets.widget("effects", HudStyle.tr(c, "\u042d\u0444\u0444\u0435\u043a\u0442\u044b", "Effects"), c.effects), HudWidgets.widget("session", HudStyle.tr(c, "\u0421\u0442\u0430\u0442\u0438\u0441\u0442\u0438\u043a\u0430 \u0441\u0435\u0441\u0441\u0438\u0438", "Session Stats"), c.session), HudWidgets.widget("sizeTimer", HudStyle.tr(c, "\u0422\u0430\u0439\u043c\u0435\u0440 \u0440\u0430\u0437\u043c\u0435\u0440\u0430", "Size timer"), c.sizeTimer), HudWidgets.widget("waypoints", HudStyle.tr(c, "\u041c\u0435\u0442\u043a\u0438", "Waypoints"), c.waypoints)));
        if ("SEPARATE".equals(c.performance.layout)) {
            WatermarkText.Kind[] kindArray = List.of("fps", "ping", "frame", "graph").iterator();
            while (kindArray.hasNext()) {
                int visible;
                String key;
                if ((visible = (switch (key = kindArray.next()) {
                    case "fps" -> c.performance.showFps;
                    case "ping" -> c.performance.showPing;
                    case "frame" -> c.performance.showFrameTime;
                    default -> c.performance.showGraph;
                })) == 0) continue;
                String string = "performance." + key;
                String string2 = HudStyle.tr(c, "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c", "Performance");
                result.add(new Widget(string, string2 + " \u00b7 " + (switch (key) {
                    case "fps" -> "FPS";
                    case "ping" -> HudStyle.tr(c, "\u041f\u0438\u043d\u0433", "Ping");
                    case "frame" -> HudStyle.tr(c, "\u0412\u0440\u0435\u043c\u044f \u043a\u0430\u0434\u0440\u0430", "Frame time");
                    default -> HudStyle.tr(c, "\u0413\u0440\u0430\u0444\u0438\u043a", "Graph");
                }), c.performance.partPosition(key), "LEFT", false));
            }
        } else {
            result.add(HudWidgets.widget("performance", HudStyle.tr(c, "\u041f\u0440\u043e\u0438\u0437\u0432\u043e\u0434\u0438\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c", "Performance"), c.performance));
        }
        if ("SEPARATE".equals(c.watermark.layout)) {
            for (WatermarkText.Kind kind : WatermarkText.Kind.values()) {
                boolean visible;
                String key = kind.name().toLowerCase(Locale.ROOT);
                switch (kind) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case BRAND: {
                        boolean bl;
                        if (c.hudTitle("watermark") && !c.watermark.text.isEmpty()) {
                            bl = true;
                            break;
                        }
                        bl = false;
                        break;
                    }
                    case FPS: {
                        boolean bl = c.watermark.showFps;
                        break;
                    }
                    case PING: {
                        boolean bl = c.watermark.showPing;
                        break;
                    }
                    case SERVER: {
                        boolean bl = c.watermark.showServer;
                        break;
                    }
                    case COORDINATES: {
                        boolean bl = c.watermark.showCoordinates;
                        break;
                    }
                    case CLOCK: {
                        boolean bl = c.watermark.showClock;
                        break;
                    }
                    case SESSION: {
                        boolean bl = c.watermark.showSession;
                        break;
                    }
                    case ONLINE: {
                        boolean bl = visible = c.watermark.showOnline;
                    }
                }
                if (!visible) continue;
                String string = "watermark." + key;
                String string3 = HudStyle.tr(c, "\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a", "Watermark");
                result.add(new Widget(string, string3 + " \u00b7 " + (switch (kind) {
                    default -> throw new MatchException(null, null);
                    case WatermarkText.Kind.BRAND -> "BloodstoneHelperVL";
                    case WatermarkText.Kind.FPS -> "FPS";
                    case WatermarkText.Kind.PING -> HudStyle.tr(c, "\u041f\u0438\u043d\u0433", "Ping");
                    case WatermarkText.Kind.SERVER -> HudStyle.tr(c, "\u0421\u0435\u0440\u0432\u0435\u0440", "Server");
                    case WatermarkText.Kind.COORDINATES -> HudStyle.tr(c, "\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b", "Coordinates");
                    case WatermarkText.Kind.CLOCK -> HudStyle.tr(c, "\u0427\u0430\u0441\u044b", "Clock");
                    case WatermarkText.Kind.SESSION -> HudStyle.tr(c, "\u0421\u0435\u0441\u0441\u0438\u044f", "Session");
                    case WatermarkText.Kind.ONLINE -> HudStyle.tr(c, "\u041e\u043d\u043b\u0430\u0439\u043d", "Online");
                }), c.watermark.partPosition(key), "LEFT", false));
            }
        } else {
            result.add(HudWidgets.widget("watermark", HudStyle.tr(c, "\u0412\u0430\u0442\u0435\u0440\u043c\u0430\u0440\u043a", "Watermark"), c.watermark));
        }
        if ("SEPARATE".equals(c.armor.layout)) {
            String[] slots = new String[]{"head", "chest", "legs", "feet"};
            String[] ru = new String[]{"\u0428\u043b\u0435\u043c", "\u041d\u0430\u0433\u0440\u0443\u0434\u043d\u0438\u043a", "\u041f\u043e\u043d\u043e\u0436\u0438", "\u0411\u043e\u0442\u0438\u043d\u043a\u0438"};
            String[] en = new String[]{"Helmet", "Chestplate", "Leggings", "Boots"};
            for (int i = 0; i < slots.length; ++i) {
                result.add(new Widget("armor." + slots[i], HudStyle.tr(c, ru[i], en[i]), c.armor.partPosition(slots[i]), "LEFT", false));
            }
        } else {
            result.add(HudWidgets.widget("armor", HudStyle.tr(c, "\u0411\u0440\u043e\u043d\u044f", "Armor HUD"), c.armor));
        }
        if ("SEPARATE".equals(c.itemCounter.layout)) {
            for (String id : c.itemCounter.items) {
                result.add(new Widget("itemCounter." + id, HudStyle.tr(c, "\u041f\u0440\u0435\u0434\u043c\u0435\u0442", "Item") + " \u00b7 " + id, c.itemCounter.partPosition(id), "LEFT", false));
            }
        } else {
            result.add(HudWidgets.widget("itemCounter", HudStyle.tr(c, "\u0421\u0447\u0451\u0442\u0447\u0438\u043a \u043f\u0440\u0435\u0434\u043c\u0435\u0442\u043e\u0432", "Item Counter"), c.itemCounter));
        }
        for (GeneralHudSettings.Metric m : GeneralHudSettings.Metric.values()) {
            result.add(HudWidgets.widget("generalHud." + m.key(), HudStyle.tr(c, m.ru, m.en), c.generalHud.get(m)));
        }
        result.add(HudWidgets.widget("comboCounter", HudStyle.tr(c, "\u0421\u0447\u0451\u0442\u0447\u0438\u043a \u043a\u043e\u043c\u0431\u043e", "Combo counter"), c.comboCounter));
        return List.copyOf(result);
    }

    public static Widget get(String key, ClientConfig c) {
        return HudWidgets.all(c).stream().filter(w -> w.key.equals(key)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown HUD widget: " + key));
    }

    private static Widget widget(String key, String title, ClientConfig.WidgetSettings c) {
        return new Widget(key, title, c.position, c.alignment, c.bottom);
    }

    public static void reset(ClientConfig c) {
        ClientConfig d = new ClientConfig();
        c.staffWorkspace.statusPosition = d.staffWorkspace.statusPosition;
        for (GeneralHudSettings.Metric m : GeneralHudSettings.Metric.values()) {
            HudWidgets.reset(c.generalHud.get(m), d.generalHud.get(m));
        }
        HudWidgets.reset(c.reachDisplay, d.reachDisplay);
        HudWidgets.reset(c.comboCounter, d.comboCounter);
        HudWidgets.reset(c.saturation, d.saturation);
        HudWidgets.reset(c.combatHud.blastLeggings, d.combatHud.blastLeggings);
        c.cooldowns.position = d.cooldowns.position;
        c.cooldowns.alignment = d.cooldowns.alignment;
        c.cooldowns.bottom = d.cooldowns.bottom;
        c.pvp.position = d.pvp.position;
        c.pvp.alignment = d.pvp.alignment;
        c.pvp.bottom = d.pvp.bottom;
        c.music.position = d.music.position;
        HudWidgets.reset(c.armor, d.armor);
        HudWidgets.reset(c.itemCounter, d.itemCounter);
        HudWidgets.reset(c.notifications, d.notifications);
        HudWidgets.reset(c.spectatorTimer, d.spectatorTimer);
        HudWidgets.reset(c.staffStats, d.staffStats);
        c.performance.parts.clear();
        HudWidgets.reset(c.performance, d.performance);
        HudWidgets.reset(c.session, d.session);
        HudWidgets.reset(c.effects, d.effects);
        HudWidgets.reset(c.watermark, d.watermark);
        HudWidgets.reset(c.waypoints, d.waypoints);
        HudWidgets.reset(c.sizeTimer, d.sizeTimer);
        c.armor.parts.clear();
        c.itemCounter.parts.clear();
        c.watermark.parts.clear();
    }

    private static void reset(ClientConfig.WidgetSettings c, ClientConfig.WidgetSettings d) {
        c.position = d.position;
        c.alignment = d.alignment;
        c.bottom = d.bottom;
    }

    public record Activation(boolean enabled, boolean mentionNotifications, boolean cooldownNotifications, boolean groupEnabled, boolean violationNotifications) {
        public Activation(boolean enabled, boolean mentions, boolean cooldowns) {
            this(enabled, mentions, cooldowns, false, false);
        }

        public Activation(boolean enabled, boolean mentions, boolean cooldowns, boolean group) {
            this(enabled, mentions, cooldowns, group, false);
        }
    }

    public record Widget(String key, String title, ClientConfig.HudPosition position, String alignment, boolean bottom) {
    }
}

