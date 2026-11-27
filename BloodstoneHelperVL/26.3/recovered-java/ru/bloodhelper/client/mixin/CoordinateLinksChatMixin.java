/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.ChatScreen
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Style
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.modules.ChatNicknameRefresh;
import ru.bloodhelper.client.modules.CoordinateLinksModule;
import ru.bloodhelper.client.modules.ScreenshotActions;

@Mixin(value={ChatScreen.class})
public abstract class CoordinateLinksChatMixin {
    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void bloodhelper$refreshNicknameLinks(CallbackInfo callback) {
        ((ChatNicknameRefresh)Minecraft.getInstance().gui.hud.getChat()).bloodhelper$refreshNicknameLinks();
    }

    @Inject(method={"handleComponentClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$localCoordinateLink(Style style, boolean insertion, CallbackInfoReturnable<Boolean> callback) {
        if (StaffAccess.clicked(style, insertion)) {
            callback.setReturnValue((Object)true);
            return;
        }
        if (ScreenshotActions.clicked(style, insertion, (Screen)((ChatScreen)this))) {
            callback.setReturnValue((Object)true);
            return;
        }
        if (CoordinateLinksModule.clicked(style, insertion, (Screen)((ChatScreen)this))) {
            callback.setReturnValue((Object)true);
        }
    }
}

