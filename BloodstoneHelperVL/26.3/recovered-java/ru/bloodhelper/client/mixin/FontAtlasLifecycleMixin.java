package ru.bloodhelper.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.CustomFont;

@Mixin(value={GameRenderer.class})
public abstract class FontAtlasLifecycleMixin {
    @Inject(method={"render"}, at={@At(value="RETURN")})
    private void bloodhelper$finishFontFrame(CallbackInfo ci) {
        CustomFont.endFrame();
    }
}

