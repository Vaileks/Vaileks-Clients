package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;

@Mixin(value={EntityHitboxDebugRenderer.class})
abstract class VanillaHitboxesMixin {
    VanillaHitboxesMixin() {
    }

    @Inject(method={"emitGizmos"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$replaceVanilla(CallbackInfo ci) {
        if (BloodHelperClient.config().hitboxes.enabled) {
            ci.cancel();
        }
    }
}

