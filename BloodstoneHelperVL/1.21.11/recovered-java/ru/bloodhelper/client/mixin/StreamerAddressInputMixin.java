/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_310
 *  net.minecraft.class_332
 *  net.minecraft.class_342
 *  net.minecraft.class_420
 *  net.minecraft.class_422
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_420;
import net.minecraft.class_422;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_342.class})
public abstract class StreamerAddressInputMixin {
    @Inject(method={"method_48579"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$maskAddress(class_332 g, int mouseX, int mouseY, float delta, CallbackInfo callback) {
        if (!StreamerMode.hideServerAddress()) {
            return;
        }
        class_437 screen = class_310.method_1551().field_1755;
        if (!(screen instanceof class_420) && !(screen instanceof class_422)) {
            return;
        }
        class_342 input = (class_342)this;
        UiDraw.border(g, input.method_46426(), input.method_46427(), input.method_25368(), input.method_25364(), 3, -15461351, input.method_25370() ? -7829356 : -12369077);
        CustomFont.regular().draw(g, HudStyle.tr("\u0421\u043a\u0440\u044b\u0442\u043e \u00b7 Streamer Mode", "Hidden \u00b7 Streamer Mode"), input.method_46426() + 6, (float)input.method_46427() + (float)(input.method_25364() - 10) / 2.0f, 10.0f, -4737087);
        callback.cancel();
    }
}

