package ru.bloodhelper.client.mixin;

import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.CustomFont;

@Mixin(value={class_757.class})
public abstract class FontAtlasLifecycleMixin {
    @Inject(method={"method_3192"}, at={@At(value="RETURN")})
    private void bloodhelper$finishFontFrame(class_9779 delta, boolean renderLevel, CallbackInfo ci) {
        CustomFont.endFrame();
    }
}

