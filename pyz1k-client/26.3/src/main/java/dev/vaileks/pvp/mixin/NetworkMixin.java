/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.network.protocol.game.ClientboundDamageEventPacket
 *  net.minecraft.network.protocol.game.ClientboundEntityEventPacket
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.extensions.pvp.mixin;

import pyz1k.extensions.pvp.Core;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
abstract class NetworkMixin {
    NetworkMixin() {
    }

    @Inject(method={"handleEntityEvent"}, at={@At(value="TAIL")})
    private void pvp$status(ClientboundEntityEventPacket p, CallbackInfo ci) {
        Core.status(p);
    }

    @Inject(method={"handleDamageEvent"}, at={@At(value="TAIL")})
    private void pvp$damage(ClientboundDamageEventPacket p, CallbackInfo ci) {
        Core.damage(p);
    }
}

