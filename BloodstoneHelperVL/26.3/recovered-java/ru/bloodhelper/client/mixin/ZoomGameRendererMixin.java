package ru.bloodhelper.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.bloodhelper.client.modules.ZoomModule;

@Mixin(value={GameRenderer.class})
abstract class ZoomGameRendererMixin {
    ZoomGameRendererMixin() {
    }

    @ModifyExpressionValue(method={"bobView"}, at={@At(value="FIELD", target="Lnet/minecraft/client/renderer/state/level/CameraEntityRenderState;bob:F")})
    private float bloodhelper$relativeViewBobbing(float original) {
        return (float)((double)original * ZoomModule.bobbingScale());
    }
}

