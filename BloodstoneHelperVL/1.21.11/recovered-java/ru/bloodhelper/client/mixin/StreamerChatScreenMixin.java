/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_332
 *  net.minecraft.class_408
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_332;
import net.minecraft.class_408;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={class_408.class})
public abstract class StreamerChatScreenMixin {
    @Inject(method={"method_25394"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hideInputAndSuggestions(class_332 g, int mouseX, int mouseY, float delta, CallbackInfo callback) {
        if (!StreamerMode.hideChat()) {
            return;
        }
        String label = HudStyle.tr("Streamer Mode \u00b7 \u0427\u0430\u0442 \u0438 \u0432\u0432\u043e\u0434 \u0441\u043a\u0440\u044b\u0442\u044b", "Streamer Mode \u00b7 Chat and input hidden");
        CustomFont font = CustomFont.regular();
        String visible = font.ellipsize(label, 10.0f, Math.max(1, g.method_51421() - 22));
        UiDraw.rounded(g, 4, g.method_51443() - 22, g.method_51421() - 8, 18, 5, -635955172);
        font.draw(g, visible, 10.0f, g.method_51443() - 18, 10.0f, -1776408);
        callback.cancel();
    }
}

