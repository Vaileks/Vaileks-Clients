/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl
 *  net.minecraft.client.multiplayer.ServerData
 *  net.minecraft.network.Connection
 *  net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
 *  net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.resourcepack.BloodstoneResourcePack;

@Mixin(value={ClientCommonPacketListenerImpl.class})
public abstract class BloodstonePackPacketMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;
    @Shadow
    @Final
    protected Connection connection;
    @Shadow
    @Final
    protected ServerData serverData;

    @Inject(method={"handleResourcePackPush"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void bloodhelper$serverPack(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        if (BloodstoneResourcePack.offer(this.minecraft, this.connection, this.serverData, packet)) {
            ci.cancel();
        }
    }

    @Inject(method={"handleResourcePackPop"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift=At.Shift.AFTER)})
    private void bloodhelper$popPack(ClientboundResourcePackPopPacket packet, CallbackInfo ci) {
        BloodstoneResourcePack.pop(this.connection, packet.id());
    }
}

