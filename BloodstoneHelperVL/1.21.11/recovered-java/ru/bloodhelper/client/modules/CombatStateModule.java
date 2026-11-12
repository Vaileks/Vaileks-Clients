/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1796
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 */
package ru.bloodhelper.client.modules;

import java.util.Objects;
import net.minecraft.class_1796;
import net.minecraft.class_2960;
import net.minecraft.class_310;
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
    private static class_2960 group;
    private static Object contextPlayer;
    private static Object contextWorld;
    private static Object contextConnection;
    private static Object contextConfig;

    private CombatStateModule() {
    }

    public static void tick(class_310 client) {
        if (client == null || !client.method_18854()) {
            return;
        }
        ClientConfig config = BloodHelperClient.config();
        if (!((config.pvpTimerEnabled() || config.combatLayoutEnabled() || config.musicDucking.enabled || config.backgroundAlerts.enabled && config.backgroundAlerts.combat || config.combatHud.enabled && config.combatHud.blastLeggings.enabled && config.combatHud.blastLeggings.onlyInCombat) && client.field_1724 != null && client.field_1687 != null && client.method_1562() != null && client.field_1724.method_5805() && !client.field_1724.method_29504() && !client.field_1724.method_31481())) {
            CombatStateModule.reset();
            return;
        }
        if (contextPlayer != client.field_1724 || contextWorld != client.field_1687 || contextConnection != client.method_1562() || contextConfig != config || !Objects.equals(configuredGroup, config.pvp.cooldownGroup)) {
            CombatStateModule.reset();
            contextPlayer = client.field_1724;
            contextWorld = client.field_1687;
            contextConnection = client.method_1562();
            contextConfig = config;
            configuredGroup = config.pvp.cooldownGroup;
            class_2960 class_29602 = group = configuredGroup == null ? null : class_2960.method_12829((String)configuredGroup);
        }
        if (!config.pvpTimerEnabled() || !config.pvp.endNotification) {
            PvpEndNotice.clear();
        }
        if (group == null) {
            CLOCK.clear();
            return;
        }
        ItemCooldownsAccessor manager = (ItemCooldownsAccessor)client.field_1724.method_7357();
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

    public static void cooldownRemoved(class_1796 manager, class_2960 removedGroup) {
        class_310 client = class_310.method_1551();
        if (client == null || !client.method_18854() || client.field_1724 == null || client.field_1724.method_7357() != manager || !Objects.equals(group, removedGroup)) {
            return;
        }
        boolean wasActive = CLOCK.active();
        CLOCK.removed(((ItemCooldownsAccessor)manager).bloodhelper$tickCount());
        if (wasActive && !CLOCK.active() && contextPlayer == client.field_1724 && contextWorld == client.field_1687 && contextConnection == client.method_1562() && contextConfig == BloodHelperClient.config() && client.field_1724.method_5805() && !client.field_1724.method_29504() && !client.field_1724.method_31481() && client.field_1687 != null && client.method_1562() != null && Objects.equals(configuredGroup, BloodHelperClient.config().pvp.cooldownGroup)) {
            CombatStateModule.announceEnd();
        }
    }

    public static void cooldownAdded(class_1796 manager, class_2960 addedGroup, int duration) {
        class_310 client = class_310.method_1551();
        if (client == null || !client.method_18854() || client.field_1724 == null || client.field_1724.method_7357() != manager || !addedGroup.toString().equals(BloodHelperClient.config().pvp.cooldownGroup)) {
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

