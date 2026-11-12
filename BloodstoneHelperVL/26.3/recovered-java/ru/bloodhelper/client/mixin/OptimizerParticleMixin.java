/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.particle.Particle
 *  net.minecraft.client.particle.ParticleEngine
 *  net.minecraft.core.particles.ParticleOptions
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.modules.OptimizerModule;

@Mixin(value={ParticleEngine.class})
public abstract class OptimizerParticleMixin {
    @Inject(method={"createParticle"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$ambientParticles(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz, CallbackInfoReturnable<Particle> cir) {
        if (OptimizerModule.skipParticle(options)) {
            cir.setReturnValue(null);
        }
    }
}

