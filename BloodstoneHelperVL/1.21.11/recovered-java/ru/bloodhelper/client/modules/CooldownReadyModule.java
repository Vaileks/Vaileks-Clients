/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1304
 *  net.minecraft.class_1792
 *  net.minecraft.class_1796
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1935
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_437
 *  net.minecraft.class_5250
 *  net.minecraft.class_7923
 */
package ru.bloodhelper.client.modules;

import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1796;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1935;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_7923;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.config.CooldownReadySettings;
import ru.bloodhelper.client.config.NotificationSoundSettings;
import ru.bloodhelper.client.gui.UiLanguage;
import ru.bloodhelper.client.hud.FeatureAlerts;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.NotificationSounds;
import ru.bloodhelper.client.mixin.ItemCooldownsAccessor;
import ru.bloodhelper.client.modules.CooldownReadyScreen;
import ru.bloodhelper.client.modules.CooldownReadyState;

public final class CooldownReadyModule {
    private static final CooldownReadyState STATE = new CooldownReadyState();
    private static final LinkedHashMap<String, Metadata> KNOWN = new LinkedHashMap();
    private static final class_1304[] EQUIPMENT = class_1304.values();
    private static ClientConfig contextConfig;
    private static ClientConfig knownConfig;
    private static CooldownReadySettings contextSettings;
    private static CooldownReadySettings knownSettings;
    private static Object contextPlayer;
    private static Object contextConnection;
    private static Object knownConnection;
    private static class_1796 contextManager;
    private static long lastSoundAt;
    private static int knownRevision;

    private CooldownReadyModule() {
    }

    public static void tick(class_310 client) {
        CooldownReadyModule.synchronizeKnownContext(client);
        if (!CooldownReadyModule.eligible(client)) {
            CooldownReadyModule.reset();
            return;
        }
        CooldownReadyModule.synchronizeContext(client, client.field_1724.method_7357());
    }

    public static void reset() {
        if (contextManager != null || STATE.size() != 0) {
            STATE.reset();
            FeatureAlerts.clearOwner("cooldownReady");
        }
        contextConfig = null;
        contextSettings = null;
        contextPlayer = null;
        contextConnection = null;
        contextManager = null;
        lastSoundAt = Long.MIN_VALUE;
        if (!KNOWN.isEmpty()) {
            KNOWN.clear();
            ++knownRevision;
        }
    }

    private static boolean eligible(class_310 client) {
        return client != null && client.method_18854() && BloodHelperClient.config().cooldownReady.enabled && client.field_1724 != null && client.field_1724.method_5805() && !client.field_1724.method_31481() && client.field_1687 != null && client.method_1562() != null;
    }

    private static boolean synchronizeContext(class_310 client, class_1796 manager) {
        ClientConfig config = BloodHelperClient.config();
        if (contextConfig == config && contextSettings == config.cooldownReady && contextPlayer == client.field_1724 && contextManager == manager && contextConnection == client.method_1562()) {
            return true;
        }
        CooldownReadyModule.reset();
        CooldownReadyModule.synchronizeKnownContext(client);
        contextConfig = config;
        contextSettings = config.cooldownReady;
        contextPlayer = client.field_1724;
        contextManager = manager;
        contextConnection = client.method_1562();
        return false;
    }

    private static void synchronizeKnownContext(class_310 client) {
        if (client == null || !client.method_18854()) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        if (knownConnection != client.method_1562() || knownConfig != config || knownSettings != config.cooldownReady) {
            KNOWN.clear();
            ++knownRevision;
            knownConnection = client.method_1562();
            knownConfig = config;
            knownSettings = config.cooldownReady;
        }
    }

    private static boolean ownActiveManager(class_1796 manager) {
        class_310 client = class_310.method_1551();
        if (client == null || !client.method_18854()) {
            return false;
        }
        if (!CooldownReadyModule.eligible(client)) {
            CooldownReadyModule.reset();
            return false;
        }
        if (client.field_1724.method_7357() != manager) {
            return false;
        }
        CooldownReadyModule.synchronizeContext(client, manager);
        return true;
    }

    public static void started(class_1796 manager, class_2960 group, int duration) {
        if (!CooldownReadyModule.ownActiveManager(manager)) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        String id = group.toString();
        if (id.equals(config.pvp.cooldownGroup)) {
            STATE.cancelled(id);
            return;
        }
        if (duration > 0) {
            CooldownReadyModule.discover(class_310.method_1551(), manager, group);
        }
        STATE.started(id, ((ItemCooldownsAccessor)manager).bloodhelper$tickCount(), duration, config.cooldownReady.minimumSeconds * 20);
    }

    public static void cancelled(class_1796 manager, class_2960 group) {
        if (CooldownReadyModule.ownActiveManager(manager)) {
            STATE.cancelled(group.toString());
        }
    }

    public static void naturallyEnded(class_1796 manager, class_2960 group) {
        if (!CooldownReadyModule.ownActiveManager(manager)) {
            return;
        }
        String id = group.toString();
        if (!STATE.naturallyEnded(id, ((ItemCooldownsAccessor)manager).bloodhelper$tickCount())) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        CooldownReadySettings settings = config.cooldownReady;
        if (!settings.accepts(id, config.pvp.cooldownGroup)) {
            return;
        }
        if (CooldownReadyModule.observedIcon(id).method_7960()) {
            CooldownReadyModule.discover(class_310.method_1551(), manager, group);
        }
        if (settings.notify) {
            FeatureAlerts.push("cooldownReady", id + ":" + ((ItemCooldownsAccessor)manager).bloodhelper$tickCount(), CooldownReadyModule.label(id), HudStyle.tr(config, "\u041c\u043e\u0436\u043d\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c", "Ready to use"), "cooldown:" + id);
        }
        long now = System.nanoTime();
        if (settings.sound && settings.volume > 0.0 && (lastSoundAt == Long.MIN_VALUE || now - lastSoundAt >= 800000000L)) {
            lastSoundAt = now;
            NotificationSounds.play(class_310.method_1551(), NotificationSoundSettings.Event.COOLDOWN_READY, 1.3f, (float)settings.volume);
        }
    }

    private static void discover(class_310 client, class_1796 manager, class_2960 group) {
        class_5250 name;
        class_1792 item;
        String id = group.toString();
        Metadata old = KNOWN.get(id);
        class_1799 icon = class_1799.field_8037;
        for (class_1304 slot : EQUIPMENT) {
            class_1799 stack = client.field_1724.method_6118(slot);
            if (stack.method_7960() || !manager.method_62836(stack).equals((Object)group)) continue;
            icon = stack.method_46651(1);
            break;
        }
        if (icon.method_7960()) {
            for (class_1799 stack : client.field_1724.method_31548().method_67533()) {
                if (stack.method_7960() || !manager.method_62836(stack).equals((Object)group)) continue;
                icon = stack.method_46651(1);
                break;
            }
        }
        if (icon.method_7960() && old != null && !old.icon.method_7960()) {
            return;
        }
        if (icon.method_7960() && (item = (class_1792)class_7923.field_41178.method_17966(group).orElse(null)) != null && item != class_1802.field_8162) {
            icon = new class_1799((class_1935)item);
        }
        if (old == null && KNOWN.size() >= 128) {
            KNOWN.remove(KNOWN.keySet().iterator().next());
        }
        class_5250 class_52502 = name = icon.method_7960() ? null : icon.method_7964().method_27661();
        if (old == null || !icon.method_7960()) {
            KNOWN.put(id, new Metadata((class_2561)name, icon));
            ++knownRevision;
        }
    }

    public static class_1799 observedIcon(String group) {
        Metadata metadata = KNOWN.get(group);
        return metadata == null ? class_1799.field_8037 : metadata.icon;
    }

    private static String label(String id) {
        Metadata metadata = KNOWN.get(id);
        String value = metadata != null && metadata.name != null ? UiLanguage.text(metadata.name).strip() : "";
        return value.isEmpty() ? id : value;
    }

    public static List<Group> groups() {
        CooldownReadyModule.synchronizeKnownContext(class_310.method_1551());
        ClientConfig config = BloodHelperClient.config();
        LinkedHashMap<String, Group> rows = new LinkedHashMap<String, Group>();
        for (String id : config.cooldownReady.selectedGroups) {
            if (id.equals(config.pvp.cooldownGroup)) continue;
            rows.put(id, new Group(id, CooldownReadyModule.label(id), true));
        }
        for (String id : KNOWN.keySet()) {
            if (id.equals(config.pvp.cooldownGroup)) continue;
            rows.putIfAbsent(id, new Group(id, CooldownReadyModule.label(id), false));
        }
        return List.copyOf(rows.values());
    }

    public static int knownRevision() {
        return knownRevision;
    }

    public static void openScreen(class_437 parent) {
        class_310 client = class_310.method_1551();
        if (CooldownReadyModule.eligible(client)) {
            class_1796 manager = client.field_1724.method_7357();
            for (class_2960 group : ((ItemCooldownsAccessor)manager).bloodhelper$cooldowns().keySet()) {
                if (group.toString().equals(BloodHelperClient.config().pvp.cooldownGroup)) continue;
                CooldownReadyModule.discover(client, manager, group);
            }
        }
        client.method_1507((class_437)new CooldownReadyScreen(parent));
    }

    static {
        lastSoundAt = Long.MIN_VALUE;
    }

    private record Metadata(class_2561 name, class_1799 icon) {
    }

    public record Group(String id, String label, boolean selected) {
    }
}

