package pyz1k.extensions.mixin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import pyz1k.extensions.*;
@Mixin(MinecraftClient.class)
public abstract class ClientMixin {
    @Inject(method="tick",at=@At("TAIL"))
    private void vaileks$tick(CallbackInfo ci){ExtensionClient.tick();}
}
