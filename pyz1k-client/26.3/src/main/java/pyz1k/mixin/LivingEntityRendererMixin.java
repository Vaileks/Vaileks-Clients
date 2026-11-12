/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.entity.LivingEntityRenderer
 *  net.minecraft.client.renderer.entity.state.LivingEntityRenderState
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package pyz1k.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.cosmetics.CosmeticsManager;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin {
    @Inject(method={"extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void pyz1k$applyLocalCosmeticHat(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || entity != minecraft.player) {
            return;
        }
        ItemStack stack = CosmeticsManager.getInstance().getSelectedStack();
        if (stack.isEmpty()) {
            return;
        }
        state.wornHeadType = null;
        state.wornHeadProfile = null;
        minecraft.getItemModelResolver().updateForLiving(state.headItem, stack, ItemDisplayContext.HEAD, entity);
    }
}

