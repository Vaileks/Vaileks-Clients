/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.PlayerTabOverlay
 *  net.minecraft.client.multiplayer.PlayerInfo
 *  net.minecraft.network.chat.Component
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.circles.BloodstoneCircles;

@Mixin(value={PlayerTabOverlay.class})
public abstract class CirclesTabMixin {
    @Inject(method={"getNameForDisplay"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$circles(PlayerInfo info, CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue((Object)BloodstoneCircles.rewrite((Component)cir.getReturnValue(), true));
    }
}

