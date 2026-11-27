/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.class_2583
 *  net.minecraft.class_310
 *  net.minecraft.class_408
 *  net.minecraft.class_437
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package ru.bloodhelper.client.mixin;

import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_408;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.bloodhelper.client.access.StaffAccess;
import ru.bloodhelper.client.modules.ChatNicknameRefresh;
import ru.bloodhelper.client.modules.CoordinateLinksModule;
import ru.bloodhelper.client.modules.ScreenshotActions;

@Mixin(value={class_408.class})
public abstract class CoordinateLinksChatMixin {
    @Inject(method={"method_25426"}, at={@At(value="TAIL")})
    private void bloodhelper$refreshNicknameLinks(CallbackInfo callback) {
        ((ChatNicknameRefresh)class_310.method_1551().field_1705.method_1743()).bloodhelper$refreshNicknameLinks();
    }

    @Inject(method={"method_75825"}, at={@At(value="HEAD")}, cancellable=true)
    private void bloodhelper$localCoordinateLink(class_2583 style, boolean insertion, CallbackInfoReturnable<Boolean> callback) {
        if (StaffAccess.clicked(style, insertion)) {
            callback.setReturnValue((Object)true);
            return;
        }
        if (ScreenshotActions.clicked(style, insertion, (class_437)((class_408)this))) {
            callback.setReturnValue((Object)true);
            return;
        }
        if (CoordinateLinksModule.clicked(style, insertion, (class_437)((class_408)this))) {
            callback.setReturnValue((Object)true);
        }
    }
}

