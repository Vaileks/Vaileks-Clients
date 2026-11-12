/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  net.minecraft.class_10017
 *  net.minecraft.class_10055
 *  net.minecraft.class_1058
 *  net.minecraft.class_11659
 *  net.minecraft.class_11683$class_11792
 *  net.minecraft.class_1921
 *  net.minecraft.class_3879
 *  net.minecraft.class_3887
 *  net.minecraft.class_4587
 *  net.minecraft.class_583
 *  net.minecraft.class_591
 *  net.minecraft.class_922
 *  net.minecraft.class_989
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.class_10017;
import net.minecraft.class_10055;
import net.minecraft.class_1058;
import net.minecraft.class_11659;
import net.minecraft.class_11683;
import net.minecraft.class_1921;
import net.minecraft.class_3879;
import net.minecraft.class_3887;
import net.minecraft.class_4587;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_922;
import net.minecraft.class_989;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.skins.SkinMesh;
import ru.bloodhelper.client.skins.SkinsRenderer;

@Mixin(value={class_922.class})
abstract class SkinsPlayerRendererMixin {
    @Shadow
    protected class_583<?> field_4737;

    SkinsPlayerRendererMixin() {
    }

    @WrapOperation(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_11659;method_73490(Lnet/minecraft/class_3879;Ljava/lang/Object;Lnet/minecraft/class_4587;Lnet/minecraft/class_1921;IIILnet/minecraft/class_1058;ILnet/minecraft/class_11683$class_11792;)V")})
    private void bloodhelper$skin(class_11659 collector, class_3879<?> model, Object state, class_4587 poses, class_1921 type, int light, int overlay, int tint, class_1058 sprite, int outline, class_11683.class_11792 crumbling, Operation<Void> original) {
        SkinMesh mesh = SkinsRenderer.selected(state);
        if (mesh != null && model instanceof class_591) {
            class_591 playerModel = (class_591)model;
            SkinsRenderer.submit(mesh, (class_10055)state, playerModel, poses, collector, light, overlay, tint, outline);
        } else {
            original.call(new Object[]{collector, model, state, poses, type, light, overlay, tint, sprite, outline, crumbling});
        }
    }

    @WrapOperation(method={"method_4054(Lnet/minecraft/class_10042;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_3887;method_4199(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10017;FF)V")})
    private void bloodhelper$skinLayers(class_3887<?, ?> layer, class_4587 poses, class_11659 collector, int light, class_10017 state, float yaw, float pitch, Operation<Void> original) {
        if (!(this.field_4737 instanceof class_591) || SkinsRenderer.selected(state) == null || layer instanceof class_989 && SkinsRenderer.heldItems(state)) {
            original.call(new Object[]{layer, poses, collector, light, state, Float.valueOf(yaw), Float.valueOf(pitch)});
        }
    }
}

