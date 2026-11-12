package pyz1k.extensions.mixin;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.*;
@Mixin(InGameHud.class)
public abstract class WelcomeMixin {
    @Inject(method="render",at=@At("HEAD"),cancellable=true)
    private void vaileks$hide(DrawContext ctx,RenderTickCounter counter,CallbackInfo ci){if(ScreenshotMode.active())ci.cancel();}
    @Inject(method="render",at=@At("TAIL"))
    private void vaileks$welcome(DrawContext ctx,RenderTickCounter counter,CallbackInfo ci){if(!ScreenshotMode.active()){WelcomeOverlay.render(ctx);FeatureAlerts.render(ctx);}}
}
