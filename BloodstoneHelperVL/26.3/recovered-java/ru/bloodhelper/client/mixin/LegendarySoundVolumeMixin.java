/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.client.sounds.ChannelAccess$ChannelHandle
 *  net.minecraft.client.sounds.SoundEngine
 *  net.minecraft.resources.Identifier
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
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.LegendarySoundVolume;

@Mixin(value={SoundEngine.class})
public abstract class LegendarySoundVolumeMixin {
    @Shadow
    private boolean loaded;
    @Shadow
    @Final
    private Map<SoundInstance, ChannelAccess.ChannelHandle> instanceToChannel;
    @Unique
    private LegendarySoundVolume.Snapshot bloodhelper$appliedGains = LegendarySoundVolume.snapshot();

    @Shadow
    protected abstract float calculateVolume(SoundInstance var1);

    @ModifyExpressionValue(method={"play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/sounds/SoundEngine;calculateVolume(FLnet/minecraft/sounds/SoundSource;)F")})
    private float bloodhelper$initialGain(float vanillaGain, SoundInstance sound) {
        return LegendarySoundVolume.active() ? LegendarySoundVolume.scale(vanillaGain, sound.getIdentifier(), sound.getSound() == null ? null : sound.getSound().getLocation()) : vanillaGain;
    }

    @Inject(method={"calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F"}, at={@At(value="RETURN")}, cancellable=true)
    private void bloodhelper$updatedGain(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        if (LegendarySoundVolume.active()) {
            cir.setReturnValue((Object)Float.valueOf(LegendarySoundVolume.scale(cir.getReturnValueF(), sound.getIdentifier(), sound.getSound() == null ? null : sound.getSound().getLocation())));
        }
    }

    @Inject(method={"tick(Z)V"}, at={@At(value="TAIL")})
    private void bloodhelper$refreshPlaying(boolean paused, CallbackInfo ci) {
        LegendarySoundVolume.Snapshot previous;
        LegendarySoundVolume.Snapshot next = LegendarySoundVolume.snapshot();
        if (next == (previous = this.bloodhelper$appliedGains)) {
            return;
        }
        this.bloodhelper$appliedGains = next;
        if (!this.loaded || this.instanceToChannel.isEmpty()) {
            return;
        }
        for (Map.Entry<SoundInstance, ChannelAccess.ChannelHandle> entry : this.instanceToChannel.entrySet()) {
            Identifier file;
            SoundInstance sound = entry.getKey();
            Identifier id = sound.getIdentifier();
            Identifier identifier = file = sound.getSound() == null ? null : sound.getSound().getLocation();
            if (previous.multiplier(id, file) == next.multiplier(id, file)) continue;
            float gain = this.calculateVolume(sound);
            entry.getValue().execute(channel -> channel.setVolume(gain));
        }
    }
}

