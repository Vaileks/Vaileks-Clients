package pyz1k.extensions.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.ExtensionClient;

@Mixin(value={Minecraft.class})
public abstract class ClientMixin {
    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void vaileks$tick(CallbackInfo ci) {
        ExtensionClient.tick();
    }
}

