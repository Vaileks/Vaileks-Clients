package pyz1k.extensions.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pyz1k.extensions.FeatureAlerts;
import pyz1k.extensions.ScreenshotMode;
import pyz1k.extensions.WelcomeOverlay;

@Mixin(value={Hud.class})
public abstract class WelcomeMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void vaileks$hide(GuiGraphicsExtractor ctx, DeltaTracker counter, CallbackInfo ci) {
        if (ScreenshotMode.active()) {
            ci.cancel();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void vaileks$welcome(GuiGraphicsExtractor ctx, DeltaTracker counter, CallbackInfo ci) {
        if (!ScreenshotMode.active()) {
            WelcomeOverlay.render(ctx);
            FeatureAlerts.render(ctx);
        }
    }
}

