/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.input.MouseButtonInfo
 *  net.minecraft.client.player.LocalPlayer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.FreeLookModule;
import ru.bloodhelper.client.modules.PlayerMetrics;

@Mixin(value={MouseHandler.class})
public class FreeLookMouseMixin {
    @Inject(method={"onButton"}, at={@At(value="HEAD")})
    private void bloodhelper$click(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (window == Minecraft.getInstance().getWindow().handle()) {
            PlayerMetrics.click(info.button(), action);
        }
    }

    @Redirect(method={"turnPlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")}, require=1)
    private void bloodhelper$freeLook(LocalPlayer player, double dx, double dy) {
        if (!FreeLookModule.turn(dx, dy)) {
            player.turn(dx, dy);
        }
    }
}

