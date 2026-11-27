/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_1282
 *  net.minecraft.class_1297
 *  net.minecraft.class_1309
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.PlayerMetrics;
import ru.bloodhelper.client.skins.VeritiAnimation;
import ru.bloodhelper.client.visuals.AbilityTracker;

@Mixin(value={class_1309.class})
public class LegendaryDamageMixin {
    @Inject(method={"method_48922"}, at={@At(value="HEAD")})
    private void bloodhelper$onDamage(class_1282 source, CallbackInfo ci) {
        class_1309 self = (class_1309)this;
        if (self.method_73183().method_8608()) {
            AbilityTracker.onDamage((class_1297)self, source);
            VeritiAnimation.damage((class_1297)self, source);
            PlayerMetrics.damage((class_1297)self, source);
        }
    }
}

