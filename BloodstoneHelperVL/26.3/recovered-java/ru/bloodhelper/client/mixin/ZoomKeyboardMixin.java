/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyboardHandler
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.input.KeyEvent
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={KeyboardHandler.class})
abstract class ZoomKeyboardMixin {
    ZoomKeyboardMixin() {
    }

    @Inject(method={"keyPress"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$zoomKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (ZoomModule.keyAction(Minecraft.getInstance(), window, action, event)) {
            ci.cancel();
        }
    }
}

