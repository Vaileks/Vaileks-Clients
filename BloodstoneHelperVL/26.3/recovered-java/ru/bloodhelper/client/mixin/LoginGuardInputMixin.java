/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.EditBox
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.gui.UiDraw;
import ru.bloodhelper.client.mixin.LoginGuardChatAccessor;
import ru.bloodhelper.client.modules.LoginGuard;

@Mixin(value={EditBox.class})
public abstract class LoginGuardInputMixin {
    @Inject(method={"extractWidgetRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hidePassword(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!BloodHelperClient.config().loginGuard) {
            return;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        if (!(screen instanceof ChatScreen) || ((LoginGuardChatAccessor)screen).bloodhelper$loginInput() != this) {
            return;
        }
        EditBox input = (EditBox)this;
        int start = LoginGuard.secretStart(input.getValue());
        if (start < 0) {
            return;
        }
        int x = input.getX() + (input.isBordered() ? 4 : 0);
        int y = input.getY() + (input.isBordered() ? (input.getHeight() - 8) / 2 : 0);
        String prefix = input.getValue().substring(0, start);
        Font font = Minecraft.getInstance().font;
        if (input.isBordered()) {
            UiDraw.border(g, input.getX(), input.getY(), input.getWidth(), input.getHeight(), 3, -15461351, -9934731);
        }
        g.text(font, prefix, x, y, -2039584, true);
        int left = Math.min(x + font.width(prefix) + 2, input.getX() + input.getWidth() - 4);
        int width = Math.max(0, input.getX() + input.getWidth() - 4 - left);
        UiDraw.roundedSolid(g, left, y - 1, width, 11, 3, -11841703);
        UiDraw.roundedSolid(g, left + 2, y + 1, Math.max(0, width - 4), 7, 2, -8946554);
        UiDraw.roundedSolid(g, left + 4, y + 3, Math.max(0, width - 8), 3, 1, -6709851);
        ci.cancel();
    }
}

