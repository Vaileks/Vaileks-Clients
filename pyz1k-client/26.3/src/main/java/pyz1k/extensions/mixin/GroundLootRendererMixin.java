package pyz1k.extensions.mixin;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;import net.minecraft.client.renderer.entity.ItemEntityRenderer;import net.minecraft.world.entity.item.ItemEntity;import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.SubmitNodeCollector;import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.*;
@Mixin(ItemEntityRenderer.class)
public class GroundLootRendererMixin {
 @Inject(method="extractRenderState(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;F)V",at=@At("TAIL"))
 private void lootGloow$capture(ItemEntity e,ItemEntityRenderState state,float tick,CallbackInfo ci){((GroundLootState)(Object)state).lootGloow$setScale(LootGloow.scale(e));}
 @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("HEAD"))
 private void lootGloow$push(ItemEntityRenderState state,PoseStack pose,SubmitNodeCollector queue,CameraRenderState camera,CallbackInfo ci){float s=((GroundLootState)(Object)state).lootGloow$getScale();if(s!=1){pose.pushPose();pose.scale(s,s,s);}}
 @Inject(method="submit(Lnet/minecraft/client/renderer/entity/state/ItemEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",at=@At("RETURN"))
 private void lootGloow$pop(ItemEntityRenderState state,PoseStack pose,SubmitNodeCollector queue,CameraRenderState camera,CallbackInfo ci){if(((GroundLootState)(Object)state).lootGloow$getScale()!=1)pose.popPose();}
}
