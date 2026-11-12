/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  net.minecraft.class_2960
 *  net.minecraft.class_329
 *  net.minecraft.class_332
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  ru.bloodhelper.client.modules.CrosshairRenderer
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.class_2960;
import net.minecraft.class_329;
import net.minecraft.class_332;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.CrosshairRenderer;

@Mixin(value={class_329.class})
public abstract class CrosshairMixin {
    @WrapOperation(method={"method_1736"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_332;method_52706(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/class_2960;IIII)V")}, require=0)
    private void bloodhelper$crosshair(class_332 graphics, RenderPipeline pipeline, class_2960 sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (sprite.equals((Object)class_2960.method_60656((String)"hud/crosshair")) && CrosshairRenderer.enabled()) {
            CrosshairRenderer.render((class_332)graphics);
        } else {
            original.call(new Object[]{graphics, pipeline, sprite, x, y, width, height});
        }
    }

    @ModifyExpressionValue(method={"method_1736"}, at={@At(value="INVOKE", target="Lnet/minecraft/class_5498;method_31034()Z")}, require=0)
    private boolean bloodhelper$thirdPerson(boolean firstPerson) {
        return firstPerson || CrosshairRenderer.showInF5();
    }
}

