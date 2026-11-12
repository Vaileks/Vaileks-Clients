/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.DebugScreenOverlay
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={DebugScreenOverlay.class})
public abstract class StreamerDebugMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideDebug(CallbackInfo callback) {
        if (StreamerMode.hideDebug()) {
            callback.cancel();
        }
    }
}

