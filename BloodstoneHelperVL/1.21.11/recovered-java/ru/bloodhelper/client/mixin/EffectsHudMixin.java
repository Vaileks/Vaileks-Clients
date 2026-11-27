/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  net.minecraft.class_9779
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;

@Mixin(value={class_329.class})
public abstract class EffectsHudMixin {
    @Inject(method={"method_1765"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$effects(class_332 graphics, class_9779 delta, CallbackInfo ci) {
        ClientConfig c = BloodHelperClient.config();
        if (c.effects.enabled && c.effects.showHud && c.effects.hideVanilla) {
            ci.cancel();
        }
    }
}

