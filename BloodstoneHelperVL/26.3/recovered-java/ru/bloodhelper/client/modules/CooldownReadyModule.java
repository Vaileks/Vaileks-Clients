/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemCooldowns
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package ru.bloodhelper.client.modules;

import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
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
    private static final EquipmentSlot[] EQUIPMENT = EquipmentSlot.values();
    private static ClientConfig contextConfig;
    private static ClientConfig knownConfig;
    private static CooldownReadySettings contextSettings;
    private static CooldownReadySettings knownSettings;
    private static Object contextPlayer;
    private static Object contextConnection;
    private static Object knownConnection;
    private static ItemCooldowns contextManager;
    private static long lastSoundAt;
    private static int knownRevision;

    private CooldownReadyModule() {
    }

    public static void tick(Minecraft client) {
        CooldownReadyModule.synchronizeKnownContext(client);
        if (!CooldownReadyModule.eligible(client)) {
            CooldownReadyModule.reset();
            return;
        }
        CooldownReadyModule.synchronizeContext(client, client.player.getCooldowns());
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

    private static boolean eligible(Minecraft client) {
        return client != null && client.isSameThread() && BloodHelperClient.config().cooldownReady.enabled && client.player != null && client.player.isAlive() && !client.player.isRemoved() && client.level != null && client.getConnection() != null;
    }

    private static boolean synchronizeContext(Minecraft client, ItemCooldowns manager) {
        ClientConfig config = BloodHelperClient.config();
        if (contextConfig == config && contextSettings == config.cooldownReady && contextPlayer == client.player && contextManager == manager && contextConnection == client.getConnection()) {
            return true;
        }
        CooldownReadyModule.reset();
        CooldownReadyModule.synchronizeKnownContext(client);
        contextConfig = config;
        contextSettings = config.cooldownReady;
        contextPlayer = client.player;
        contextManager = manager;
        contextConnection = client.getConnection();
        return false;
    }

    private static void synchronizeKnownContext(Minecraft client) {
        if (client == null || !client.isSameThread()) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        if (knownConnection != client.getConnection() || knownConfig != config || knownSettings != config.cooldownReady) {
            KNOWN.clear();
            ++knownRevision;
            knownConnection = client.getConnection();
            knownConfig = config;
            knownSettings = config.cooldownReady;
        }
    }

    private static boolean ownActiveManager(ItemCooldowns manager) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !client.isSameThread()) {
            return false;
        }
        if (!CooldownReadyModule.eligible(client)) {
            CooldownReadyModule.reset();
            return false;
        }
        if (client.player.getCooldowns() != manager) {
            return false;
        }
        CooldownReadyModule.synchronizeContext(client, manager);
        return true;
    }

    public static void started(ItemCooldowns manager, Identifier group, int duration) {
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
            CooldownReadyModule.discover(Minecraft.getInstance(), manager, group);
        }
        STATE.started(id, ((ItemCooldownsAccessor)manager).bloodhelper$tickCount(), duration, config.cooldownReady.minimumSeconds * 20);
    }

    public static void cancelled(ItemCooldowns manager, Identifier group) {
        if (CooldownReadyModule.ownActiveManager(manager)) {
            STATE.cancelled(group.toString());
        }
    }

    public static void naturallyEnded(ItemCooldowns manager, Identifier group) {
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
        if (CooldownReadyModule.observedIcon(id).isEmpty()) {
            CooldownReadyModule.discover(Minecraft.getInstance(), manager, group);
        }
        if (settings.notify) {
            FeatureAlerts.push("cooldownReady", id + ":" + ((ItemCooldownsAccessor)manager).bloodhelper$tickCount(), CooldownReadyModule.label(id), HudStyle.tr(config, "\u041c\u043e\u0436\u043d\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c", "Ready to use"), "cooldown:" + id);
        }
        long now = System.nanoTime();
        if (settings.sound && settings.volume > 0.0 && (lastSoundAt == Long.MIN_VALUE || now - lastSoundAt >= 800000000L)) {
            lastSoundAt = now;
            NotificationSounds.play(Minecraft.getInstance(), NotificationSoundSettings.Event.COOLDOWN_READY, 1.3f, (float)settings.volume);
        }
    }

    private static void discover(Minecraft client, ItemCooldowns manager, Identifier group) {
        MutableComponent name;
        Item item;
        String id = group.toString();
        Metadata old = KNOWN.get(id);
        ItemStack icon = ItemStack.EMPTY;
        for (EquipmentSlot slot : EQUIPMENT) {
            ItemStack stack = client.player.getItemBySlot(slot);
            if (stack.isEmpty() || !manager.getCooldownGroup(stack).equals((Object)group)) continue;
            icon = stack.copyWithCount(1);
            break;
        }
        if (icon.isEmpty()) {
            for (ItemStack stack : client.player.getInventory().getNonEquipmentItems()) {
                if (stack.isEmpty() || !manager.getCooldownGroup(stack).equals((Object)group)) continue;
                icon = stack.copyWithCount(1);
                break;
            }
        }
        if (icon.isEmpty() && old != null && !old.icon.isEmpty()) {
            return;
        }
        if (icon.isEmpty() && (item = (Item)BuiltInRegistries.ITEM.getOptional(group).orElse(null)) != null && item != Items.AIR) {
            icon = new ItemStack((ItemLike)item);
        }
        if (old == null && KNOWN.size() >= 128) {
            KNOWN.remove(KNOWN.keySet().iterator().next());
        }
        MutableComponent mutableComponent = name = icon.isEmpty() ? null : icon.getHoverName().copy();
        if (old == null || !icon.isEmpty()) {
            KNOWN.put(id, new Metadata((Component)name, icon));
            ++knownRevision;
        }
    }

    public static ItemStack observedIcon(String group) {
        Metadata metadata = KNOWN.get(group);
        return metadata == null ? ItemStack.EMPTY : metadata.icon;
    }

    private static String label(String id) {
        Metadata metadata = KNOWN.get(id);
        String value = metadata != null && metadata.name != null ? UiLanguage.text(metadata.name).strip() : "";
        return value.isEmpty() ? id : value;
    }

    public static List<Group> groups() {
        CooldownReadyModule.synchronizeKnownContext(Minecraft.getInstance());
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

    public static void openScreen(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        if (CooldownReadyModule.eligible(client)) {
            ItemCooldowns manager = client.player.getCooldowns();
            for (Identifier group : ((ItemCooldownsAccessor)manager).bloodhelper$cooldowns().keySet()) {
                if (group.toString().equals(BloodHelperClient.config().pvp.cooldownGroup)) continue;
                CooldownReadyModule.discover(client, manager, group);
            }
        }
        client.gui.setScreen((Screen)new CooldownReadyScreen(parent));
    }

    static {
        lastSoundAt = Long.MIN_VALUE;
    }

    private record Metadata(Component name, ItemStack icon) {
    }

    public record Group(String id, String label, boolean selected) {
    }
}

