package pyz1k.extensions.mixin;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;import net.minecraft.client.render.entity.ItemEntityRenderer;import net.minecraft.entity.ItemEntity;import net.minecraft.client.util.math.MatrixStack;import net.minecraft.client.render.command.OrderedRenderCommandQueue;import net.minecraft.client.render.state.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.*;
@Mixin(ItemEntityRenderer.class)
public class GroundLootRendererMixin {
 @Inject(method="updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V",at=@At("TAIL"))
 private void lootGloow$capture(ItemEntity e,ItemEntityRenderState state,float tick,CallbackInfo ci){((GroundLootState)(Object)state).lootGloow$setScale(LootGloow.scale(e));}
 @Inject(method="render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",at=@At("HEAD"))
 private void lootGloow$push(ItemEntityRenderState state,MatrixStack pose,OrderedRenderCommandQueue queue,CameraRenderState camera,CallbackInfo ci){float s=((GroundLootState)(Object)state).lootGloow$getScale();if(s!=1){pose.push();pose.scale(s,s,s);}}
 @Inject(method="render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",at=@At("RETURN"))
 private void lootGloow$pop(ItemEntityRenderState state,MatrixStack pose,OrderedRenderCommandQueue queue,CameraRenderState camera,CallbackInfo ci){if(((GroundLootState)(Object)state).lootGloow$getScale()!=1)pose.pop();}
}
