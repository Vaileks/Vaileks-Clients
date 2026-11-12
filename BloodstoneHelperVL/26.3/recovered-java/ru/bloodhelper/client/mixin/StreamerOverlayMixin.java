/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.Hud
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={Hud.class})
public abstract class StreamerOverlayMixin {
    @Inject(method={"extractScoreboardSidebar", "extractTabList", "extractTitle", "extractOverlayMessage", "extractBossOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideServerOverlays(CallbackInfo callback) {
        if (StreamerMode.hideServerOverlays()) {
            callback.cancel();
        }
    }
}

