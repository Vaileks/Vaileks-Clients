/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundDamageEventPacket
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  net.minecraft.network.protocol.game.ClientboundExplodePacket
 *  net.minecraft.network.protocol.game.ClientboundSoundEntityPacket
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.killeffects.KillEffectEngine;
import pyz1k.killeffects.ServerKillSoundFilter;

@Mixin(value={ClientPacketListener.class})
public abstract class ServerKillSoundMixin {
    @Inject(method={"handleDamageEvent"}, at={@At(value="HEAD")})
    private void pyz1k$recordDamage(ClientboundDamageEventPacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().isSameThread()) {
            KillEffectEngine.recordDamage(packet.entityId(), packet.sourceCauseId(), packet.sourceDirectId());
        }
    }

    @Inject(method={"handleExplosion"}, at={@At(value="HEAD")})
    private void pyz1k$recordExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().isSameThread()) {
            KillEffectEngine.recordExplosion(packet.center(), packet.radius());
        }
    }

    @Inject(method={"handleEntityEvent"}, at={@At(value="HEAD")})
    private void pyz1k$recordDeath(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread() && mc.level != null && packet.getEventId() == 3) {
            KillEffectEngine.recordDeath(packet.getEntity((Level)mc.level));
        }
    }

    @Inject(method={"handleSoundEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$muteKillSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        if (!Minecraft.getInstance().isSameThread()) {
            return;
        }
        if (ServerKillSoundFilter.shouldMute(((SoundEvent)packet.getSound().value()).location(), packet.getX(), packet.getY(), packet.getZ())) {
            ci.cancel();
        }
    }

    @Inject(method={"handleSoundEntityEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$muteKillEntitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isSameThread() || mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(packet.getId());
        if (entity != null && ServerKillSoundFilter.shouldMute(((SoundEvent)packet.getSound().value()).location(), entity.getX(), entity.getY(), entity.getZ())) {
            ci.cancel();
        }
    }
}

