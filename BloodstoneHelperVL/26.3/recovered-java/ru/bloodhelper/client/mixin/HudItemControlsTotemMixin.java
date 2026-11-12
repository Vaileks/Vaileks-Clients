/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.mojang.blaze3d.platform.Window
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ScreenEffectRenderer
 *  net.minecraft.client.renderer.SubmitNodeCollector
 *  net.minecraft.client.renderer.state.level.PlayerRenderState
 *  net.minecraft.world.item.Items
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Constant
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyConstant
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.config.HudItemControlsSettings;
import ru.bloodhelper.client.modules.HudItemControlsModule;
import ru.bloodhelper.client.modules.TotemDurationState;

@Mixin(value={ScreenEffectRenderer.class})
abstract class HudItemControlsTotemMixin {
    @Unique
    private boolean bloodhelper$custom;
    @Unique
    private int bloodhelper$duration = 40;

    HudItemControlsTotemMixin() {
    }

    @Inject(method={"renderItemActivationAnimation"}, at={@At(value="HEAD")})
    private void bloodhelper$state(PlayerRenderState state, PoseStack pose, float partialTick, SubmitNodeCollector collector, CallbackInfo ci) {
        this.bloodhelper$custom = state.itemActivation != null && state.itemActivation.item.is((Object)Items.TOTEM_OF_UNDYING) && HudItemControlsModule.settings().totem.enabled;
        Minecraft mc = Minecraft.getInstance();
        this.bloodhelper$duration = state.itemActivation != null && state.itemActivation.item.is((Object)Items.TOTEM_OF_UNDYING) && mc.player != null ? ((TotemDurationState)mc.player.itemActivation()).bloodhelper$duration() : 40;
    }

    @Unique
    private boolean bloodhelper$customTotem() {
        return this.bloodhelper$custom;
    }

    @ModifyConstant(method={"renderItemActivationAnimation"}, constant={@Constant(intValue=40)})
    private int bloodhelper$elapsed(int original) {
        return this.bloodhelper$duration;
    }

    @ModifyConstant(method={"renderItemActivationAnimation"}, constant={@Constant(floatValue=40.0f)})
    private float bloodhelper$progress(float original) {
        return this.bloodhelper$duration;
    }

    @WrapOperation(method={"renderItemActivationAnimation"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V")})
    private void bloodhelper$position(PoseStack pose, float x, float y, float z, Operation<Void> original) {
        if (this.bloodhelper$customTotem()) {
            HudItemControlsSettings.Totem s = HudItemControlsModule.settings().totem;
            Window window = Minecraft.getInstance().getWindow();
            double halfHeight = (double)(-z) * Math.tan(Math.toRadians(HudItemControlsModule.hudFov / 2.0f));
            x += (float)((2.0 * s.x - 1.0) * halfHeight * (double)window.getWidth() / Math.max(1.0, (double)window.getHeight()));
            y += (float)((1.0 - 2.0 * s.y) * halfHeight);
        }
        original.call(new Object[]{pose, Float.valueOf(x), Float.valueOf(y), Float.valueOf(z)});
    }

    @WrapOperation(method={"renderItemActivationAnimation"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V")})
    private void bloodhelper$size(PoseStack pose, float x, float y, float z, Operation<Void> original) {
        float scale = this.bloodhelper$customTotem() ? (float)HudItemControlsModule.settings().totem.scale : 1.0f;
        original.call(new Object[]{pose, Float.valueOf(x * scale), Float.valueOf(y * scale), Float.valueOf(z * scale)});
    }
}

