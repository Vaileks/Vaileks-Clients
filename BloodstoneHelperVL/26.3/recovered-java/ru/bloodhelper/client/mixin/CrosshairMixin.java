/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.renderpearl.api.pipeline.RenderPipeline
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.Hud
 *  net.minecraft.resources.Identifier
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.CrosshairRenderer;

@Mixin(value={Hud.class})
public abstract class CrosshairMixin {
    @WrapOperation(method={"extractCrosshair"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal=0)}, require=1)
    private void bloodhelper$crosshair(GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original) {
        if (CrosshairRenderer.enabled()) {
            CrosshairRenderer.render(graphics);
        } else {
            original.call(new Object[]{graphics, pipeline, sprite, x, y, width, height});
        }
    }

    @ModifyExpressionValue(method={"extractCrosshair"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/CameraType;isFirstPerson()Z")}, require=1)
    private boolean bloodhelper$thirdPerson(boolean firstPerson) {
        return firstPerson || CrosshairRenderer.showInF5();
    }
}

