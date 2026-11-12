/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.components.CommandSuggestions
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bloodhelper.client.BloodHelperClient;
import ru.bloodhelper.client.mixin.LoginGuardChatAccessor;
import ru.bloodhelper.client.modules.LoginGuard;

@Mixin(value={CommandSuggestions.class})
public abstract class LoginGuardSuggestionsMixin {
    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$hidePasswordSuggestions(GuiGraphicsExtractor g, int mouseX, int mouseY, CallbackInfo ci) {
        if (!BloodHelperClient.config().loginGuard) {
            return;
        }
        Screen screen = Minecraft.getInstance().gui.screen();
        if (screen instanceof ChatScreen && LoginGuard.secretStart(((LoginGuardChatAccessor)screen).bloodhelper$loginInput().getValue()) >= 0) {
            ci.cancel();
        }
    }
}

