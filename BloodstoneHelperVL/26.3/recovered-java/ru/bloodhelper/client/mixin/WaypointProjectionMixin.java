package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.visuals.WorldProjection;

@Mixin(value={LevelRenderer.class})
public abstract class WaypointProjectionMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void bloodhelper$capture(CallbackInfo ci, @Local(argsOnly=true) CameraRenderState source) {
        WorldProjection.capture(source, source.viewRotationMatrix, source.projectionMatrix);
    }
}

