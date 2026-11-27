package pyz1k.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.cosmetics.CosmeticsManager;

@Mixin(value={HumanoidArmorLayer.class})
public abstract class HumanoidArmorLayerMixin {
    @Inject(method={"renderArmorPiece"}, at={@At(value="HEAD")}, cancellable=true)
    private void pyz1k$hideLocalHelmetForCosmetic(PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack, EquipmentSlot slot, int packedLight, HumanoidRenderState state, CallbackInfo ci) {
        boolean cosmeticVisible;
        if (!(state instanceof AvatarRenderState)) {
            return;
        }
        AvatarRenderState avatarState = (AvatarRenderState)state;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || avatarState.id != minecraft.player.getId()) {
            return;
        }
        CosmeticsManager cosmetics = CosmeticsManager.getInstance();
        if (cosmetics.isCustomModelAvailable()) {
            ci.cancel();
            return;
        }
        if (slot != EquipmentSlot.HEAD) {
            return;
        }
        if (cosmetics.getSelected() == null) {
            return;
        }
        boolean bl = cosmeticVisible = !cosmetics.isOnlyWithHelmet() || !stack.isEmpty();
        if (cosmeticVisible) {
            ci.cancel();
        }
    }
}

