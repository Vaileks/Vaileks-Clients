/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket
 *  net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket
 *  net.minecraft.network.protocol.game.ClientboundSetHealthPacket
 *  net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;
import ru.bloodhelper.client.modules.CombatTimelineModule;
import ru.bloodhelper.client.modules.DeathRecapModule;
import ru.bloodhelper.client.modules.DeathScreenshotModule;

@Mixin(value={ClientPacketListener.class})
public abstract class DeathRecapMixin {
    @Inject(method={"handlePlayerCombatKill"}, at={@At(value="TAIL")})
    private void bloodhelper$ownDeathMessage(ClientboundPlayerCombatKillPacket packet, CallbackInfo callback) {
        DeathScreenshotModule.tick(Minecraft.getInstance());
        DeathRecapModule.receiveDeathMessage(packet.playerId(), packet.message());
    }

    @Inject(method={"handleSetHealth"}, at={@At(value="TAIL")})
    private void bloodhelper$observeDeathHealth(ClientboundSetHealthPacket packet, CallbackInfo callback) {
        ClientConfig settings = BloodHelperClient.config();
        if (settings.deathScreenshot.enabled || settings.deathRecap.enabled) {
            DeathRecapModule.tick(Minecraft.getInstance());
        }
    }

    @Inject(method={"handleEntityEvent"}, at={@At(value="TAIL")})
    private void bloodhelper$observeOwnTotem(ClientboundEntityEventPacket packet, CallbackInfo callback) {
        if (BloodHelperClient.config().deathRecap.enabled && BloodHelperClient.config().combatTimeline.enabled) {
            CombatTimelineModule.receiveStatus(Minecraft.getInstance(), packet);
        }
    }

    @Inject(method={"handleUpdateMobEffect"}, at={@At(value="TAIL")})
    private void bloodhelper$observeOwnEffect(ClientboundUpdateMobEffectPacket packet, CallbackInfo callback) {
        Minecraft client = Minecraft.getInstance();
        if (BloodHelperClient.config().deathRecap.enabled && BloodHelperClient.config().combatTimeline.enabled && client.player != null && packet.getEntityId() == client.player.getId()) {
            CombatTimelineModule.effectsChanged(client, false);
        }
    }

    @Inject(method={"handleRemoveMobEffect"}, at={@At(value="TAIL")})
    private void bloodhelper$observeOwnEffectRemoval(ClientboundRemoveMobEffectPacket packet, CallbackInfo callback) {
        Minecraft client = Minecraft.getInstance();
        if (BloodHelperClient.config().deathRecap.enabled && BloodHelperClient.config().combatTimeline.enabled && client.player != null && packet.entityId() == client.player.getId()) {
            CombatTimelineModule.effectsChanged(client, true);
        }
    }
}

