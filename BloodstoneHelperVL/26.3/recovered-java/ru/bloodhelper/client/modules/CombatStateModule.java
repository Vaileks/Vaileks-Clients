/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.Identifier
 *  net.minecraft.world.item.ItemCooldowns
 */
package ru.bloodhelper.client.modules;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.gui.PvpEndNotice;
import ru.bloodhelper.client.mixin.CooldownInstanceAccessor;
import ru.bloodhelper.client.mixin.ItemCooldownsAccessor;
import ru.bloodhelper.client.modules.CombatStateClock;

public final class CombatStateModule {
    public static final int BLOODSTONE_PVP_SECONDS = 30;
    public static final int BLOODSTONE_PVP_TICKS = 600;
    private static final CombatStateClock CLOCK = new CombatStateClock();
    private static String configuredGroup;
    private static Identifier group;
    private static Object contextPlayer;
    private static Object contextWorld;
    private static Object contextConnection;
    private static Object contextConfig;

    private CombatStateModule() {
    }

    public static void tick(Minecraft client) {
        if (client == null || !client.isSameThread()) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        if (!((config.pvpTimerEnabled() || config.combatLayoutEnabled() || config.musicDucking.enabled || config.backgroundAlerts.enabled && config.backgroundAlerts.combat || config.combatHud.enabled && config.combatHud.blastLeggings.enabled && config.combatHud.blastLeggings.onlyInCombat) && client.player != null && client.level != null && client.getConnection() != null && client.player.isAlive() && !client.player.isDeadOrDying() && !client.player.isRemoved())) {
            CombatStateModule.reset();
            return;
        }
        if (contextPlayer != client.player || contextWorld != client.level || contextConnection != client.getConnection() || contextConfig != config || !Objects.equals(configuredGroup, config.pvp.cooldownGroup)) {
            CombatStateModule.reset();
            contextPlayer = client.player;
            contextWorld = client.level;
            contextConnection = client.getConnection();
            contextConfig = config;
            configuredGroup = config.pvp.cooldownGroup;
            Identifier identifier = group = configuredGroup == null ? null : Identifier.tryParse((String)configuredGroup);
        }
        if (!config.pvpTimerEnabled() || !config.pvp.endNotification) {
            PvpEndNotice.clear();
        }
        if (group == null) {
            CLOCK.clear();
            return;
        }
        ItemCooldownsAccessor manager = (ItemCooldownsAccessor)client.player.getCooldowns();
        Object cooldown = manager.bloodhelper$cooldowns().get(group);
        boolean wasActive = CLOCK.active();
        if (cooldown == null) {
            CLOCK.advance(manager.bloodhelper$tickCount());
        } else {
            CooldownInstanceAccessor timer = (CooldownInstanceAccessor)cooldown;
            CLOCK.sample(manager.bloodhelper$tickCount(), timer.bloodhelper$startTime(), timer.bloodhelper$endTime(), "minecraft:trident".equals(configuredGroup));
        }
        if (CLOCK.active()) {
            PvpEndNotice.clear();
        } else if (wasActive) {
            CombatStateModule.announceEnd();
        }
    }

    public static void cooldownRemoved(ItemCooldowns manager, Identifier removedGroup) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !client.isSameThread() || client.player == null || client.player.getCooldowns() != manager || !Objects.equals(group, removedGroup)) {
            return;
        }
        boolean wasActive = CLOCK.active();
        CLOCK.removed(((ItemCooldownsAccessor)manager).bloodhelper$tickCount());
        if (wasActive && !CLOCK.active() && contextPlayer == client.player && contextWorld == client.level && contextConnection == client.getConnection() && contextConfig == BloodHelperClient.config() && client.player.isAlive() && !client.player.isDeadOrDying() && !client.player.isRemoved() && client.level != null && client.getConnection() != null && Objects.equals(configuredGroup, BloodHelperClient.config().pvp.cooldownGroup)) {
            CombatStateModule.announceEnd();
        }
    }

    public static void cooldownAdded(ItemCooldowns manager, Identifier addedGroup, int duration) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || !client.isSameThread() || client.player == null || client.player.getCooldowns() != manager || !addedGroup.toString().equals(BloodHelperClient.config().pvp.cooldownGroup)) {
            return;
        }
        if (duration <= 0) {
            CombatStateModule.cooldownRemoved(manager, addedGroup);
        } else {
            CombatStateModule.tick(client);
        }
    }

    private static void announceEnd() {
        ClientConfig config = BloodHelperClient.config();
        if (config.pvpTimerEnabled() && config.pvp.endNotification) {
            PvpEndNotice.show();
        }
    }

    public static boolean active() {
        return CLOCK.active();
    }

    public static double secondsRemaining() {
        return CLOCK.secondsRemaining();
    }

    public static float remainingFraction() {
        return CLOCK.remainingFraction();
    }

    public static void reset() {
        CLOCK.clear();
        configuredGroup = null;
        group = null;
        contextConfig = null;
        contextConnection = null;
        contextWorld = null;
        contextPlayer = null;
        PvpEndNotice.clear();
    }
}

