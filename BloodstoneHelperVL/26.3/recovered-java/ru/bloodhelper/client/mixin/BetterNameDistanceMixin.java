/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.bloodhelper.client.modules.BetterNameVisibilityModule;

@Mixin(value={EntityRenderer.class})
public abstract class BetterNameDistanceMixin {
    @ModifyVariable(method={"extractNameTags(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FDD)V"}, at={@At(value="HEAD")}, ordinal=0, argsOnly=true)
    private double bloodhelper$nameDistance(double original) {
        return BetterNameVisibilityModule.active() && BetterNameVisibilityModule.settings().unlimitedDistance ? Double.MAX_VALUE : original;
    }
}

