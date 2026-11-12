/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.DeltaTracker
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.config.ClientConfig;

@Mixin(value={Hud.class})
public abstract class EffectsHudMixin {
    @Inject(method={"extractEffects"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$effects(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        ClientConfig c = BloodHelperClient.config();
        if (c.effects.enabled && c.effects.showHud && c.effects.hideVanilla) {
            ci.cancel();
        }
    }
}

