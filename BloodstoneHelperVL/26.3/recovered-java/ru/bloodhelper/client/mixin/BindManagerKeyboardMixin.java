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
import ru.bloodhelper.client.gui.BloodHelperScreen;
import ru.bloodhelper.client.modules.BindManagerScreen;

@Mixin(value={KeyboardHandler.class})
abstract class BindManagerKeyboardMixin {
    BindManagerKeyboardMixin() {
    }

    @Inject(method={"keyPress"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$captureBind(long window, int action, KeyEvent event, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (BindManagerScreen.captureKeyboard(client, window, action, event) || BloodHelperScreen.captureKeyboard(client, window, action, event)) {
            ci.cancel();
        }
    }
}

