package pyz1k.extensions.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.FeatureModules;

@Mixin(EntityRenderer.class)
public abstract class NamesMixin {
    @Inject(method="updateRenderState",at=@At("TAIL"))
    private void vaileks$label(Entity entity,EntityRenderState state,float delta,CallbackInfo ci){
        var m=FeatureModules.names;var mc=MinecraftClient.getInstance();
        if(m==null||!m.isEnable()||!(entity instanceof PlayerEntity)||entity==mc.player||entity.isInvisible())return;
        float distance=m.distance.getValue();
        if(state.squaredDistanceToCamera>distance*distance){state.displayName=null;return;}
        state.displayName=entity.getDisplayName();
        state.nameLabelPos=new Vec3d(0,entity.getHeight()+0.35,0);
    }
    @Inject(method="renderLabelIfPresent",at=@At("HEAD"),cancellable=true)
    private void vaileks$draw(EntityRenderState state,MatrixStack pose,OrderedRenderCommandQueue queue,CameraRenderState camera,CallbackInfo ci){
        var m=FeatureModules.names;
        if(m==null||!m.isEnable()||!(state instanceof PlayerEntityRenderState)||state.displayName==null||state.nameLabelPos==null)return;
        float distance=m.distance.getValue();if(state.squaredDistanceToCamera>distance*distance){ci.cancel();return;}
        var font=MinecraftClient.getInstance().textRenderer;
        pose.push();pose.translate(state.nameLabelPos);pose.multiply(camera.orientation);
        float scale=0.025f*m.scale.getValue();pose.scale(scale,-scale,scale);
        queue.submitText(pose,-font.getWidth(state.displayName)*0.5f,0,state.displayName.asOrderedText(),true,
            m.throughWalls.isValue()?TextRenderer.TextLayerType.SEE_THROUGH:TextRenderer.TextLayerType.NORMAL,
            0xf000f0,m.color.getColor(),m.background.isValue()?0x9908090c:0,0);
        pose.pop();ci.cancel();
    }
}
