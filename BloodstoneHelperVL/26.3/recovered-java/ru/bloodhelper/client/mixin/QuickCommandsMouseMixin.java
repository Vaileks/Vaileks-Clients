/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.MouseHandler
 *  net.minecraft.client.input.MouseButtonInfo
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.QuickCommandsModule;

@Mixin(value={MouseHandler.class})
abstract class QuickCommandsMouseMixin {
    QuickCommandsMouseMixin() {
    }

    @Inject(method={"onButton"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$wheelPress(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (QuickCommandsModule.mousePress(Minecraft.getInstance(), window, info, action)) {
            ci.cancel();
        }
    }
}

