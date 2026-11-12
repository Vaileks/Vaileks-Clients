/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.DirectJoinServerScreen
 *  net.minecraft.client.gui.screens.ManageServerScreen
 *  net.minecraft.client.gui.screens.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.ManageServerScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.gui.CustomFont;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.hud.HudStyle;
import ru.bloodhelper.client.modules.StreamerMode;

@Mixin(value={EditBox.class})
public abstract class StreamerAddressInputMixin {
    @Inject(method={"extractWidgetRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$maskAddress(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo callback) {
        if (!StreamerMode.hideServerAddress()) {
            return;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        if (!(screen instanceof DirectJoinServerScreen) && !(screen instanceof ManageServerScreen)) {
            return;
        }
        EditBox input = (EditBox)this;
        UiDraw.border(g, input.getX(), input.getY(), input.getWidth(), input.getHeight(), 3, -15461351, input.isFocused() ? -7829356 : -12369077);
        CustomFont.regular().draw(g, HudStyle.tr("\u0421\u043a\u0440\u044b\u0442\u043e \u00b7 Streamer Mode", "Hidden \u00b7 Streamer Mode"), input.getX() + 6, (float)input.getY() + (float)(input.getHeight() - 10) / 2.0f, 10.0f, -4737087);
        callback.cancel();
    }
}

