package ru.bloodhelper.client.mixin;

import net.minecraft.class_340;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_340.class})
public abstract class StreamerDebugMixin {
    @Inject(method={"method_1846"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideDebug(CallbackInfo callback) {
        if (StreamerMode.hideDebug()) {
            callback.cancel();
        }
    }
}

