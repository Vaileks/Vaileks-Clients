/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
 *  net.minecraft.class_1297
 *  net.minecraft.class_2338
 *  net.minecraft.class_239$class_240
 *  net.minecraft.class_243
 *  net.minecraft.class_304
 *  net.minecraft.class_304$class_11900
 *  net.minecraft.class_310
 *  net.minecraft.class_3675
 *  net.minecraft.class_3675$class_307
 *  net.minecraft.class_3959
 *  net.minecraft.class_3959$class_242
 *  net.minecraft.class_3959$class_3960
 *  net.minecraft.class_3965
 *  net.minecraft.class_4184
 *  net.minecraft.class_437
 *  net.minecraft.class_5218
 *  net.minecraft.class_634
 *  net.minecraft.class_746
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package ru.bloodhelper.client.modules;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_4184;
import net.minecraft.class_437;
import net.minecraft.class_5218;
import net.minecraft.class_634;
import net.minecraft.class_746;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ConfigStore;
import ru.bloodhelper.client.config.WaypointSettings;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.hud.WorldWaypoints;
import ru.bloodhelper.client.modules.DeathRecapModule;
import ru.bloodhelper.client.modules.WaypointEditScreen;
import ru.bloodhelper.client.modules.WaypointMath;
import ru.bloodhelper.client.modules.WaypointScreen;

public final class WaypointsModule {
    private static class_304 mark;
    private static class_304 quickMark;
    private static Ping ping;
    private static Object connection;
    private static String scope;
    private static String previousDimension;
    private static List<WaypointSettings.Entry> visible;
    private static int ticks;
    private static boolean dirty;
    private static Object previousSettings;

    private WaypointsModule() {
    }

    public static void register(class_304.class_11900 category) {
        mark = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.waypoint", class_3675.class_307.field_1668, class_3675.field_16237.method_1444(), category));
        quickMark = KeyBindingHelper.registerKeyBinding((class_304)new class_304("key.bloodhelper.quick_ping", class_3675.class_307.field_1668, 71, category));
    }

    public static String pingKeyName() {
        return quickMark == null || quickMark.method_1415() ? "None" : quickMark.method_16007().getString();
    }

    public static String keyName() {
        return mark == null || mark.method_1415() ? "None" : mark.method_16007().getString();
    }

    public static void tick(class_310 client) {
        boolean pressed = false;
        boolean pingPressed = false;
        if (mark != null) {
            while (mark.method_1436()) {
                pressed = true;
            }
        }
        if (quickMark != null) {
            while (quickMark.method_1436()) {
                pingPressed = true;
            }
        }
        WaypointsModule.updateScope(client);
        WaypointSettings settings = BloodHelperClient.config().waypoints;
        String dimension = WaypointsModule.dimension(client);
        if (!dimension.equals(previousDimension)) {
            dirty = true;
            previousDimension = dimension;
        }
        if (previousSettings != settings) {
            dirty = true;
            previousSettings = settings;
        }
        if (!settings.enabled || client.field_1724 == null || client.field_1687 == null || scope.isEmpty()) {
            visible = List.of();
            ping = null;
            dirty = true;
            return;
        }
        if (pressed && client.field_1755 == null) {
            WaypointsModule.createAtPlayer(null);
        }
        if (pingPressed && settings.quickPing && client.field_1755 == null && !client.field_1724.method_7325()) {
            WaypointsModule.markLookedBlock(client);
        }
        if (!(ping == null || System.nanoTime() < ping.expiresAt() && scope.equals(ping.scope()) && dimension.equals(ping.dimension()) && settings.quickPing)) {
            ping = null;
        }
        if (!dirty && ++ticks % 4 != 0) {
            return;
        }
        dirty = false;
        class_746 player = client.field_1724;
        visible = settings.entries.stream().filter(e -> e.visible && WaypointMath.matches(e, scope, dimension)).sorted(Comparator.comparingDouble(e -> WaypointMath.distance(player.method_23317(), player.method_23318(), player.method_23321(), e))).limit(settings.maxVisible).map(WaypointSettings.Entry::copy).toList();
    }

    private static void updateScope(class_310 client) {
        class_634 next = client.method_1562();
        if (next == connection) {
            return;
        }
        connection = next;
        visible = List.of();
        ping = null;
        dirty = true;
        scope = "";
        if (next == null) {
            return;
        }
        scope = client.method_1576() != null ? WaypointMath.scope("local", client.method_1576().method_27050(class_5218.field_24188).toAbsolutePath().normalize().toString()) : (client.method_1558() != null ? WaypointMath.remoteScope(client.method_1558().field_3761) : WaypointMath.scope("session", UUID.randomUUID().toString()));
    }

    public static String currentScope() {
        WaypointsModule.updateScope(class_310.method_1551());
        return scope;
    }

    public static String currentDimension() {
        return WaypointsModule.dimension(class_310.method_1551());
    }

    private static String dimension(class_310 client) {
        return client.field_1687 == null ? "" : client.field_1687.method_27983().method_29177().toString();
    }

    public static List<WaypointSettings.Entry> visible() {
        class_310 client = class_310.method_1551();
        if (connection != client.method_1562() || visible.isEmpty() || !WaypointMath.matches(visible.getFirst(), scope, WaypointsModule.dimension(client))) {
            return List.of();
        }
        return visible;
    }

    public static List<WaypointSettings.Entry> currentEntries() {
        String active = WaypointsModule.currentScope();
        return BloodHelperClient.config().waypoints.entries.stream().filter(e -> !active.isEmpty() && active.equals(e.scope)).toList();
    }

    public static void changed() {
        dirty = true;
        ConfigStore.requestSave();
    }

    public static void openScreen(class_437 parent) {
        class_310.method_1551().method_1507((class_437)new WaypointScreen(parent));
    }

    public static boolean editSaved(String id, String entryScope, class_437 parent) {
        if (!entryScope.equals(WaypointsModule.currentScope())) {
            return false;
        }
        for (WaypointSettings.Entry entry : WaypointsModule.currentEntries()) {
            if (!entry.id.equals(id)) continue;
            class_310 mc = class_310.method_1551();
            mc.method_1507((class_437)new WaypointEditScreen(parent, entry, false));
            return true;
        }
        return false;
    }

    public static boolean removeSaved(String id, String entryScope) {
        if (!entryScope.equals(WaypointsModule.currentScope())) {
            return false;
        }
        boolean removed = BloodHelperClient.config().waypoints.entries.removeIf(e -> e.id.equals(id) && e.scope.equals(entryScope));
        if (removed) {
            WaypointsModule.changed();
        }
        return removed;
    }

    public static void createAtPlayer(class_437 parent) {
        class_310 client = class_310.method_1551();
        if (client.field_1724 == null || client.field_1687 == null || WaypointsModule.currentScope().isEmpty()) {
            return;
        }
        class_2338 pos = client.field_1724.method_24515();
        WaypointSettings.Entry entry = new WaypointSettings.Entry();
        entry.name = HudStyle.tr("\u041d\u043e\u0432\u0430\u044f \u043c\u0435\u0442\u043a\u0430", "New waypoint");
        entry.scope = scope;
        entry.dimension = WaypointsModule.currentDimension();
        entry.x = pos.method_10263();
        entry.y = pos.method_10264();
        entry.z = pos.method_10260();
        entry.color = BloodHelperClient.config().accent | 0xFF000000;
        client.method_1507((class_437)new WaypointEditScreen(parent, entry, true));
    }

    public static Ping ping() {
        class_310 mc = class_310.method_1551();
        WaypointSettings settings = BloodHelperClient.config().waypoints;
        return settings.enabled && settings.quickPing && connection == mc.method_1562() && mc.field_1724 != null && ping != null && System.nanoTime() < ping.expiresAt() && ping.scope().equals(WaypointsModule.currentScope()) && ping.dimension().equals(WaypointsModule.dimension(mc)) ? ping : null;
    }

    public static double pingSeconds() {
        Ping current = WaypointsModule.ping();
        return current == null ? 0.0 : Math.max(0.0, (double)(current.expiresAt() - System.nanoTime()) / 1.0E9);
    }

    public static void reset() {
        connection = null;
        previousDimension = "";
        scope = "";
        visible = List.of();
        ping = null;
        dirty = true;
        WorldWaypoints.reset();
    }

    public static void markLookedBlock(class_310 client) {
        WaypointSettings settings = BloodHelperClient.config().waypoints;
        if (!settings.enabled || !settings.quickPing || client.field_1724 == null || client.field_1687 == null || client.field_1755 != null || WaypointsModule.currentScope().isEmpty()) {
            return;
        }
        class_4184 camera = client.field_1773.method_19418();
        class_243 origin = camera.method_71156();
        Vector3f forward = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)camera.method_23767());
        class_243 target = origin.method_1031((double)(forward.x * 160.0f), (double)(forward.y * 160.0f), (double)(forward.z * 160.0f));
        class_3965 hit = client.field_1687.method_17742(new class_3959(origin, target, class_3959.class_3960.field_17559, class_3959.class_242.field_1348, (class_1297)client.field_1724));
        if (hit.method_17783() != class_239.class_240.field_1332 || !client.field_1687.method_22340(hit.method_17777())) {
            return;
        }
        class_2338 pos = hit.method_17777();
        if (client.field_1724.method_5715()) {
            WaypointSettings.Entry entry = new WaypointSettings.Entry();
            entry.name = HudStyle.tr("\u041d\u043e\u0432\u0430\u044f \u043c\u0435\u0442\u043a\u0430", "New waypoint");
            entry.scope = scope;
            entry.dimension = WaypointsModule.dimension(client);
            entry.x = pos.method_10263();
            entry.y = pos.method_10264();
            entry.z = pos.method_10260();
            entry.color = BloodHelperClient.config().accent | 0xFF000000;
            client.method_1507((class_437)new WaypointEditScreen(null, entry, true));
            return;
        }
        double x = (double)pos.method_10263() + 0.5;
        double y = (double)pos.method_10264() + 1.15;
        double z = (double)pos.method_10260() + 0.5;
        if (WaypointsModule.ping() != null && ping.x() == x && ping.y() == y && ping.z() == z) {
            ping = null;
            return;
        }
        ping = new Ping(x, y, z, scope, WaypointsModule.dimension(client), System.nanoTime() + (long)Math.clamp((long)settings.pingSeconds, 3, 120) * 1000000000L);
    }

    public static boolean canMarkDeath(DeathRecapModule.Snapshot snapshot) {
        return BloodHelperClient.config().waypoints.enabled && snapshot != null && DeathRecapModule.isCurrentConnection(snapshot) && !WaypointsModule.currentScope().isEmpty() && BloodHelperClient.config().waypoints.entries.size() < 256;
    }

    public static void markDeath(DeathRecapModule.Snapshot snapshot, class_437 parent) {
        if (!WaypointsModule.canMarkDeath(snapshot)) {
            return;
        }
        WaypointSettings.Entry entry = new WaypointSettings.Entry();
        entry.name = HudStyle.tr("\u041c\u0435\u0441\u0442\u043e \u0441\u043c\u0435\u0440\u0442\u0438", "Death location");
        entry.scope = WaypointsModule.currentScope();
        entry.dimension = snapshot.dimension();
        entry.x = snapshot.x();
        entry.y = snapshot.y();
        entry.z = snapshot.z();
        entry.color = -887175;
        if (WaypointsModule.save(entry, true)) {
            WaypointsModule.openScreen(parent);
        }
    }

    static boolean save(WaypointSettings.Entry entry, boolean create) {
        WaypointSettings settings = BloodHelperClient.config().waypoints;
        if (entry == null || !entry.scope.equals(WaypointsModule.currentScope()) || !WaypointSettings.validCoordinates(entry.x, entry.y, entry.z)) {
            return false;
        }
        if (create) {
            if (settings.entries.size() >= 256) {
                return false;
            }
            settings.entries.add(entry.copy());
        } else {
            boolean replaced = false;
            for (int i = 0; i < settings.entries.size(); ++i) {
                if (!settings.entries.get((int)i).id.equals(entry.id) || !settings.entries.get((int)i).scope.equals(entry.scope)) continue;
                settings.entries.set(i, entry.copy());
                replaced = true;
                break;
            }
            if (!replaced) {
                return false;
            }
        }
        WaypointsModule.changed();
        return true;
    }

    static {
        scope = "";
        previousDimension = "";
        visible = List.of();
        dirty = true;
    }

    public record Ping(double x, double y, double z, String scope, String dimension, long expiresAt) {
    }
}

