/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.access.StaffAccess;

@Mixin(value={ClientPacketListener.class})
public abstract class StaffCommandPreviewMixin {
    @Inject(method={"sendCommand"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$reviewCommand(String command, CallbackInfo callback) {
        if (StaffAccess.interceptChat("/" + command)) {
            callback.cancel();
        }
    }

    @Inject(method={"sendCommand"}, at={@At(value="RETURN")})
    private void bloodhelper$sentCommand(String command, CallbackInfo callback) {
        StaffAccess.commandSent("/" + command);
    }

    @Inject(method={"sendUnsignedCommand"}, at={@At(value="RETURN")}, require=0)
    private void bloodhelper$sentUnsigned(String command, CallbackInfoReturnable<Boolean> callback) {
        if (((Boolean)callback.getReturnValue()).booleanValue()) {
            StaffAccess.commandSent("/" + command);
        }
    }
}

