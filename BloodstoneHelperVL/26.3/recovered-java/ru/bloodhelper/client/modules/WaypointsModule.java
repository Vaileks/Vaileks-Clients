/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.Camera
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.storage.LevelResource
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package ru.bloodhelper.client.modules;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
    private static KeyMapping mark;
    private static KeyMapping quickMark;
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

    public static void register(KeyMapping.Category category) {
        mark = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.waypoint", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), category));
        quickMark = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.bloodhelper.quick_ping", InputConstants.Type.KEYBOARD, 10, category));
    }

    public static String pingKeyName() {
        return quickMark == null || quickMark.isUnbound() ? "None" : quickMark.getTranslatedKeyMessage().getString();
    }

    public static String keyName() {
        return mark == null || mark.isUnbound() ? "None" : mark.getTranslatedKeyMessage().getString();
    }

    public static void tick(Minecraft client) {
        boolean pressed = false;
        boolean pingPressed = false;
        if (mark != null) {
            while (mark.consumeClick()) {
                pressed = true;
            }
        }
        if (quickMark != null) {
            while (quickMark.consumeClick()) {
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
        if (!settings.enabled || client.player == null || client.level == null || scope.isEmpty()) {
            visible = List.of();
            ping = null;
            dirty = true;
            return;
        }
        if (pressed && client.gui.screen() == null) {
            WaypointsModule.createAtPlayer(null);
        }
        if (pingPressed && settings.quickPing && client.gui.screen() == null && !client.player.isSpectator()) {
            WaypointsModule.markLookedBlock(client);
        }
        if (!(ping == null || System.nanoTime() < ping.expiresAt() && scope.equals(ping.scope()) && dimension.equals(ping.dimension()) && settings.quickPing)) {
            ping = null;
        }
        if (!dirty && ++ticks % 4 != 0) {
            return;
        }
        dirty = false;
        LocalPlayer player = client.player;
        visible = settings.entries.stream().filter(e -> e.visible && WaypointMath.matches(e, scope, dimension)).sorted(Comparator.comparingDouble(e -> WaypointMath.distance(player.getX(), player.getY(), player.getZ(), e))).limit(settings.maxVisible).map(WaypointSettings.Entry::copy).toList();
    }

    private static void updateScope(Minecraft client) {
        ClientPacketListener next = client.getConnection();
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
        scope = client.getSingleplayerServer() != null ? WaypointMath.scope("local", client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize().toString()) : (client.getCurrentServer() != null ? WaypointMath.remoteScope(client.getCurrentServer().ip) : WaypointMath.scope("session", UUID.randomUUID().toString()));
    }

    public static String currentScope() {
        WaypointsModule.updateScope(Minecraft.getInstance());
        return scope;
    }

    public static String currentDimension() {
        return WaypointsModule.dimension(Minecraft.getInstance());
    }

    private static String dimension(Minecraft client) {
        return client.level == null ? "" : client.level.dimension().identifier().toString();
    }

    public static List<WaypointSettings.Entry> visible() {
        Minecraft client = Minecraft.getInstance();
        if (connection != client.getConnection() || visible.isEmpty() || !WaypointMath.matches(visible.getFirst(), scope, WaypointsModule.dimension(client))) {
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

    public static void openScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen((Screen)new WaypointScreen(parent));
    }

    public static boolean editSaved(String id, String entryScope, Screen parent) {
        if (!entryScope.equals(WaypointsModule.currentScope())) {
            return false;
        }
        for (WaypointSettings.Entry entry : WaypointsModule.currentEntries()) {
            if (!entry.id.equals(id)) continue;
            Minecraft mc = Minecraft.getInstance();
            mc.gui.setScreen((Screen)new WaypointEditScreen(parent, entry, false));
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

    public static void createAtPlayer(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || WaypointsModule.currentScope().isEmpty()) {
            return;
        }
        BlockPos pos = client.player.blockPosition();
        WaypointSettings.Entry entry = new WaypointSettings.Entry();
        entry.name = HudStyle.tr("\u041d\u043e\u0432\u0430\u044f \u043c\u0435\u0442\u043a\u0430", "New waypoint");
        entry.scope = scope;
        entry.dimension = WaypointsModule.currentDimension();
        entry.x = pos.getX();
        entry.y = pos.getY();
        entry.z = pos.getZ();
        entry.color = BloodHelperClient.config().accent | 0xFF000000;
        client.gui.setScreen((Screen)new WaypointEditScreen(parent, entry, true));
    }

    public static Ping ping() {
        Minecraft mc = Minecraft.getInstance();
        WaypointSettings settings = BloodHelperClient.config().waypoints;
        return settings.enabled && settings.quickPing && connection == mc.getConnection() && mc.player != null && ping != null && System.nanoTime() < ping.expiresAt() && ping.scope().equals(WaypointsModule.currentScope()) && ping.dimension().equals(WaypointsModule.dimension(mc)) ? ping : null;
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

    public static void markLookedBlock(Minecraft client) {
        WaypointSettings settings = BloodHelperClient.config().waypoints;
        if (!settings.enabled || !settings.quickPing || client.player == null || client.level == null || client.gui.screen() != null || WaypointsModule.currentScope().isEmpty()) {
            return;
        }
        Camera camera = client.gameRenderer.mainCamera();
        Vec3 origin = camera.position();
        Vector3f forward = new Vector3f(0.0f, 0.0f, -1.0f).rotate((Quaternionfc)camera.rotation());
        Vec3 target = origin.add((double)(forward.x * 160.0f), (double)(forward.y * 160.0f), (double)(forward.z * 160.0f));
        BlockHitResult hit = client.level.clip(new ClipContext(origin, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)client.player));
        if (hit.getType() != HitResult.Type.BLOCK || !client.level.hasChunkAt(hit.getBlockPos())) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (client.player.isShiftKeyDown()) {
            WaypointSettings.Entry entry = new WaypointSettings.Entry();
            entry.name = HudStyle.tr("\u041d\u043e\u0432\u0430\u044f \u043c\u0435\u0442\u043a\u0430", "New waypoint");
            entry.scope = scope;
            entry.dimension = WaypointsModule.dimension(client);
            entry.x = pos.getX();
            entry.y = pos.getY();
            entry.z = pos.getZ();
            entry.color = BloodHelperClient.config().accent | 0xFF000000;
            client.gui.setScreen((Screen)new WaypointEditScreen(null, entry, true));
            return;
        }
        double x = (double)pos.getX() + 0.5;
        double y = (double)pos.getY() + 1.15;
        double z = (double)pos.getZ() + 0.5;
        if (WaypointsModule.ping() != null && ping.x() == x && ping.y() == y && ping.z() == z) {
            ping = null;
            return;
        }
        ping = new Ping(x, y, z, scope, WaypointsModule.dimension(client), System.nanoTime() + (long)Math.clamp((long)settings.pingSeconds, 3, 120) * 1000000000L);
    }

    public static boolean canMarkDeath(DeathRecapModule.Snapshot snapshot) {
        return BloodHelperClient.config().waypoints.enabled && snapshot != null && DeathRecapModule.isCurrentConnection(snapshot) && !WaypointsModule.currentScope().isEmpty() && BloodHelperClient.config().waypoints.entries.size() < 256;
    }

    public static void markDeath(DeathRecapModule.Snapshot snapshot, Screen parent) {
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

