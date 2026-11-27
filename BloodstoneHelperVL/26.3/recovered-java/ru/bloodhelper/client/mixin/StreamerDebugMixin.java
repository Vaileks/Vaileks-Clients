package ru.bloodhelper.client.mixin;

import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={DebugScreenOverlay.class})
public abstract class StreamerDebugMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideDebug(CallbackInfo callback) {
        if (StreamerMode.hideDebug()) {
            callback.cancel();
        }
    }
}

