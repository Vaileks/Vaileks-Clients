/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.class_1113
 *  net.minecraft.class_1140
 *  net.minecraft.class_2960
 *  net.minecraft.class_4235$class_4236
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Map;
import net.minecraft.class_1113;
import net.minecraft.class_1140;
import net.minecraft.class_2960;
import net.minecraft.class_4235;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.LegendarySoundVolume;

@Mixin(value={class_1140.class})
public abstract class LegendarySoundVolumeMixin {
    @Shadow
    private boolean field_5563;
    @Shadow
    @Final
    private Map<class_1113, class_4235.class_4236> field_18950;
    @Unique
    private LegendarySoundVolume.Snapshot bloodhelper$appliedGains = LegendarySoundVolume.snapshot();

    @Shadow
    protected abstract float method_4853(class_1113 var1);

    @ModifyExpressionValue(method={"method_4854(Lnet/minecraft/class_1113;)Lnet/minecraft/class_1140$class_11518;"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_1140;method_43222(FLnet/minecraft/class_3419;)F")})
    private float bloodhelper$initialGain(float vanillaGain, class_1113 sound) {
        return LegendarySoundVolume.active() ? LegendarySoundVolume.scale(vanillaGain, sound.method_4775(), sound.method_4776() == null ? null : sound.method_4776().method_4767()) : vanillaGain;
    }

    @Inject(method={"method_4853(Lnet/minecraft/class_1113;)F"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$updatedGain(class_1113 sound, CallbackInfoReturnable<Float> cir) {
        if (LegendarySoundVolume.active()) {
            cir.setReturnValue((Object)Float.valueOf(LegendarySoundVolume.scale(cir.getReturnValueF(), sound.method_4775(), sound.method_4776() == null ? null : sound.method_4776().method_4767())));
        }
    }

    @Inject(method={"method_20185(Z)V"}, at={@At(value="TAIL")})
    private void bloodhelper$refreshPlaying(boolean paused, CallbackInfo ci) {
        LegendarySoundVolume.Snapshot previous;
        LegendarySoundVolume.Snapshot next = LegendarySoundVolume.snapshot();
        if (next == (previous = this.bloodhelper$appliedGains)) {
            return;
        }
        this.bloodhelper$appliedGains = next;
        if (!this.field_5563 || this.field_18950.isEmpty()) {
            return;
        }
        for (Map.Entry<class_1113, class_4235.class_4236> entry : this.field_18950.entrySet()) {
            class_2960 file;
            class_1113 sound = entry.getKey();
            class_2960 id = sound.method_4775();
            class_2960 class_29602 = file = sound.method_4776() == null ? null : sound.method_4776().method_4767();
            if (previous.multiplier(id, file) == next.multiplier(id, file)) continue;
            float gain = this.method_4853(sound);
            entry.getValue().method_19735(channel -> channel.method_19647(gain));
        }
    }
}

